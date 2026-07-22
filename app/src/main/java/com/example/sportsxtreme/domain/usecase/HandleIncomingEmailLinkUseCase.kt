package com.example.sportsxtreme.domain.usecase

import com.example.sportsxtreme.domain.repository.AuthRepository

class HandleIncomingEmailLinkUseCase(private val repository: AuthRepository) {
    operator fun invoke(link: String): Boolean {
        if (!repository.isEmailSignInLink(link)) return false
        repository.storePendingEmailSignInLink(link)
        return true
    }
}
