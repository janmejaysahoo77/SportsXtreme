package com.example.sportsxtreme.data.repository

import com.example.sportsxtreme.data.local.dao.NotificationDao
import com.example.sportsxtreme.data.local.entity.NotificationEntity
import com.example.sportsxtreme.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val notificationDao: NotificationDao
) : NotificationRepository {
    override fun getAllNotifications(): Flow<List<NotificationEntity>> =
        notificationDao.getAllNotifications()

    override suspend fun insertNotification(notification: NotificationEntity) =
        notificationDao.insertNotification(notification)

    override suspend fun markAsRead(id: String) =
        notificationDao.markAsRead(id)

    override suspend fun markAllAsRead() =
        notificationDao.markAllAsRead()

    override suspend fun deleteNotification(id: String) =
        notificationDao.deleteNotification(id)

    override suspend fun clearAllNotifications() =
        notificationDao.clearAllNotifications()
}
