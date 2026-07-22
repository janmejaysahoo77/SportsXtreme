package com.example.sportsxtreme.domain.usecase

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.repository.AuthRepository

class SendLoginEmailLinkUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String): Resource<Unit> {
        return repository.sendLoginEmailLink(email)
    }
}
