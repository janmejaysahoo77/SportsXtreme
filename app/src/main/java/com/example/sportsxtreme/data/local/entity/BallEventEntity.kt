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
    val wides: Int = 0,
    val noBalls: Int = 0,
    val byes: Int = 0,
    val legByes: Int = 0,
    val penaltyRuns: Int = 0,
    val wicketPlayerId: String? = null,
    val wicketType: String? = null,
    val wicketAssistPlayerIds: String = "",
    val isLegalDelivery: Boolean,
    val recordedByUserId: String,
    val timestamp: Long,
    val previousEventId: String? = null,
    val metadataJson: String = "{}",
    val synced: Boolean = false
)
