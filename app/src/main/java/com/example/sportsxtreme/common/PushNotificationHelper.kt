package com.example.sportsxtreme.common

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.sportsxtreme.R
import com.example.sportsxtreme.presentation.notifications.NotificationActivity
import com.google.firebase.messaging.FirebaseMessaging

object PushNotificationHelper {
    private const val TAG = "PushNotificationHelper"
    const val CHANNEL_ID = "sports_updates_channel"
    private const val CHANNEL_NAME = "SportsXtreme Updates"
    private const val CHANNEL_DESC = "Notifications for match updates, team invites, and tournament alerts."

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            if (notificationManager?.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = CHANNEL_DESC
                    enableVibration(true)
                    setShowBadge(true)
                }
                notificationManager?.createNotificationChannel(channel)
                Log.d(TAG, "Notification channel created: $CHANNEL_ID")
            }
        }
    }

    fun showNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int = (System.currentTimeMillis() % 10000).toInt(),
        intent: Intent? = null
    ) {
        createNotificationChannel(context)

        val targetIntent = intent ?: Intent(context, NotificationActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            targetIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.appiconfromcanvawithbg)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.notify(notificationId, builder.build())
        Log.d(TAG, "Push notification displayed: ID=$notificationId, title=$title")
    }

    fun subscribeToAllUsersTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic("all_users")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Subscribed to FCM topic: all_users")
                } else {
                    Log.e(TAG, "Failed to subscribe to FCM topic: ${task.exception?.message}")
                }
            }
    }

    fun subscribeToUserTopic(userId: String) {
        if (userId.isBlank()) return
        val sanitizedTopic = "user_${userId.replace(Regex("[^a-zA-Z0-9-_.~%]"), "_")}"
        FirebaseMessaging.getInstance().subscribeToTopic(sanitizedTopic)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Subscribed to FCM user topic: $sanitizedTopic")
                } else {
                    Log.e(TAG, "Failed to subscribe to FCM user topic: ${task.exception?.message}")
                }
            }
    }
}
