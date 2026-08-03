package com.example.sportsxtreme.data.sync

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.data.local.dao.BallEventDao
import com.example.sportsxtreme.data.local.dao.SyncQueueDao
import com.example.sportsxtreme.data.local.mapper.toDomain
import com.example.sportsxtreme.data.remote.firestore.FirestoreScoringDataSource
import com.example.sportsxtreme.domain.model.SyncState
import com.example.sportsxtreme.domain.repository.ScoringRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class DeliverySyncProcessor @Inject constructor(
    private val syncQueueDao: SyncQueueDao,
    private val ballEventDao: BallEventDao,
    private val scoringRepository: ScoringRepository,
    private val firestoreScoringDataSource: FirestoreScoringDataSource
) {
    suspend fun syncReadyDeliveries(limit: Int = MAX_BATCH_SIZE): Boolean {
        while (true) {
            val operations = syncQueueDao.getReadyOperations(System.currentTimeMillis(), limit)
            if (operations.isEmpty()) return true
            for (operation in operations) {
                try {
                    val delivery = ballEventDao.getBallEvent(operation.entityId)
                    if (delivery == null) {
                        syncQueueDao.delete(operation.operationId)
                        continue
                    }
                    val scorecard = when (val result = scoringRepository
                        .observeScorecard(delivery.matchId, delivery.inningsId)
                        .first()
                    ) {
                        is Resource.Success -> requireNotNull(result.data)
                        is Resource.Error -> error(result.message ?: "Scorecard is not available")
                        is Resource.Loading -> error("Scorecard is still loading")
                    }
                    firestoreScoringDataSource.syncDelivery(delivery.toDomain(), scorecard)
                    ballEventDao.updateSyncState(delivery.ballId, SyncState.SYNCED.name)
                    syncQueueDao.delete(operation.operationId)
                } catch (_: Exception) {
                    syncQueueDao.update(
                        operation.copy(
                            state = "FAILED",
                            attemptCount = operation.attemptCount + 1,
                            nextAttemptAtEpochMs = System.currentTimeMillis(),
                            updatedAtEpochMs = System.currentTimeMillis()
                        )
                    )
                    return false
                }
            }
        }
    }

    private companion object {
        const val MAX_BATCH_SIZE = 25
    }
}
