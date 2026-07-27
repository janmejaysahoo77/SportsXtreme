package com.example.sportsxtreme.domain.model

data class Innings(
    val id: String,
    val matchId: String,
    val number: Int,
    val battingTeamId: String,
    val bowlingTeamId: String,
    val strikerId: String? = null,
    val nonStrikerId: String? = null,
    val currentBowlerId: String? = null,
    val score: Int = 0,
    val wickets: Int = 0,
    val legalBalls: Int = 0,
    val target: Int? = null,
    val status: InningsStatus = InningsStatus.NOT_STARTED,
    val startedAtEpochMs: Long? = null,
    val completedAtEpochMs: Long? = null
)

enum class InningsStatus {
    NOT_STARTED,
    LIVE,
    COMPLETED
}
