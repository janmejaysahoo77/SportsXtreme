package com.sportsxtreme.app.domain.repository

import com.sportsxtreme.app.common.Resource
import com.sportsxtreme.app.domain.model.Match
import com.sportsxtreme.app.domain.model.BallType
import com.sportsxtreme.app.domain.model.MatchFormat
import com.sportsxtreme.app.domain.model.MatchState
import com.sportsxtreme.app.domain.model.MatchTeam
import com.sportsxtreme.app.domain.model.MatchType
import com.sportsxtreme.app.domain.model.PlayingXI
import com.sportsxtreme.app.domain.model.SportType
import com.sportsxtreme.app.domain.model.TeamSide
import com.sportsxtreme.app.domain.model.Toss
import kotlinx.coroutines.flow.Flow

interface MatchRepository {
    suspend fun createMatch(request: CreateMatchRequest): Resource<Match>
    suspend fun updateMatchSettings(
        matchId: String,
        format: MatchFormat,
        ballType: BallType,
        overs: Int,
        maxOversPerBowler: Int? = null
    ): Resource<Match>
    suspend fun updateMatchDetails(
        matchId: String,
        venue: String,
        matchDateEpochMs: Long,
        matchTime: String
    ): Resource<Match>
    suspend fun updateMatchTeams(
        matchId: String,
        teamAId: String,
        teamBId: String
    ): Resource<Match>
    suspend fun selectPlayingXI(matchId: String, side: TeamSide, playingXI: PlayingXI): Resource<Match>
    suspend fun saveToss(matchId: String, toss: Toss): Resource<Match>
    suspend fun selectOpeningPlayers(
        matchId: String,
        strikerId: String,
        nonStrikerId: String,
        bowlerId: String
    ): Resource<MatchState>
    suspend fun startMatch(matchId: String): Resource<MatchState>
    suspend fun finishInnings(matchId: String, inningsId: String): Resource<MatchState>
    suspend fun selectNextBatter(matchId: String, playerId: String): Resource<MatchState>
    suspend fun selectNextBowler(matchId: String, playerId: String): Resource<MatchState>
    suspend fun startSecondInnings(matchId: String, strikerId: String, nonStrikerId: String, bowlerId: String): Resource<MatchState>
    suspend fun finishMatch(matchId: String): Resource<Match>
    suspend fun abandonMatch(matchId: String): Resource<Match>
    suspend fun updateInningsTarget(matchId: String, target: Int): Resource<MatchState>
    suspend fun applyPenaltyRuns(matchId: String, teamId: String, runs: Int): Resource<MatchState>
    fun observeMatch(matchId: String): Flow<Resource<Match>>
    fun observeActiveMatch(): Flow<Resource<Match>>
    fun observeLatestScoringMatch(): Flow<Resource<Match>>
    suspend fun pauseScoring(matchId: String): Long?
    suspend fun resumeScoring(matchId: String): Boolean
    suspend fun expirePausedScoringMatch(matchId: String, pausedAtEpochMs: Long): Boolean
    suspend fun deleteMatch(matchId: String): Resource<Unit>
    fun observeMatchState(matchId: String): Flow<Resource<MatchState>>
}

data class CreateMatchRequest(
    val matchType: MatchType,
    val sport: SportType,
    val organiserId: String,
    val title: String,
    val teamA: MatchTeam,
    val teamB: MatchTeam,
    val tournamentId: String? = null,
    val createdAtEpochMs: Long
)
