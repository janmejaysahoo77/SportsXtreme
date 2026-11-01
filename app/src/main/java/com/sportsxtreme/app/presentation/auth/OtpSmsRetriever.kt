package com.sportsxtreme.app.presentation.auth

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.Status

/** Starts SMS Retriever before Firebase sends the OTP to avoid a screen-transition race. */
internal class OtpSmsRetriever(
    private val context: Context,
    private val onMessage: (String) -> Unit
) {
    private var registered = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != SmsRetriever.SMS_RETRIEVED_ACTION) return
            val extras = intent.extras
            val status = if (Build.VERSION.SDK_INT >= 33) {
                extras?.getParcelable(SmsRetriever.EXTRA_STATUS, Status::class.java)
            } else {
                @Suppress("DEPRECATION")
                extras?.getParcelable(SmsRetriever.EXTRA_STATUS)
            }
            when (status?.statusCode) {
                com.google.android.gms.common.api.CommonStatusCodes.SUCCESS -> {
                    extras?.getString(SmsRetriever.EXTRA_SMS_MESSAGE)?.let(onMessage)
                        ?: Log.w(TAG, "Retriever succeeded without an SMS message")
                }
                com.google.android.gms.common.api.CommonStatusCodes.TIMEOUT -> Log.d(TAG, "SMS Retriever timed out")
                else -> Log.w(TAG, "SMS Retriever returned status ${status?.statusCode}")
            }
        }
    }

    fun startListening(onReady: () -> Unit = {}) {
        if (!registered) {
            ContextCompat.registerReceiver(
                context,
                receiver,
                IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION),
                SmsRetriever.SEND_PERMISSION,
                null,
                ContextCompat.RECEIVER_EXPORTED
            )
            registered = true
        }
        SmsRetriever.getClient(context).startSmsRetriever()
            .addOnSuccessListener {
                Log.d(TAG, "SMS Retriever started")
                onReady()
            }
            .addOnFailureListener {
                Log.e(TAG, "Could not start SMS Retriever; continuing with Firebase auto-retrieval", it)
                onReady()
            }
    }

    fun close() {
        if (!registered) return
        runCatching { context.unregisterReceiver(receiver) }
        registered = false
    }

    private companion object {
        const val TAG = "OTP_AUTOFILL"
    }
}
