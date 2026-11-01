package com.sportsxtreme.app.domain.usecase

import com.sportsxtreme.app.common.Resource
import com.sportsxtreme.app.domain.repository.AuthRepository

class SendLoginEmailLinkUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String): Resource<Unit> {
        return repository.sendLoginEmailLink(email)
    }
}
