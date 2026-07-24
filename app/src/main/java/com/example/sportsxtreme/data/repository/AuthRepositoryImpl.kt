package com.example.sportsxtreme.data.repository

import android.content.Context
import android.util.Log
import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.data.local.auth.PendingEmailLinkStore
import com.example.sportsxtreme.data.remote.auth.AuthDataSource
import com.example.sportsxtreme.data.remote.auth.FirebasePhoneAuthManager
import com.example.sportsxtreme.data.remote.auth.PhoneAuthManager
import com.example.sportsxtreme.data.remote.firestore.FirestoreUserDataSource
import com.example.sportsxtreme.data.remote.firestore.PendingFirestoreUserDataSource
import com.example.sportsxtreme.domain.model.AuthProvider
import com.example.sportsxtreme.domain.model.AuthSession
import com.example.sportsxtreme.domain.model.EmailSignupRequest
import com.example.sportsxtreme.domain.model.PendingUserProfile
import com.example.sportsxtreme.domain.model.PhoneAuthSession
import com.example.sportsxtreme.domain.model.User
import com.example.sportsxtreme.domain.model.UserAchievement
import com.example.sportsxtreme.domain.model.UserProfile
import com.example.sportsxtreme.domain.model.UserProfileSettings
import com.example.sportsxtreme.domain.model.UserProfileStats
import com.example.sportsxtreme.domain.repository.AuthRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthActionCodeException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider

class AuthRepositoryImpl(
    context: Context,
    private val authDataSource: AuthDataSource = AuthDataSource(context.applicationContext),
    private val firestoreUserDataSource: FirestoreUserDataSource = PendingFirestoreUserDataSource(),
    private val phoneAuthManager: PhoneAuthManager = FirebasePhoneAuthManager(),
    private val pendingEmailLinkStore: PendingEmailLinkStore = PendingEmailLinkStore(context.applicationContext)
) : AuthRepository {
    override fun currentUser(): User? {
        return authDataSource.currentUser()
    }

    override suspend fun createEmailAccount(request: EmailSignupRequest): Resource<AuthSession> {
        return authDataSource.createEmailAccount(request).fold(
            onSuccess = { session ->
                when (val seedResult = seedFirestoreProfile(session.user, "email signup")) {
                    is Resource.Success -> Resource.Success(session)
                    is Resource.Error -> Resource.Error(seedResult.message ?: "Firestore profile setup failed")
                    is Resource.Loading -> Resource.Loading()
                }
            },
            onFailure = { error -> Resource.Error(error.toAuthMessage("Signup failed")) }
        )
    }

    override suspend fun resendEmailVerification(): Resource<Unit> {
        return authDataSource.resendEmailVerification().toResource("Could not resend verification email")
    }

    override suspend fun checkEmailVerification(): Resource<Boolean> {
        return authDataSource.reloadAndCheckEmailVerified().toResource("Could not check email verification")
    }

    override suspend fun signInWithEmailAndPassword(email: String, password: String): Resource<AuthSession> {
        return authDataSource.signInWithEmailAndPassword(email, password).fold(
            onSuccess = { session ->
                if (!session.user.isEmailVerified) {
                    authDataSource.signOut()
                    Resource.Error("Please verify your email before logging in.")
                } else {
                    Resource.Success(session)
                }
            },
            onFailure = { error -> Resource.Error(error.toAuthMessage("Login failed")) }
        )
    }

    override suspend fun signInWithGoogle(idToken: String): Resource<AuthSession> {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return authDataSource.signInWithCredential(credential, AuthProvider.GOOGLE).fold(
            onSuccess = { session ->
                when (val seedResult = seedFirestoreProfile(session.user, "Google sign-in")) {
                    is Resource.Success -> Resource.Success(session)
                    is Resource.Error -> Resource.Error(seedResult.message ?: "Firestore profile setup failed")
                    is Resource.Loading -> Resource.Loading()
                }
            },
            onFailure = { error -> Resource.Error(error.toAuthMessage("Google signup failed")) }
        )
    }

    override suspend fun sendLoginEmailLink(email: String): Resource<Unit> {
        return authDataSource.sendEmailSignInLink(email).fold(
            onSuccess = {
                pendingEmailLinkStore.saveEmail(email)
                Resource.Success(Unit)
            },
            onFailure = { error ->
                Resource.Error(error.toAuthMessage("Could not send login link"))
            }
        )
    }

    override fun isEmailSignInLink(link: String): Boolean {
        return authDataSource.isSignInWithEmailLink(link)
    }

    override fun storePendingEmailSignInLink(link: String) {
        pendingEmailLinkStore.saveLink(link)
    }

    override fun hasPendingEmailSignInLink(): Boolean {
        return pendingEmailLinkStore.getLink() != null
    }

    override suspend fun completeEmailLinkLogin(email: String, link: String?): Resource<AuthSession> {
        val emailLink = link ?: pendingEmailLinkStore.getLink()
        if (emailLink.isNullOrBlank()) {
            return Resource.Error("Open the latest login email link, then try again.")
        }
        if (!authDataSource.isSignInWithEmailLink(emailLink)) {
            pendingEmailLinkStore.clearLink()
            return Resource.Error("This login link is invalid or has been changed. Request a new login link.")
        }

        return authDataSource.signInWithEmailLink(email, emailLink).fold(
            onSuccess = { session ->
                pendingEmailLinkStore.clearAll()
                if (session.isNewUser) {
                    authDataSource.deleteCurrentUserAndSignOut()
                    return Resource.Error("No account found with this email. Please sign up first.")
                }
                Resource.Success(session)
            },
            onFailure = { error ->
                Resource.Error(error.toAuthMessage("Login failed"))
            }
        )
    }

    override fun pendingEmailForLink(): String? {
        return pendingEmailLinkStore.getEmail()
    }

    override suspend fun createOrUpdateUserProfile(profile: PendingUserProfile): Resource<User> {
        return firestoreUserDataSource.createOrUpdateUser(profile).toResource("User profile initialization failed")
    }

    private suspend fun seedFirestoreProfile(user: User, source: String): Resource<User> {
        return firestoreUserDataSource.createOrUpdateUser(user.toPendingProfile()).fold(
            onSuccess = { Resource.Success(it) },
            onFailure = { error ->
                Log.e(TAG, "Firestore profile seed failed during $source for uid=${user.id}", error)
                Resource.Error("Account created, but Firestore profile setup failed: ${error.toAuthMessage("Firestore write failed")}")
            }
        )
    }

    private fun User.toPendingProfile(): PendingUserProfile {
        return PendingUserProfile(
            id = id,
            name = name,
            email = email,
            phoneNumber = mobileNumber,
            profilePhotoUrl = profilePhotoUrl,
            authProvider = authProvider,
            isEmailVerified = isEmailVerified,
            isPhoneVerified = isPhoneVerified
        )
    }

    override suspend fun getUserProfile(userId: String): Resource<UserProfile> {
        return firestoreUserDataSource.getUserProfile(userId).toResource("Could not load user profile")
    }

    override suspend fun updateUserProfile(profile: UserProfile): Resource<UserProfile> {
        return firestoreUserDataSource.updateUserProfile(profile).toResource("Could not update user profile")
    }

    override suspend fun getUserProfileStats(userId: String): Resource<UserProfileStats> {
        return firestoreUserDataSource.getUserProfileStats(userId).toResource("Could not load profile stats")
    }

    override suspend fun updateUserProfileStats(stats: UserProfileStats): Resource<UserProfileStats> {
        return firestoreUserDataSource.updateUserProfileStats(stats).toResource("Could not update profile stats")
    }

    override suspend fun getUserProfileSettings(userId: String): Resource<UserProfileSettings> {
        return firestoreUserDataSource.getUserProfileSettings(userId).toResource("Could not load profile settings")
    }

    override suspend fun updateUserProfileSettings(settings: UserProfileSettings): Resource<UserProfileSettings> {
        return firestoreUserDataSource.updateUserProfileSettings(settings).toResource("Could not update profile settings")
    }

    override suspend fun getUserAchievements(userId: String): Resource<List<UserAchievement>> {
        return firestoreUserDataSource.getUserAchievements(userId).toResource("Could not load achievements")
    }

    override suspend fun sendPhoneOtp(phoneNumber: String): Resource<PhoneAuthSession> {
        return phoneAuthManager.sendOtp(phoneNumber).toResource("Could not send OTP")
    }

    override suspend fun resendPhoneOtp(session: PhoneAuthSession): Resource<PhoneAuthSession> {
        return phoneAuthManager.resendOtp(session).toResource("Could not resend OTP")
    }

    override suspend fun verifyPhoneOtp(verificationId: String, otpCode: String): Resource<Unit> {
        return phoneAuthManager.verifyOtp(verificationId, otpCode).toResource("OTP verification failed")
    }

    override suspend fun login(mobileNumber: String): Resource<User> {
        // TODO: Implement network/local call
        return Resource.Success(User("1", "Dummy User", mobileNumber))
    }

    override suspend fun signup(name: String, email: String, password: String): Resource<User> {
        return createEmailAccount(
            EmailSignupRequest(
                name = name,
                email = email,
                password = password,
                phoneNumber = ""
            )
        ).mapData { it.user }
    }

    override suspend fun signupWithGoogle(idToken: String): Resource<User> {
        return signInWithGoogle(idToken).mapData { it.user }
    }

    private fun <T> Result<T>.toResource(defaultMessage: String): Resource<T> {
        return fold(
            onSuccess = { value -> Resource.Success(value) },
            onFailure = { error -> Resource.Error(error.toAuthMessage(defaultMessage)) }
        )
    }

    private fun Throwable.toAuthMessage(defaultMessage: String): String {
        val rawMessage = message.orEmpty()
        return when {
            this is FirebaseNetworkException -> "You're offline or the network is unavailable. Check your connection and try again."
            this is FirebaseAuthInvalidUserException -> "No account found with this email. Please sign up first."
            this is FirebaseAuthActionCodeException -> "This login link has expired or was already used. Request a new login link."
            this is FirebaseAuthInvalidCredentialsException -> "Email or password is incorrect."
            rawMessage.contains("operation is not allowed", ignoreCase = true) ||
                rawMessage.contains("sign-in provider is disabled", ignoreCase = true) -> {
                "Email link login is not enabled in Firebase yet. Enable Email/Password sign-in and Email link sign-in, then try again."
            }
            rawMessage.contains("blocked all requests", ignoreCase = true) ||
                rawMessage.contains("unusual activity", ignoreCase = true) -> {
                "Firebase has temporarily blocked verification emails from this device because too many requests were sent. Wait a while before trying again."
            }
            rawMessage.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) -> {
                "This login link is invalid or does not match the email address. Request a new login link."
            }
            rawMessage.isNotBlank() -> rawMessage
            else -> defaultMessage
        }
    }

    private fun <T, R> Resource<T>.mapData(mapper: (T) -> R): Resource<R> {
        return when (this) {
            is Resource.Success -> {
                val value = data ?: return Resource.Error("Authentication returned an empty result")
                Resource.Success(mapper(value))
            }
            is Resource.Error -> Resource.Error(message ?: "Authentication failed")
            is Resource.Loading -> Resource.Loading()
        }
    }

    private companion object {
        const val TAG = "AuthRepository"
    }
}
