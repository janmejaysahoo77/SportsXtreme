package com.example.sportsxtreme.data.remote.firestore

import com.example.sportsxtreme.domain.model.MatchSlotClaim
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

data class RemoteMatchClaims(val teamA: MatchSlotClaim? = null, val teamB: MatchSlotClaim? = null)

@Singleton
class FirebaseFirestoreMatchClaimsDataSource @Inject constructor(private val firestore: FirebaseFirestore) {
    fun observe(matchId: String): Flow<RemoteMatchClaims> = callbackFlow {
        trySend(RemoteMatchClaims())
        val registration = firestore.collection("matches").document(matchId).addSnapshotListener { snapshot, _ ->
            val claim: (String) -> MatchSlotClaim? = { field ->
                @Suppress("UNCHECKED_CAST") val map = snapshot?.get(field) as? Map<String, Any?>
                map?.let { MatchSlotClaim(it["userId"] as? String ?: return@let null, it["displayName"] as? String ?: return@let null, it["replacedDummyPlayerId"] as? String ?: "") }
            }
            trySend(RemoteMatchClaims(claim("teamAClaim"), claim("teamBClaim")))
        }
        awaitClose { registration.remove() }
    }
}
