package com.sportsxtreme.app.domain.usecase

import com.sportsxtreme.app.common.Resource
import com.sportsxtreme.app.domain.model.PendingUserProfile
import com.sportsxtreme.app.domain.model.User
import com.sportsxtreme.app.domain.repository.AuthRepository

class CreateOrUpdateUserProfileUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(profile: PendingUserProfile): Resource<User> {
        return repository.createOrUpdateUserProfile(profile)
    }
}
