package com.sportsxtreme.app.domain.usecase

import com.sportsxtreme.app.common.Resource
import com.sportsxtreme.app.domain.model.Match
import com.sportsxtreme.app.domain.model.MatchTeam
import com.sportsxtreme.app.domain.model.MatchType
import com.sportsxtreme.app.domain.model.SportType
import com.sportsxtreme.app.domain.model.TeamSide
import com.sportsxtreme.app.domain.repository.CreateMatchRequest
import com.sportsxtreme.app.domain.repository.MatchRepository

class CreateMatchUseCase(private val repository: MatchRepository) {
    suspend operator fun invoke(request: CreateMatchRequest): Resource<Match> {
        return repository.createMatch(request)
    }

    suspend operator fun invoke(matchType: MatchType): Resource<Match> {
        require(matchType == MatchType.FRIENDLY) { "Only friendly matches can be created from this flow" }

        val createdAt = System.currentTimeMillis()
        val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            .orEmpty()
            .ifBlank { LOCAL_ORGANISER_ID }
        return repository.createMatch(
            CreateMatchRequest(
                matchType = matchType,
                sport = SportType.CRICKET,
                organiserId = currentUserId,
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
