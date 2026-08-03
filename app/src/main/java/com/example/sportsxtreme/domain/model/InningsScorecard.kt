package com.example.sportsxtreme.domain.model

data class InningsScorecard(
    val batting: List<BattingScorecard>,
    val bowling: List<BowlingScorecard>,
    val summary: ScorecardSummary
)

data class BattingScorecard(
    val playerId: String,
    val runs: Int,
    val balls: Int,
    val fours: Int,
    val sixes: Int,
    val status: String,
    val dismissalType: DismissalType? = null,
    val dismissedByBowlerId: String? = null,
    val dismissedByFielderId: String? = null
) {
    val strikeRate: Double
        get() = if (balls == 0) 0.0 else runs * 100.0 / balls
}

data class BowlingScorecard(
    val playerId: String,
    val legalBalls: Int,
    val maidens: Int,
    val runsConceded: Int,
    val wickets: Int,
    val wides: Int,
    val noBalls: Int
) {
    val overs: Overs
        get() = Overs(legalBalls / 6, legalBalls % 6)

    val economy: Double
        get() = if (legalBalls == 0) 0.0 else runsConceded * 6.0 / legalBalls
}

data class ScorecardSummary(
    val totalScore: Int,
    val wickets: Int,
    val legalBalls: Int,
    val target: Int?,
    val strikerId: String?,
    val nonStrikerId: String?,
    val bowlerId: String?,
    val updatedAtEpochMs: Long,
    val scheduledOvers: Int?
) {
    val overs: Overs
        get() = Overs(legalBalls / 6, legalBalls % 6)

    val currentRunRate: Double
        get() = if (legalBalls == 0) 0.0 else totalScore * 6.0 / legalBalls

    val requiredRunRate: Double?
        get() {
            val target = target ?: return null
            val remainingBalls = (scheduledOvers ?: return null) * 6 - legalBalls
            return if (remainingBalls <= 0) null else (target - totalScore).coerceAtLeast(0) * 6.0 / remainingBalls
        }
}
