package com.example.sportsxtreme.presentation.auth

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.AuthSession
import com.example.sportsxtreme.domain.model.EmailSignupRequest
import com.example.sportsxtreme.domain.model.PendingUserProfile
import com.example.sportsxtreme.domain.model.PhoneAuthSession
import com.example.sportsxtreme.domain.model.User
import com.example.sportsxtreme.domain.usecase.AuthUseCases
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withTimeoutOrNull

class AuthViewModel(
    private val useCases: AuthUseCases
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = MutableStateFlow(AuthState(authenticatedUser = useCases.getCurrentUser()))
    val state: StateFlow<AuthState> = _state.asStateFlow()

    suspend fun createEmailAccount(name: String, email: String, password: String, phoneNumber: String): Resource<AuthSession> {
        setLoading()
        val result = useCases.createEmailAccount(
            EmailSignupRequest(
                name = name,
                email = email,
                password = password,
                phoneNumber = phoneNumber
            )
        )
        return result.also { updateAuthSession(it) }
    }

    suspend fun resendEmailVerification(): Resource<Unit> {
        _state.value = state.value.copy(
            isLoading = true,
            emailVerificationStatus = EmailVerificationStatus.ResendingEmail,
            errorMessage = null
        )
        val result = useCases.resendEmailVerification()
        return when (result) {
            is Resource.Success -> {
                _state.value = state.value.copy(
                    isLoading = false,
                    emailVerificationStatus = EmailVerificationStatus.ResendSuccess,
                    errorMessage = null
                )
                Resource.Success(Unit)
            }
            is Resource.Error -> {
                val message = result.message ?: "Could not resend verification email"
                _state.value = state.value.copy(
                    isLoading = false,
                    emailVerificationStatus = EmailVerificationStatus.Failure,
                    errorMessage = message
                )
                Resource.Error(message)
            }
            is Resource.Loading -> Resource.Loading()
        }
    }

    suspend fun confirmEmailVerification(): Resource<User> {
        val pendingUser = state.value.pendingAuthSession?.user
            ?: useCases.getCurrentUser()
            ?: return Resource.Error("No authenticated account is waiting for email verification.")
        _state.value = state.value.copy(
            isLoading = true,
            emailVerificationStatus = EmailVerificationStatus.CheckingVerification,
            errorMessage = null
        )

        return when (val result = useCases.checkEmailVerification()) {
            is Resource.Success -> {
                if (result.data == true) {
                    completeAuthentication(pendingUser.copy(isEmailVerified = true))
                } else {
                    val message = "Your email has not been verified yet."
                    _state.value = state.value.copy(
                        isLoading = false,
                        emailVerificationStatus = EmailVerificationStatus.NotVerified,
                        errorMessage = message
                    )
                    Resource.Error(message)
                }
            }
            is Resource.Error -> {
                val message = result.message ?: "Could not check email verification"
                _state.value = state.value.copy(
                    isLoading = false,
                    emailVerificationStatus = EmailVerificationStatus.Failure,
                    errorMessage = message
                )
                Resource.Error(message)
            }
            is Resource.Loading -> Resource.Loading()
        }
    }

    suspend fun signInWithGoogle(idToken: String): Resource<AuthSession> {
        setLoading()
        val result = useCases.signInWithGoogle(idToken)
        return result.also { updateAuthSession(it) }
    }

    suspend fun signInWithEmailAndPassword(email: String, password: String): Resource<User> {
        _state.value = state.value.copy(isLoading = true, errorMessage = null)
        return when (val result = useCases.signInWithEmailAndPassword(email, password)) {
            is Resource.Success -> {
                val session = result.data ?: return Resource.Error("Login returned an empty account.")
                completeAuthentication(session.user)
            }
            is Resource.Error -> {
                val message = result.message ?: "Login failed"
                _state.value = state.value.copy(isLoading = false, errorMessage = message)
                Resource.Error(message)
            }
            is Resource.Loading -> Resource.Loading()
        }
    }

    suspend fun sendLoginEmailLink(email: String): Resource<Unit> {
        _state.value = state.value.copy(isLoading = true, errorMessage = null)
        return when (val result = useCases.sendLoginEmailLink(email)) {
            is Resource.Success -> {
                _state.value = state.value.copy(isLoading = false, errorMessage = null)
                Resource.Success(Unit)
            }
            is Resource.Error -> {
                val message = result.message ?: "Could not send login link"
                _state.value = state.value.copy(isLoading = false, errorMessage = message)
                Resource.Error(message)
            }
            is Resource.Loading -> Resource.Loading()
        }
    }

    suspend fun completeEmailLinkLogin(email: String, link: String? = null): Resource<User> {
        _state.value = state.value.copy(isLoading = true, errorMessage = null)
        return when (val result = useCases.completeEmailLinkLogin(email, link)) {
            is Resource.Success -> {
                val session = result.data ?: return Resource.Error("Login returned an empty account.")
                completeAuthentication(session.user.copy(isEmailVerified = true))
            }
            is Resource.Error -> {
                val message = result.message ?: "Login failed"
                _state.value = state.value.copy(isLoading = false, errorMessage = message)
                Resource.Error(message)
            }
            is Resource.Loading -> Resource.Loading()
        }
    }

    fun handleIncomingEmailLink(link: String?): Boolean {
        if (link.isNullOrBlank()) return false
        return useCases.handleIncomingEmailLink(link)
    }

    fun hasPendingEmailSignInLink(): Boolean {
        return useCases.getPendingEmailLink.hasPendingLink()
    }

    fun pendingEmailForLink(): String? {
        return useCases.getPendingEmailLink.pendingEmail()
    }

    suspend fun completePendingAuthentication(): Resource<User> {
        val authSession = state.value.pendingAuthSession
            ?: return Resource.Error("No authenticated account is waiting to be completed.")
        return completeAuthentication(authSession.user)
    }

    suspend fun sendPhoneOtp(phoneNumber: String): Resource<PhoneAuthSession> {
        _state.value = state.value.copy(
            isLoading = true,
            phoneVerificationStatus = PhoneVerificationStatus.SendingOtp,
            errorMessage = null
        )
        val result = useCases.sendPhoneOtp(phoneNumber)
        return result.also { updatePhoneSession(it) }
    }

    suspend fun resendPhoneOtp(): Resource<PhoneAuthSession> {
        val currentSession = state.value.pendingPhoneSession
            ?: return Resource.Error("No phone verification session is active.")
        _state.value = state.value.copy(
            isLoading = true,
            phoneVerificationStatus = PhoneVerificationStatus.SendingOtp,
            errorMessage = null
        )
        val result = useCases.resendPhoneOtp(currentSession)
        return result.also { updatePhoneSession(it) }
    }

    suspend fun verifyPhoneOtp(otpCode: String): Resource<Unit> {
        val phoneSession = state.value.pendingPhoneSession
            ?: return Resource.Error("No phone verification session is active.")
        if (phoneSession.isAutoVerified) {
            return markPhoneVerified(phoneSession.phoneNumber, PhoneVerificationStatus.AutoVerified)
        }
        _state.value = state.value.copy(
            isLoading = true,
            phoneVerificationStatus = PhoneVerificationStatus.VerifyingOtp,
            errorMessage = null
        )
        val result = useCases.verifyPhoneOtp(phoneSession.verificationId, otpCode)
        return when (result) {
            is Resource.Success -> {
                markPhoneVerified(phoneSession.phoneNumber, PhoneVerificationStatus.VerificationSuccess)
            }
            is Resource.Error -> {
                val message = result.message ?: "OTP verification failed"
                _state.value = state.value.copy(
                    isLoading = false,
                    phoneVerificationStatus = PhoneVerificationStatus.VerificationFailure,
                    errorMessage = message
                )
                Resource.Error(message)
            }
            is Resource.Loading -> result
        }
    }

    fun completeWithVerifiedPhone(phoneNumber: String): Resource<User> {
        val authSession = state.value.pendingAuthSession
            ?: return Resource.Error("No authenticated account is waiting for phone verification.")
        val user = authSession.user.copy(mobileNumber = phoneNumber, isPhoneVerified = true)
        _state.value = state.value.copy(
            isLoading = false,
            pendingAuthSession = null,
            authenticatedUser = user,
            phoneVerificationStatus = PhoneVerificationStatus.VerificationSuccess,
            errorMessage = null
        )
        return Resource.Success(user)
    }

    private suspend fun completeAuthentication(user: User): Resource<User> {
        _state.value = state.value.copy(isLoading = true, errorMessage = null)
        val result = withTimeoutOrNull(PROFILE_WRITE_TIMEOUT_MS) {
            useCases.createOrUpdateUserProfile(
                PendingUserProfile(
                    id = user.id,
                    name = user.name,
                    email = user.email,
                    phoneNumber = user.mobileNumber,
                    profilePhotoUrl = user.profilePhotoUrl,
                    authProvider = user.authProvider,
                    isEmailVerified = user.isEmailVerified,
                    isPhoneVerified = user.isPhoneVerified
                )
            )
        } ?: return completeAuthWithoutBlockingOnProfile(user)

        return when (result) {
            is Resource.Success -> {
                val authenticatedUser = result.data ?: user
                _state.value = state.value.copy(
                    isLoading = false,
                    pendingAuthSession = null,
                    pendingPhoneSession = null,
                    authenticatedUser = authenticatedUser,
                    emailVerificationStatus = if (authenticatedUser.isEmailVerified) {
                        EmailVerificationStatus.Verified
                    } else {
                        state.value.emailVerificationStatus
                    },
                    errorMessage = null
                )
                Resource.Success(authenticatedUser)
            }
            is Resource.Error -> {
                completeAuthWithoutBlockingOnProfile(user)
            }
            is Resource.Loading -> result
        }
    }

    private fun completeAuthWithoutBlockingOnProfile(user: User): Resource<User> {
        _state.value = state.value.copy(
            isLoading = false,
            pendingAuthSession = null,
            pendingPhoneSession = null,
            authenticatedUser = user,
            emailVerificationStatus = EmailVerificationStatus.Verified,
            errorMessage = null
        )
        return Resource.Success(user)
    }

    fun clear() {
        scope.cancel()
    }

    private suspend fun markPhoneVerified(phoneNumber: String, status: PhoneVerificationStatus): Resource<Unit> {
        val verifiedUser = state.value.pendingAuthSession?.user?.copy(
            mobileNumber = phoneNumber,
            isPhoneVerified = true
        )
        if (verifiedUser != null) {
            val profileResult = withTimeoutOrNull(PROFILE_WRITE_TIMEOUT_MS) {
                useCases.createOrUpdateUserProfile(
                    PendingUserProfile(
                        id = verifiedUser.id,
                        name = verifiedUser.name,
                        email = verifiedUser.email,
                        phoneNumber = verifiedUser.mobileNumber,
                        profilePhotoUrl = verifiedUser.profilePhotoUrl,
                        authProvider = verifiedUser.authProvider,
                        isEmailVerified = verifiedUser.isEmailVerified,
                        isPhoneVerified = verifiedUser.isPhoneVerified
                    )
                )
            } ?: Resource.Error("Phone verified, but profile update timed out. Try again.")

            if (profileResult is Resource.Error) {
                val message = profileResult.message ?: "Phone verified, but profile update failed."
                _state.value = state.value.copy(
                    isLoading = false,
                    phoneVerificationStatus = PhoneVerificationStatus.VerificationFailure,
                    errorMessage = message
                )
                return Resource.Error(message)
            }
        }
        _state.value = state.value.copy(
            isLoading = false,
            pendingAuthSession = null,
            pendingPhoneSession = null,
            authenticatedUser = verifiedUser ?: state.value.authenticatedUser,
            phoneVerificationStatus = status,
            errorMessage = null
        )
        return Resource.Success(Unit)
    }

    private fun updateAuthSession(result: Resource<AuthSession>) {
        when (result) {
            is Resource.Success -> {
                _state.value = state.value.copy(
                    isLoading = false,
                    pendingAuthSession = result.data,
                    emailVerificationStatus = EmailVerificationStatus.VerificationEmailSent,
                    errorMessage = null
                )
            }
            is Resource.Error -> setError(result.message ?: "Authentication failed")
            is Resource.Loading -> setLoading()
        }
    }

    private fun updatePhoneSession(result: Resource<PhoneAuthSession>) {
        when (result) {
            is Resource.Success -> {
                val status = if (result.data?.isAutoVerified == true) {
                    PhoneVerificationStatus.AutoVerified
                } else {
                    PhoneVerificationStatus.OtpSent
                }
                _state.value = state.value.copy(
                    isLoading = false,
                    pendingPhoneSession = result.data,
                    phoneVerificationStatus = status,
                    errorMessage = null
                )
            }
            is Resource.Error -> {
                _state.value = state.value.copy(
                    isLoading = false,
                    phoneVerificationStatus = PhoneVerificationStatus.VerificationFailure,
                    errorMessage = result.message ?: "Phone verification failed"
                )
            }
            is Resource.Loading -> setLoading()
        }
    }

    private fun setLoading() {
        _state.value = state.value.copy(isLoading = true, errorMessage = null)
    }

    private fun setError(message: String) {
        _state.value = state.value.copy(
            isLoading = false,
            phoneVerificationStatus = PhoneVerificationStatus.VerificationFailure,
            errorMessage = message
        )
    }

    private fun String.isFirestoreDatabaseMissing(): Boolean {
        return contains("database (default) does not exist", ignoreCase = true) ||
            contains("Cloud Firestore database", ignoreCase = true)
    }

    private companion object {
        const val PROFILE_WRITE_TIMEOUT_MS = 3000L
    }
}
