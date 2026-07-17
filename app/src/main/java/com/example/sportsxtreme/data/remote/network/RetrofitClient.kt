package com.example.sportsxtreme.data.remote.network

import android.content.Context
import com.example.sportsxtreme.data.local.datastore.SessionManager
import com.example.sportsxtreme.data.remote.interceptor.AuthorizationInterceptor
import com.example.sportsxtreme.data.remote.interceptor.LoggingInterceptor
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    fun create(context: Context): Retrofit {
        val sessionManager = SessionManager(context)
        val okHttpClient = provideOkHttpClient(sessionManager)

        return Retrofit.Builder()
            .baseUrl(NetworkConstants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun provideOkHttpClient(sessionManager: SessionManager): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(NetworkConstants.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(NetworkConstants.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(NetworkConstants.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(AuthorizationInterceptor(sessionManager))
            .addInterceptor(LoggingInterceptor.create())
            .build()
    }
}
