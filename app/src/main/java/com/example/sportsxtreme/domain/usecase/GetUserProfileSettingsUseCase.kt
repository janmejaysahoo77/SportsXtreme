package com.example.sportsxtreme.domain.usecase

import com.example.sportsxtreme.domain.repository.AuthRepository

class GetUserProfileSettingsUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(userId: String) = repository.getUserProfileSettings(userId)
}
