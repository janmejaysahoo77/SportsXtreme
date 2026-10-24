package com.example.sportsxtreme.data.remote.auth

import android.content.Context
import android.util.Log
import com.example.sportsxtreme.domain.model.AuthProvider
import com.example.sportsxtreme.domain.model.AuthSession
import com.example.sportsxtreme.domain.model.EmailSignupRequest
import com.example.sportsxtreme.domain.model.User
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class AuthDataSource(
    private val appContext: Context,
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    fun currentUser(): User? {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            Log.d(TAG, "[AUTH] AuthDataSource.currentUser(): null (No user signed in)")
            return null
        }
        val user = User(
            id = firebaseUser.uid,
            name = firebaseUser.displayName.orEmpty(),
            mobileNumber = firebaseUser.phoneNumber.orEmpty(),
            email = firebaseUser.email.orEmpty(),
            authProvider = AuthProvider.EMAIL_PASSWORD,
            isEmailVerified = firebaseUser.isEmailVerified,
            isPhoneVerified = !firebaseUser.phoneNumber.isNullOrBlank()
        )
        Log.d(TAG, "[AUTH] AuthDataSource.currentUser(): UID=${user.id}, email=${user.email}, isVerified=${user.isEmailVerified}")
        return user
    }

    suspend fun createEmailAccount(request: EmailSignupRequest): Result<AuthSession> {
        Log.d(TAG, "[AUTH] AuthDataSource.createEmailAccount creating account for email=${request.email}")
        return suspendCancellableCoroutine { continuation ->
            val task = firebaseAuth.createUserWithEmailAndPassword(request.email, request.password)

            task.addOnSuccessListener { authResult ->
                val firebaseUser = authResult.user
                val user = User(
                    id = firebaseUser?.uid.orEmpty(),
                    name = request.name,
                    mobileNumber = request.phoneNumber,
                    email = request.email,
                    authProvider = AuthProvider.EMAIL_PASSWORD,
                    isEmailVerified = firebaseUser?.isEmailVerified == true,
                    isPhoneVerified = false
                )
                if (firebaseUser == null) {
                    Log.e(TAG, "[AUTH] AuthDataSource.createEmailAccount: Account created but firebaseUser is null")
                    continuation.resume(Result.failure(IllegalStateException("Firebase account was created, but no active user was returned.")))
                    return@addOnSuccessListener
                }

                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(request.name)
                    .build()
                firebaseUser.updateProfile(profileUpdates)
                    .addOnSuccessListener {
                        firebaseUser.sendEmailVerification()
                            .addOnSuccessListener {
                                Log.i(TAG, "[AUTH] AuthDataSource.createEmailAccount SUCCESS! UID=${firebaseUser.uid}")
                                continuation.resume(Result.success(AuthSession(user, authResult.additionalUserInfo?.isNewUser == true)))
                            }
                            .addOnFailureListener { exception ->
                                Log.e(TAG, "[AUTH] AuthDataSource.createEmailAccount: Verification email failed: ${exception.message}", exception)
                                continuation.resume(Result.failure(exception))
                            }
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "[AUTH] AuthDataSource.createEmailAccount: Profile update failed: ${exception.message}", exception)
                        continuation.resume(Result.failure(exception))
                    }
            }.addOnFailureListener { exception ->
                Log.e(TAG, "[AUTH] AuthDataSource.createEmailAccount FAILED: ${exception.message}", exception)
                continuation.resume(Result.failure(exception))
            }
        }
    }

    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<AuthSession> {
        Log.d(TAG, "[AUTH] AuthDataSource.signInWithEmailAndPassword for email=$email")
        return suspendCancellableCoroutine { continuation ->
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    val firebaseUser = authResult.user
                    val user = User(
                        id = firebaseUser?.uid.orEmpty(),
                        name = firebaseUser?.displayName.orEmpty(),
                        mobileNumber = firebaseUser?.phoneNumber.orEmpty(),
                        email = firebaseUser?.email ?: email,
                        authProvider = AuthProvider.EMAIL_PASSWORD,
                        isEmailVerified = firebaseUser?.isEmailVerified == true,
                        isPhoneVerified = !firebaseUser?.phoneNumber.isNullOrBlank()
                    )
                    Log.i(TAG, "[AUTH] AuthDataSource.signInWithEmailAndPassword SUCCESS! UID=${user.id}, isEmailVerified=${user.isEmailVerified}")
                    continuation.resume(Result.success(AuthSession(user, authResult.additionalUserInfo?.isNewUser == true)))
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "[AUTH] AuthDataSource.signInWithEmailAndPassword FAILED: ${exception.message}", exception)
                    continuation.resume(Result.failure(exception))
                }
        }
    }

    suspend fun sendEmailSignInLink(email: String): Result<Unit> {
        Log.d(TAG, "[AUTH] AuthDataSource.sendEmailSignInLink for email=$email")
        return suspendCancellableCoroutine { continuation ->
            firebaseAuth.sendSignInLinkToEmail(email, emailLinkActionCodeSettings())
                .addOnSuccessListener {
                    Log.i(TAG, "[AUTH] AuthDataSource.sendEmailSignInLink SUCCESS for email=$email")
                    continuation.resume(Result.success(Unit))
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "[AUTH] AuthDataSource.sendEmailSignInLink FAILED: ${exception.message}", exception)
                    continuation.resume(Result.failure(exception))
                }
        }
    }

    fun isSignInWithEmailLink(link: String): Boolean {
        val result = firebaseAuth.isSignInWithEmailLink(link)
        Log.d(TAG, "[AUTH] AuthDataSource.isSignInWithEmailLink: $result")
        return result
    }

    suspend fun signInWithEmailLink(email: String, link: String): Result<AuthSession> {
        Log.d(TAG, "[AUTH] AuthDataSource.signInWithEmailLink for email=$email")
        return suspendCancellableCoroutine { continuation ->
            firebaseAuth.signInWithEmailLink(email, link)
                .addOnSuccessListener { authResult ->
                    val firebaseUser = authResult.user
                    val user = User(
                        id = firebaseUser?.uid.orEmpty(),
                        name = firebaseUser?.displayName.orEmpty(),
                        mobileNumber = firebaseUser?.phoneNumber.orEmpty(),
                        email = firebaseUser?.email ?: email,
                        authProvider = AuthProvider.EMAIL_PASSWORD,
                        isEmailVerified = true,
                        isPhoneVerified = !firebaseUser?.phoneNumber.isNullOrBlank()
                    )
                    Log.i(TAG, "[AUTH] AuthDataSource.signInWithEmailLink SUCCESS! UID=${user.id}")
                    continuation.resume(Result.success(AuthSession(user, authResult.additionalUserInfo?.isNewUser == true)))
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "[AUTH] AuthDataSource.signInWithEmailLink FAILED: ${exception.message}", exception)
                    continuation.resume(Result.failure(exception))
                }
        }
    }

    suspend fun deleteCurrentUserAndSignOut(): Result<Unit> {
        Log.d(TAG, "[AUTH] AuthDataSource.deleteCurrentUserAndSignOut called")
        return suspendCancellableCoroutine { continuation ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser == null) {
                firebaseAuth.signOut()
                continuation.resume(Result.success(Unit))
                return@suspendCancellableCoroutine
            }

            firebaseUser.delete()
                .addOnSuccessListener {
                    firebaseAuth.signOut()
                    Log.i(TAG, "[AUTH] User deleted and signed out successfully")
                    continuation.resume(Result.success(Unit))
                }
                .addOnFailureListener { exception ->
                    firebaseAuth.signOut()
                    Log.e(TAG, "[AUTH] User deletion failed, signed out: ${exception.message}", exception)
                    continuation.resume(Result.failure(exception))
                }
        }
    }

    fun signOut() {
        Log.d(TAG, "[AUTH] AuthDataSource.signOut called")
        firebaseAuth.signOut()
    }

    suspend fun resendEmailVerification(): Result<Unit> {
        Log.d(TAG, "[AUTH] AuthDataSource.resendEmailVerification called")
        return suspendCancellableCoroutine { continuation ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser == null) {
                continuation.resume(Result.failure(IllegalStateException("No authenticated email account is waiting for verification.")))
                return@suspendCancellableCoroutine
            }

            firebaseUser.sendEmailVerification()
                .addOnSuccessListener {
                    Log.i(TAG, "[AUTH] AuthDataSource.resendEmailVerification SUCCESS")
                    continuation.resume(Result.success(Unit))
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "[AUTH] AuthDataSource.resendEmailVerification FAILED: ${exception.message}", exception)
                    continuation.resume(Result.failure(exception))
                }
        }
    }

    suspend fun sendEmailChangeVerification(newEmail: String): Result<Unit> {
        Log.d(TAG, "[AUTH] AuthDataSource.sendEmailChangeVerification for newEmail=$newEmail")
        return suspendCancellableCoroutine { continuation ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser == null) {
                continuation.resume(Result.failure(IllegalStateException("No authenticated account is available for email verification.")))
                return@suspendCancellableCoroutine
            }
            if (newEmail.isBlank()) {
                continuation.resume(Result.failure(IllegalArgumentException("Enter a valid email address.")))
                return@suspendCancellableCoroutine
            }

            firebaseUser.verifyBeforeUpdateEmail(newEmail)
                .addOnSuccessListener {
                    Log.i(TAG, "[AUTH] AuthDataSource.sendEmailChangeVerification SUCCESS for $newEmail")
                    continuation.resume(Result.success(Unit))
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "[AUTH] AuthDataSource.sendEmailChangeVerification FAILED: ${exception.message}", exception)
                    continuation.resume(Result.failure(exception))
                }
        }
    }

    suspend fun reloadAndCheckEmailVerified(): Result<Boolean> {
        Log.d(TAG, "[AUTH] AuthDataSource.reloadAndCheckEmailVerified called")
        return suspendCancellableCoroutine { continuation ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser == null) {
                continuation.resume(Result.failure(IllegalStateException("No authenticated email account is waiting for verification.")))
                return@suspendCancellableCoroutine
            }

            firebaseUser.reload()
                .addOnSuccessListener {
                    val isVerified = firebaseAuth.currentUser?.isEmailVerified == true
                    Log.i(TAG, "[AUTH] AuthDataSource.reloadAndCheckEmailVerified result: isEmailVerified=$isVerified")
                    continuation.resume(Result.success(isVerified))
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "[AUTH] AuthDataSource.reloadAndCheckEmailVerified FAILED: ${exception.message}", exception)
                    continuation.resume(Result.failure(exception))
                }
        }
    }

    suspend fun signup(name: String, email: String, password: String): Result<User> {
        return createEmailAccount(
            EmailSignupRequest(
                name = name,
                email = email,
                password = password,
                phoneNumber = ""
            )
        ).map { it.user }
    }

    suspend fun signInWithCredential(credential: AuthCredential, provider: AuthProvider): Result<AuthSession> {
        Log.d(TAG, "[AUTH] AuthDataSource.signInWithCredential with provider=$provider")
        return suspendCancellableCoroutine { continuation ->
            val task = firebaseAuth.signInWithCredential(credential)

            task.addOnSuccessListener { authResult ->
                val firebaseUser = authResult.user
                val user = User(
                    id = firebaseUser?.uid.orEmpty(),
                    name = firebaseUser?.displayName.orEmpty(),
                    mobileNumber = firebaseUser?.phoneNumber.orEmpty(),
                    email = firebaseUser?.email.orEmpty(),
                    authProvider = provider,
                    isEmailVerified = provider == AuthProvider.GOOGLE || firebaseUser?.isEmailVerified == true,
                    isPhoneVerified = !firebaseUser?.phoneNumber.isNullOrBlank()
                )
                Log.i(TAG, "[AUTH] AuthDataSource.signInWithCredential SUCCESS! UID=${user.id}, provider=$provider, isNewUser=${authResult.additionalUserInfo?.isNewUser}")
                continuation.resume(Result.success(AuthSession(user, authResult.additionalUserInfo?.isNewUser == true)))
            }.addOnFailureListener { exception ->
                Log.e(TAG, "[AUTH] AuthDataSource.signInWithCredential FAILED: ${exception.message}", exception)
                continuation.resume(Result.failure(exception))
            }
        }
    }

    suspend fun signupWithCredential(credential: AuthCredential): Result<User> {
        return signInWithCredential(credential, AuthProvider.GOOGLE).map { it.user }
    }

    private fun emailLinkActionCodeSettings(): ActionCodeSettings {
        return ActionCodeSettings.newBuilder()
            .setUrl(EMAIL_LINK_CONTINUE_URL)
            .setHandleCodeInApp(true)
            .setAndroidPackageName(appContext.packageName, true, null)
            .build()
    }

    private companion object {
        private const val TAG = "AuthDataSource"
        const val EMAIL_LINK_CONTINUE_URL = "https://sportsxtreme-95fbb.firebaseapp.com/auth"
    }
}
