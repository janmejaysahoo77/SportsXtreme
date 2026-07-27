package com.example.sportsxtreme.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "ball_events", indices = [Index("matchId"), Index("inningsId")])
data class BallEventEntity(
    @PrimaryKey val ballId: String,
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
    val runs: Int,
    val extraType: String = "NONE",
    val extraRuns: Int = 0,
    val additionalExtrasJson: String = "[]",
    val dismissalType: String = "NONE",
    val dismissedPlayerId: String? = null,
    val dismissalAssistPlayerIds: String = "",
    val isLegalDelivery: Boolean,
    val eventType: String = "DELIVERY",
    val reversedEventId: String? = null,
    val comment: String? = null,
    val recordedByUserId: String,
    val timestamp: Long,
    val previousEventId: String? = null,
    val metadataJson: String = "{}",
    val syncState: String = "PENDING"
)
