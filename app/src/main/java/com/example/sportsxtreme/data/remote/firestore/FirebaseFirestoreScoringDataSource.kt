package com.example.sportsxtreme.data.remote.firestore

import com.example.sportsxtreme.domain.model.BallEvent
import com.example.sportsxtreme.domain.model.BallEventType
import com.example.sportsxtreme.domain.model.Dismissal
import com.example.sportsxtreme.domain.model.DismissalType
import com.example.sportsxtreme.domain.model.ExtraRun
import com.example.sportsxtreme.domain.model.ExtraType
import com.example.sportsxtreme.domain.model.InningsScorecard
import com.example.sportsxtreme.domain.model.SyncState
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.WriteBatch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine

@Singleton
class FirebaseFirestoreScoringDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) : FirestoreScoringDataSource {
    override suspend fun syncDelivery(event: BallEvent, scorecard: InningsScorecard) {
        val match = firestore.collection(MATCHES_COLLECTION).document(event.matchId)
        firestore.batch().apply {
            set(match.collection(DELIVERIES_COLLECTION).document(event.ballId), event.toFirestorePayload(), SetOptions.merge())
            scorecard.batting.forEach { batting ->
                set(
                    match.collection(SCORECARD_COLLECTION)
                        .document(BATTING_DOCUMENT)
                        .collection(ENTRIES_COLLECTION)
                        .document("${event.inningsId}_${batting.playerId}"),
                    mapOf(
                        "inningsId" to event.inningsId,
                        "innings" to event.inningsNumber,
                        "playerId" to batting.playerId,
                        "runs" to batting.runs,
                        "balls" to batting.balls,
                        "fours" to batting.fours,
                        "sixes" to batting.sixes,
                        "strikeRate" to batting.strikeRate,
                        "status" to batting.status,
                        "dismissalType" to batting.dismissalType?.name,
                        "dismissedByBowlerId" to batting.dismissedByBowlerId,
                        "dismissedByFielderId" to batting.dismissedByFielderId,
                        "updatedAtEpochMs" to scorecard.summary.updatedAtEpochMs,
                        "syncedAt" to FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge()
                )
            }
            scorecard.bowling.forEach { bowling ->
                set(
                    match.collection(SCORECARD_COLLECTION)
                        .document(BOWLING_DOCUMENT)
                        .collection(ENTRIES_COLLECTION)
                        .document("${event.inningsId}_${bowling.playerId}"),
                    mapOf(
                        "inningsId" to event.inningsId,
                        "innings" to event.inningsNumber,
                        "playerId" to bowling.playerId,
                        "legalBalls" to bowling.legalBalls,
                        "overs" to bowling.overs.display,
                        "maidens" to bowling.maidens,
                        "runs" to bowling.runsConceded,
                        "wickets" to bowling.wickets,
                        "economy" to bowling.economy,
                        "wides" to bowling.wides,
                        "noBalls" to bowling.noBalls,
                        "updatedAtEpochMs" to scorecard.summary.updatedAtEpochMs,
                        "syncedAt" to FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge()
                )
            }
            set(
                match.collection(SCORECARD_COLLECTION)
                    .document(SUMMARY_DOCUMENT)
                    .collection(ENTRIES_COLLECTION)
                    .document(event.inningsId),
                mapOf(
                    "inningsId" to event.inningsId,
                    "innings" to event.inningsNumber,
                    "totalScore" to scorecard.summary.totalScore,
                    "wickets" to scorecard.summary.wickets,
                    "legalBalls" to scorecard.summary.legalBalls,
                    "overs" to scorecard.summary.overs.display,
                    "currentRunRate" to scorecard.summary.currentRunRate,
                    "requiredRunRate" to scorecard.summary.requiredRunRate,
                    "target" to scorecard.summary.target,
                    "strikerId" to scorecard.summary.strikerId,
                    "nonStrikerId" to scorecard.summary.nonStrikerId,
                    "bowlerId" to scorecard.summary.bowlerId,
                    "updatedAtEpochMs" to scorecard.summary.updatedAtEpochMs,
                    "syncedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
        }.commitAwait()
    }

    override fun observeBallEvents(matchId: String, inningsId: String): Flow<List<BallEvent>> = callbackFlow {
        val registration = firestore.collection(MATCHES_COLLECTION)
            .document(matchId)
            .collection(DELIVERIES_COLLECTION)
            .whereEqualTo("inningsId", inningsId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                } else {
                    trySend(snapshot?.documents.orEmpty().mapNotNull { document -> document.toBallEvent() })
                }
            }
        awaitClose { registration.remove() }
    }

    private fun BallEvent.toFirestorePayload(): Map<String, Any?> = mapOf(
        "deliveryId" to ballId,
        "matchId" to matchId,
        "inningsId" to inningsId,
        "inning" to inningsNumber,
        "sequenceNumber" to sequenceNumber,
        "over" to overNumber,
        "ball" to ballNumber,
        "legalBallNumber" to legalBallNumber,
        "battingTeamId" to battingTeamId,
        "bowlingTeamId" to bowlingTeamId,
        "batsmanId" to strikerId,
        "nonStrikerId" to nonStrikerId,
        "bowlerId" to bowlerId,
        "runs" to runsOffBat,
        "extras" to extras.sumOf { it.runs },
        "extraType" to (extras.firstOrNull()?.type?.name ?: "NONE"),
        "extraBreakdown" to extras.map { mapOf("type" to it.type.name, "runs" to it.runs) },
        "wicket" to (dismissal != null),
        "dismissalType" to (dismissal?.type?.name ?: "NONE"),
        "dismissedPlayerId" to dismissal?.dismissedPlayerId,
        "fielderId" to dismissal?.assistedByPlayerIds?.firstOrNull(),
        "isLegalDelivery" to isLegalDelivery,
        "eventType" to eventType.name,
        "reversedEventId" to reversedEventId,
        "timestamp" to timestampEpochMs,
        "updatedAtEpochMs" to timestampEpochMs,
        "pendingSync" to false,
        "recordedByUserId" to recordedByUserId,
        "syncedAt" to FieldValue.serverTimestamp()
    )

    private fun DocumentSnapshot.toBallEvent(): BallEvent? = runCatching {
        val dismissalType = getString("dismissalType")
            ?.let { value -> runCatching { DismissalType.valueOf(value) }.getOrDefault(DismissalType.NONE) }
            ?: DismissalType.NONE
        val dismissedPlayerId = getString("dismissedPlayerId")
        BallEvent(
            ballId = getString("deliveryId") ?: id,
            matchId = getString("matchId") ?: reference.parent.parent?.id.orEmpty(),
            inningsId = getString("inningsId") ?: return null,
            inningsNumber = getLong("inning")?.toInt() ?: return null,
            sequenceNumber = getLong("sequenceNumber") ?: return null,
            overNumber = getLong("over")?.toInt() ?: 0,
            ballNumber = getLong("ball")?.toInt() ?: 0,
            legalBallNumber = getLong("legalBallNumber")?.toInt() ?: 0,
            battingTeamId = getString("battingTeamId").orEmpty(),
            bowlingTeamId = getString("bowlingTeamId").orEmpty(),
            strikerId = getString("batsmanId").orEmpty(),
            nonStrikerId = getString("nonStrikerId").orEmpty(),
            bowlerId = getString("bowlerId").orEmpty(),
            runsOffBat = getLong("runs")?.toInt() ?: 0,
            extras = extraRuns(),
            dismissal = if (dismissalType == DismissalType.NONE || dismissedPlayerId == null) {
                null
            } else {
                    Dismissal(
                        type = dismissalType,
                        dismissedPlayerId = dismissedPlayerId,
                        assistedByPlayerIds = listOfNotNull(getString("fielderId"))
                    )
            },
            isLegalDelivery = getBoolean("isLegalDelivery") ?: true,
            eventType = getString("eventType")
                ?.let { value -> runCatching { BallEventType.valueOf(value) }.getOrDefault(BallEventType.DELIVERY) }
                ?: BallEventType.DELIVERY,
            reversedEventId = getString("reversedEventId"),
            recordedByUserId = getString("recordedByUserId").orEmpty(),
            timestampEpochMs = getLong("updatedAtEpochMs") ?: getLong("timestamp") ?: 0L,
            syncState = SyncState.SYNCED
        )
    }.getOrNull()

    private fun DocumentSnapshot.extraRuns(): List<ExtraRun> =
        (get("extraBreakdown") as? List<*>)
            ?.mapNotNull { entry ->
                val map = entry as? Map<*, *> ?: return@mapNotNull null
                val type = (map["type"] as? String)
                    ?.let { value -> runCatching { ExtraType.valueOf(value) }.getOrNull() }
                    ?: return@mapNotNull null
                val runs = (map["runs"] as? Number)?.toInt() ?: return@mapNotNull null
                ExtraRun(type, runs)
            }
            .orEmpty()

    private suspend fun WriteBatch.commitAwait() = suspendCancellableCoroutine<Unit> { continuation ->
        commit()
            .addOnSuccessListener {
                if (continuation.isActive) continuation.resume(Unit)
            }
            .addOnFailureListener { error ->
                if (continuation.isActive) continuation.resumeWithException(error)
            }
    }

    private companion object {
        const val MATCHES_COLLECTION = "matches"
        const val DELIVERIES_COLLECTION = "deliveries"
        const val SCORECARD_COLLECTION = "scorecard"
        const val BATTING_DOCUMENT = "batting"
        const val BOWLING_DOCUMENT = "bowling"
        const val SUMMARY_DOCUMENT = "summary"
        const val ENTRIES_COLLECTION = "entries"
    }
}
