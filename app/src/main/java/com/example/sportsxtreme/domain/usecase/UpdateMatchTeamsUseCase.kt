package com.example.sportsxtreme.domain.usecase

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.Match
import com.example.sportsxtreme.domain.repository.MatchRepository
import com.example.sportsxtreme.domain.repository.TeamRepository

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
