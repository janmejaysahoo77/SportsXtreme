package com.example.sportsxtreme.data.local.scoring

import androidx.room.withTransaction
import com.example.sportsxtreme.data.local.dao.BallEventDao
import com.example.sportsxtreme.data.local.dao.BattingDao
import com.example.sportsxtreme.data.local.dao.BowlingDao
import com.example.sportsxtreme.data.local.dao.InningsDao
import com.example.sportsxtreme.data.local.dao.MatchSummaryDao
import com.example.sportsxtreme.data.local.dao.MatchDao
import com.example.sportsxtreme.data.local.dao.PlayerDao
import com.example.sportsxtreme.data.local.dao.SyncQueueDao
import com.example.sportsxtreme.data.local.database.SportsXtremeDatabase
import com.example.sportsxtreme.data.local.entity.BattingEntity
import com.example.sportsxtreme.data.local.entity.BowlingEntity
import com.example.sportsxtreme.data.local.entity.MatchSummaryEntity
import com.example.sportsxtreme.data.local.entity.SyncQueueEntity
import com.example.sportsxtreme.data.local.mapper.toDomain
import com.example.sportsxtreme.data.local.mapper.toEntity
import com.example.sportsxtreme.domain.model.BallEvent
import com.example.sportsxtreme.domain.model.BallEventType
import com.example.sportsxtreme.domain.model.SyncState
import com.example.sportsxtreme.domain.scoring.InningsProjection
import com.example.sportsxtreme.domain.scoring.InningsProjectionCalculator
import javax.inject.Inject
import javax.inject.Singleton
import java.util.UUID

data class LocalScoringResult(
    val inserted: Boolean,
    val projection: InningsProjection
)

@Singleton
class LocalScoringStore @Inject constructor(
    private val database: SportsXtremeDatabase,
    private val inningsDao: InningsDao,
    private val ballEventDao: BallEventDao,
    private val battingDao: BattingDao,
    private val bowlingDao: BowlingDao,
    private val matchSummaryDao: MatchSummaryDao,
    private val syncQueueDao: SyncQueueDao,
    private val matchDao: MatchDao,
    private val playerDao: PlayerDao
) {
    suspend fun recordDelivery(event: BallEvent): LocalScoringResult = database.withTransaction {
        val innings = requireNotNull(inningsDao.getInnings(event.inningsId)) { "Innings not found" }
        require(innings.matchId == event.matchId) { "Delivery does not belong to this innings" }
        require(innings.number == event.inningsNumber) { "Delivery innings number does not match" }

        val inserted = ballEventDao.insertBall(event.copy(syncState = SyncState.PENDING).toEntity()) != -1L
        val events = ballEventDao.getBallEvents(event.matchId, event.inningsId).map { it.toDomain() }
        val projection = resolveBattingPair(innings, InningsProjectionCalculator.calculate(events))
        val updatedAtEpochMs = events.maxOfOrNull { it.timestampEpochMs } ?: event.timestampEpochMs

        persistProjection(innings, projection, updatedAtEpochMs)
        if (inserted) enqueueDeliverySync(event)

        LocalScoringResult(inserted = inserted, projection = projection)
    }

    suspend fun reverseLatestDelivery(
        matchId: String,
        requestedByUserId: String,
        reason: String?
    ): LocalScoringResult = database.withTransaction {
        val originalDelivery = requireNotNull(ballEventDao.getLatestActiveDelivery(matchId)) {
            "No active delivery is available to undo"
        }.toDomain()
        val existingEvents = ballEventDao
            .getBallEvents(matchId, originalDelivery.inningsId)
            .map { it.toDomain() }
        val timestampEpochMs = System.currentTimeMillis()
        val reversal = originalDelivery.copy(
            ballId = UUID.randomUUID().toString(),
            sequenceNumber = (existingEvents.maxOfOrNull { it.sequenceNumber } ?: 0L) + 1L,
            runsOffBat = 0,
            extras = emptyList(),
            dismissal = null,
            isLegalDelivery = false,
            eventType = BallEventType.REVERSAL,
            reversedEventId = originalDelivery.ballId,
            comment = reason,
            recordedByUserId = requestedByUserId,
            timestampEpochMs = timestampEpochMs,
            syncState = SyncState.PENDING,
            previousEventId = originalDelivery.ballId
        )
        check(ballEventDao.insertBall(reversal.toEntity()) != -1L) { "Unable to record delivery reversal" }
        val innings = requireNotNull(inningsDao.getInnings(originalDelivery.inningsId)) { "Innings not found" }
        val events = ballEventDao.getBallEvents(matchId, originalDelivery.inningsId).map { it.toDomain() }
        val projection = resolveBattingPair(innings, InningsProjectionCalculator.calculate(events))
        persistProjection(innings, projection, timestampEpochMs)
        enqueueDeliverySync(reversal)

        LocalScoringResult(inserted = true, projection = projection)
    }

    suspend fun reconcileRemoteDeliveries(deliveries: List<BallEvent>): Boolean = database.withTransaction {
        val changedInningsIds = linkedSetOf<String>()
        deliveries.forEach { remoteDelivery ->
            val localDelivery = ballEventDao.getBallEvent(remoteDelivery.ballId)
            if (localDelivery == null || remoteDelivery.timestampEpochMs > localDelivery.timestamp) {
                ballEventDao.upsertBall(remoteDelivery.copy(syncState = SyncState.SYNCED).toEntity())
                syncQueueDao.deleteByEntity("DELIVERY", remoteDelivery.ballId)
                changedInningsIds += remoteDelivery.inningsId
            }
        }
        changedInningsIds.forEach { inningsId ->
            val innings = requireNotNull(inningsDao.getInnings(inningsId)) { "Innings not found" }
            val events = ballEventDao.getBallEvents(innings.matchId, inningsId).map { it.toDomain() }
            persistProjection(
                innings = innings,
                projection = resolveBattingPair(innings, InningsProjectionCalculator.calculate(events)),
                updatedAtEpochMs = events.maxOfOrNull { it.timestampEpochMs } ?: 0L
            )
        }
        changedInningsIds.isNotEmpty()
    }

    private suspend fun persistProjection(
        innings: com.example.sportsxtreme.data.local.entity.InningsEntity,
        projection: InningsProjection,
        updatedAtEpochMs: Long
    ) {
        inningsDao.updateInnings(
            innings.copy(
                strikerId = projection.strikerId,
                nonStrikerId = projection.nonStrikerId,
                currentBowlerId = projection.bowlerId,
                score = projection.totalScore,
                wickets = projection.wickets,
                legalBalls = projection.legalBalls
            )
        )
        matchDao.getMatch(innings.matchId)?.let { match ->
            matchDao.updateMatch(
                match.copy(
                    strikerId = projection.strikerId,
                    nonStrikerId = projection.nonStrikerId,
                    currentBowlerId = projection.bowlerId,
                    updatedAtEpochMs = updatedAtEpochMs
                )
            )
        }
        battingDao.deleteForInnings(innings.matchId, innings.inningsId)
        battingDao.replaceAll(projection.batting.map { scorecard ->
            BattingEntity(
                matchId = innings.matchId,
                inningsId = innings.inningsId,
                inningsNumber = innings.number,
                playerId = scorecard.playerId,
                runs = scorecard.runs,
                balls = scorecard.balls,
                fours = scorecard.fours,
                sixes = scorecard.sixes,
                status = scorecard.status,
                dismissalType = scorecard.dismissalType,
                dismissedByBowlerId = scorecard.dismissedByBowlerId,
                dismissedByFielderId = scorecard.dismissedByFielderId,
                updatedAtEpochMs = updatedAtEpochMs
            )
        })
        bowlingDao.deleteForInnings(innings.matchId, innings.inningsId)
        bowlingDao.replaceAll(projection.bowling.map { scorecard ->
            BowlingEntity(
                matchId = innings.matchId,
                inningsId = innings.inningsId,
                inningsNumber = innings.number,
                playerId = scorecard.playerId,
                legalBalls = scorecard.legalBalls,
                maidens = scorecard.maidens,
                runsConceded = scorecard.runsConceded,
                wickets = scorecard.wickets,
                wides = scorecard.wides,
                noBalls = scorecard.noBalls,
                updatedAtEpochMs = updatedAtEpochMs
            )
        })
        matchSummaryDao.upsert(
            MatchSummaryEntity(
                matchId = innings.matchId,
                inningsId = innings.inningsId,
                inningsNumber = innings.number,
                totalScore = projection.totalScore,
                wickets = projection.wickets,
                legalBalls = projection.legalBalls,
                target = innings.target,
                strikerId = projection.strikerId,
                nonStrikerId = projection.nonStrikerId,
                bowlerId = projection.bowlerId,
                updatedAtEpochMs = updatedAtEpochMs
            )
        )
    }

    private suspend fun resolveBattingPair(
        innings: com.example.sportsxtreme.data.local.entity.InningsEntity,
        projection: InningsProjection
    ): InningsProjection {
        if (projection.strikerId != null && projection.nonStrikerId != null) return projection

        val selectedPlayers = playerDao.getPlayingXI(innings.matchId, innings.battingTeamId)
            .map { it.playerId }
            .ifEmpty { playerDao.getPlayers(innings.battingTeamId).map { it.playerId } }
        val availablePlayers = selectedPlayers.filterNot { it in projection.batting.map { batting -> batting.playerId } }
            .iterator()
        return projection.copy(
            strikerId = projection.strikerId ?: availablePlayers.takeIf { it.hasNext() }?.next(),
            nonStrikerId = projection.nonStrikerId ?: availablePlayers.takeIf { it.hasNext() }?.next()
        )
    }

    private suspend fun enqueueDeliverySync(event: BallEvent) {
        syncQueueDao.enqueue(
            SyncQueueEntity(
                operationId = event.ballId,
                matchId = event.matchId,
                entityType = "DELIVERY",
                entityId = event.ballId,
                operationType = "UPSERT",
                nextAttemptAtEpochMs = event.timestampEpochMs,
                createdAtEpochMs = event.timestampEpochMs,
                updatedAtEpochMs = event.timestampEpochMs
            )
        )
    }
}
