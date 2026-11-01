package com.sportsxtreme.app.domain.usecase

import com.sportsxtreme.app.common.Resource
import com.sportsxtreme.app.domain.model.Match
import com.sportsxtreme.app.domain.repository.MatchRepository

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
