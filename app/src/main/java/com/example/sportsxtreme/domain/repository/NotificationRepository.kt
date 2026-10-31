package com.example.sportsxtreme.domain.repository

import com.example.sportsxtreme.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getAllNotifications(): Flow<List<NotificationEntity>>
    fun getUnreadCount(): Flow<Int>
    suspend fun insertNotification(notification: NotificationEntity)
    suspend fun markAsRead(id: String)
    suspend fun markAllAsRead()
    suspend fun deleteNotification(id: String)
    suspend fun clearAllNotifications()
}
