package com.example.sportsxtreme.common

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.inappmessaging.FirebaseInAppMessaging
import com.google.firebase.inappmessaging.FirebaseInAppMessagingClickListener
import com.google.firebase.inappmessaging.FirebaseInAppMessagingDismissListener
import com.google.firebase.inappmessaging.FirebaseInAppMessagingDisplayErrorListener
import com.google.firebase.inappmessaging.FirebaseInAppMessagingImpressionListener

object FirebaseInAppMessagingHelper {

    private const val TAG = "FirebaseInAppMessaging"
    private var isInitialized = false
    private var appContext: Context? = null

    fun initialize(context: Context? = null) {
        context?.let { appContext = it.applicationContext }
        if (isInitialized) {
            syncInAppMessagingState()
            return
        }
        isInitialized = true

        try {
            val fiam = FirebaseInAppMessaging.getInstance()

            // Reset local FIAM impression counters & frequency limiters so messages show continuously without wiping app data
            resetImpressionLimits(appContext)

            // Enable automatic message fetching & display
            fiam.isAutomaticDataCollectionEnabled = true
            fiam.setMessagesSuppressed(false)

            // Register Impression Listener
            fiam.addImpressionListener(FirebaseInAppMessagingImpressionListener { message ->
                Log.d(TAG, "In-App Message Impression recorded: campaignId=${message.campaignMetadata?.campaignId}")
            })

            // Register Click Listener
            fiam.addClickListener(FirebaseInAppMessagingClickListener { message, action ->
                val actionUrl = action.actionUrl
                Log.d(TAG, "In-App Message Clicked: actionUrl=$actionUrl, campaignId=${message.campaignMetadata?.campaignId}")
            })

            // Register Dismiss Listener
            fiam.addDismissListener(FirebaseInAppMessagingDismissListener { message ->
                Log.d(TAG, "In-App Message Dismissed: campaignId=${message.campaignMetadata?.campaignId}")
            })

            // Register Display Error Listener
            fiam.addDisplayErrorListener(FirebaseInAppMessagingDisplayErrorListener { message, errorReason ->
                Log.e(TAG, "In-App Message Display Error: campaignId=${message.campaignMetadata?.campaignId}, reason=$errorReason")
            })

            // Register AuthStateListener to guarantee in-app messages remain active and unsuppressed on login/auth changes
            try {
                FirebaseAuth.getInstance().addAuthStateListener { auth ->
                    val user = auth.currentUser
                    Log.d(TAG, "AuthStateListener fired: user=${user?.uid ?: "GUEST"}")
                    resetImpressionLimits(appContext)
                    syncInAppMessagingState(user?.uid)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error attaching AuthStateListener: ${e.message}", e)
            }

            Log.d(TAG, "Firebase In-App Messaging initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Firebase In-App Messaging: ${e.message}", e)
        }
    }

    /**
     * Resets local SharedPreferences where FIAM SDK records impressions and frequency caps,
     * allowing messages/campaigns to display continuously without wiping entire app data.
     */
    fun resetImpressionLimits(context: Context? = appContext) {
        val ctx = context ?: appContext ?: return
        try {
            val knownPrefs = listOf(
                "fiam_impressions",
                "fiam_eligibility",
                "fiam_rate_limiting",
                "fiam_campaigns",
                "fiam_impressions_store",
                "fiam_analytics",
                "com.google.firebase.inappmessaging",
                "com.google.firebase.inappmessaging.display"
            )
            for (prefName in knownPrefs) {
                ctx.getSharedPreferences(prefName, Context.MODE_PRIVATE).edit().clear().apply()
            }
            Log.d(TAG, "FIAM impression limits and frequency counters reset successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to reset FIAM impression limits: ${e.message}", e)
        }
    }

    /**
     * Synchronize and force unsuppress In-App Messaging state for both authenticated and guest users.
     */
    fun syncInAppMessagingState(userId: String? = FirebaseAuth.getInstance().currentUser?.uid) {
        try {
            resetImpressionLimits(appContext)
            val fiam = FirebaseInAppMessaging.getInstance()
            fiam.isAutomaticDataCollectionEnabled = true
            fiam.setMessagesSuppressed(false)

            PushNotificationHelper.subscribeToAllUsersTopic()
            if (!userId.isNullOrBlank()) {
                PushNotificationHelper.subscribeToUserTopic(userId)
                fiam.triggerEvent("user_authenticated")
            } else {
                fiam.triggerEvent("guest_session")
            }
            fiam.triggerEvent("app_open")
            Log.d(TAG, "FIAM state synchronized: messagesSuppressed=false, user=${userId ?: "GUEST"}")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing In-App Messaging state: ${e.message}", e)
        }
    }

    /**
     * Suppress or unsuppress In-App Messages (e.g. during active match scoring or checkout).
     */
    fun setMessagesSuppressed(suppress: Boolean) {
        try {
            FirebaseInAppMessaging.getInstance().setMessagesSuppressed(suppress)
            Log.d(TAG, "In-App Messaging suppression set to: $suppress")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting message suppression: ${e.message}", e)
        }
    }

    /**
     * Trigger a custom campaign event programmatically.
     */
    fun triggerEvent(eventName: String) {
        try {
            resetImpressionLimits(appContext)
            val fiam = FirebaseInAppMessaging.getInstance()
            fiam.isAutomaticDataCollectionEnabled = true
            fiam.setMessagesSuppressed(false)
            fiam.triggerEvent(eventName)
            Log.d(TAG, "Triggered In-App Messaging custom event: $eventName")
        } catch (e: Exception) {
            Log.e(TAG, "Error triggering custom event '$eventName': ${e.message}", e)
        }
    }
}
