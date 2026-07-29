package com.example.sportsxtreme.domain.repository

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.Match
import com.example.sportsxtreme.domain.model.BallType
import com.example.sportsxtreme.domain.model.MatchFormat
import com.example.sportsxtreme.domain.model.MatchState
import com.example.sportsxtreme.domain.model.MatchTeam
import com.example.sportsxtreme.domain.model.MatchType
import com.example.sportsxtreme.domain.model.PlayingXI
import com.example.sportsxtreme.domain.model.SportType
import com.example.sportsxtreme.domain.model.TeamSide
import com.example.sportsxtreme.domain.model.Toss
import kotlinx.coroutines.flow.Flow

interface MatchRepository {
    suspend fun createMatch(request: CreateMatchRequest): Resource<Match>
    suspend fun updateMatchSettings(
        matchId: String,
        format: MatchFormat,
        ballType: BallType,
        overs: Int
    ): Resource<Match>
    suspend fun updateMatchDetails(
        matchId: String,
        venue: String,
        matchDateEpochMs: Long,
        matchTime: String
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
    suspend fun finishMatch(matchId: String): Resource<Match>
    fun observeMatch(matchId: String): Flow<Resource<Match>>
    fun observeActiveMatch(): Flow<Resource<Match>>
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
