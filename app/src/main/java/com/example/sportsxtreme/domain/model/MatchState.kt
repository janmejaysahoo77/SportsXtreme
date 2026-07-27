package com.example.sportsxtreme.domain.model

data class MatchState(
    val matchId: String,
    val inningsId: String?,
    val matchStatus: MatchStatus,
    val battingTeam: MatchTeam?,
    val bowlingTeam: MatchTeam?,
    val striker: Player?,
    val nonStriker: Player?,
    val bowler: Player?,
    val score: Int,
    val wickets: Int,
    val legalBalls: Int,
    val overs: Overs,
    val currentOverEvents: List<BallEvent>,
    val target: Int? = null,
    val lastEventId: String? = null,
    val updatedAtEpochMs: Long
)

data class Overs(
    val completedOvers: Int,
    val ballsInCurrentOver: Int
) {
    val display: String
        get() = "$completedOvers.$ballsInCurrentOver"
}
