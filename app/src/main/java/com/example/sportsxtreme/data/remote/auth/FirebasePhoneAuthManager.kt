package com.example.sportsxtreme.data.remote.auth

import android.app.Activity
import com.example.sportsxtreme.domain.model.PhoneAuthSession
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class FirebasePhoneAuthManager(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) : PhoneAuthManager {
    private var activityRef: WeakReference<Activity>? = null
    private val resendTokens = ConcurrentHashMap<String, PhoneAuthProvider.ForceResendingToken>()

    fun bindActivity(activity: Activity) {
        activityRef = WeakReference(activity)
    }

    override suspend fun sendOtp(phoneNumber: String): Result<PhoneAuthSession> {
        return startPhoneVerification(phoneNumber = phoneNumber, resendToken = null)
    }

    override suspend fun resendOtp(session: PhoneAuthSession): Result<PhoneAuthSession> {
        val token = resendTokens[session.verificationId]
            ?: return Result.failure(IllegalStateException("OTP resend is not available for this verification session."))
        return startPhoneVerification(phoneNumber = session.phoneNumber, resendToken = token)
    }

    override suspend fun verifyOtp(verificationId: String, otpCode: String): Result<Unit> {
        if (verificationId.isBlank()) {
            return Result.failure(FirebaseAuthInvalidCredentialsException("missing-verification-id", "OTP session has expired. Request a new code."))
        }
        if (otpCode.length != OTP_LENGTH || otpCode.any { !it.isDigit() }) {
            return Result.failure(FirebaseAuthInvalidCredentialsException("invalid-verification-code", "Enter a valid 6 digit OTP."))
        }

        val credential = PhoneAuthProvider.getCredential(verificationId, otpCode)
        return signInOrLinkWithPhoneCredential(credential)
    }

    private suspend fun startPhoneVerification(
        phoneNumber: String,
        resendToken: PhoneAuthProvider.ForceResendingToken?
    ): Result<PhoneAuthSession> {
        val normalizedPhoneNumber = normalizePhoneNumber(phoneNumber)
        if (normalizedPhoneNumber == null) {
            return Result.failure(
                FirebaseAuthInvalidCredentialsException(
                    "invalid-phone-number",
                    "Enter a valid phone number. Use +91XXXXXXXXXX or a 10 digit Indian number."
                )
            )
        }

        val activity = activityRef?.get()
            ?: return Result.failure(IllegalStateException("Phone authentication needs an active Activity."))

        return suspendCancellableCoroutine { continuation ->
            var resumed = false
            fun resumeOnce(result: Result<PhoneAuthSession>) {
                if (!resumed && continuation.isActive) {
                    resumed = true
                    continuation.resume(result)
                }
            }

            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInOrLinkWithPhoneCredential(credential) { result ->
                        resumeOnce(
                            result.fold(
                                onSuccess = {
                                    Result.success(
                                        PhoneAuthSession(
                                            verificationId = "",
                                            phoneNumber = normalizedPhoneNumber,
                                            canResend = false,
                                            isAutoVerified = true
                                        )
                                    )
                                },
                                onFailure = { error -> Result.failure(error) }
                            )
                        )
                    }
                }

                override fun onVerificationFailed(exception: FirebaseException) {
                    resumeOnce(Result.failure(mapFirebasePhoneException(exception)))
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    resendTokens[verificationId] = token
                    resumeOnce(
                        Result.success(
                            PhoneAuthSession(
                                verificationId = verificationId,
                                phoneNumber = normalizedPhoneNumber,
                                canResend = true
                            )
                        )
                    )
                }

                override fun onCodeAutoRetrievalTimeOut(verificationId: String) {
                    // Manual verification can continue after auto-retrieval times out.
                }
            }

            val optionsBuilder = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(normalizedPhoneNumber)
                .setTimeout(OTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)

            if (resendToken != null) {
                optionsBuilder.setForceResendingToken(resendToken)
            }

            PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
        }
    }

    private suspend fun signInOrLinkWithPhoneCredential(credential: PhoneAuthCredential): Result<Unit> {
        return suspendCancellableCoroutine { continuation ->
            signInOrLinkWithPhoneCredential(credential) { result ->
                if (continuation.isActive) {
                    continuation.resume(result)
                }
            }
        }
    }

    private fun signInOrLinkWithPhoneCredential(
        credential: PhoneAuthCredential,
        onResult: (Result<Unit>) -> Unit
    ) {
        val currentUser = firebaseAuth.currentUser
        val task = currentUser?.linkWithCredential(credential) ?: firebaseAuth.signInWithCredential(credential)
        task.addOnSuccessListener {
            onResult(Result.success(Unit))
        }.addOnFailureListener { exception ->
            onResult(Result.failure(mapFirebasePhoneException(exception)))
        }
    }

    private fun mapFirebasePhoneException(exception: Exception): Exception {
        val message = when (exception) {
            is FirebaseAuthInvalidCredentialsException -> {
                when (exception.errorCode) {
                    "ERROR_INVALID_VERIFICATION_CODE" -> "Invalid OTP. Check the code and try again."
                    "ERROR_SESSION_EXPIRED" -> "OTP expired. Request a new code."
                    "ERROR_INVALID_PHONE_NUMBER" -> "Enter a valid phone number with country code."
                    else -> exception.message ?: "Invalid phone verification request."
                }
            }
            is FirebaseAuthException -> exception.message ?: "Firebase phone authentication failed."
            is FirebaseException -> exception.message ?: "Network or Firebase phone verification failed."
            else -> exception.message ?: "Phone verification failed."
        }
        return Exception(message.toPhoneFriendlyMessage(), exception)
    }

    private fun String.toPhoneFriendlyMessage(): String {
        return when {
            contains("BILLING_NOT_ENABLED", ignoreCase = true) ->
                "Firebase cannot send real OTP SMS until billing is enabled for this project. Enable Blaze billing or use Firebase test phone numbers."
            contains("SMS unable to be sent until this region", ignoreCase = true) ->
                "SMS is blocked for this phone region in Firebase. Enable India (+91) in Firebase Authentication > Settings > SMS region policy."
            contains("given sign-in provider is disabled", ignoreCase = true) ->
                "Phone sign-in is blocked in Firebase. Enable Phone provider and allow this SMS region in Firebase Authentication settings."
            else -> this
        }
    }

    private fun normalizePhoneNumber(phoneNumber: String): String? {
        val compact = phoneNumber.filter { it.isDigit() || it == '+' }
        if (compact.startsWith("+") && compact.count { it.isDigit() } in MIN_PHONE_DIGITS..MAX_PHONE_DIGITS) {
            return compact
        }

        val digits = compact.filter { it.isDigit() }
        return when {
            digits.length == INDIA_PHONE_DIGITS -> "+91$digits"
            digits.length == INDIA_COUNTRY_CODE_WITH_PHONE_DIGITS && digits.startsWith(INDIA_COUNTRY_CODE) -> "+$digits"
            else -> null
        }
    }

    private companion object {
        const val OTP_LENGTH = 6
        const val OTP_TIMEOUT_SECONDS = 60L
        const val MIN_PHONE_DIGITS = 10
        const val MAX_PHONE_DIGITS = 15
        const val INDIA_PHONE_DIGITS = 10
        const val INDIA_COUNTRY_CODE_WITH_PHONE_DIGITS = 12
        const val INDIA_COUNTRY_CODE = "91"
    }
}
