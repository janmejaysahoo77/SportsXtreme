package com.sportsxtreme.app.presentation.auth

enum class PhoneVerificationStatus {
    Idle,
    SendingOtp,
    OtpSent,
    VerifyingOtp,
    VerificationSuccess,
    VerificationFailure,
    AutoVerified
}
