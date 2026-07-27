package com.example.sportsxtreme.data.remote.firestore

import com.example.sportsxtreme.domain.model.Match
import com.example.sportsxtreme.domain.model.MatchState
import com.example.sportsxtreme.domain.model.PlayingXI
import com.example.sportsxtreme.domain.model.TeamSide
import com.example.sportsxtreme.domain.model.Toss
import com.example.sportsxtreme.domain.repository.CreateMatchRequest
import kotlinx.coroutines.flow.Flow

interface FirestoreMatchDataSource {
    suspend fun createMatch(request: CreateMatchRequest): Match
    suspend fun savePlayingXI(matchId: String, side: TeamSide, playingXI: PlayingXI): Match
    suspend fun saveToss(matchId: String, toss: Toss): Match
    suspend fun saveOpeningPlayers(
        matchId: String,
        strikerId: String,
        nonStrikerId: String,
        bowlerId: String
    ): MatchState
    suspend fun markMatchLive(matchId: String): MatchState
    suspend fun finishInnings(matchId: String, inningsId: String): MatchState
    suspend fun finishMatch(matchId: String): Match
    fun observeMatch(matchId: String): Flow<Match>
    fun observeMatchState(matchId: String): Flow<MatchState>
}
