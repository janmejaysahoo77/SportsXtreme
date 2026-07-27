package com.example.sportsxtreme.domain.model

data class BallEvent(
    val id: String,
    val matchId: String,
    val inningsId: String,
    val sequenceNumber: Long,
    val overNumber: Int,
    val ballNumberInOver: Int,
    val legalBallNumber: Int,
    val battingTeamId: String,
    val bowlingTeamId: String,
    val strikerId: String,
    val nonStrikerId: String,
    val bowlerId: String,
    val runsOffBat: Int,
    val extras: Extras = Extras(),
    val wicket: Wicket? = null,
    val isLegalDelivery: Boolean,
    val recordedByUserId: String,
    val recordedAtEpochMs: Long,
    val previousEventId: String? = null,
    val metadata: Map<String, String> = emptyMap()
) {
    val totalRuns: Int
        get() = runsOffBat + extras.total
}

data class Extras(
    val wides: Int = 0,
    val noBalls: Int = 0,
    val byes: Int = 0,
    val legByes: Int = 0,
    val penalty: Int = 0
) {
    val total: Int
        get() = wides + noBalls + byes + legByes + penalty
}

data class Wicket(
    val dismissedPlayerId: String,
    val type: WicketType,
    val assistedByPlayerIds: List<String> = emptyList()
)

enum class WicketType {
    BOWLED,
    CAUGHT,
    RUN_OUT,
    LBW,
    STUMPED,
    HIT_WICKET,
    RETIRED,
    OTHER
}
