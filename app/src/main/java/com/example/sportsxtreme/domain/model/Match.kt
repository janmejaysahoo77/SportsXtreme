package com.example.sportsxtreme.domain.model

data class Match(
    val id: String,
    val matchType: MatchType,
    val sport: SportType,
    val organiserId: String,
    val tournamentId: String? = null,
    val title: String,
    val teamA: MatchTeam,
    val teamB: MatchTeam,
    val playingXI: Map<TeamSide, PlayingXI> = emptyMap(),
    val toss: Toss? = null,
    val innings: List<Innings> = emptyList(),
    val status: MatchStatus = MatchStatus.CREATED,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long
)

data class MatchTeam(
    val teamId: String,
    val name: String,
    val shortName: String,
    val side: TeamSide
)

enum class MatchType {
    FRIENDLY,
    TOURNAMENT
}

enum class SportType {
    CRICKET
}

enum class TeamSide {
    TEAM_A,
    TEAM_B
}

enum class MatchStatus {
    CREATED,
    TEAM_SELECTION,
    TOSS_PENDING,
    TEAM_A_PLAYING_XI_SELECTED,
    TEAM_B_PLAYING_XI_SELECTED,
    DETAILS_CONFIRMED,
    TOSS_COMPLETED,
    OPENERS_SELECTED,
    READY,
    IN_PROGRESS,
    LIVE,
    INNINGS_BREAK,
    COMPLETED,
    ABANDONED
}
