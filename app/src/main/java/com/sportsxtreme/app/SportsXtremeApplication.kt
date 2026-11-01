package com.sportsxtreme.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.sportsxtreme.app.common.FirebaseInAppMessagingHelper
import com.sportsxtreme.app.common.PushNotificationHelper
import com.sportsxtreme.app.data.sync.DeliverySyncScheduler
import com.sportsxtreme.app.data.di.AuthDependencies
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SportsXtremeApplication : Application(), Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var deliverySyncScheduler: DeliverySyncScheduler

    override fun onCreate() {
        super.onCreate()
        FirebaseAppCheckInstaller.install()
        AuthDependencies.initialize(this)
        PushNotificationHelper.subscribeToAllUsersTopic()
        FirebaseInAppMessagingHelper.initialize(this)
        deliverySyncScheduler.enqueue()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
