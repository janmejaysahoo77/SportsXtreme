package com.sportsxtreme.app.data.remote.firestore

import com.sportsxtreme.app.domain.model.LiveMatch
import com.sportsxtreme.app.domain.model.LiveScorePayload
import com.sportsxtreme.app.domain.model.MatchStatus
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import com.google.android.gms.tasks.Tasks
import javax.inject.Inject
import javax.inject.Singleton
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Firestore-backed implementation of [FirestoreLiveMatchDataSource].
 *
 * It listens to the `matches` collection and reads the lightweight `liveScore`
 * projection. For second innings, it also reads the first innings summary so
 * the home card can show the opening score with wickets. Ball-by-ball data is
 * never downloaded here.
 */
@Singleton
class FirebaseFirestoreLiveMatchDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) : FirestoreLiveMatchDataSource {
    private val teamLogoCache = ConcurrentHashMap<String, String>()
    private val firstInningsCache = ConcurrentHashMap<String, Pair<Int, Int>>()

    override fun observeLiveMatches(): Flow<List<LiveMatch>> = callbackFlow {
        // Listen to the matches collection. Each match document contains a
        // nested `liveScore` map that the scorer keeps up to date.
        val registration = firestore.collection(MATCHES_COLLECTION)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                if (error != null) {
                    close(error)
                } else {
                    // On a fresh install, Firestore can first report an empty
                    // local cache. Wait for the server result before declaring
                    // that there are no live matches.
                    if (snapshot?.let { it.metadata.isFromCache && it.documents.isEmpty() } == true) {
                        return@addSnapshotListener
                    }
                    val matches = snapshot?.documents.orEmpty()
                        .mapNotNull { document -> document.toLiveMatch() }
                    resolveFirstInnings(matches) { withFirstInnings ->
                        resolveTeamLogos(withFirstInnings) { enriched -> trySend(enriched) }
                    }
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
                    val match = snapshot?.toLiveMatch()
                    if (match == null) trySend(null)
                    else resolveFirstInnings(listOf(match)) { withFirstInnings ->
                        resolveTeamLogos(withFirstInnings) { enriched -> trySend(enriched.firstOrNull()) }
                    }
                }
            }
        awaitClose { registration.remove() }
    }

    private fun resolveFirstInnings(matches: List<LiveMatch>, onResolved: (List<LiveMatch>) -> Unit) {
        val needsLookup = matches.filter {
            (it.target != null || it.isCompleted) && (it.firstInningsScore == null || it.firstInningsWickets == null)
        }
        val idsToLoad = needsLookup.map { it.matchId }.distinct().filterNot(firstInningsCache::containsKey)
        if (idsToLoad.isEmpty()) {
            onResolved(matches.withFirstInnings())
            return
        }

        Tasks.whenAllComplete(idsToLoad.map { matchId ->
            firestore.collection(MATCHES_COLLECTION).document(matchId)
                .collection("scorecard").document("summary").collection("entries")
                .get()
        }).addOnSuccessListener { completed ->
            completed.forEachIndexed { index, task ->
                if (!task.isSuccessful) return@forEachIndexed
                val documents = (task.result as? com.google.firebase.firestore.QuerySnapshot)?.documents.orEmpty()
                val firstInnings = documents.firstOrNull { doc ->
                    val num = doc.getLong("innings")?.toInt()
                        ?: doc.getLong("inningsNumber")?.toInt()
                        ?: doc.getLong("number")?.toInt()
                        ?: doc.getLong("inning")?.toInt()
                    num == 1
                } ?: documents.firstOrNull() ?: return@forEachIndexed

                val runs = ((firstInnings.get("totalScore") ?: firstInnings.get("score") ?: firstInnings.get("runs")) as? Number)?.toInt()
                    ?: return@forEachIndexed
                val wickets = ((firstInnings.get("wickets") ?: firstInnings.get("wicketCount")) as? Number)?.toInt() ?: 0
                firstInningsCache[idsToLoad[index]] = runs to wickets
            }
            onResolved(matches.withFirstInnings())
        }.addOnFailureListener { onResolved(matches.withFirstInnings()) }
    }

    private fun List<LiveMatch>.withFirstInnings(): List<LiveMatch> = map { match ->
        val cached = firstInningsCache[match.matchId]
        if (cached == null) match
        else match.copy(
            firstInningsScore = match.firstInningsScore ?: cached.first,
            firstInningsWickets = match.firstInningsWickets ?: cached.second
        )
    }

    private fun resolveTeamLogos(matches: List<LiveMatch>, onResolved: (List<LiveMatch>) -> Unit) {
        val idsToLoad = matches.flatMap { listOf(it.teamAId, it.teamBId) }
            .filter(String::isNotBlank).distinct().filterNot(teamLogoCache::containsKey)
        if (idsToLoad.isEmpty()) {
            onResolved(matches.withTeamLogos())
            return
        }
        Tasks.whenAllComplete(idsToLoad.map { id -> firestore.collection("teams").document(id).get() })
            .addOnSuccessListener { completed ->
                completed.forEachIndexed { index, task ->
                    // whenAllComplete succeeds even when one or more of its
                    // child reads failed. Accessing result on a failed task
                    // throws RuntimeExecutionException (for example when the
                    // viewer is signed out and team reads require auth).
                    val data = if (task.isSuccessful) {
                        (task.result as? DocumentSnapshot)?.data.orEmpty()
                    } else {
                        emptyMap()
                    }
                    val logo = listOf("logoUrl", "teamLogoUrl", "photoUrl")
                        .firstNotNullOfOrNull { key -> (data[key] as? String)?.takeIf(String::isNotBlank) }
                    if (logo != null) teamLogoCache[idsToLoad[index]] = logo
                    else teamLogoCache.putIfAbsent(idsToLoad[index], "")
                }
                onResolved(matches.withTeamLogos())
            }
            .addOnFailureListener { onResolved(matches.withTeamLogos()) }
    }

    private fun List<LiveMatch>.withTeamLogos(): List<LiveMatch> = map { match ->
        match.copy(
            teamALogoUrl = match.teamALogoUrl.ifBlank { teamLogoCache[match.teamAId].orEmpty() },
            teamBLogoUrl = match.teamBLogoUrl.ifBlank { teamLogoCache[match.teamBId].orEmpty() }
        )
    }

    /**
     * Reads the lightweight `liveScore` map embedded in the match document.
     *
     * The scorer writes this map on every delivery sync, so the Home Screen
     * always reflects the latest score without downloading ball-by-ball data.
     */
    private fun DocumentSnapshot.toLiveMatch(): LiveMatch? {
        val liveScore = get("liveScore") as? Map<*, *> ?: emptyMap<Any, Any>()
        val teamA = get("teamA") as? Map<*, *> ?: emptyMap<Any, Any>()
        val teamB = get("teamB") as? Map<*, *> ?: emptyMap<Any, Any>()
        val matchId = getString("matchId") ?: id
        // `liveScore` is a denormalized score projection and may lag behind
        // match completion. The match document is the lifecycle source of truth.
        val rawStatus = getString("status")
            ?: getString("scheduleStatus")
            ?: (liveScore["status"] as? String)
            ?: return null
        val status = when (rawStatus.trim().uppercase()) {
            "COMPLETED", "FINISHED", "FINALIZATION_PENDING", "FINALIZING", "FINALIZED" ->
                MatchStatus.COMPLETED.name
            else -> rawStatus.trim().uppercase()
        }
        val score = (liveScore["score"] as? Number)?.toInt() ?: 0
        val wickets = (liveScore["wickets"] as? Number)?.toInt() ?: 0
        val overs = liveScore["overs"] as? String ?: "0.0"
        val currentRunRate = (liveScore["currentRunRate"] as? Number)?.toDouble() ?: 0.0
        val requiredRunRate = (liveScore["requiredRunRate"] as? Number)?.toDouble()
        val target = (liveScore["target"] as? Number)?.toInt()
        val firstInnings = (get("innings") as? List<*>)
            .orEmpty()
            .mapNotNull { it as? Map<*, *> }
            .minByOrNull {
                (it["number"] as? Number)?.toInt()
                    ?: (it["innings"] as? Number)?.toInt()
                    ?: (it["inningsNumber"] as? Number)?.toInt()
                    ?: (it["inning"] as? Number)?.toInt()
                    ?: Int.MAX_VALUE
            }
        val firstInningsScore = (
            firstInnings?.get("score") ?: firstInnings?.get("totalScore") ?: firstInnings?.get("runs")
                ?: liveScore["firstInningsScore"]
            ) as? Number
        val firstInningsWickets = (
            firstInnings?.get("wickets") ?: firstInnings?.get("wicketCount")
                ?: liveScore["firstInningsWickets"]
            ) as? Number
        val strikerName = liveScore["strikerName"] as? String
        val strikerRuns = (liveScore["strikerRuns"] as? Number)?.toInt() ?: 0
        val strikerBalls = (liveScore["strikerBalls"] as? Number)?.toInt() ?: 0
        val strikerFours = (liveScore["strikerFours"] as? Number)?.toInt() ?: 0
        val strikerSixes = (liveScore["strikerSixes"] as? Number)?.toInt() ?: 0
        val nonStrikerName = liveScore["nonStrikerName"] as? String
        val nonStrikerRuns = (liveScore["nonStrikerRuns"] as? Number)?.toInt() ?: 0
        val nonStrikerBalls = (liveScore["nonStrikerBalls"] as? Number)?.toInt() ?: 0
        val nonStrikerFours = (liveScore["nonStrikerFours"] as? Number)?.toInt() ?: 0
        val nonStrikerSixes = (liveScore["nonStrikerSixes"] as? Number)?.toInt() ?: 0
        val bowlerName = liveScore["bowlerName"] as? String
        val bowlerOvers = liveScore["bowlerOvers"] as? String ?: "0.0"
        val bowlerRuns = (liveScore["bowlerRuns"] as? Number)?.toInt() ?: 0
        val bowlerWickets = (liveScore["bowlerWickets"] as? Number)?.toInt() ?: 0
        val matchStatusNote = liveScore["matchStatusNote"] as? String
        val finalizedAt = ((get("finalization") as? Map<*, *>)?.get("finalizedAt") as? Timestamp)
            ?.toDate()?.time
        val updatedAtEpochMs = finalizedAt
            ?: (get("updatedAtEpochMs") as? Number)?.toLong()
            ?: (liveScore["updatedAtEpochMs"] as? Number)?.toLong()
            ?: 0L

        return LiveScorePayload(
            matchId = matchId,
            tournamentName = liveScore["tournamentName"] as? String ?: getString("title").orEmpty(),
            teamAId = liveScore["teamAId"] as? String ?: getString("teamAId") ?: teamA["teamId"] as? String ?: "",
            teamBId = liveScore["teamBId"] as? String ?: getString("teamBId") ?: teamB["teamId"] as? String ?: "",
            battingTeamId = liveScore["battingTeamId"] as? String,
            teamAName = liveScore["teamAName"] as? String ?: teamA["name"] as? String ?: "",
            teamBName = liveScore["teamBName"] as? String ?: teamB["name"] as? String ?: "",
            teamAShortName = liveScore["teamAShortName"] as? String ?: teamA["shortName"] as? String ?: "",
            teamBShortName = liveScore["teamBShortName"] as? String ?: teamB["shortName"] as? String ?: "",
            teamALogoUrl = (teamA["logoUrl"] as? String)?.takeIf(String::isNotBlank)
                ?: (teamA["teamLogoUrl"] as? String)?.takeIf(String::isNotBlank)
                ?: (liveScore["teamALogoUrl"] as? String).orEmpty(),
            teamBLogoUrl = (teamB["logoUrl"] as? String)?.takeIf(String::isNotBlank)
                ?: (teamB["teamLogoUrl"] as? String)?.takeIf(String::isNotBlank)
                ?: (liveScore["teamBLogoUrl"] as? String).orEmpty(),
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
            strikerFours = strikerFours,
            strikerSixes = strikerSixes,
            nonStrikerName = nonStrikerName,
            nonStrikerRuns = nonStrikerRuns,
            nonStrikerBalls = nonStrikerBalls,
            nonStrikerFours = nonStrikerFours,
            nonStrikerSixes = nonStrikerSixes,
            strikerPlayerId = liveScore["strikerPlayerId"] as? String,
            nonStrikerPlayerId = liveScore["nonStrikerPlayerId"] as? String,
            bowlerName = bowlerName,
            bowlerOvers = bowlerOvers,
            bowlerRuns = bowlerRuns,
            bowlerWickets = bowlerWickets,
            matchStatusNote = matchStatusNote,
            updatedAtEpochMs = updatedAtEpochMs,
            tournamentStage = listOf("roundName", "selectedStage", "stageName")
                .firstNotNullOfOrNull { getString(it)?.trim()?.takeIf(String::isNotBlank) }.orEmpty(),
            firstInningsScore = firstInningsScore?.toInt(),
            firstInningsWickets = firstInningsWickets?.toInt()
        ).toDomain()
    }

    private companion object {
        const val MATCHES_COLLECTION = "matches"
    }
}
