package com.sportsxtreme.app.domain.usecase

import com.sportsxtreme.app.common.Resource
import com.sportsxtreme.app.domain.model.MatchState
import com.sportsxtreme.app.domain.repository.MatchRepository

class StartMatchUseCase(private val repository: MatchRepository) {
    suspend operator fun invoke(matchId: String): Resource<MatchState> {
        return repository.startMatch(matchId)
    }
}
