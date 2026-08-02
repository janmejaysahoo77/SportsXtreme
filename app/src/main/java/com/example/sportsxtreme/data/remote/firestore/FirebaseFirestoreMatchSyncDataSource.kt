package com.example.sportsxtreme.data.remote.firestore

import com.example.sportsxtreme.domain.model.Innings
import com.example.sportsxtreme.domain.model.Match
import com.example.sportsxtreme.domain.model.MatchTeam
import com.example.sportsxtreme.domain.model.PlayingXI
import com.example.sportsxtreme.domain.model.Toss
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

@Singleton
class FirebaseFirestoreMatchSyncDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    suspend fun sync(match: Match): Result<Unit> = suspendCancellableCoroutine { continuation ->
        val ownerId = auth.currentUser?.uid
        if (ownerId == null) {
            continuation.resume(Result.failure(IllegalStateException("Sign in before saving a match")))
            return@suspendCancellableCoroutine
        }
        firestore.collection(MATCHES_COLLECTION)
            .document(match.id)
            .set(match.toFirestorePayload(ownerId), SetOptions.merge())
            .addOnSuccessListener {
                if (continuation.isActive) continuation.resume(Result.success(Unit))
            }
            .addOnFailureListener { error ->
                if (continuation.isActive) continuation.resume(Result.failure(error))
            }
    }

    private fun Match.toFirestorePayload(ownerId: String): Map<String, Any?> = mapOf(
        "matchId" to id,
        "ownerId" to ownerId,
        "matchType" to matchType.name,
        "sport" to sport.name,
        "organiserId" to organiserId,
        "tournamentId" to tournamentId,
        "title" to title,
        "teamA" to teamA.toFirestorePayload(),
        "teamB" to teamB.toFirestorePayload(),
        "playingXI" to playingXI.mapKeys { it.key.name }.mapValues { it.value.toFirestorePayload() },
        "toss" to toss?.toFirestorePayload(),
        "innings" to innings.map { it.toFirestorePayload() },
        "status" to status.name,
        "format" to format?.name,
        "ballType" to ballType?.name,
        "overs" to overs,
        "venue" to venue,
        "matchDateEpochMs" to matchDateEpochMs,
        "matchTime" to matchTime,
        "createdAtEpochMs" to createdAtEpochMs,
        "updatedAtEpochMs" to updatedAtEpochMs,
        "syncedAt" to FieldValue.serverTimestamp()
    )

    private fun MatchTeam.toFirestorePayload(): Map<String, String> = mapOf(
        "teamId" to teamId,
        "name" to name,
        "shortName" to shortName,
        "side" to side.name
    )

    private fun PlayingXI.toFirestorePayload(): Map<String, Any> = mapOf(
        "teamId" to teamId,
        "side" to side.name,
        "playerIds" to playerIds,
        "selectedByUserId" to selectedByUserId,
        "selectedAtEpochMs" to selectedAtEpochMs
    )

    private fun Toss.toFirestorePayload(): Map<String, Any> = mapOf(
        "winnerTeamId" to winnerTeamId,
        "decision" to decision.name,
        "completedByUserId" to completedByUserId,
        "completedAtEpochMs" to completedAtEpochMs
    )

    private fun Innings.toFirestorePayload(): Map<String, Any?> = mapOf(
        "inningsId" to id,
        "number" to number,
        "battingTeamId" to battingTeamId,
        "bowlingTeamId" to bowlingTeamId,
        "strikerId" to strikerId,
        "nonStrikerId" to nonStrikerId,
        "currentBowlerId" to currentBowlerId,
        "score" to score,
        "wickets" to wickets,
        "legalBalls" to legalBalls,
        "target" to target,
        "status" to status.name,
        "startedAtEpochMs" to startedAtEpochMs,
        "completedAtEpochMs" to completedAtEpochMs
    )

    private companion object {
        const val MATCHES_COLLECTION = "matches"
    }
}
