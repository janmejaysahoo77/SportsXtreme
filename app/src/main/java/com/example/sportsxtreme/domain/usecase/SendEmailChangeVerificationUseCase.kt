package com.example.sportsxtreme.domain.usecase

import com.example.sportsxtreme.domain.repository.AuthRepository

class SendEmailChangeVerificationUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(newEmail: String) = repository.sendEmailChangeVerification(newEmail)
}
