package com.example.sportsxtreme.domain.usecase

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.repository.AuthRepository

class VerifyPhoneOtpUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(verificationId: String, otpCode: String): Resource<Unit> {
        return repository.verifyPhoneOtp(verificationId, otpCode)
    }
}
