package com.example.sportsxtreme.domain.usecase

import com.example.sportsxtreme.domain.repository.AuthRepository

class GetUserProfileStatsUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(userId: String) = repository.getUserProfileStats(userId)
}
