package com.example.sportsxtreme.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "bowling_scorecards",
    primaryKeys = ["matchId", "inningsId", "playerId"],
    indices = [Index(value = ["matchId", "inningsId"])]
)
data class BowlingEntity(
    val matchId: String,
    val inningsId: String,
    val inningsNumber: Int,
    val playerId: String,
    val legalBalls: Int = 0,
    val maidens: Int = 0,
    val runsConceded: Int = 0,
    val wickets: Int = 0,
    val wides: Int = 0,
    val noBalls: Int = 0,
    val updatedAtEpochMs: Long
)
