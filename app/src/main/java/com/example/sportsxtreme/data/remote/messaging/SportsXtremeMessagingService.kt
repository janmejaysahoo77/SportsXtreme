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
        Log.d(TAG, "New FCM Registration Token generated: $token")
        com.example.sportsxtreme.common.FirebaseInAppMessagingHelper.syncInAppMessagingState()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "FCM Message received from: ${remoteMessage.from}")
        com.example.sportsxtreme.common.FirebaseInAppMessagingHelper.syncInAppMessagingState()

        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "SportsXtreme Update"

        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: remoteMessage.data["message"]
            ?: "You have a new update."

        val category = remoteMessage.data["category"] ?: remoteMessage.data["badge"] ?: "UPDATES"
        val iconType = remoteMessage.data["iconType"] ?: "SPORTS"
        val targetUrl = remoteMessage.data["targetUrl"] ?: remoteMessage.data["actionUrl"] ?: remoteMessage.data["deepLink"] ?: remoteMessage.data["url"]
        val extraData = remoteMessage.data["extraData"]

        // Extract Image URLs & Button Text for In-App Dialog
        val imageUrl = remoteMessage.notification?.imageUrl?.toString()
            ?: remoteMessage.data["imageUrl"]
            ?: remoteMessage.data["image"]

        val landscapeImageUrl = remoteMessage.data["landscapeImageUrl"]
            ?: remoteMessage.data["landscapeImage"]
            ?: remoteMessage.data["banner"]
            ?: remoteMessage.data["landscape"]

        val primaryButtonText = remoteMessage.data["primaryButtonText"]
            ?: remoteMessage.data["buttonText"]
            ?: remoteMessage.data["actionText"]
            ?: "VIEW NOW"

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

        // 2. Trigger In-App Message Dialog if app is in foreground or receiving FCM data
        val inAppMessage = com.example.sportsxtreme.domain.model.InAppMessage(
            title = title,
            body = body,
            imageUrl = imageUrl,
            landscapeImageUrl = landscapeImageUrl,
            primaryButtonText = primaryButtonText,
            targetUrl = targetUrl,
            category = category
        )
        com.example.sportsxtreme.common.InAppMessageManager.showInAppMessage(inAppMessage)

        // 3. Display System Notification Alert
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
