package com.example.sportsxtreme.data.sync

import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PausedScoringExpiryScheduler @Inject constructor(
    private val workManager: WorkManager
) {
    fun schedule(matchId: String, pausedAtEpochMs: Long) {
        val request = OneTimeWorkRequestBuilder<PausedScoringExpiryWorker>()
            .setInitialDelay(RESUME_WINDOW_MS, TimeUnit.MILLISECONDS)
            .setInputData(
                Data.Builder()
                    .putString(PausedScoringExpiryWorker.INPUT_MATCH_ID, matchId)
                    .putLong(PausedScoringExpiryWorker.INPUT_PAUSED_AT, pausedAtEpochMs)
                    .build()
            )
            .build()
        workManager.enqueueUniqueWork(workName(matchId), ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(matchId: String) {
        workManager.cancelUniqueWork(workName(matchId))
    }

    private fun workName(matchId: String) = "paused-scoring-expiry-$matchId"

    private companion object {
        const val RESUME_WINDOW_MS = 2 * 60 * 60 * 1000L
    }
}
