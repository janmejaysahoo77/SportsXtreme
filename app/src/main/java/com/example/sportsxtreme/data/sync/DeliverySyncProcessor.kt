package com.example.sportsxtreme.data.sync

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.data.local.dao.BallEventDao
import com.example.sportsxtreme.data.local.dao.MatchDao
import com.example.sportsxtreme.data.local.dao.PlayerDao
import com.example.sportsxtreme.data.local.dao.SyncQueueDao
import com.example.sportsxtreme.data.local.dao.TeamDao
import com.example.sportsxtreme.data.local.mapper.toDomain
import com.example.sportsxtreme.data.remote.firestore.FirestoreScoringDataSource
import com.example.sportsxtreme.domain.model.LiveScorePayload
import com.example.sportsxtreme.domain.model.MatchStatus
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
    private val firestoreScoringDataSource: FirestoreScoringDataSource,
    private val matchDao: MatchDao,
    private val teamDao: TeamDao,
    private val playerDao: PlayerDao
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
                    // Push the lightweight liveScore document FIRST. This is the
                    // only document the spectator's Home screen reads, so it must
                    // reach Firestore even if the detailed batch sync below fails
                    // (e.g. Firestore rules restrict subcollection writes).
                    firestoreScoringDataSource.syncLiveScore(
                        matchId = delivery.matchId,
                        payload = buildLiveScorePayload(delivery.matchId, scorecard)
                    )
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

    /**
     * Builds the lightweight [LiveScorePayload] from the local Room data.
     *
     * This is the ONLY document the Home Screen reads, so it must contain the
     * current score, overs, run rates, current batter/bowler and match status —
     * but never ball-by-ball data.
     */
    private suspend fun buildLiveScorePayload(
        matchId: String,
        scorecard: com.example.sportsxtreme.domain.model.InningsScorecard
    ): LiveScorePayload {
        val match = matchDao.getMatch(matchId)
        val teamA = match?.teamAId?.let { teamDao.getTeam(it) }
        val teamB = match?.teamBId?.let { teamDao.getTeam(it) }
        val summary = scorecard.summary

        val striker = summary.strikerId?.let { playerDao.getPlayer(it) }
        val nonStriker = summary.nonStrikerId?.let { playerDao.getPlayer(it) }
        val bowler = summary.bowlerId?.let { playerDao.getPlayer(it) }
        val bowlerScorecard = scorecard.bowling.firstOrNull { it.playerId == summary.bowlerId }

        val status = match?.status?.let { runCatching { MatchStatus.valueOf(it) }.getOrDefault(MatchStatus.CREATED) }
            ?: MatchStatus.CREATED

        return LiveScorePayload(
            matchId = matchId,
            tournamentName = match?.title ?: "",
            teamAName = teamA?.teamName ?: "",
            teamBName = teamB?.teamName ?: "",
            teamAShortName = teamA?.shortName ?: "",
            teamBShortName = teamB?.shortName ?: "",
            status = status.name,
            score = summary.totalScore,
            wickets = summary.wickets,
            overs = summary.overs.display,
            currentRunRate = summary.currentRunRate,
            requiredRunRate = summary.requiredRunRate,
            target = summary.target,
            strikerName = striker?.playerName,
            strikerRuns = scorecard.batting.firstOrNull { it.playerId == summary.strikerId }?.runs ?: 0,
            strikerBalls = scorecard.batting.firstOrNull { it.playerId == summary.strikerId }?.balls ?: 0,
            nonStrikerName = nonStriker?.playerName,
            bowlerName = bowler?.playerName,
            bowlerOvers = bowlerScorecard?.overs?.display ?: "0.0",
            bowlerRuns = bowlerScorecard?.runsConceded ?: 0,
            bowlerWickets = bowlerScorecard?.wickets ?: 0,
            matchStatusNote = statusNote(status),
            updatedAtEpochMs = summary.updatedAtEpochMs
        )
    }

    private fun statusNote(status: MatchStatus): String? = when (status) {
        MatchStatus.LIVE -> "Powerplay"
        MatchStatus.INNINGS_BREAK -> "Innings Break"
        MatchStatus.COMPLETED -> "Completed"
        MatchStatus.ABANDONED -> "Rain Delay"
        else -> null
    }

    private companion object {
        const val MAX_BATCH_SIZE = 25
    }
}