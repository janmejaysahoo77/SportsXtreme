package com.example.sportsxtreme.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey val matchId: String,
    val createdBy: String,
    val matchType: String,
    val sport: String,
    val tournamentId: String?,
    val title: String,
    val format: String? = null,
    val ballType: String? = null,
    val status: String,
    val teamAId: String,
    val teamBId: String,
    val battingTeamId: String? = null,
    val bowlingTeamId: String? = null,
    val tossWinner: String? = null,
    val tossDecision: String? = null,
    val tossCompletedBy: String? = null,
    val tossCompletedAtEpochMs: Long? = null,
    val strikerId: String? = null,
    val nonStrikerId: String? = null,
    val currentBowlerId: String? = null,
    val overs: Int? = null,
    val venue: String? = null,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long
)
