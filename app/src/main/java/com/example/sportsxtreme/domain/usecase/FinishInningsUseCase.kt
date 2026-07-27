package com.example.sportsxtreme.domain.usecase

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.MatchState
import com.example.sportsxtreme.domain.repository.MatchRepository

class FinishInningsUseCase(private val repository: MatchRepository) {
    suspend operator fun invoke(matchId: String, inningsId: String): Resource<MatchState> {
        return repository.finishInnings(matchId, inningsId)
    }
}
