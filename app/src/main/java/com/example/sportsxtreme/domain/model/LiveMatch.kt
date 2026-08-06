package com.example.sportsxtreme.domain.model

/**
 * Lightweight live match summary used on the Home Screen.
 *
 * This model intentionally avoids ball-by-ball data. It is a denormalized
 * projection of the current match state, written to Firestore by the scorer
 * and read by all spectators via a SnapshotListener.
 */
data class LiveMatch(
    val matchId: String,
    val tournamentName: String,
    val teamAName: String,
    val teamBName: String,
    val teamAShortName: String,
    val teamBShortName: String,
    val status: MatchStatus,
    val score: Int,
    val wickets: Int,
    val overs: String,
    val currentRunRate: Double,
    val requiredRunRate: Double?,
    val target: Int?,
    val strikerName: String?,
    val strikerRuns: Int,
    val strikerBalls: Int,
    val nonStrikerName: String?,
    val bowlerName: String?,
    val bowlerOvers: String,
    val bowlerRuns: Int,
    val bowlerWickets: Int,
    val matchStatusNote: String?,
    val updatedAtEpochMs: Long
) {
    /** True when the match is currently being played. */
    val isLive: Boolean get() = status == MatchStatus.LIVE

    /** True when the match has finished. */
    val isCompleted: Boolean get() = status == MatchStatus.COMPLETED

    /** True when the match is scheduled but not yet started. */
    val isUpcoming: Boolean get() = status in setOf(
        MatchStatus.CREATED,
        MatchStatus.TEAM_SELECTION,
        MatchStatus.TOSS_PENDING,
        MatchStatus.TEAM_A_PLAYING_XI_SELECTED,
        MatchStatus.TEAM_B_PLAYING_XI_SELECTED,
        MatchStatus.DETAILS_CONFIRMED,
        MatchStatus.TOSS_COMPLETED,
        MatchStatus.OPENERS_SELECTED,
        MatchStatus.READY
    )
}

/**
 * Firestore payload shape for the lightweight `liveScore` document.
 *
 * This is the ONLY document read on the Home Screen to keep reads cheap.
 */
data class LiveScorePayload(
    val matchId: String,
    val tournamentName: String,
    val teamAName: String,
    val teamBName: String,
    val teamAShortName: String,
    val teamBShortName: String,
    val status: String,
    val score: Int,
    val wickets: Int,
    val overs: String,
    val currentRunRate: Double,
    val requiredRunRate: Double?,
    val target: Int?,
    val strikerName: String?,
    val strikerRuns: Int,
    val strikerBalls: Int,
    val nonStrikerName: String?,
    val bowlerName: String?,
    val bowlerOvers: String,
    val bowlerRuns: Int,
    val bowlerWickets: Int,
    val matchStatusNote: String?,
    val updatedAtEpochMs: Long
) {
    fun toDomain(): LiveMatch = LiveMatch(
        matchId = matchId,
        tournamentName = tournamentName,
        teamAName = teamAName,
        teamBName = teamBName,
        teamAShortName = teamAShortName,
        teamBShortName = teamBShortName,
        status = runCatching { MatchStatus.valueOf(status) }.getOrDefault(MatchStatus.CREATED),
        score = score,
        wickets = wickets,
        overs = overs,
        currentRunRate = currentRunRate,
        requiredRunRate = requiredRunRate,
        target = target,
        strikerName = strikerName,
        strikerRuns = strikerRuns,
        strikerBalls = strikerBalls,
        nonStrikerName = nonStrikerName,
        bowlerName = bowlerName,
        bowlerOvers = bowlerOvers,
        bowlerRuns = bowlerRuns,
        bowlerWickets = bowlerWickets,
        matchStatusNote = matchStatusNote,
        updatedAtEpochMs = updatedAtEpochMs
    )
}