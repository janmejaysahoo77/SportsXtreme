package com.sportsxtreme.app.domain.usecase

import com.sportsxtreme.app.common.Resource
import com.sportsxtreme.app.domain.repository.AuthRepository

class CheckEmailVerificationUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): Resource<Boolean> {
        return repository.checkEmailVerification()
    }
}
