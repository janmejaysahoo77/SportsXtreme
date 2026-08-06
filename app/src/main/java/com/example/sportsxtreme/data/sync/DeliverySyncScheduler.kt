package com.example.sportsxtreme.data.sync

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeliverySyncScheduler @Inject constructor(
    private val workManager: WorkManager
) {
    fun enqueue() {
        val request = OneTimeWorkRequestBuilder<DeliverySyncWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
            .build()
        // REPLACE (not KEEP) so every ball recorded while a previous sync is
        // still enqueued/running still triggers a fresh sync. KEEP silently
        // drops the new request, which is why the friend's phone never saw
        // the live scorecard update.
        workManager.enqueueUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }

    private companion object {
        const val UNIQUE_WORK_NAME = "delivery-sync"
    }
}
