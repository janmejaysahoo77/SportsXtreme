package com.example.sportsxtreme.domain.usecase

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.BallType
import com.example.sportsxtreme.domain.model.Match
import com.example.sportsxtreme.domain.model.MatchFormat
import com.example.sportsxtreme.domain.repository.MatchRepository

class UpdateMatchSettingsUseCase(private val repository: MatchRepository) {
    suspend operator fun invoke(
        matchId: String,
        format: MatchFormat,
        ballType: BallType,
        overs: Int
    ): Resource<Match> {
        return repository.updateMatchSettings(matchId, format, ballType, overs)
    }
}
