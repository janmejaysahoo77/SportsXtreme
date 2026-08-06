package com.example.sportsxtreme.data.remote.firestore

import com.example.sportsxtreme.domain.model.BallEvent
import com.example.sportsxtreme.domain.model.InningsScorecard
import com.example.sportsxtreme.domain.model.LiveScorePayload
import kotlinx.coroutines.flow.Flow

interface FirestoreScoringDataSource {
    suspend fun syncDelivery(event: BallEvent, scorecard: InningsScorecard)

    /**
     * Writes the lightweight `liveScore` map to the match document.
     *
     * This is the ONLY document the Home Screen reads, so it must be kept
     * up to date on every delivery sync. It deliberately excludes ball-by-ball
     * data to keep Home Screen reads cheap.
     */
    suspend fun syncLiveScore(matchId: String, payload: LiveScorePayload)

    fun observeBallEvents(matchId: String, inningsId: String): Flow<List<BallEvent>>
}
