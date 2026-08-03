package com.example.sportsxtreme.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "batting_scorecards",
    primaryKeys = ["matchId", "inningsId", "playerId"],
    indices = [Index(value = ["matchId", "inningsId"])]
)
data class BattingEntity(
    val matchId: String,
    val inningsId: String,
    val inningsNumber: Int,
    val playerId: String,
    val runs: Int = 0,
    val balls: Int = 0,
    val fours: Int = 0,
    val sixes: Int = 0,
    val status: String = "NOT_BATTED",
    val dismissalType: String? = null,
    val dismissedByBowlerId: String? = null,
    val dismissedByFielderId: String? = null,
    val updatedAtEpochMs: Long
)
