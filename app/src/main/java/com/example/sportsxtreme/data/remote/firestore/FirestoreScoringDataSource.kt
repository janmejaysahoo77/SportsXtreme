package com.example.sportsxtreme.data.remote.firestore

import com.example.sportsxtreme.domain.model.BallEvent
import com.example.sportsxtreme.domain.model.MatchState
import kotlinx.coroutines.flow.Flow

interface FirestoreScoringDataSource {
    suspend fun appendBallEvent(event: BallEvent): MatchState
    suspend fun appendUndoEvent(matchId: String, requestedByUserId: String, reason: String?): MatchState
    fun observeBallEvents(matchId: String, inningsId: String): Flow<List<BallEvent>>
}
