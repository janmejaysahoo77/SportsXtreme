package com.example.sportsxtreme.data.remote.firestore

import com.example.sportsxtreme.domain.model.LiveMatch
import com.example.sportsxtreme.domain.model.LiveScorePayload
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Firestore-backed implementation of [FirestoreLiveMatchDataSource].
 *
 * It listens to the `matches` collection and reads only the lightweight
 * `liveScore` document under each match. This is the ONLY document read on the
 * Home Screen — ball-by-ball data is never downloaded here.
 */
@Singleton
class FirebaseFirestoreLiveMatchDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) : FirestoreLiveMatchDataSource {

    override fun observeLiveMatches(): Flow<List<LiveMatch>> = callbackFlow {
        // Listen to the matches collection. Each match document contains a
        // nested `liveScore` map that the scorer keeps up to date.
        val registration = firestore.collection(MATCHES_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                } else {
                    val matches = snapshot?.documents.orEmpty()
                        .mapNotNull { document -> document.toLiveMatch() }
                    trySend(matches)
                }
            }
        awaitClose { registration.remove() }
    }

    override fun observeLiveMatch(matchId: String): Flow<LiveMatch?> = callbackFlow {
        val registration = firestore.collection(MATCHES_COLLECTION)
            .document(matchId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                } else {
                    trySend(snapshot?.toLiveMatch())
                }
            }
        awaitClose { registration.remove() }
    }

    /**
     * Reads the lightweight `liveScore` map embedded in the match document.
     *
     * The scorer writes this map on every delivery sync, so the Home Screen
     * always reflects the latest score without downloading ball-by-ball data.
     */
    private fun DocumentSnapshot.toLiveMatch(): LiveMatch? {
        val liveScore = get("liveScore") as? Map<*, *> ?: return null
        val matchId = getString("matchId") ?: id
        val status = liveScore["status"] as? String ?: return null
        val score = (liveScore["score"] as? Number)?.toInt() ?: 0
        val wickets = (liveScore["wickets"] as? Number)?.toInt() ?: 0
        val overs = liveScore["overs"] as? String ?: "0.0"
        val currentRunRate = (liveScore["currentRunRate"] as? Number)?.toDouble() ?: 0.0
        val requiredRunRate = (liveScore["requiredRunRate"] as? Number)?.toDouble()
        val target = (liveScore["target"] as? Number)?.toInt()
        val strikerName = liveScore["strikerName"] as? String
        val strikerRuns = (liveScore["strikerRuns"] as? Number)?.toInt() ?: 0
        val strikerBalls = (liveScore["strikerBalls"] as? Number)?.toInt() ?: 0
        val nonStrikerName = liveScore["nonStrikerName"] as? String
        val bowlerName = liveScore["bowlerName"] as? String
        val bowlerOvers = liveScore["bowlerOvers"] as? String ?: "0.0"
        val bowlerRuns = (liveScore["bowlerRuns"] as? Number)?.toInt() ?: 0
        val bowlerWickets = (liveScore["bowlerWickets"] as? Number)?.toInt() ?: 0
        val matchStatusNote = liveScore["matchStatusNote"] as? String
        val updatedAtEpochMs = (liveScore["updatedAtEpochMs"] as? Number)?.toLong() ?: 0L

        return LiveScorePayload(
            matchId = matchId,
            tournamentName = liveScore["tournamentName"] as? String ?: "",
            teamAName = liveScore["teamAName"] as? String ?: "",
            teamBName = liveScore["teamBName"] as? String ?: "",
            teamAShortName = liveScore["teamAShortName"] as? String ?: "",
            teamBShortName = liveScore["teamBShortName"] as? String ?: "",
            status = status,
            score = score,
            wickets = wickets,
            overs = overs,
            currentRunRate = currentRunRate,
            requiredRunRate = requiredRunRate,
            target = target,
            strikerName = strikerName,
            strikerRuns = strikerRuns,
            strikerBalls = strikerBalls,
            nonStrikerName = nonStrikerName,
            bowlerName = bowlerName,
            bowlerOvers = bowlerOvers,
            bowlerRuns = bowlerRuns,
            bowlerWickets = bowlerWickets,
            matchStatusNote = matchStatusNote,
            updatedAtEpochMs = updatedAtEpochMs
        ).toDomain()
    }

    private companion object {
        const val MATCHES_COLLECTION = "matches"
    }
}