package com.sportsxtreme.app.domain.usecase

import com.sportsxtreme.app.common.Resource
import com.sportsxtreme.app.domain.model.BallType
import com.sportsxtreme.app.domain.model.Match
import com.sportsxtreme.app.domain.model.MatchFormat
import com.sportsxtreme.app.domain.repository.MatchRepository

class UpdateMatchSettingsUseCase(private val repository: MatchRepository) {
    suspend operator fun invoke(
        matchId: String,
        format: MatchFormat,
        ballType: BallType,
        overs: Int,
        maxOversPerBowler: Int? = null
    ): Resource<Match> {
        return repository.updateMatchSettings(matchId, format, ballType, overs, maxOversPerBowler)
    }
}
