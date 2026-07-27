package com.example.sportsxtreme.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "players",
    foreignKeys = [ForeignKey(
        entity = TeamEntity::class,
        parentColumns = ["teamId"],
        childColumns = ["teamId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("teamId")]
)
data class PlayerEntity(
    @PrimaryKey val playerId: String,
    val teamId: String,
    val playerName: String,
    val battingOrder: Int? = null,
    val playingXI: Boolean = false,
    val hasBatted: Boolean = false,
    val hasBowled: Boolean = false,
    val isOut: Boolean = false,
    val jerseyNumber: Int? = null,
    val linkedUserId: String? = null,
    val role: String,
    val isGuestPlayer: Boolean = true
)
