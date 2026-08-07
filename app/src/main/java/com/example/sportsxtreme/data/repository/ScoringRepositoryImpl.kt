package com.example.sportsxtreme.data.repository

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.data.local.dao.BallEventDao
import com.example.sportsxtreme.data.local.dao.BattingDao
import com.example.sportsxtreme.data.local.dao.BowlingDao
import com.example.sportsxtreme.data.local.dao.InningsDao
import com.example.sportsxtreme.data.local.dao.MatchDao
import com.example.sportsxtreme.data.local.dao.MatchSummaryDao
import com.example.sportsxtreme.data.local.dao.PlayerDao
import com.example.sportsxtreme.data.local.dao.TeamDao
import com.example.sportsxtreme.data.local.scoring.LocalScoringStore
import com.example.sportsxtreme.data.remote.firestore.FirestoreScoringDataSource
import com.example.sportsxtreme.data.sync.DeliverySyncScheduler
import com.example.sportsxtreme.data.local.mapper.toDomain
import com.example.sportsxtreme.data.local.mapper.toMatchTeam
import com.example.sportsxtreme.domain.model.BallEvent
import com.example.sportsxtreme.domain.model.BattingScorecard
import com.example.sportsxtreme.domain.model.BowlingScorecard
import com.example.sportsxtreme.domain.model.DismissalType
import com.example.sportsxtreme.domain.model.InningsScorecard
import com.example.sportsxtreme.domain.model.LiveScorePayload
import com.example.sportsxtreme.domain.model.MatchState
import com.example.sportsxtreme.domain.model.MatchStatus
import com.example.sportsxtreme.domain.model.Overs
import com.example.sportsxtreme.domain.model.ScorecardSummary
import com.example.sportsxtreme.domain.model.TeamSide
import com.example.sportsxtreme.domain.repository.ScoringRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScoringRepositoryImpl @Inject constructor(
    private val matchDao: MatchDao,
    private val inningsDao: InningsDao,
    private val ballEventDao: BallEventDao,
    private val battingDao: BattingDao,
    private val bowlingDao: BowlingDao,
    private val matchSummaryDao: MatchSummaryDao,
    private val localScoringStore: LocalScoringStore,
    private val deliverySyncScheduler: DeliverySyncScheduler,
    private val firestoreScoringDataSource: FirestoreScoringDataSource,
    private val teamDao: TeamDao,
    private val playerDao: PlayerDao
) : ScoringRepository {
    override suspend fun recordBall(event: BallEvent): Resource<MatchState> = runCatching {
        localScoringStore.recordDelivery(event)
        runCatching { publishLiveScore(event) }
        deliverySyncScheduler.enqueue()
        Resource.Success(matchStateFor(event.matchId))
    }.getOrElse { Resource.Error(it.message ?: "Unable to save ball event") }

    override suspend fun undoLastBall(
        matchId: String,
        requestedByUserId: String,
        reason: String?
    ): Resource<MatchState> = runCatching {
        localScoringStore.reverseLatestDelivery(matchId, requestedByUserId, reason)
        deliverySyncScheduler.enqueue()
        Resource.Success(matchStateFor(matchId))
    }.getOrElse { Resource.Error(it.message ?: "Unable to undo ball event") }

    override fun observeBallEvents(matchId: String, inningsId: String): Flow<Resource<List<BallEvent>>> =
        ballEventDao.observeBallEvents(matchId, inningsId).map { events ->
            runCatching { Resource.Success(events.map { it.toDomain() }) }
                .getOrElse { Resource.Error(it.message ?: "Unable to observe ball events") }
        }.withRealtimeReconciliation(matchId, inningsId)

    override fun observeScorecard(
        matchId: String,
        inningsId: String
    ): Flow<Resource<InningsScorecard>> = combine(
        battingDao.observeScorecard(matchId, inningsId),
        bowlingDao.observeScorecard(matchId, inningsId),
        matchSummaryDao.observeSummary(matchId, inningsId),
        matchDao.observeMatch(matchId),
        inningsDao.observeInnings(matchId)
    ) { batting, bowling, persistedSummary, match, innings ->
        runCatching {
            val summary = persistedSummary ?: innings
                .firstOrNull { it.inningsId == inningsId }
                ?.let { currentInnings ->
                    com.example.sportsxtreme.data.local.entity.MatchSummaryEntity(
                        matchId = currentInnings.matchId,
                        inningsId = currentInnings.inningsId,
                        inningsNumber = currentInnings.number,
                        totalScore = currentInnings.score,
                        wickets = currentInnings.wickets,
                        legalBalls = currentInnings.legalBalls,
                        target = currentInnings.target,
                        strikerId = currentInnings.strikerId,
                        nonStrikerId = currentInnings.nonStrikerId,
                        bowlerId = currentInnings.currentBowlerId,
                        updatedAtEpochMs = 0L
                    )
                }
                ?: error("Innings not found")
            InningsScorecard(
                batting = batting.map { scorecard ->
                    BattingScorecard(
                        playerId = scorecard.playerId,
                        runs = scorecard.runs,
                        balls = scorecard.balls,
                        fours = scorecard.fours,
                        sixes = scorecard.sixes,
                        status = scorecard.status,
                        dismissalType = scorecard.dismissalType?.let(DismissalType::valueOf),
                        dismissedByBowlerId = scorecard.dismissedByBowlerId,
                        dismissedByFielderId = scorecard.dismissedByFielderId
                    )
                },
                bowling = bowling.map { scorecard ->
                    BowlingScorecard(
                        playerId = scorecard.playerId,
                        legalBalls = scorecard.legalBalls,
                        maidens = scorecard.maidens,
                        runsConceded = scorecard.runsConceded,
                        wickets = scorecard.wickets,
                        wides = scorecard.wides,
                        noBalls = scorecard.noBalls
                    )
                },
                summary = ScorecardSummary(
                    totalScore = summary.totalScore,
                    wickets = summary.wickets,
                    legalBalls = summary.legalBalls,
                    target = summary.target,
                    strikerId = summary.strikerId,
                    nonStrikerId = summary.nonStrikerId,
                    bowlerId = summary.bowlerId,
                    updatedAtEpochMs = summary.updatedAtEpochMs,
                    scheduledOvers = match?.overs
                )
            )
        }.fold(
            onSuccess = { scorecard -> Resource.Success(scorecard) },
            onFailure = { error -> Resource.Error<InningsScorecard>(error.message ?: "Unable to observe scorecard") }
        )
    }.withRealtimeReconciliation(matchId, inningsId)

    private fun <T> Flow<Resource<T>>.withRealtimeReconciliation(
        matchId: String,
        inningsId: String
    ): Flow<Resource<T>> = channelFlow {
        launch {
            firestoreScoringDataSource.observeBallEvents(matchId, inningsId)
                .catch { }
                .collect { remoteDeliveries -> localScoringStore.reconcileRemoteDeliveries(remoteDeliveries) }
        }
        collect { state -> send(state) }
    }

    private suspend fun publishLiveScore(event: BallEvent) {
        val scorecard = when (val result = observeScorecard(event.matchId, event.inningsId).first()) {
            is Resource.Success -> requireNotNull(result.data)
            is Resource.Error -> error(result.message ?: "Scorecard is not available")
            is Resource.Loading -> error("Scorecard is still loading")
        }
        val match = requireNotNull(matchDao.getMatch(event.matchId)) { "Match not found" }
        val teamA = teamDao.getTeam(match.teamAId)
        val teamB = teamDao.getTeam(match.teamBId)
        val summary = scorecard.summary
        val striker = summary.strikerId?.let { playerDao.getPlayer(it) }
        val nonStriker = summary.nonStrikerId?.let { playerDao.getPlayer(it) }
        val bowler = summary.bowlerId?.let { playerDao.getPlayer(it) }
        val bowlerScorecard = scorecard.bowling.firstOrNull { it.playerId == summary.bowlerId }

        firestoreScoringDataSource.syncLiveScore(
            matchId = event.matchId,
            payload = LiveScorePayload(
                matchId = event.matchId,
                tournamentName = match.title,
                teamAId = match.teamAId,
                teamBId = match.teamBId,
                battingTeamId = match.battingTeamId,
                teamAName = teamA?.teamName.orEmpty(),
                teamBName = teamB?.teamName.orEmpty(),
                teamAShortName = teamA?.shortName.orEmpty(),
                teamBShortName = teamB?.shortName.orEmpty(),
                status = match.status,
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
                matchStatusNote = if (match.status == MatchStatus.LIVE.name) "Powerplay" else null,
                updatedAtEpochMs = summary.updatedAtEpochMs
            )
        )
    }

    private suspend fun matchStateFor(matchId: String): MatchState {
        val match = requireNotNull(matchDao.getMatch(matchId)) { "Match not found" }
        val innings = inningsDao.getLatestInnings(matchId)
        val legalBalls = innings?.legalBalls ?: 0
        val currentOverEvents = innings?.let { currentInnings ->
            val events = ballEventDao.getBallEvents(matchId, currentInnings.inningsId)
                .map { it.toDomain() }
            events.takeLastWhile { it.overNumber == events.lastOrNull()?.overNumber }
        }.orEmpty()
        return MatchState(
            matchId = matchId,
            inningsId = innings?.inningsId,
            matchStatus = MatchStatus.valueOf(match.status),
            battingTeam = match.battingTeamId?.let { teamId -> teamDao.getTeam(teamId)?.toMatchTeam(
                if (teamId == match.teamAId) TeamSide.TEAM_A else TeamSide.TEAM_B
            ) },
            bowlingTeam = match.bowlingTeamId?.let { teamId -> teamDao.getTeam(teamId)?.toMatchTeam(
                if (teamId == match.teamAId) TeamSide.TEAM_A else TeamSide.TEAM_B
            ) },
            striker = innings?.strikerId?.let { playerDao.getPlayer(it)?.toDomain() },
            nonStriker = innings?.nonStrikerId?.let { playerDao.getPlayer(it)?.toDomain() },
            bowler = innings?.currentBowlerId?.let { playerDao.getPlayer(it)?.toDomain() },
            score = innings?.score ?: 0,
            wickets = innings?.wickets ?: 0,
            legalBalls = legalBalls,
            overs = Overs(legalBalls / 6, legalBalls % 6),
            currentOverEvents = currentOverEvents,
            target = innings?.target,
            currentInnings = innings?.toDomain(),
            updatedAtEpochMs = match.updatedAtEpochMs
        )
    }
}
