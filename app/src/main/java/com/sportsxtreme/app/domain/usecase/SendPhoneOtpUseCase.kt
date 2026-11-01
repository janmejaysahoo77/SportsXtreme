package com.sportsxtreme.app.domain.usecase

import com.sportsxtreme.app.common.Resource
import com.sportsxtreme.app.domain.model.PhoneAuthSession
import com.sportsxtreme.app.domain.repository.AuthRepository

class SendPhoneOtpUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(phoneNumber: String): Resource<PhoneAuthSession> {
        return repository.sendPhoneOtp(phoneNumber)
    }
}
