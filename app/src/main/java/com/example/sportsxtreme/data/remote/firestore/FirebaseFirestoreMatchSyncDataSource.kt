package com.example.sportsxtreme.data.remote.firestore

import com.example.sportsxtreme.domain.model.Innings
import com.example.sportsxtreme.domain.model.Match
import com.example.sportsxtreme.domain.model.MatchTeam
import com.example.sportsxtreme.domain.model.PlayingXI
import com.example.sportsxtreme.domain.model.Toss
import com.example.sportsxtreme.data.local.dao.PlayerDao
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
    private val auth: FirebaseAuth,
    private val playerDao: PlayerDao
) {
    suspend fun sync(match: Match): Result<Unit> {
        val ownerId = auth.currentUser?.uid
        if (ownerId == null) {
            return Result.failure(IllegalStateException("Sign in before saving a match"))
        }
        val profiles = match.playingXI.values.flatMap { it.playerIds }.distinct().associate { playerId ->
            val player = playerDao.getPlayer(playerId)
            val name = player?.playerName?.trim().orEmpty().ifBlank {
                val shortId = playerId.substringAfterLast("::")
                shortId.ifBlank { playerId }
            }
            playerId to mapOf(
                "name" to name,
                "linkedUserId" to player?.linkedUserId
            )
        }
        val payload = match.toFirestorePayload(ownerId).toMutableMap()
        payload["playerProfiles"] = profiles
        // Completion is provisional until the backend validates and confirms it.
        payload["status"] = if (match.status == com.example.sportsxtreme.domain.model.MatchStatus.COMPLETED) "FINALIZATION_PENDING" else match.status.name
        return suspendCancellableCoroutine { continuation ->
            firestore.collection(MATCHES_COLLECTION)
            .document(match.id)
            .set(payload, SetOptions.merge())
            .addOnSuccessListener {
                if (continuation.isActive) continuation.resume(Result.success(Unit))
            }
            .addOnFailureListener { error ->
                if (continuation.isActive) continuation.resume(Result.failure(error))
            }
        }
    }

    private fun Match.toFirestorePayload(ownerId: String): Map<String, Any?> = mapOf(
        "matchId" to id,
        "ownerId" to ownerId,
        "matchType" to matchType.name,
        "sport" to sport.name,
        "organiserId" to organiserId.ifBlank { ownerId },
        "tournamentId" to tournamentId,
        "title" to title,
        "teamA" to teamA.toFirestorePayload(),
        "teamB" to teamB.toFirestorePayload(),
        "playingXI" to playingXI.mapKeys { it.key.name }.mapValues { it.value.toFirestorePayload() },
        "toss" to toss?.toFirestorePayload(),
        "innings" to innings.map { it.toFirestorePayload() },
        "status" to status.name,
        "createdBy" to organiserId.ifBlank { ownerId },
        "teamAId" to teamA.teamId,
        "teamBId" to teamB.teamId,
        "format" to (format?.name ?: "T20"),
        "ballType" to (ballType?.name ?: "LEATHER"),
        "overs" to (overs ?: 20),
        "maxOversPerBowler" to maxOversPerBowler,
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
        "completedAtEpochMs" to completedAtEpochMs,
        "penaltyScoreAdjustment" to penaltyScoreAdjustment,
        "opponentPenaltyRuns" to opponentPenaltyRuns
    )

    private companion object {
        const val MATCHES_COLLECTION = "matches"
    }
}
