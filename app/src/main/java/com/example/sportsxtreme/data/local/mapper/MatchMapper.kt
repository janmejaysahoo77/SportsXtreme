package com.example.sportsxtreme.data.local.mapper

import com.example.sportsxtreme.data.local.entity.MatchEntity
import com.example.sportsxtreme.data.local.entity.PlayingXIEntity
import com.example.sportsxtreme.data.local.entity.TeamEntity
import com.example.sportsxtreme.domain.model.Match
import com.example.sportsxtreme.domain.model.MatchStatus
import com.example.sportsxtreme.domain.model.MatchTeam
import com.example.sportsxtreme.domain.model.MatchType
import com.example.sportsxtreme.domain.model.PlayingXI
import com.example.sportsxtreme.domain.model.SportType
import com.example.sportsxtreme.domain.model.TeamSide
import com.example.sportsxtreme.domain.model.Toss
import com.example.sportsxtreme.domain.model.TossDecision
import com.example.sportsxtreme.domain.repository.CreateMatchRequest

fun CreateMatchRequest.toEntity(matchId: String): MatchEntity = MatchEntity(
    matchId = matchId,
    createdBy = organiserId,
    matchType = matchType.name,
    sport = sport.name,
    tournamentId = tournamentId,
    title = title,
    status = if (matchType == MatchType.FRIENDLY) {
        MatchStatus.TEAM_SELECTION.name
    } else {
        MatchStatus.CREATED.name
    },
    teamAId = teamA.teamId,
    teamBId = teamB.teamId,
    createdAtEpochMs = createdAtEpochMs,
    updatedAtEpochMs = createdAtEpochMs
)

fun MatchEntity.toDomain(
    teamA: TeamEntity,
    teamB: TeamEntity,
    teamAXI: List<PlayingXIEntity> = emptyList(),
    teamBXI: List<PlayingXIEntity> = emptyList(),
    innings: List<com.example.sportsxtreme.domain.model.Innings> = emptyList()
): Match = Match(
    id = matchId,
    matchType = MatchType.valueOf(matchType),
    sport = SportType.valueOf(sport),
    organiserId = createdBy,
    tournamentId = tournamentId,
    title = title,
    teamA = teamA.toMatchTeam(TeamSide.TEAM_A),
    teamB = teamB.toMatchTeam(TeamSide.TEAM_B),
    playingXI = buildMap {
        if (teamAXI.isNotEmpty()) put(TeamSide.TEAM_A, teamAXI.toDomain(teamAId, TeamSide.TEAM_A))
        if (teamBXI.isNotEmpty()) put(TeamSide.TEAM_B, teamBXI.toDomain(teamBId, TeamSide.TEAM_B))
    },
    toss = toToss(),
    innings = innings,
    status = MatchStatus.valueOf(status),
    createdAtEpochMs = createdAtEpochMs,
    updatedAtEpochMs = updatedAtEpochMs
)

fun TeamEntity.toMatchTeam(side: TeamSide): MatchTeam = MatchTeam(
    teamId = teamId,
    name = teamName,
    shortName = shortName,
    side = side
)

fun MatchEntity.toToss(): Toss? {
    val winner = tossWinner ?: return null
    val decision = tossDecision ?: return null
    val completedBy = tossCompletedBy ?: return null
    val completedAt = tossCompletedAtEpochMs ?: return null
    return Toss(winner, TossDecision.valueOf(decision), completedBy, completedAt)
}

fun List<PlayingXIEntity>.toDomain(teamId: String, side: TeamSide): PlayingXI {
    val firstSelection = first()
    return PlayingXI(
        teamId = teamId,
        side = side,
        playerIds = map { it.playerId },
        selectedByUserId = firstSelection.selectedByUserId,
        selectedAtEpochMs = firstSelection.selectedAtEpochMs
    )
}
