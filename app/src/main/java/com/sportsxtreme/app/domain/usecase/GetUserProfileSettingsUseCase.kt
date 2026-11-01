package com.sportsxtreme.app.domain.usecase

import com.sportsxtreme.app.domain.repository.AuthRepository

class GetUserProfileSettingsUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(userId: String) = repository.getUserProfileSettings(userId)
}
