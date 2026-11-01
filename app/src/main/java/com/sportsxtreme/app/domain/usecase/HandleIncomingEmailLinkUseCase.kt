package com.sportsxtreme.app.domain.usecase

import com.sportsxtreme.app.domain.repository.AuthRepository

class HandleIncomingEmailLinkUseCase(private val repository: AuthRepository) {
    operator fun invoke(link: String): Boolean {
        if (!repository.isEmailSignInLink(link)) return false
        repository.storePendingEmailSignInLink(link)
        return true
    }
}
