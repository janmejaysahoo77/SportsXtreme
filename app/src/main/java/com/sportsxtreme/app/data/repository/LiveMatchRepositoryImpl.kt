package com.sportsxtreme.app.data.repository

import com.sportsxtreme.app.common.Resource
import com.sportsxtreme.app.data.local.dao.LiveMatchDao
import com.sportsxtreme.app.data.local.entity.LiveMatchEntity
import com.sportsxtreme.app.data.remote.firestore.FirestoreLiveMatchDataSource
import com.sportsxtreme.app.domain.model.LiveMatch
import com.sportsxtreme.app.domain.model.MatchStatus
import com.sportsxtreme.app.domain.repository.LiveMatchRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Combines the Firestore SnapshotListener (real-time source of truth) with the
 * Room cache (offline fallback).
 */
@Singleton
class LiveMatchRepositoryImpl @Inject constructor(
    private val liveMatchDao: LiveMatchDao,
    private val firestoreLiveMatchDataSource: FirestoreLiveMatchDataSource
) : LiveMatchRepository {

    override fun observeLiveMatches(): Flow<Resource<List<LiveMatch>>> = channelFlow {
        launch {
            liveMatchDao.observeAll()
                .map { entities ->
                    val nowEpochMs = System.currentTimeMillis()
                    entities.asSequence()
                        .filterNot { it.matchId == PLACEHOLDER_MATCH_ID }
                        .map { it.toDomain() }
                        .toList()
                        .toHomeFeed(nowEpochMs)
                }
                .collect { cached ->
                    // A fresh install has an empty Room table. Keep the home
                    // section loading until Firestore supplies its first result
                    // instead of presenting that temporary cache miss as no matches.
                    if (cached.isNotEmpty()) send(Resource.Success(cached))
                }
        }
        launch {
            firestoreLiveMatchDataSource.observeLiveMatches()
                .catch { error -> send(Resource.Error(error.message ?: "Unable to load live matches")) }
                .collect { remoteMatches ->
                    val nowEpochMs = System.currentTimeMillis()
                    val homeMatches = remoteMatches
                        .filterNot { it.matchId == PLACEHOLDER_MATCH_ID }
                        .toHomeFeed(nowEpochMs)
                    liveMatchDao.replaceAll(homeMatches.map { it.toEntity() })
                    send(Resource.Success(homeMatches))
                }
        }
    }

    override fun observeLiveMatch(matchId: String): Flow<Resource<LiveMatch>> = channelFlow {
        launch {
            liveMatchDao.observeAll()
                .map { entities -> entities.firstOrNull { it.matchId == matchId }?.toDomain() }
                .collect { cached -> if (cached != null) send(Resource.Success(cached)) }
        }
        launch {
            firestoreLiveMatchDataSource.observeLiveMatch(matchId)
                .catch { error -> send(Resource.Error(error.message ?: "Unable to load live match")) }
                .collect { remoteMatch ->
                    if (remoteMatch != null) {
                        liveMatchDao.upsert(remoteMatch.toEntity())
                        send(Resource.Success(remoteMatch))
                    }
                }
        }
    }

    private fun LiveMatch.toEntity(): LiveMatchEntity = LiveMatchEntity(
        matchId = matchId,
        tournamentName = tournamentName,
        teamAId = teamAId,
        teamBId = teamBId,
        battingTeamId = battingTeamId,
        teamAName = teamAName,
        teamBName = teamBName,
        teamAShortName = teamAShortName,
        teamBShortName = teamBShortName,
        teamALogoUrl = teamALogoUrl,
        teamBLogoUrl = teamBLogoUrl,
        status = status.name,
        score = score,
        wickets = wickets,
        firstInningsScore = firstInningsScore,
        firstInningsWickets = firstInningsWickets,
        overs = overs,
        currentRunRate = currentRunRate,
        requiredRunRate = requiredRunRate,
        target = target,
        strikerName = strikerName,
        strikerRuns = strikerRuns,
        strikerBalls = strikerBalls,
        strikerFours = strikerFours,
        strikerSixes = strikerSixes,
        nonStrikerName = nonStrikerName,
        nonStrikerRuns = nonStrikerRuns,
        nonStrikerBalls = nonStrikerBalls,
        nonStrikerFours = nonStrikerFours,
        nonStrikerSixes = nonStrikerSixes,
        strikerPlayerId = strikerPlayerId,
        nonStrikerPlayerId = nonStrikerPlayerId,
        bowlerName = bowlerName,
        bowlerOvers = bowlerOvers,
        bowlerRuns = bowlerRuns,
        bowlerWickets = bowlerWickets,
        matchStatusNote = matchStatusNote,
        updatedAtEpochMs = updatedAtEpochMs,
        tournamentStage = tournamentStage
    )

    private fun LiveMatchEntity.toDomain(): LiveMatch = LiveMatch(
        matchId = matchId,
        tournamentName = tournamentName,
        teamAId = teamAId,
        teamBId = teamBId,
        battingTeamId = battingTeamId,
        teamAName = teamAName,
        teamBName = teamBName,
        teamAShortName = teamAShortName,
        teamBShortName = teamBShortName,
        teamALogoUrl = teamALogoUrl,
        teamBLogoUrl = teamBLogoUrl,
        status = runCatching { MatchStatus.valueOf(status) }.getOrDefault(MatchStatus.CREATED),
        score = score,
        wickets = wickets,
        firstInningsScore = firstInningsScore,
        firstInningsWickets = firstInningsWickets,
        overs = overs,
        currentRunRate = currentRunRate,
        requiredRunRate = requiredRunRate,
        target = target,
        strikerName = strikerName,
        strikerRuns = strikerRuns,
        strikerBalls = strikerBalls,
        strikerFours = strikerFours,
        strikerSixes = strikerSixes,
        nonStrikerName = nonStrikerName,
        nonStrikerRuns = nonStrikerRuns,
        nonStrikerBalls = nonStrikerBalls,
        nonStrikerFours = nonStrikerFours,
        nonStrikerSixes = nonStrikerSixes,
        strikerPlayerId = strikerPlayerId,
        nonStrikerPlayerId = nonStrikerPlayerId,
        bowlerName = bowlerName,
        bowlerOvers = bowlerOvers,
        bowlerRuns = bowlerRuns,
        bowlerWickets = bowlerWickets,
        matchStatusNote = matchStatusNote,
        updatedAtEpochMs = updatedAtEpochMs,
        tournamentStage = tournamentStage
    )

    private fun isFreshLiveMatch(match: LiveMatch, nowEpochMs: Long): Boolean =
        match.isActivelyPlaying &&
            match.updatedAtEpochMs > 0L &&
            nowEpochMs - match.updatedAtEpochMs <= MAX_LIVE_SCORE_AGE_MS

    private fun List<LiveMatch>.toHomeFeed(nowEpochMs: Long): List<LiveMatch> {
        val activeMatches = filter { isFreshLiveMatch(it, nowEpochMs) }
        val recentNonLiveMatches = asSequence()
            .filter { match ->
                match.isCompleted || match.isAbandoned || match.isPaused ||
                    match.status == MatchStatus.INNINGS_BREAK
            }
            .filter { it.updatedAtEpochMs > 0L }
            .filter { match ->
                match.isCompleted || match.isAbandoned ||
                    nowEpochMs - match.updatedAtEpochMs <= MAX_LIVE_SCORE_AGE_MS
            }
            .sortedByDescending { it.updatedAtEpochMs }
            .take(MAX_RECENT_MATCHES)
            .toList()
        return activeMatches + recentNonLiveMatches
    }

    private companion object {
        const val PLACEHOLDER_MATCH_ID = "mock-live-match-1"
        const val MAX_LIVE_SCORE_AGE_MS = 2 * 60 * 60 * 1000L
        const val MAX_RECENT_MATCHES = 5
    }
}
