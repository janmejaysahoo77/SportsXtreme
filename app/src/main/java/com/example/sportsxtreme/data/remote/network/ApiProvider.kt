package com.example.sportsxtreme.data.remote.network

import android.content.Context
import com.example.sportsxtreme.data.remote.api.ApiService

object ApiProvider {
    fun provideApiService(context: Context): ApiService {
        return RetrofitClient.create(context).create(ApiService::class.java)
    }
}
