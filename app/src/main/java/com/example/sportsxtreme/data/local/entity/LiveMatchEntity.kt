package com.example.sportsxtreme.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room cache for the lightweight live match summary shown on the Home Screen.
 *
 * This is the offline fallback when the device has no internet connection.
 * It mirrors the Firestore `liveScore` document so the Home Screen can render
 * the last synced score instantly and then update in real time when the
 * SnapshotListener reconnects.
 */
@Entity(tableName = "live_matches")
data class LiveMatchEntity(
    @PrimaryKey val matchId: String,
    val tournamentName: String,
    val teamAName: String,
    val teamBName: String,
    val teamAShortName: String,
    val teamBShortName: String,
    val status: String,
    val score: Int,
    val wickets: Int,
    val overs: String,
    val currentRunRate: Double,
    val requiredRunRate: Double?,
    val target: Int?,
    val strikerName: String?,
    val strikerRuns: Int,
    val strikerBalls: Int,
    val nonStrikerName: String?,
    val bowlerName: String?,
    val bowlerOvers: String,
    val bowlerRuns: Int,
    val bowlerWickets: Int,
    val matchStatusNote: String?,
    val updatedAtEpochMs: Long
)