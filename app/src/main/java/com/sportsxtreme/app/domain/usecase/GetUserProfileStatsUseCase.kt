package com.sportsxtreme.app.domain.usecase

import com.sportsxtreme.app.domain.repository.AuthRepository

class GetUserProfileStatsUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(userId: String) = repository.getUserProfileStats(userId)
}
