package com.example.sportsxtreme.data.remote.interceptor

import okhttp3.logging.HttpLoggingInterceptor

object LoggingInterceptor {
    fun create(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
            redactHeader("Authorization")
        }
    }
}
