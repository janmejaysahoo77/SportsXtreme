package com.example.sportsxtreme

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.sportsxtreme.data.sync.DeliverySyncScheduler
import com.example.sportsxtreme.data.di.AuthDependencies
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SportsXtremeApplication : Application(), Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var deliverySyncScheduler: DeliverySyncScheduler

    override fun onCreate() {
        super.onCreate()
        AuthDependencies.initialize(this)
        deliverySyncScheduler.enqueue()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
