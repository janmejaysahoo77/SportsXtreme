package com.example.sportsxtreme.domain.usecase

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.AuthSession
import com.example.sportsxtreme.domain.model.EmailSignupRequest
import com.example.sportsxtreme.domain.repository.AuthRepository

class CreateEmailAccountUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(request: EmailSignupRequest): Resource<AuthSession> {
        return repository.createEmailAccount(request)
    }
}
