package com.example.sportsxtreme.domain.usecase

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.Match
import com.example.sportsxtreme.domain.model.MatchTeam
import com.example.sportsxtreme.domain.model.MatchType
import com.example.sportsxtreme.domain.model.SportType
import com.example.sportsxtreme.domain.model.TeamSide
import com.example.sportsxtreme.domain.repository.CreateMatchRequest
import com.example.sportsxtreme.domain.repository.MatchRepository

class CreateMatchUseCase(private val repository: MatchRepository) {
    suspend operator fun invoke(request: CreateMatchRequest): Resource<Match> {
        return repository.createMatch(request)
    }

    suspend operator fun invoke(matchType: MatchType): Resource<Match> {
        require(matchType == MatchType.FRIENDLY) { "Only friendly matches can be created from this flow" }

        val createdAt = System.currentTimeMillis()
        return repository.createMatch(
            CreateMatchRequest(
                matchType = matchType,
                sport = SportType.CRICKET,
                organiserId = LOCAL_ORGANISER_ID,
                title = "Friendly Match",
                teamA = MatchTeam(
                    teamId = FRIENDLY_TEAM_A_ID,
                    name = "Team A",
                    shortName = "A",
                    side = TeamSide.TEAM_A
                ),
                teamB = MatchTeam(
                    teamId = FRIENDLY_TEAM_B_ID,
                    name = "Team B",
                    shortName = "B",
                    side = TeamSide.TEAM_B
                ),
                createdAtEpochMs = createdAt
            )
        )
    }

    private companion object {
        const val LOCAL_ORGANISER_ID = "local-organiser"
        const val FRIENDLY_TEAM_A_ID = "friendly-team-a"
        const val FRIENDLY_TEAM_B_ID = "friendly-team-b"
    }
}
