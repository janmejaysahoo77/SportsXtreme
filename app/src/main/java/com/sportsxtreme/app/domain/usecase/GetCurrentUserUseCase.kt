package com.sportsxtreme.app.domain.usecase

import com.sportsxtreme.app.domain.model.User
import com.sportsxtreme.app.domain.repository.AuthRepository

class GetCurrentUserUseCase(private val repository: AuthRepository) {
    operator fun invoke(): User? {
        return repository.currentUser()
    }
}
