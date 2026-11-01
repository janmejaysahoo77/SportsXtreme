package com.sportsxtreme.app.presentation.auth

import com.sportsxtreme.app.domain.model.AuthSession
import com.sportsxtreme.app.domain.model.PhoneAuthSession
import com.sportsxtreme.app.domain.model.User

data class AuthState(
    val isLoading: Boolean = false,
    val emailVerificationStatus: EmailVerificationStatus = EmailVerificationStatus.Idle,
    val phoneVerificationStatus: PhoneVerificationStatus = PhoneVerificationStatus.Idle,
    val pendingAuthSession: AuthSession? = null,
    val pendingPhoneSession: PhoneAuthSession? = null,
    val isNewPhoneUser: Boolean = false,
    val authenticatedUser: User? = null,
    val smsMessage: String? = null,
    val errorMessage: String? = null
)
