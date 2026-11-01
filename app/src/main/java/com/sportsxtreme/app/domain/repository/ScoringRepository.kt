package com.sportsxtreme.app.domain.repository

import com.sportsxtreme.app.common.Resource
import com.sportsxtreme.app.domain.model.BallEvent
import com.sportsxtreme.app.domain.model.MatchState
import com.sportsxtreme.app.domain.model.InningsScorecard
import kotlinx.coroutines.flow.Flow

interface ScoringRepository {
    suspend fun recordBall(event: BallEvent): Resource<MatchState>
    suspend fun undoLastBall(matchId: String, requestedByUserId: String, reason: String? = null): Resource<MatchState>
    fun observeBallEvents(matchId: String, inningsId: String): Flow<Resource<List<BallEvent>>>
    fun observeScorecard(matchId: String, inningsId: String): Flow<Resource<InningsScorecard>>
}
