package com.example.sportsxtreme.domain.usecase

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.repository.AuthRepository

class CheckEmailVerificationUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): Resource<Boolean> {
        return repository.checkEmailVerification()
    }
}
