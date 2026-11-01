package com.sportsxtreme.app.domain.usecase

import com.sportsxtreme.app.common.Resource
import com.sportsxtreme.app.domain.model.PhoneAuthSession
import com.sportsxtreme.app.domain.repository.AuthRepository

class ResendPhoneOtpUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(session: PhoneAuthSession): Resource<PhoneAuthSession> {
        return repository.resendPhoneOtp(session)
    }
}
