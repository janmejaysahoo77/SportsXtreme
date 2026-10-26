package com.example.sportsxtreme.data.remote.messaging

import android.util.Log
import com.example.sportsxtreme.common.PushNotificationHelper
import com.example.sportsxtreme.data.local.dao.NotificationDao
import com.example.sportsxtreme.data.local.entity.NotificationEntity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SportsXtremeMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationDao: NotificationDao

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM Registration Token generated")
        PushNotificationHelper.subscribeToAllUsersTopic()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "FCM Message received from: ${remoteMessage.from}")

        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "SportsXtreme Update"

        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: remoteMessage.data["message"]
            ?: "You have a new update."

        val category = remoteMessage.data["category"] ?: "UPDATES"
        val iconType = remoteMessage.data["iconType"] ?: "SPORTS"
        val targetUrl = remoteMessage.data["targetUrl"]
        val extraData = remoteMessage.data["extraData"]

        val entity = NotificationEntity(
            title = title,
            message = body,
            category = category,
            iconType = iconType,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            targetUrl = targetUrl,
            extraData = extraData
        )

        // 1. Save dynamically into Room DB
        serviceScope.launch {
            try {
                notificationDao.insertNotification(entity)
                Log.d(TAG, "FCM notification saved to Room DB: $title")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save FCM notification to Room DB: ${e.message}", e)
            }
        }

        // 2. Display System Notification Alert
        PushNotificationHelper.showNotification(
            context = applicationContext,
            title = title,
            message = body
        )
    }

    companion object {
        private const val TAG = "SportsXtremeFCMService"
    }
}
