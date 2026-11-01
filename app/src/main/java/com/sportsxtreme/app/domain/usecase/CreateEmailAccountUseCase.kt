package com.sportsxtreme.app.domain.usecase

import com.sportsxtreme.app.common.Resource
import com.sportsxtreme.app.domain.model.AuthSession
import com.sportsxtreme.app.domain.model.EmailSignupRequest
import com.sportsxtreme.app.domain.repository.AuthRepository

class CreateEmailAccountUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(request: EmailSignupRequest): Resource<AuthSession> {
        return repository.createEmailAccount(request)
    }
}
