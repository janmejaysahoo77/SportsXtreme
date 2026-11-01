package com.sportsxtreme.app.data.di

import android.app.Activity
import android.content.Context
import com.sportsxtreme.app.data.remote.auth.AuthDataSource
import com.sportsxtreme.app.data.remote.auth.FirebasePhoneAuthManager
import com.sportsxtreme.app.data.remote.firestore.FirebaseFirestoreUserDataSource
import com.sportsxtreme.app.data.repository.AuthRepositoryImpl
import com.sportsxtreme.app.domain.repository.AuthRepository
import com.sportsxtreme.app.domain.usecase.AuthUseCases
import com.sportsxtreme.app.domain.usecase.CreateEmailAccountUseCase
import com.sportsxtreme.app.domain.usecase.CreateOrUpdateUserProfileUseCase
import com.sportsxtreme.app.domain.usecase.GetCurrentUserUseCase
import com.sportsxtreme.app.domain.usecase.GetUserAchievementsUseCase
import com.sportsxtreme.app.domain.usecase.GetUserProfileUseCase
import com.sportsxtreme.app.domain.usecase.GetUserProfileSettingsUseCase
import com.sportsxtreme.app.domain.usecase.GetUserProfileStatsUseCase
import com.sportsxtreme.app.domain.usecase.ResendPhoneOtpUseCase
import com.sportsxtreme.app.domain.usecase.ResendEmailVerificationUseCase
import com.sportsxtreme.app.domain.usecase.SendEmailChangeVerificationUseCase
import com.sportsxtreme.app.domain.usecase.SendPhoneOtpUseCase
import com.sportsxtreme.app.domain.usecase.SignInWithGoogleUseCase
import com.sportsxtreme.app.domain.usecase.VerifyPhoneOtpUseCase
import com.sportsxtreme.app.domain.usecase.CheckEmailVerificationUseCase
import com.sportsxtreme.app.domain.usecase.CompleteEmailLinkLoginUseCase
import com.sportsxtreme.app.domain.usecase.GetPendingEmailLinkUseCase
import com.sportsxtreme.app.domain.usecase.HandleIncomingEmailLinkUseCase
import com.sportsxtreme.app.domain.usecase.SendLoginEmailLinkUseCase
import com.sportsxtreme.app.domain.usecase.SignInWithEmailAndPasswordUseCase
import com.sportsxtreme.app.domain.usecase.UpdateUserProfileUseCase
import com.sportsxtreme.app.presentation.auth.AuthViewModel

object AuthDependencies {
    private lateinit var appContext: Context

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    private val phoneAuthManager: FirebasePhoneAuthManager by lazy {
        FirebasePhoneAuthManager()
    }

    private val repository: AuthRepository by lazy {
        check(::appContext.isInitialized) {
            "AuthDependencies.initialize(context) must be called before using auth dependencies."
        }
        AuthRepositoryImpl(
            context = appContext,
            authDataSource = AuthDataSource(appContext),
            firestoreUserDataSource = FirebaseFirestoreUserDataSource(),
            phoneAuthManager = phoneAuthManager
        )
    }

    private val useCases: AuthUseCases by lazy {
        AuthUseCases(
            createEmailAccount = CreateEmailAccountUseCase(repository),
            resendEmailVerification = ResendEmailVerificationUseCase(repository),
            sendEmailChangeVerification = SendEmailChangeVerificationUseCase(repository),
            checkEmailVerification = CheckEmailVerificationUseCase(repository),
            signInWithEmailAndPassword = SignInWithEmailAndPasswordUseCase(repository),
            signInWithGoogle = SignInWithGoogleUseCase(repository),
            sendLoginEmailLink = SendLoginEmailLinkUseCase(repository),
            completeEmailLinkLogin = CompleteEmailLinkLoginUseCase(repository),
            handleIncomingEmailLink = HandleIncomingEmailLinkUseCase(repository),
            getPendingEmailLink = GetPendingEmailLinkUseCase(repository),
            createOrUpdateUserProfile = CreateOrUpdateUserProfileUseCase(repository),
            getUserProfile = GetUserProfileUseCase(repository),
            updateUserProfile = UpdateUserProfileUseCase(repository),
            getUserProfileStats = GetUserProfileStatsUseCase(repository),
            getUserProfileSettings = GetUserProfileSettingsUseCase(repository),
            getUserAchievements = GetUserAchievementsUseCase(repository),
            sendPhoneOtp = SendPhoneOtpUseCase(repository),
            resendPhoneOtp = ResendPhoneOtpUseCase(repository),
            verifyPhoneOtp = VerifyPhoneOtpUseCase(repository),
            getCurrentUser = GetCurrentUserUseCase(repository)
        )
    }

    private val authViewModel: AuthViewModel by lazy {
        AuthViewModel(useCases)
    }

    fun authViewModel(): AuthViewModel {
        return authViewModel
    }

    fun authUseCases(): AuthUseCases {
        return useCases
    }

    fun bindPhoneAuthActivity(activity: Activity) {
        phoneAuthManager.bindActivity(activity)
    }
}
