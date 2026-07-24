package com.example.sportsxtreme.domain.usecase

data class AuthUseCases(
    val createEmailAccount: CreateEmailAccountUseCase,
    val resendEmailVerification: ResendEmailVerificationUseCase,
    val checkEmailVerification: CheckEmailVerificationUseCase,
    val signInWithEmailAndPassword: SignInWithEmailAndPasswordUseCase,
    val signInWithGoogle: SignInWithGoogleUseCase,
    val sendLoginEmailLink: SendLoginEmailLinkUseCase,
    val completeEmailLinkLogin: CompleteEmailLinkLoginUseCase,
    val handleIncomingEmailLink: HandleIncomingEmailLinkUseCase,
    val getPendingEmailLink: GetPendingEmailLinkUseCase,
    val createOrUpdateUserProfile: CreateOrUpdateUserProfileUseCase,
    val getUserProfile: GetUserProfileUseCase,
    val updateUserProfile: UpdateUserProfileUseCase,
    val getUserProfileStats: GetUserProfileStatsUseCase,
    val getUserProfileSettings: GetUserProfileSettingsUseCase,
    val getUserAchievements: GetUserAchievementsUseCase,
    val sendPhoneOtp: SendPhoneOtpUseCase,
    val resendPhoneOtp: ResendPhoneOtpUseCase,
    val verifyPhoneOtp: VerifyPhoneOtpUseCase,
    val getCurrentUser: GetCurrentUserUseCase
)
