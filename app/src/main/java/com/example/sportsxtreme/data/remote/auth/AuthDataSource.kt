package com.example.sportsxtreme.data.remote.auth

import android.content.Context
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
        val firebaseUser = firebaseAuth.currentUser ?: return null
        return User(
            id = firebaseUser.uid,
            name = firebaseUser.displayName.orEmpty(),
            mobileNumber = firebaseUser.phoneNumber.orEmpty(),
            email = firebaseUser.email.orEmpty(),
            profilePhotoUrl = firebaseUser.photoUrl?.toString(),
            authProvider = AuthProvider.EMAIL_PASSWORD,
            isEmailVerified = firebaseUser.isEmailVerified,
            isPhoneVerified = !firebaseUser.phoneNumber.isNullOrBlank()
        )
    }

    suspend fun createEmailAccount(request: EmailSignupRequest): Result<AuthSession> {
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
                                continuation.resume(Result.success(AuthSession(user, authResult.additionalUserInfo?.isNewUser == true)))
                            }
                            .addOnFailureListener { exception ->
                                continuation.resume(Result.failure(exception))
                            }
                    }
                    .addOnFailureListener { exception ->
                        continuation.resume(Result.failure(exception))
                    }
            }.addOnFailureListener { exception ->
                continuation.resume(Result.failure(exception))
            }
        }
    }

    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<AuthSession> {
        return suspendCancellableCoroutine { continuation ->
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    val firebaseUser = authResult.user
                    val user = User(
                        id = firebaseUser?.uid.orEmpty(),
                        name = firebaseUser?.displayName.orEmpty(),
                        mobileNumber = firebaseUser?.phoneNumber.orEmpty(),
                        email = firebaseUser?.email ?: email,
                        profilePhotoUrl = firebaseUser?.photoUrl?.toString(),
                        authProvider = AuthProvider.EMAIL_PASSWORD,
                        isEmailVerified = firebaseUser?.isEmailVerified == true,
                        isPhoneVerified = !firebaseUser?.phoneNumber.isNullOrBlank()
                    )
                    continuation.resume(Result.success(AuthSession(user, authResult.additionalUserInfo?.isNewUser == true)))
                }
                .addOnFailureListener { exception ->
                    continuation.resume(Result.failure(exception))
                }
        }
    }

    suspend fun sendEmailSignInLink(email: String): Result<Unit> {
        return suspendCancellableCoroutine { continuation ->
            firebaseAuth.sendSignInLinkToEmail(email, emailLinkActionCodeSettings())
                .addOnSuccessListener {
                    continuation.resume(Result.success(Unit))
                }
                .addOnFailureListener { exception ->
                    continuation.resume(Result.failure(exception))
                }
        }
    }

    fun isSignInWithEmailLink(link: String): Boolean {
        return firebaseAuth.isSignInWithEmailLink(link)
    }

    suspend fun signInWithEmailLink(email: String, link: String): Result<AuthSession> {
        return suspendCancellableCoroutine { continuation ->
            firebaseAuth.signInWithEmailLink(email, link)
                .addOnSuccessListener { authResult ->
                    val firebaseUser = authResult.user
                    val user = User(
                        id = firebaseUser?.uid.orEmpty(),
                        name = firebaseUser?.displayName.orEmpty(),
                        mobileNumber = firebaseUser?.phoneNumber.orEmpty(),
                        email = firebaseUser?.email ?: email,
                        profilePhotoUrl = firebaseUser?.photoUrl?.toString(),
                        authProvider = AuthProvider.EMAIL_PASSWORD,
                        isEmailVerified = true,
                        isPhoneVerified = !firebaseUser?.phoneNumber.isNullOrBlank()
                    )
                    continuation.resume(Result.success(AuthSession(user, authResult.additionalUserInfo?.isNewUser == true)))
                }
                .addOnFailureListener { exception ->
                    continuation.resume(Result.failure(exception))
                }
        }
    }

    suspend fun deleteCurrentUserAndSignOut(): Result<Unit> {
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
                    continuation.resume(Result.success(Unit))
                }
                .addOnFailureListener { exception ->
                    firebaseAuth.signOut()
                    continuation.resume(Result.failure(exception))
                }
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
    }

    suspend fun resendEmailVerification(): Result<Unit> {
        return suspendCancellableCoroutine { continuation ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser == null) {
                continuation.resume(Result.failure(IllegalStateException("No authenticated email account is waiting for verification.")))
                return@suspendCancellableCoroutine
            }

            firebaseUser.sendEmailVerification()
                .addOnSuccessListener {
                    continuation.resume(Result.success(Unit))
                }
                .addOnFailureListener { exception ->
                    continuation.resume(Result.failure(exception))
                }
        }
    }

    suspend fun sendEmailChangeVerification(newEmail: String): Result<Unit> {
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
                    continuation.resume(Result.success(Unit))
                }
                .addOnFailureListener { exception ->
                    continuation.resume(Result.failure(exception))
                }
        }
    }

    suspend fun reloadAndCheckEmailVerified(): Result<Boolean> {
        return suspendCancellableCoroutine { continuation ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser == null) {
                continuation.resume(Result.failure(IllegalStateException("No authenticated email account is waiting for verification.")))
                return@suspendCancellableCoroutine
            }

            firebaseUser.reload()
                .addOnSuccessListener {
                    continuation.resume(Result.success(firebaseAuth.currentUser?.isEmailVerified == true))
                }
                .addOnFailureListener { exception ->
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
        return suspendCancellableCoroutine { continuation ->
            val task = firebaseAuth.signInWithCredential(credential)

            task.addOnSuccessListener { authResult ->
                val firebaseUser = authResult.user
                val user = User(
                    id = firebaseUser?.uid.orEmpty(),
                    name = firebaseUser?.displayName.orEmpty(),
                    mobileNumber = firebaseUser?.phoneNumber.orEmpty(),
                    email = firebaseUser?.email.orEmpty(),
                    profilePhotoUrl = firebaseUser?.photoUrl?.toString(),
                    authProvider = provider,
                    isEmailVerified = provider == AuthProvider.GOOGLE || firebaseUser?.isEmailVerified == true,
                    isPhoneVerified = !firebaseUser?.phoneNumber.isNullOrBlank()
                )
                continuation.resume(Result.success(AuthSession(user, authResult.additionalUserInfo?.isNewUser == true)))
            }.addOnFailureListener { exception ->
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
        const val EMAIL_LINK_CONTINUE_URL = "https://sportsxtreme-95fbb.firebaseapp.com/auth"
    }
}
