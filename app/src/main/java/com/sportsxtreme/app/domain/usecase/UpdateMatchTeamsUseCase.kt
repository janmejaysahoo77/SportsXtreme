package com.sportsxtreme.app.domain.usecase

import com.sportsxtreme.app.common.Resource
import com.sportsxtreme.app.domain.model.Match
import com.sportsxtreme.app.domain.repository.MatchRepository
import com.sportsxtreme.app.domain.repository.TeamRepository

class UpdateMatchTeamsUseCase(
    private val repository: MatchRepository,
    private val teamRepository: TeamRepository
) {
    suspend operator fun invoke(
        matchId: String,
        teamAId: String,
        teamBId: String
    ): Resource<Match> {
        teamRepository.ensureDummyTeamsExist()
        return repository.updateMatchTeams(matchId, teamAId, teamBId)
    }
}
