package com.example.sportsxtreme.presentation.auth

import com.example.sportsxtreme.domain.model.AuthSession
import com.example.sportsxtreme.domain.model.PhoneAuthSession
import com.example.sportsxtreme.domain.model.User

data class AuthState(
    val isLoading: Boolean = false,
    val emailVerificationStatus: EmailVerificationStatus = EmailVerificationStatus.Idle,
    val phoneVerificationStatus: PhoneVerificationStatus = PhoneVerificationStatus.Idle,
    val pendingAuthSession: AuthSession? = null,
    val pendingPhoneSession: PhoneAuthSession? = null,
    val authenticatedUser: User? = null,
    val errorMessage: String? = null
)
