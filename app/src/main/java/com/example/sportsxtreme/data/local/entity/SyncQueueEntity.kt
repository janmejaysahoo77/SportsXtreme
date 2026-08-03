package com.example.sportsxtreme.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sync_queue",
    indices = [
        Index(value = ["state", "nextAttemptAtEpochMs"]),
        Index(value = ["entityType", "entityId"], unique = true),
        Index("matchId")
    ]
)
data class SyncQueueEntity(
    @PrimaryKey val operationId: String,
    val matchId: String,
    val entityType: String,
    val entityId: String,
    val operationType: String,
    val state: String = "PENDING",
    val attemptCount: Int = 0,
    val nextAttemptAtEpochMs: Long,
    val lastError: String? = null,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long
)
