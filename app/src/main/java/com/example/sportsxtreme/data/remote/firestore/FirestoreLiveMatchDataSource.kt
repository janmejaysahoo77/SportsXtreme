package com.example.sportsxtreme.data.remote.firestore

import com.example.sportsxtreme.domain.model.LiveMatch
import kotlinx.coroutines.flow.Flow

/**
 * Data source for the lightweight live match summaries shown on the Home Screen.
 *
 * Only the `liveScore` document under each match is read here — never
 * ball-by-ball data. This keeps Home Screen reads cheap and fast.
 */
interface FirestoreLiveMatchDataSource {
    /**
     * Emits the list of live match summaries in real time.
     *
     * Uses a Firestore SnapshotListener so the Home Screen updates instantly
     * whenever the scorer records a ball.
     */
    fun observeLiveMatches(): Flow<List<LiveMatch>>

    /**
     * Emits a single live match summary for the Match Detail Screen.
     */
    fun observeLiveMatch(matchId: String): Flow<LiveMatch?>
}
