package com.example.sportsxtreme.domain.usecase

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.AuthSession
import com.example.sportsxtreme.domain.repository.AuthRepository

class CompleteEmailLinkLoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, link: String? = null): Resource<AuthSession> {
        return repository.completeEmailLinkLogin(email, link)
    }
}
