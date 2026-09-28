package com.example.sportsxtreme.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.sportsxtreme.domain.repository.MatchRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class PausedScoringExpiryWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val matchRepository: MatchRepository
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result {
        val matchId = inputData.getString(INPUT_MATCH_ID) ?: return Result.failure()
        val pausedAt = inputData.getLong(INPUT_PAUSED_AT, 0L)
        if (pausedAt <= 0L) return Result.failure()
        matchRepository.expirePausedScoringMatch(matchId, pausedAt)
        return Result.success()
    }

    companion object {
        const val INPUT_MATCH_ID = "match_id"
        const val INPUT_PAUSED_AT = "paused_at_epoch_ms"
    }
}
