package com.example.sportsxtreme.data.repository

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.data.local.dao.LiveMatchDao
import com.example.sportsxtreme.data.local.entity.LiveMatchEntity
import com.example.sportsxtreme.data.remote.firestore.FirestoreLiveMatchDataSource
import com.example.sportsxtreme.domain.model.LiveMatch
import com.example.sportsxtreme.domain.model.MatchStatus
import com.example.sportsxtreme.domain.repository.LiveMatchRepository
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
                .map { entities -> entities.map { it.toDomain() } }
                .collect { cached -> send(Resource.Success(cached)) }
        }
        launch {
            firestoreLiveMatchDataSource.observeLiveMatches()
                .catch { error -> send(Resource.Error(error.message ?: "Unable to load live matches")) }
                .collect { remoteMatches ->
                    // Firestore is the source of truth. When it returns an
                    // empty list, clear the Room cache so stale scores never
                    // linger on a spectator's phone.
                    if (remoteMatches.isEmpty()) {
                        liveMatchDao.clearAll()
                    } else {
                        liveMatchDao.upsertAll(remoteMatches.map { it.toEntity() })
                    }
                    send(Resource.Success(remoteMatches))
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
        teamAName = teamAName,
        teamBName = teamBName,
        teamAShortName = teamAShortName,
        teamBShortName = teamBShortName,
        status = status.name,
        score = score,
        wickets = wickets,
        overs = overs,
        currentRunRate = currentRunRate,
        requiredRunRate = requiredRunRate,
        target = target,
        strikerName = strikerName,
        strikerRuns = strikerRuns,
        strikerBalls = strikerBalls,
        nonStrikerName = nonStrikerName,
        bowlerName = bowlerName,
        bowlerOvers = bowlerOvers,
        bowlerRuns = bowlerRuns,
        bowlerWickets = bowlerWickets,
        matchStatusNote = matchStatusNote,
        updatedAtEpochMs = updatedAtEpochMs
    )

    private fun LiveMatchEntity.toDomain(): LiveMatch = LiveMatch(
        matchId = matchId,
        tournamentName = tournamentName,
        teamAName = teamAName,
        teamBName = teamBName,
        teamAShortName = teamAShortName,
        teamBShortName = teamBShortName,
        status = runCatching { MatchStatus.valueOf(status) }.getOrDefault(MatchStatus.CREATED),
        score = score,
        wickets = wickets,
        overs = overs,
        currentRunRate = currentRunRate,
        requiredRunRate = requiredRunRate,
        target = target,
        strikerName = strikerName,
        strikerRuns = strikerRuns,
        strikerBalls = strikerBalls,
        nonStrikerName = nonStrikerName,
        bowlerName = bowlerName,
        bowlerOvers = bowlerOvers,
        bowlerRuns = bowlerRuns,
        bowlerWickets = bowlerWickets,
        matchStatusNote = matchStatusNote,
        updatedAtEpochMs = updatedAtEpochMs
    )
}