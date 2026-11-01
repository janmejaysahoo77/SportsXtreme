package com.sportsxtreme.app.domain.usecase

import com.sportsxtreme.app.common.Resource
import com.sportsxtreme.app.domain.model.AuthSession
import com.sportsxtreme.app.domain.repository.AuthRepository

class CompleteEmailLinkLoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, link: String? = null): Resource<AuthSession> {
        return repository.completeEmailLinkLogin(email, link)
    }
}
