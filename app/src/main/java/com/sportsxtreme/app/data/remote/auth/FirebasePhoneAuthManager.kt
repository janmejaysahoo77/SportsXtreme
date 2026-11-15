package com.sportsxtreme.app.data.remote.auth

import android.app.Activity
import android.util.Log
import com.sportsxtreme.app.domain.model.PhoneAuthSession
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

import com.google.firebase.auth.FirebaseAuthUserCollisionException

class FirebasePhoneAuthManager(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) : PhoneAuthManager {
    private var activityRef: WeakReference<Activity>? = null
    private val resendTokens = ConcurrentHashMap<String, PhoneAuthProvider.ForceResendingToken>()
    var onAutoVerifiedListener: ((PhoneAuthSession) -> Unit)? = null
    @Volatile var activeAutoVerifiedSession: PhoneAuthSession? = null

    fun bindActivity(activity: Activity) {
        Log.d(TAG, "[OTP_SYSTEM] Binding activity for Phone Auth: ${activity.javaClass.simpleName}")
        activityRef = WeakReference(activity)
    }

    override suspend fun sendOtp(phoneNumber: String): Result<PhoneAuthSession> {
        Log.d(TAG, "[OTP_SYSTEM] sendOtp requested for phoneNumber: $phoneNumber")
        activeAutoVerifiedSession = null
        return startPhoneVerification(phoneNumber = phoneNumber, resendToken = null)
    }

    override suspend fun resendOtp(session: PhoneAuthSession): Result<PhoneAuthSession> {
        Log.d(TAG, "[OTP_SYSTEM] resendOtp requested for verificationId: ${session.verificationId}, phone: ${session.phoneNumber}")
        activeAutoVerifiedSession = null
        val token = resendTokens[session.verificationId]
        if (token == null) {
            Log.w(TAG, "[OTP_SYSTEM] Resend token not found for verificationId=${session.verificationId}; starting fresh phone verification.")
        }
        return startPhoneVerification(phoneNumber = session.phoneNumber, resendToken = token)
    }

    override suspend fun verifyOtp(verificationId: String, otpCode: String): Result<Boolean> {
        Log.d(TAG, "[OTP_SYSTEM] verifyOtp requested - verificationId: '$verificationId', code length: ${otpCode.length}")

        // Check if session was already auto-verified by Firebase
        activeAutoVerifiedSession?.let { autoSession ->
            if (autoSession.isAutoVerified) {
                Log.i(TAG, "[OTP_SYSTEM] verifyOtp called but session is already auto-verified. Returning success.")
                return Result.success(autoSession.isNewUser)
            }
        }

        val currentUser = firebaseAuth.currentUser
        if (currentUser != null && !currentUser.phoneNumber.isNullOrBlank()) {
            Log.i(TAG, "[OTP_SYSTEM] User is already authenticated with phone: ${currentUser.phoneNumber}")
            return Result.success(false)
        }

        if (verificationId.isBlank()) {
            Log.e(TAG, "[OTP_SYSTEM] verifyOtp failed: Missing verificationId")
            return Result.failure(FirebaseAuthInvalidCredentialsException("missing-verification-id", "OTP session has expired. Request a new code."))
        }
        if (otpCode.length != OTP_LENGTH || otpCode.any { !it.isDigit() }) {
            Log.e(TAG, "[OTP_SYSTEM] verifyOtp failed: Invalid code format (length=${otpCode.length})")
            return Result.failure(FirebaseAuthInvalidCredentialsException("invalid-verification-code", "Enter a valid 6 digit OTP."))
        }

        val credential = PhoneAuthProvider.getCredential(verificationId, otpCode)
        Log.d(TAG, "[OTP_SYSTEM] PhoneAuthProvider credential generated successfully for verificationId: $verificationId")
        return signInOrLinkWithPhoneCredential(credential)
    }

    private suspend fun startPhoneVerification(
        phoneNumber: String,
        resendToken: PhoneAuthProvider.ForceResendingToken?
    ): Result<PhoneAuthSession> {
        val normalizedPhoneNumber = normalizePhoneNumber(phoneNumber)
        if (normalizedPhoneNumber == null) {
            Log.e(TAG, "[OTP_SYSTEM] Failed to normalize phone number: '$phoneNumber'")
            return Result.failure(
                FirebaseAuthInvalidCredentialsException(
                    "invalid-phone-number",
                    "Enter a valid phone number. Use +91XXXXXXXXXX or a 10 digit Indian number."
                )
            )
        }
        Log.d(TAG, "[OTP_SYSTEM] Starting phone verification for normalized number: '$normalizedPhoneNumber' (isResend=${resendToken != null})")

        val activity = activityRef?.get()
        if (activity == null) {
            Log.e(TAG, "[OTP_SYSTEM] Cannot start phone verification: Activity reference is null")
            return Result.failure(IllegalStateException("Phone authentication needs an active Activity."))
        }

        return suspendCancellableCoroutine { continuation ->
            var resumed = false
            val callbackLock = Any()
            var manualSessionDelivered = false
            var autoVerificationStarted = false
            var autoVerificationFinished = false
            var autoVerificationFailure: Throwable? = null
            var codeSentSession: PhoneAuthSession? = null

            fun resumeOnce(result: Result<PhoneAuthSession>) {
                val shouldResume = synchronized(callbackLock) {
                    if (!resumed && continuation.isActive) {
                        resumed = true
                        true
                    } else {
                        false
                    }
                }
                if (shouldResume) continuation.resume(result)
            }

            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    Log.i(TAG, "[OTP_SYSTEM] OnVerificationStateChangedCallbacks: Instant/auto verification completed (smsCodeAvailable=${!credential.smsCode.isNullOrBlank()})")
                    synchronized(callbackLock) {
                        autoVerificationStarted = true
                    }

                    signInOrLinkWithPhoneCredential(credential) { result ->
                        val wasManualDelivered = synchronized(callbackLock) {
                            autoVerificationFinished = true
                            if (result.isFailure) autoVerificationFailure = result.exceptionOrNull()
                            manualSessionDelivered
                        }

                        result.fold(
                            onSuccess = { isNew ->
                                Log.i(TAG, "[OTP_SYSTEM] Auto-verification sign-in succeeded. isNewUser=$isNew")
                                val autoSession = PhoneAuthSession(
                                    verificationId = codeSentSession?.verificationId ?: "",
                                    phoneNumber = normalizedPhoneNumber,
                                    canResend = false,
                                    isAutoVerified = true,
                                    isNewUser = isNew,
                                    autoFilledCode = credential.smsCode
                                )
                                activeAutoVerifiedSession = autoSession

                                if (!wasManualDelivered) {
                                    synchronized(callbackLock) { manualSessionDelivered = true }
                                    resumeOnce(Result.success(autoSession))
                                } else {
                                    Log.i(TAG, "[OTP_SYSTEM] Manual session was already active when Firebase auto-verified. Notifying ViewModel.")
                                    onAutoVerifiedListener?.invoke(autoSession)
                                }
                            },
                            onFailure = { error ->
                                Log.e(TAG, "[OTP_SYSTEM] Auto-verification sign-in failed: ${error.message}", error)
                                if (!wasManualDelivered) {
                                    codeSentSession?.let { session ->
                                        synchronized(callbackLock) { manualSessionDelivered = true }
                                        resumeOnce(Result.success(session))
                                    } ?: resumeOnce(Result.failure(error))
                                }
                            }
                        )
                    }
                }

                override fun onVerificationFailed(exception: FirebaseException) {
                    Log.e(TAG, "[OTP_SYSTEM] OnVerificationStateChangedCallbacks: Verification failed. Exception: ${exception.message}", exception)
                    resumeOnce(Result.failure(mapFirebasePhoneException(exception)))
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    Log.i(TAG, "[OTP_SYSTEM] OnVerificationStateChangedCallbacks: Code sent successfully! verificationId: '$verificationId'")
                    resendTokens[verificationId] = token
                    val session = PhoneAuthSession(
                        verificationId = verificationId,
                        phoneNumber = normalizedPhoneNumber,
                        canResend = true
                    )
                    val shouldDeliverManualSession = synchronized(callbackLock) {
                        codeSentSession = session
                        if (!autoVerificationStarted && !manualSessionDelivered) {
                            manualSessionDelivered = true
                            true
                        } else if (
                            autoVerificationFinished &&
                            autoVerificationFailure != null &&
                            !manualSessionDelivered
                        ) {
                            manualSessionDelivered = true
                            true
                        } else {
                            false
                        }
                    }
                    if (shouldDeliverManualSession) {
                        resumeOnce(Result.success(session))
                    } else {
                        Log.i(TAG, "[OTP_SYSTEM] Code sent while Firebase auto-verification owns this session; keeping one completion path")
                    }
                }

                override fun onCodeAutoRetrievalTimeOut(verificationId: String) {
                    Log.w(TAG, "[OTP_SYSTEM] OnVerificationStateChangedCallbacks: Code auto retrieval timed out for verificationId: '$verificationId'")
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

            Log.d(TAG, "[OTP_SYSTEM] Invoking PhoneAuthProvider.verifyPhoneNumber...")
            PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
        }
    }

    private suspend fun signInOrLinkWithPhoneCredential(credential: PhoneAuthCredential): Result<Boolean> {
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
        onResult: (Result<Boolean>) -> Unit
    ) {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null && currentUser.phoneNumber.isNullOrBlank()) {
            Log.d(TAG, "[OTP_SYSTEM] Attempting linkWithCredential for UID=${currentUser.uid}")
            currentUser.linkWithCredential(credential).addOnSuccessListener { taskResult ->
                val isNew = taskResult.additionalUserInfo?.isNewUser == true
                Log.i(TAG, "[OTP_SYSTEM] Phone credential link SUCCESS! isNewUser=$isNew, UID=${currentUser.uid}")
                onResult(Result.success(isNew))
            }.addOnFailureListener { exception ->
                if (exception is FirebaseAuthUserCollisionException) {
                    Log.w(TAG, "[OTP_SYSTEM] Phone number is already linked to another account. Falling back to signInWithCredential.")
                    firebaseAuth.signInWithCredential(credential).addOnSuccessListener { signInResult ->
                        val isNew = signInResult.additionalUserInfo?.isNewUser == true
                        Log.i(TAG, "[OTP_SYSTEM] Phone fallback signInWithCredential SUCCESS! UID=${firebaseAuth.currentUser?.uid}")
                        onResult(Result.success(isNew))
                    }.addOnFailureListener { signInException ->
                        Log.e(TAG, "[OTP_SYSTEM] Phone fallback signInWithCredential FAILED: ${signInException.message}")
                        onResult(Result.failure(mapFirebasePhoneException(signInException)))
                    }
                } else {
                    Log.e(TAG, "[OTP_SYSTEM] Phone credential link FAILED: ${exception.message}", exception)
                    onResult(Result.failure(mapFirebasePhoneException(exception)))
                }
            }
        } else {
            Log.d(TAG, "[OTP_SYSTEM] Executing signInWithCredential for UID=${currentUser?.uid}")
            firebaseAuth.signInWithCredential(credential).addOnSuccessListener { taskResult ->
                val isNew = taskResult.additionalUserInfo?.isNewUser == true
                Log.i(TAG, "[OTP_SYSTEM] Phone credential signInWithCredential SUCCESS! isNewUser=$isNew, UID=${firebaseAuth.currentUser?.uid}")
                onResult(Result.success(isNew))
            }.addOnFailureListener { exception ->
                Log.e(TAG, "[OTP_SYSTEM] Phone credential signInWithCredential FAILED: ${exception.message}", exception)
                onResult(Result.failure(mapFirebasePhoneException(exception)))
            }
        }
    }

    private fun mapFirebasePhoneException(exception: Exception): Exception {
        val message = when (exception) {
            is FirebaseAuthUserCollisionException -> "This phone number is already registered to another account."
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
            contains("missing a valid app identifier", ignoreCase = true) ||
                contains("invalid app identifier", ignoreCase = true) ||
                contains("app not authorized", ignoreCase = true) ->
                "Firebase could not verify this app. In Firebase Console, check that Android app " +
                    "com.sportsxtreme.app has the SHA-256 fingerprint for Play Integrity and the SHA-1 " +
                    "fingerprint for reCAPTCHA registered. Also allow the Firebase Auth handler domain " +
                    "sportsxtreme-95fbb.firebaseapp.com in API key restrictions, then rebuild with the " +
                    "updated google-services.json."
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
        private const val TAG = "FirebasePhoneAuthManager"
        const val OTP_LENGTH = 6
        const val OTP_TIMEOUT_SECONDS = 60L
        const val MIN_PHONE_DIGITS = 10
        const val MAX_PHONE_DIGITS = 15
        const val INDIA_PHONE_DIGITS = 10
        const val INDIA_COUNTRY_CODE_WITH_PHONE_DIGITS = 12
        const val INDIA_COUNTRY_CODE = "91"
    }
}
