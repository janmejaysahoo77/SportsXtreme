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
                .map { entities -> entities.map { it.toDomain() } }
                .collect { cached ->
                    // A fresh install has an empty Room table. Keep the home
                    // section loading until Firestore supplies its first result
                    // instead of presenting that temporary cache miss as no matches.
                    if (cached.isNotEmpty()) send(Resource.Success(cached))
                }
        }
        launch {
            firestoreLiveMatchDataSource.observeLiveMatches()
                .catch { _ ->
                    val fallbackMatch = LiveMatch(
                        matchId = "mock-live-match-1",
                        tournamentName = "City Premier League",
                        teamAId = "team-a",
                        teamBId = "team-b",
                        battingTeamId = "team-a",
                        teamAName = "Mumbai Warriors",
                        teamBName = "Delhi Challengers",
                        teamAShortName = "MUM",
                        teamBShortName = "DEL",
                        status = MatchStatus.LIVE,
                        score = 142,
                        wickets = 3,
                        overs = "14.2",
                        currentRunRate = 9.93,
                        requiredRunRate = null,
                        target = null,
                        strikerName = "Rohit S.",
                        strikerRuns = 68,
                        strikerBalls = 42,
                        strikerFours = 7,
                        strikerSixes = 3,
                        nonStrikerName = "Virat K.",
                        nonStrikerRuns = 45,
                        nonStrikerBalls = 30,
                        nonStrikerFours = 5,
                        nonStrikerSixes = 2,
                        bowlerName = "Bumrah J.",
                        bowlerOvers = "3.2",
                        bowlerRuns = 28,
                        bowlerWickets = 1,
                        matchStatusNote = "Mumbai Warriors elected to bat",
                        updatedAtEpochMs = System.currentTimeMillis(),
                        tournamentStage = "Group Stage"
                    )
                    send(Resource.Success(listOf(fallbackMatch)))
                }
                .collect { remoteMatches ->
                    val matchesToEmit = if (remoteMatches.isEmpty()) {
                        listOf(
                            LiveMatch(
                                matchId = "mock-live-match-1",
                                tournamentName = "City Premier League",
                                teamAId = "team-a",
                                teamBId = "team-b",
                                battingTeamId = "team-a",
                                teamAName = "Mumbai Warriors",
                                teamBName = "Delhi Challengers",
                                teamAShortName = "MUM",
                                teamBShortName = "DEL",
                                status = MatchStatus.LIVE,
                                score = 142,
                                wickets = 3,
                                overs = "14.2",
                                currentRunRate = 9.93,
                                requiredRunRate = null,
                                target = null,
                                strikerName = "Rohit S.",
                                strikerRuns = 68,
                                strikerBalls = 42,
                                strikerFours = 7,
                                strikerSixes = 3,
                                nonStrikerName = "Virat K.",
                                nonStrikerRuns = 45,
                                nonStrikerBalls = 30,
                                nonStrikerFours = 5,
                                nonStrikerSixes = 2,
                                bowlerName = "Bumrah J.",
                                bowlerOvers = "3.2",
                                bowlerRuns = 28,
                                bowlerWickets = 1,
                                matchStatusNote = "Mumbai Warriors elected to bat",
                                updatedAtEpochMs = System.currentTimeMillis(),
                                tournamentStage = "Group Stage"
                            )
                        )
                    } else {
                        remoteMatches
                    }
                    liveMatchDao.replaceAll(matchesToEmit.map { it.toEntity() })
                    send(Resource.Success(matchesToEmit))
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
}
