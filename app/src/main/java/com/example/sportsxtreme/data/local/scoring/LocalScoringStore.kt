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
import com.example.sportsxtreme.domain.model.DismissalType
import com.example.sportsxtreme.domain.model.ExtraType
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
        event.dismissal?.takeIf { it.type == DismissalType.BOWLED }?.let { dismissal ->
            require(innings.strikerId != null && event.strikerId == innings.strikerId && dismissal.dismissedPlayerId == innings.strikerId) {
                "A bowled dismissal must dismiss the striker"
            }
        }
        event.dismissal?.takeIf { event.metadata["dismissalVariant"] == "Retired hurt" }?.let { dismissal ->
            require(dismissal.type == DismissalType.RETIRED_HURT) { "Retired hurt must use the retired-hurt dismissal type" }
            require(
                dismissal.dismissedPlayerId == innings.strikerId || dismissal.dismissedPlayerId == innings.nonStrikerId
            ) { "Select the current striker or non-striker" }
            val dontCountBall = event.metadata["dontCountBall"] == "true"
            require(event.isLegalDelivery == !dontCountBall) { "Retired-hurt ball count does not match the selected option" }
            require(event.extras.isEmpty()) { "Retired hurt cannot add delivery extras" }
        }
        event.dismissal?.takeIf { event.metadata["dismissalVariant"] == "Obstructing the field" }?.let { dismissal ->
            require(dismissal.type == DismissalType.OBSTRUCTING_THE_FIELD) {
                "Obstructing the field must use the correct dismissal type"
            }
            require(
                dismissal.dismissedPlayerId == innings.strikerId || dismissal.dismissedPlayerId == innings.nonStrikerId
            ) { "Select the current striker or non-striker" }
        }
        event.dismissal?.takeIf { it.type == DismissalType.HIT_BALL_TWICE }?.let { dismissal ->
            require(dismissal.dismissedPlayerId == innings.strikerId && event.strikerId == innings.strikerId) {
                "Hit the ball twice must dismiss the striker"
            }
            val noBall = event.metadata["noBall"] == "true"
            require(event.isLegalDelivery == !noBall) { "No-ball setting does not match the delivery" }
            require(event.extras.none { it.type == com.example.sportsxtreme.domain.model.ExtraType.WIDE }) {
                "Hit the ball twice cannot record a wide"
            }
            require(event.extras.any { it.type == com.example.sportsxtreme.domain.model.ExtraType.NO_BALL } == noBall) {
                "No-ball extra does not match the selected option"
            }
        }
        event.dismissal?.takeIf { event.metadata["dismissalVariant"] == "Run out" }?.let { dismissal ->
            require(dismissal.type == DismissalType.RUN_OUT) { "Run out must use the correct dismissal type" }
            require(
                dismissal.dismissedPlayerId == innings.strikerId || dismissal.dismissedPlayerId == innings.nonStrikerId
            ) { "Select the current striker or non-striker" }
            val directHit = event.metadata["directHit"] == "true"
            require(dismissal.assistedByPlayerIds.size == (if (directHit) 1 else 2)) {
                "Select one fielder for a direct hit or two fielders otherwise"
            }
            dismissal.assistedByPlayerIds.forEach { fielderId ->
                val fielder = requireNotNull(playerDao.getPlayer(fielderId)) { "Selected fielder was not found" }
                require(fielder.teamId == innings.bowlingTeamId) { "Run-out fielders must be from the fielding team" }
            }
            val type = event.metadata["runOutDeliveryType"]?.let { ExtraType.valueOf(it) }
                ?: error("Select the run-out delivery type")
            require(type in setOf(ExtraType.WIDE, ExtraType.NO_BALL, ExtraType.BYE, ExtraType.LEG_BYE)) {
                "Invalid run-out delivery type"
            }
            require(event.isLegalDelivery == (type != ExtraType.WIDE && type != ExtraType.NO_BALL)) {
                "Run-out delivery type does not match the legal-ball count"
            }
        }
        event.dismissal?.takeIf { event.metadata["dismissalVariant"] == "Caught" }?.let { dismissal ->
            val fielderId = dismissal.assistedByPlayerIds.firstOrNull()
                ?: error("Select the fielder who took the catch")
            val fielder = requireNotNull(playerDao.getPlayer(fielderId)) { "Selected fielder was not found" }
            require(fielder.teamId == innings.bowlingTeamId) { "The catcher must be from the fielding team" }
        }
        event.dismissal?.takeIf { event.metadata["dismissalVariant"] == "Stumped" }?.let { dismissal ->
            val keeperId = dismissal.assistedByPlayerIds.firstOrNull()
                ?: error("Select the wicket-keeper")
            val keeper = requireNotNull(playerDao.getPlayer(keeperId)) { "Selected wicket-keeper was not found" }
            require(keeper.teamId == innings.bowlingTeamId) { "The wicket-keeper must be from the fielding team" }
            val selectedXI = playerDao.getPlayingXI(event.matchId, innings.bowlingTeamId)
            val teamKeepers = if (selectedXI.isNotEmpty()) {
                selectedXI.mapNotNull { playerDao.getPlayer(it.playerId) }.filter { it.role == "WICKET_KEEPER" }
            } else {
                playerDao.getPlayers(innings.bowlingTeamId).filter { it.role == "WICKET_KEEPER" }
            }
            if (teamKeepers.isNotEmpty()) require(teamKeepers.any { it.playerId == keeperId }) {
                "Select the designated wicket-keeper"
            }
        }

        val existingEvents = ballEventDao.getBallEvents(event.matchId, event.inningsId).map { it.toDomain() }
        val reversedDeliveryIds = existingEvents.asSequence()
            .filter { it.eventType == BallEventType.REVERSAL }
            .mapNotNull { it.reversedEventId }
            .toSet()
        val activeDeliveries = existingEvents.filter {
            it.eventType == BallEventType.DELIVERY && it.ballId !in reversedDeliveryIds
        }
        require(innings.currentBowlerId == event.bowlerId) { "Select the next bowler before scoring" }
        if (innings.legalBalls > 0 && innings.legalBalls % 6 == 0) {
            val previousOverNumber = innings.legalBalls / 6 - 1
            val previousOverBowlerId = activeDeliveries
                .lastOrNull { it.overNumber == previousOverNumber }
                ?.bowlerId
            require(previousOverBowlerId != event.bowlerId) { "Select a different bowler for the next over" }
        }
        val matchSettings = matchDao.getMatch(event.matchId)
        matchSettings?.maxOversPerBowler?.let { limit ->
            require(limit > 0) { "The maximum overs per bowler setting is invalid" }
            if (event.isLegalDelivery) {
                val alreadyBowled = activeDeliveries.count { it.bowlerId == event.bowlerId && it.isLegalDelivery }
                require(alreadyBowled < limit * 6) { "$limit over${if (limit == 1) "" else "s"} per bowler maximum reached" }
            }
        }

        val inserted = ballEventDao.insertBall(event.copy(syncState = SyncState.PENDING).toEntity()) != -1L
        val events = ballEventDao.getBallEvents(event.matchId, event.inningsId).map { it.toDomain() }
        val projection = InningsProjectionCalculator.calculate(events)
        val updatedAtEpochMs = events.maxOfOrNull { it.timestampEpochMs } ?: event.timestampEpochMs

        persistProjection(innings, projection, updatedAtEpochMs)
        val match = matchDao.getMatch(event.matchId)
        val wicketLimit = playerDao.getPlayingXI(event.matchId, innings.battingTeamId).size.let { if (it > 1) it - 1 else 10 }
        val inningsComplete = (match?.overs?.let { projection.legalBalls >= it * 6 } == true) ||
            projection.wickets >= wicketLimit ||
            (innings.target?.let { projection.totalScore >= it } == true)
        if (inningsComplete && innings.status == "LIVE") {
            val completedAt = System.currentTimeMillis()
            inningsDao.updateInnings(innings.copy(
                strikerId = projection.strikerId,
                nonStrikerId = projection.nonStrikerId,
                currentBowlerId = projection.bowlerId,
                score = projection.totalScore,
                wickets = projection.wickets,
                legalBalls = projection.legalBalls,
                status = "COMPLETED",
                completedAtEpochMs = completedAt
            ))
            match?.let { matchDao.updateMatch(it.copy(status = "INNINGS_BREAK", updatedAtEpochMs = completedAt)) }
        }
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
        val projection = InningsProjectionCalculator.calculate(events)
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
            projection = InningsProjectionCalculator.calculate(events),
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
        val events = ballEventDao.getBallEvents(innings.matchId, innings.inningsId).map { it.toDomain() }
        val reversedDeliveryIds = events
            .asSequence()
            .filter { it.eventType == BallEventType.REVERSAL }
            .mapNotNull { it.reversedEventId }
            .toSet()
        val hasActiveDeliveries = events.any {
            it.eventType == BallEventType.DELIVERY && it.ballId !in reversedDeliveryIds
        }
        val firstDelivery = events.firstOrNull { it.eventType == BallEventType.DELIVERY }
        val persistedProjection = if (!hasActiveDeliveries && firstDelivery != null) {
            projection.copy(
                strikerId = firstDelivery.strikerId,
                nonStrikerId = firstDelivery.nonStrikerId,
                bowlerId = firstDelivery.bowlerId
            )
        } else {
            projection
        }
        inningsDao.updateInnings(
            innings.copy(
                strikerId = persistedProjection.strikerId,
                nonStrikerId = persistedProjection.nonStrikerId,
                currentBowlerId = persistedProjection.bowlerId,
                score = persistedProjection.totalScore,
                wickets = persistedProjection.wickets,
                legalBalls = persistedProjection.legalBalls
            )
        )
        matchDao.getMatch(innings.matchId)?.let { match ->
            matchDao.updateMatch(
                match.copy(
                    strikerId = persistedProjection.strikerId,
                    nonStrikerId = persistedProjection.nonStrikerId,
                    currentBowlerId = persistedProjection.bowlerId,
                    updatedAtEpochMs = updatedAtEpochMs
                )
            )
        }
        battingDao.deleteForInnings(innings.matchId, innings.inningsId)
        battingDao.replaceAll(persistedProjection.batting.map { scorecard ->
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
        bowlingDao.replaceAll(persistedProjection.bowling.map { scorecard ->
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
                totalScore = persistedProjection.totalScore,
                wickets = persistedProjection.wickets,
                legalBalls = persistedProjection.legalBalls,
                target = innings.target,
                strikerId = persistedProjection.strikerId,
                nonStrikerId = persistedProjection.nonStrikerId,
                bowlerId = persistedProjection.bowlerId,
                updatedAtEpochMs = updatedAtEpochMs
            )
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
