package com.sportsxtreme.app.domain.usecase

import com.sportsxtreme.app.common.Resource
import com.sportsxtreme.app.domain.model.MatchState
import com.sportsxtreme.app.domain.repository.ScoringRepository
import javax.inject.Inject

class UndoBallUseCase @Inject constructor(private val repository: ScoringRepository) {
    suspend operator fun invoke(
        matchId: String,
        requestedByUserId: String,
        reason: String? = null
    ): Resource<MatchState> {
        return repository.undoLastBall(matchId, requestedByUserId, reason)
    }
}
