package com.sportsxtreme.app.domain.usecase

import com.sportsxtreme.app.common.Resource
import com.sportsxtreme.app.domain.repository.AuthRepository

class VerifyPhoneOtpUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(verificationId: String, otpCode: String): Resource<Boolean> {
        return repository.verifyPhoneOtp(verificationId, otpCode)
    }
}
