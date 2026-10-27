package com.example.sportsxtreme.common

import android.util.Log
import com.example.sportsxtreme.domain.model.InAppMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object InAppMessageManager {
    private const val TAG = "InAppMessageManager"
    private val _currentMessage = MutableStateFlow<InAppMessage?>(null)
    val currentMessage: StateFlow<InAppMessage?> = _currentMessage.asStateFlow()

    fun showInAppMessage(message: InAppMessage) {
        Log.d(TAG, "Posting in-app FCM message: title='${message.title}'")
        _currentMessage.value = message
    }

    fun dismissCurrentMessage() {
        Log.d(TAG, "Dismissing in-app FCM message")
        _currentMessage.value = null
    }
}
