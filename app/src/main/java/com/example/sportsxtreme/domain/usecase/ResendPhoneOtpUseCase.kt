package com.example.sportsxtreme.domain.usecase

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.PhoneAuthSession
import com.example.sportsxtreme.domain.repository.AuthRepository

class ResendPhoneOtpUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(session: PhoneAuthSession): Resource<PhoneAuthSession> {
        return repository.resendPhoneOtp(session)
    }
}
