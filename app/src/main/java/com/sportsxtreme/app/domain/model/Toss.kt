package com.sportsxtreme.app.domain.model

data class Toss(
    val winnerTeamId: String,
    val decision: TossDecision,
    val completedByUserId: String,
    val completedAtEpochMs: Long
)

enum class TossDecision {
    BAT,
    FIELD
}
