package com.example.sportsxtreme.domain.repository

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.BallEvent
import com.example.sportsxtreme.domain.model.MatchState
import kotlinx.coroutines.flow.Flow

interface ScoringRepository {
    suspend fun recordBall(event: BallEvent): Resource<MatchState>
    suspend fun undoLastBall(matchId: String, requestedByUserId: String, reason: String? = null): Resource<MatchState>
    fun observeBallEvents(matchId: String, inningsId: String): Flow<Resource<List<BallEvent>>>
}
