package com.example.sportsxtreme.domain.usecase

import com.example.sportsxtreme.domain.repository.AuthRepository

class GetPendingEmailLinkUseCase(private val repository: AuthRepository) {
    fun hasPendingLink(): Boolean {
        return repository.hasPendingEmailSignInLink()
    }

    fun pendingEmail(): String? {
        return repository.pendingEmailForLink()
    }
}
