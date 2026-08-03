package com.example.sportsxtreme.data.remote.firestore

import com.example.sportsxtreme.domain.model.BallEvent
import com.example.sportsxtreme.domain.model.InningsScorecard
import kotlinx.coroutines.flow.Flow

interface FirestoreScoringDataSource {
    suspend fun syncDelivery(event: BallEvent, scorecard: InningsScorecard)
    fun observeBallEvents(matchId: String, inningsId: String): Flow<List<BallEvent>>
}
