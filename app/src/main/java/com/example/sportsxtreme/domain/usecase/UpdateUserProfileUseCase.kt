package com.example.sportsxtreme.domain.usecase

import com.example.sportsxtreme.domain.model.UserProfile
import com.example.sportsxtreme.domain.repository.AuthRepository

class UpdateUserProfileUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(profile: UserProfile) = repository.updateUserProfile(profile)
}
