package com.example.sportsxtreme.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "innings", indices = [Index("matchId")])
data class InningsEntity(
    @PrimaryKey val inningsId: String,
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
    val status: String,
    val startedAtEpochMs: Long? = null,
    val completedAtEpochMs: Long? = null
)
