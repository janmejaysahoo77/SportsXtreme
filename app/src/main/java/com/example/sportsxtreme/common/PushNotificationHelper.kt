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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.installations.FirebaseInstallations
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

    fun fetchAndLogFcmToken(context: Context? = null, onTokenRetrieved: ((String) -> Unit)? = null) {
        // Fetch FCM Registration Token
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result.orEmpty()
                Log.i(TAG, "\n====================================================================")
                Log.i(TAG, "FCM REGISTRATION TOKEN (Copy & paste into Firebase Console 'Send Test Message'):")
                Log.i(TAG, token)
                Log.i(TAG, "====================================================================\n")

                if (token.isNotBlank()) {
                    onTokenRetrieved?.invoke(token)

                    // Sync token to Firestore if user is authenticated
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    if (currentUser != null) {
                        updateUserFcmTokenInFirestore(currentUser.uid, token)
                    }
                }
            } else {
                Log.e(TAG, "Failed to retrieve FCM token: ${task.exception?.message}")
            }
        }

        // Fetch Firebase Installation ID
        FirebaseInstallations.getInstance().id.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val installationId = task.result.orEmpty()
                Log.i(TAG, "FIREBASE INSTALLATION ID: $installationId")
            }
        }

        // Subscribe to global topic
        FirebaseMessaging.getInstance().subscribeToTopic("all_users")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Subscribed to FCM topic: all_users")
                }
            }
    }

    fun syncFcmTokenWithFirestore(token: String? = null) {
        if (!token.isNullOrBlank()) {
            Log.i(TAG, "\n====================================================================")
            Log.i(TAG, "NEW FCM REGISTRATION TOKEN:")
            Log.i(TAG, token)
            Log.i(TAG, "====================================================================\n")

            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                updateUserFcmTokenInFirestore(currentUser.uid, token)
            }
        } else {
            fetchAndLogFcmToken()
        }
    }

    private fun updateUserFcmTokenInFirestore(uid: String, token: String) {
        val payload = mapOf(
            "fcmToken" to token,
            "fcmTokens" to FieldValue.arrayUnion(token),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .set(payload, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "FCM token synced to Firestore for UID=$uid")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to sync FCM token to Firestore: ${e.message}")
            }
    }
}
