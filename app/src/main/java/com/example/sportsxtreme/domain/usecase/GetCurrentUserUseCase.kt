package com.example.sportsxtreme.domain.usecase

import com.example.sportsxtreme.domain.model.User
import com.example.sportsxtreme.domain.repository.AuthRepository

class GetCurrentUserUseCase(private val repository: AuthRepository) {
    operator fun invoke(): User? {
        return repository.currentUser()
    }
}
