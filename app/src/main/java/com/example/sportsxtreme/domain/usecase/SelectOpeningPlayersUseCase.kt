package com.example.sportsxtreme.domain.usecase

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.MatchState
import com.example.sportsxtreme.domain.repository.MatchRepository

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
