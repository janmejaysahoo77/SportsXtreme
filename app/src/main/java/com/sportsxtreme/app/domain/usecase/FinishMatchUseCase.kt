package com.sportsxtreme.app.domain.usecase

import com.sportsxtreme.app.common.Resource
import com.sportsxtreme.app.domain.model.Match
import com.sportsxtreme.app.domain.repository.MatchRepository

class FinishMatchUseCase(private val repository: MatchRepository) {
    suspend operator fun invoke(matchId: String): Resource<Match> {
        return repository.finishMatch(matchId)
    }
}
