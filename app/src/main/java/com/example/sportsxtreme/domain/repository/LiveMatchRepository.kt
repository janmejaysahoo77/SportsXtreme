package com.example.sportsxtreme.domain.repository

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.LiveMatch
import kotlinx.coroutines.flow.Flow

/**
 * Repository for the lightweight live match summaries shown on the Home Screen.
 *
 * The implementation combines a Firestore SnapshotListener (real-time source of
 * truth) with a Room cache (offline fallback). The Home Screen only ever reads
 * the lightweight `liveScore` document — never ball-by-ball data.
 */
interface LiveMatchRepository {
    /**
     * Emits the list of live/upcoming/completed match summaries.
     *
     * The flow starts with the Room cache (instant render) and then updates
     * automatically whenever Firestore pushes a change via the SnapshotListener.
     */
    fun observeLiveMatches(): Flow<Resource<List<LiveMatch>>>

    /**
     * Emits a single match summary for the Match Detail Screen.
     */
    fun observeLiveMatch(matchId: String): Flow<Resource<LiveMatch>>
}
