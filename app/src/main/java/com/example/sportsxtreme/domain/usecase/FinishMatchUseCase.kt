package com.example.sportsxtreme.domain.usecase

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.Match
import com.example.sportsxtreme.domain.repository.MatchRepository

class FinishMatchUseCase(private val repository: MatchRepository) {
    suspend operator fun invoke(matchId: String): Resource<Match> {
        return repository.finishMatch(matchId)
    }
}
