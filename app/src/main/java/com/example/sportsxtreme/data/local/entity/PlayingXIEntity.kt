package com.example.sportsxtreme.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "playing_xi",
    primaryKeys = ["matchId", "teamId", "playerId"],
    indices = [Index("matchId"), Index("teamId"), Index("playerId")]
)
data class PlayingXIEntity(
    val matchId: String,
    val teamId: String,
    val side: String,
    val playerId: String,
    val battingOrder: Int,
    val selectedByUserId: String,
    val selectedAtEpochMs: Long
)
