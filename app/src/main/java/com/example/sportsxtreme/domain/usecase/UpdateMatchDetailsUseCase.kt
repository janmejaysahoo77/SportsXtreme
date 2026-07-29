package com.example.sportsxtreme.domain.usecase

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.Match
import com.example.sportsxtreme.domain.repository.MatchRepository

class UpdateMatchDetailsUseCase(private val repository: MatchRepository) {
    suspend operator fun invoke(
        matchId: String,
        venue: String,
        matchDateEpochMs: Long,
        matchTime: String
    ): Resource<Match> {
        return repository.updateMatchDetails(matchId, venue, matchDateEpochMs, matchTime)
    }
}
