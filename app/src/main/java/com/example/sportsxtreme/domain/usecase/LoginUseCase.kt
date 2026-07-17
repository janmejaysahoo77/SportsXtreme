package com.example.sportsxtreme.domain.usecase

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.User
import com.example.sportsxtreme.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(mobileNumber: String): Resource<User> {
        return repository.login(mobileNumber)
    }
}
