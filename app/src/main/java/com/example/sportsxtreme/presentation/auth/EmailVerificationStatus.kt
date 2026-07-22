package com.example.sportsxtreme.presentation.auth

enum class EmailVerificationStatus {
    Idle,
    VerificationEmailSent,
    CheckingVerification,
    Verified,
    NotVerified,
    ResendingEmail,
    ResendSuccess,
    Failure
}
