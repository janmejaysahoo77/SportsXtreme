package com.sportsxtreme.app.data.repository

import com.sportsxtreme.app.data.local.dao.NotificationDao
import com.sportsxtreme.app.data.local.entity.NotificationEntity
import com.sportsxtreme.app.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val notificationDao: NotificationDao
) : NotificationRepository {
    override fun getAllNotifications(): Flow<List<NotificationEntity>> =
        notificationDao.getAllNotifications()

    override fun getUnreadCount(): Flow<Int> =
        notificationDao.getUnreadCount()

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
