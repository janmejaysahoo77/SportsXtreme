package com.example.sportsxtreme.data.di

import android.app.Activity
import android.content.Context
import com.example.sportsxtreme.data.remote.auth.AuthDataSource
import com.example.sportsxtreme.data.remote.auth.FirebasePhoneAuthManager
import com.example.sportsxtreme.data.remote.firestore.FirebaseFirestoreUserDataSource
import com.example.sportsxtreme.data.repository.AuthRepositoryImpl
import com.example.sportsxtreme.domain.repository.AuthRepository
import com.example.sportsxtreme.domain.usecase.AuthUseCases
import com.example.sportsxtreme.domain.usecase.CreateEmailAccountUseCase
import com.example.sportsxtreme.domain.usecase.CreateOrUpdateUserProfileUseCase
import com.example.sportsxtreme.domain.usecase.GetCurrentUserUseCase
import com.example.sportsxtreme.domain.usecase.GetUserAchievementsUseCase
import com.example.sportsxtreme.domain.usecase.GetUserProfileUseCase
import com.example.sportsxtreme.domain.usecase.GetUserProfileSettingsUseCase
import com.example.sportsxtreme.domain.usecase.GetUserProfileStatsUseCase
import com.example.sportsxtreme.domain.usecase.ResendPhoneOtpUseCase
import com.example.sportsxtreme.domain.usecase.ResendEmailVerificationUseCase
import com.example.sportsxtreme.domain.usecase.SendEmailChangeVerificationUseCase
import com.example.sportsxtreme.domain.usecase.SendPhoneOtpUseCase
import com.example.sportsxtreme.domain.usecase.SignInWithGoogleUseCase
import com.example.sportsxtreme.domain.usecase.VerifyPhoneOtpUseCase
import com.example.sportsxtreme.domain.usecase.CheckEmailVerificationUseCase
import com.example.sportsxtreme.domain.usecase.CompleteEmailLinkLoginUseCase
import com.example.sportsxtreme.domain.usecase.GetPendingEmailLinkUseCase
import com.example.sportsxtreme.domain.usecase.HandleIncomingEmailLinkUseCase
import com.example.sportsxtreme.domain.usecase.SendLoginEmailLinkUseCase
import com.example.sportsxtreme.domain.usecase.SignInWithEmailAndPasswordUseCase
import com.example.sportsxtreme.domain.usecase.UpdateUserProfileUseCase
import com.example.sportsxtreme.presentation.auth.AuthViewModel

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
