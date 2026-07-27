package com.example.sportsxtreme.domain.usecase

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.MatchState
import com.example.sportsxtreme.domain.repository.ScoringRepository

class UndoBallUseCase(private val repository: ScoringRepository) {
    suspend operator fun invoke(
        matchId: String,
        requestedByUserId: String,
        reason: String? = null
    ): Resource<MatchState> {
        return repository.undoLastBall(matchId, requestedByUserId, reason)
    }
}
