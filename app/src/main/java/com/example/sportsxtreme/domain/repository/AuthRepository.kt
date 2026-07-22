package com.example.sportsxtreme.domain.repository

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.AuthSession
import com.example.sportsxtreme.domain.model.EmailSignupRequest
import com.example.sportsxtreme.domain.model.PendingUserProfile
import com.example.sportsxtreme.domain.model.PhoneAuthSession
import com.example.sportsxtreme.domain.model.User

interface AuthRepository {
    fun currentUser(): User?

    suspend fun createEmailAccount(request: EmailSignupRequest): Resource<AuthSession>
    suspend fun resendEmailVerification(): Resource<Unit>
    suspend fun checkEmailVerification(): Resource<Boolean>
    suspend fun signInWithEmailAndPassword(email: String, password: String): Resource<AuthSession>
    suspend fun signInWithGoogle(idToken: String): Resource<AuthSession>
    suspend fun sendLoginEmailLink(email: String): Resource<Unit>
    fun isEmailSignInLink(link: String): Boolean
    fun storePendingEmailSignInLink(link: String)
    fun hasPendingEmailSignInLink(): Boolean
    fun pendingEmailForLink(): String?
    suspend fun completeEmailLinkLogin(email: String, link: String? = null): Resource<AuthSession>
    suspend fun createOrUpdateUserProfile(profile: PendingUserProfile): Resource<User>
    suspend fun sendPhoneOtp(phoneNumber: String): Resource<PhoneAuthSession>
    suspend fun resendPhoneOtp(session: PhoneAuthSession): Resource<PhoneAuthSession>
    suspend fun verifyPhoneOtp(verificationId: String, otpCode: String): Resource<Unit>

    suspend fun login(mobileNumber: String): Resource<User>
    suspend fun signup(name: String, email: String, password: String): Resource<User>
    suspend fun signupWithGoogle(idToken: String): Resource<User>
}
