package com.sportsxtreme.app.domain.usecase

import com.sportsxtreme.app.common.Resource
import com.sportsxtreme.app.domain.model.AuthSession
import com.sportsxtreme.app.domain.repository.AuthRepository

class SignInWithGoogleUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(idToken: String, existingAccountOnly: Boolean = false): Resource<AuthSession> {
        return repository.signInWithGoogle(idToken, existingAccountOnly)
    }
}
