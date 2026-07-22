package com.example.sportsxtreme.domain.usecase

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.AuthSession
import com.example.sportsxtreme.domain.repository.AuthRepository

class SignInWithGoogleUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(idToken: String): Resource<AuthSession> {
        return repository.signInWithGoogle(idToken)
    }
}
