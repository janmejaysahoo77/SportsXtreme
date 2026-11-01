package com.sportsxtreme.app.domain.usecase

import com.sportsxtreme.app.domain.repository.AuthRepository

class SendEmailChangeVerificationUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(newEmail: String) = repository.sendEmailChangeVerification(newEmail)
}
