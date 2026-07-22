package com.example.sportsxtreme.domain.usecase

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.AuthSession
import com.example.sportsxtreme.domain.repository.AuthRepository

class SignInWithEmailAndPasswordUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Resource<AuthSession> {
        return repository.signInWithEmailAndPassword(email, password)
    }
}
