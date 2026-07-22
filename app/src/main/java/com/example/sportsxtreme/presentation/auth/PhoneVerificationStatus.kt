package com.example.sportsxtreme.presentation.auth

enum class PhoneVerificationStatus {
    Idle,
    SendingOtp,
    OtpSent,
    VerifyingOtp,
    VerificationSuccess,
    VerificationFailure,
    AutoVerified
}
