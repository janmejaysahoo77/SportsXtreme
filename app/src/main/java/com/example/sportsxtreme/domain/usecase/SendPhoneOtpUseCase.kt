package com.example.sportsxtreme.domain.usecase

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.PhoneAuthSession
import com.example.sportsxtreme.domain.repository.AuthRepository

class SendPhoneOtpUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(phoneNumber: String): Resource<PhoneAuthSession> {
        return repository.sendPhoneOtp(phoneNumber)
    }
}
