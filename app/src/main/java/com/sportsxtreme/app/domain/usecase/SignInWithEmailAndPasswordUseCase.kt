package com.sportsxtreme.app.domain.usecase

import com.sportsxtreme.app.common.Resource
import com.sportsxtreme.app.domain.model.AuthSession
import com.sportsxtreme.app.domain.repository.AuthRepository

class SignInWithEmailAndPasswordUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Resource<AuthSession> {
        return repository.signInWithEmailAndPassword(email, password)
    }
}
