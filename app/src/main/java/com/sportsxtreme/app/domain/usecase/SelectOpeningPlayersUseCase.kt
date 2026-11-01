package com.sportsxtreme.app.domain.usecase

import com.sportsxtreme.app.common.Resource
import com.sportsxtreme.app.domain.model.MatchState
import com.sportsxtreme.app.domain.repository.MatchRepository

class SelectOpeningPlayersUseCase(private val repository: MatchRepository) {
    suspend operator fun invoke(
        matchId: String,
        strikerId: String,
        nonStrikerId: String,
        bowlerId: String
    ): Resource<MatchState> {
        return repository.selectOpeningPlayers(matchId, strikerId, nonStrikerId, bowlerId)
    }
}
