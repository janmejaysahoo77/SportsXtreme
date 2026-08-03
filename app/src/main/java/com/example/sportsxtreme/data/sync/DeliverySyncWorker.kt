package com.example.sportsxtreme.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DeliverySyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val deliverySyncProcessor: DeliverySyncProcessor
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result =
        if (deliverySyncProcessor.syncReadyDeliveries()) Result.success() else Result.retry()
}
