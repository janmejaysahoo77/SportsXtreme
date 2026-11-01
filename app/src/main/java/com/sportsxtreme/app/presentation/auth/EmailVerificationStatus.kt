package com.sportsxtreme.app.presentation.auth

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
