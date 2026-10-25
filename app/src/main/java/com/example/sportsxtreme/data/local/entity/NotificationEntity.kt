package com.example.sportsxtreme.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val category: String = "UPDATES",
    val iconType: String = "SPORTS",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val targetUrl: String? = null,
    val extraData: String? = null
)
