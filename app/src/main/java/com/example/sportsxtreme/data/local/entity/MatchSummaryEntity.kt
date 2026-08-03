package com.example.sportsxtreme.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "match_summaries",
    primaryKeys = ["matchId", "inningsId"],
    indices = [Index("matchId")]
)
data class MatchSummaryEntity(
    val matchId: String,
    val inningsId: String,
    val inningsNumber: Int,
    val totalScore: Int = 0,
    val wickets: Int = 0,
    val legalBalls: Int = 0,
    val target: Int? = null,
    val strikerId: String? = null,
    val nonStrikerId: String? = null,
    val bowlerId: String? = null,
    val updatedAtEpochMs: Long
)
