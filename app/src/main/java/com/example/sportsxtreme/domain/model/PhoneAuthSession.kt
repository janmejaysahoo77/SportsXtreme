package com.example.sportsxtreme.domain.model

data class PhoneAuthSession(
    val verificationId: String,
    val phoneNumber: String,
    val canResend: Boolean = true,
    val isAutoVerified: Boolean = false
)
