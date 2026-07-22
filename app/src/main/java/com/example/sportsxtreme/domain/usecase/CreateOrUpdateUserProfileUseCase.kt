package com.example.sportsxtreme.domain.usecase

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.PendingUserProfile
import com.example.sportsxtreme.domain.model.User
import com.example.sportsxtreme.domain.repository.AuthRepository

class CreateOrUpdateUserProfileUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(profile: PendingUserProfile): Resource<User> {
        return repository.createOrUpdateUserProfile(profile)
    }
}
