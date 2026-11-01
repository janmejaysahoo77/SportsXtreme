package com.sportsxtreme.app.domain.usecase

import com.sportsxtreme.app.domain.repository.AuthRepository

class GetUserAchievementsUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(userId: String) = repository.getUserAchievements(userId)
}
