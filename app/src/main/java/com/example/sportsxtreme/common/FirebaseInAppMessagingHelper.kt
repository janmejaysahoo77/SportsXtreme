package com.example.sportsxtreme.common

import android.util.Log
import com.google.firebase.inappmessaging.FirebaseInAppMessaging
import com.google.firebase.inappmessaging.FirebaseInAppMessagingClickListener
import com.google.firebase.inappmessaging.FirebaseInAppMessagingDismissListener
import com.google.firebase.inappmessaging.FirebaseInAppMessagingDisplayErrorListener
import com.google.firebase.inappmessaging.FirebaseInAppMessagingImpressionListener

object FirebaseInAppMessagingHelper {

    private const val TAG = "FirebaseInAppMessaging"
    private var isInitialized = false

    fun initialize() {
        if (isInitialized) return
        isInitialized = true

        try {
            val fiam = FirebaseInAppMessaging.getInstance()
            
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

            Log.d(TAG, "Firebase In-App Messaging initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Firebase In-App Messaging: ${e.message}", e)
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
            FirebaseInAppMessaging.getInstance().triggerEvent(eventName)
            Log.d(TAG, "Triggered In-App Messaging custom event: $eventName")
        } catch (e: Exception) {
            Log.e(TAG, "Error triggering custom event '$eventName': ${e.message}", e)
        }
    }
}
