package com.sportsxtreme.app.domain.usecase

import com.sportsxtreme.app.domain.model.UserProfile
import com.sportsxtreme.app.domain.repository.AuthRepository

class UpdateUserProfileUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(profile: UserProfile) = repository.updateUserProfile(profile)
}
