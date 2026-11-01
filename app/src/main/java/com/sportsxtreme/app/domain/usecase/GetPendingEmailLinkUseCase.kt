package com.sportsxtreme.app.domain.usecase

import com.sportsxtreme.app.domain.repository.AuthRepository

class GetPendingEmailLinkUseCase(private val repository: AuthRepository) {
    fun hasPendingLink(): Boolean {
        return repository.hasPendingEmailSignInLink()
    }

    fun pendingEmail(): String? {
        return repository.pendingEmailForLink()
    }
}
