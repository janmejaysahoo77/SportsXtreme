package com.sportsxtreme.app.data.local.mapper

import com.sportsxtreme.app.data.local.entity.MatchEntity
import com.sportsxtreme.app.data.local.entity.PlayingXIEntity
import com.sportsxtreme.app.data.local.entity.TeamEntity
import com.sportsxtreme.app.domain.model.Match
import com.sportsxtreme.app.domain.model.BallType
import com.sportsxtreme.app.domain.model.MatchFormat
import com.sportsxtreme.app.domain.model.MatchStatus
import com.sportsxtreme.app.domain.model.MatchTeam
import com.sportsxtreme.app.domain.model.MatchType
import com.sportsxtreme.app.domain.model.PlayingXI
import com.sportsxtreme.app.domain.model.SportType
import com.sportsxtreme.app.domain.model.TeamSide
import com.sportsxtreme.app.domain.model.Toss
import com.sportsxtreme.app.domain.model.TossDecision
import com.sportsxtreme.app.domain.repository.CreateMatchRequest

fun CreateMatchRequest.toEntity(matchId: String): MatchEntity = MatchEntity(
    matchId = matchId,
    createdBy = organiserId,
    matchType = matchType.name,
    sport = sport.name,
    tournamentId = tournamentId,
    title = title,
    format = null,
    ballType = null,
    status = if (matchType == MatchType.FRIENDLY) {
        MatchStatus.TEAM_SELECTION.name
    } else {
        MatchStatus.CREATED.name
    },
    teamAId = teamA.teamId,
    teamBId = teamB.teamId,
    overs = null,
    venue = null,
    matchDateEpochMs = null,
    matchTime = null,
    createdAtEpochMs = createdAtEpochMs,
    updatedAtEpochMs = createdAtEpochMs
)

fun MatchEntity.toDomain(
    teamA: TeamEntity,
    teamB: TeamEntity,
    teamAXI: List<PlayingXIEntity> = emptyList(),
    teamBXI: List<PlayingXIEntity> = emptyList(),
    innings: List<com.sportsxtreme.app.domain.model.Innings> = emptyList()
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
    updatedAtEpochMs = updatedAtEpochMs,
    format = format?.let(MatchFormat::valueOf) ?: MatchFormat.T20,
    ballType = ballType?.let(BallType::valueOf) ?: BallType.LEATHER,
    overs = overs ?: 20,
    maxOversPerBowler = maxOversPerBowler,
    venue = venue,
    matchDateEpochMs = matchDateEpochMs,
    matchTime = matchTime
)

fun TeamEntity.toMatchTeam(side: TeamSide): MatchTeam = MatchTeam(
    teamId = teamId,
    name = teamName,
    shortName = shortName,
    side = side,
    logoUrl = logo
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
