package com.example.sportsxtreme.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teams")
data class TeamEntity(
    @PrimaryKey val teamId: String,
    val teamName: String,
    val shortName: String,
    val logo: String? = null,
    val isTeamA: Boolean = false,
    val type: String,
    val ownerUserId: String? = null,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long
)
