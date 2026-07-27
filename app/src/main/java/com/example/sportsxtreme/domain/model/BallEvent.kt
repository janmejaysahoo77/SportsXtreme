package com.example.sportsxtreme.domain.model

data class BallEvent(
    val ballId: String,
    val matchId: String,
    val inningsId: String,
    val inningsNumber: Int,
    val sequenceNumber: Long,
    val overNumber: Int,
    val ballNumber: Int,
    val legalBallNumber: Int,
    val battingTeamId: String,
    val bowlingTeamId: String,
    val strikerId: String,
    val nonStrikerId: String,
    val bowlerId: String,
    val runsOffBat: Int,
    val extras: List<ExtraRun> = emptyList(),
    val dismissal: Dismissal? = null,
    val isLegalDelivery: Boolean,
    val eventType: BallEventType = BallEventType.DELIVERY,
    val reversedEventId: String? = null,
    val comment: String? = null,
    val recordedByUserId: String,
    val timestampEpochMs: Long,
    val syncState: SyncState = SyncState.PENDING,
    val previousEventId: String? = null,
    val metadata: Map<String, String> = emptyMap()
) {
    val extraRuns: Int
        get() = extras.sumOf { it.runs }

    val totalRuns: Int
        get() = runsOffBat + extraRuns
}

data class ExtraRun(
    val type: ExtraType,
    val runs: Int
)

data class Dismissal(
    val type: DismissalType,
    val dismissedPlayerId: String,
    val assistedByPlayerIds: List<String> = emptyList()
)

enum class ExtraType {
    NONE,
    WIDE,
    NO_BALL,
    BYE,
    LEG_BYE,
    PENALTY
}

enum class DismissalType {
    NONE,
    BOWLED,
    CAUGHT,
    LBW,
    RUN_OUT,
    STUMPED,
    HIT_WICKET,
    TIMED_OUT,
    OBSTRUCTING_THE_FIELD,
    RETIRED_OUT,
    OTHER
}

enum class BallEventType {
    DELIVERY,
    REVERSAL
}

enum class SyncState {
    PENDING,
    SYNCED,
    FAILED
}
