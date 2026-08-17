package com.example.sportsxtreme.data.repository

import android.content.Context
import androidx.core.content.edit
import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.CreatedMatchInvite
import com.example.sportsxtreme.domain.model.MatchInvite
import com.example.sportsxtreme.domain.model.MatchInviteStatus
import com.example.sportsxtreme.domain.model.TeamSide
import com.example.sportsxtreme.domain.repository.MatchInviteRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.QuerySnapshot
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

@Singleton
class MatchInviteRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    @ApplicationContext context: Context
) : MatchInviteRepository {
    private val tokenPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override suspend fun createOrGetOpenInvite(matchId: String, teamSlot: TeamSide): Resource<CreatedMatchInvite> = runCatching {
        require(matchId.isNotBlank()) { "Match id is missing" }
        val organiserId = requireNotNull(auth.currentUser?.uid) { "Sign in before creating an invitation" }
        val inviteId = "$matchId-${teamSlot.name.lowercase()}"
        val rawToken = newToken()
        val now = System.currentTimeMillis()
        val invite = MatchInvite(
            inviteId = inviteId,
            matchId = matchId,
            teamSlot = teamSlot,
            organiserId = organiserId,
            tokenHash = sha256(rawToken),
            status = MatchInviteStatus.OPEN,
            createdAtEpochMs = now,
            expiresAtEpochMs = now + INVITE_TTL_MILLIS
        )
        when (createInvite(invite)) {
            InviteCreateResult.CREATED -> {
                tokenPreferences.edit { putString(tokenPreferenceKey(inviteId), rawToken) }
                Resource.Success(CreatedMatchInvite(invite, invitationUrl(rawToken)))
            }
            InviteCreateResult.ALREADY_EXISTS -> {
                val existing = requireNotNull(getInvite(inviteId)) { "Existing invitation could not be loaded" }
                require(existing.status == MatchInviteStatus.OPEN && existing.expiresAtEpochMs > System.currentTimeMillis()) {
                    "The existing invitation is no longer open"
                }
                val existingToken = requireNotNull(tokenPreferences.getString(tokenPreferenceKey(inviteId), null)) {
                    "This invite was created on another device and cannot be re-shared securely"
                }
                Resource.Success(CreatedMatchInvite(existing, invitationUrl(existingToken)))
            }
        }
    }.getOrElse { Resource.Error(it.message ?: "Unable to create invitation") }

    /** Temporary Spark-plan implementation; callers remain independent of the transport. */
    override suspend fun claimInvite(rawToken: String): Resource<com.example.sportsxtreme.domain.model.ClaimedMatchInvite> = runCatching {
        require(rawToken.isNotBlank()) { "Invalid invitation" }
        val uid = requireNotNull(auth.currentUser?.uid) { "Sign in before accepting an invitation" }
        val tokenHash = sha256(rawToken)
        val inviteSnapshot: QuerySnapshot = awaitTask {
            firestore.collection(INVITES_COLLECTION).whereEqualTo("tokenHash", tokenHash).limit(2).get()
        }
        require(inviteSnapshot.size() == 1) { "Invalid invitation" }
        val inviteRef = inviteSnapshot.documents.single().reference
        val result: com.example.sportsxtreme.domain.model.ClaimedMatchInvite = awaitTask {
            firestore.runTransaction { transaction ->
                val invite = transaction.get(inviteRef)
                require(invite.exists()) { "Invalid invitation" }
                require(invite.getString("status") == MatchInviteStatus.OPEN.name) { "Invitation already claimed" }
                require((invite.getTimestamp("expiresAt")?.toDate()?.time ?: 0L) > System.currentTimeMillis()) { "Invitation expired" }
                val matchId = invite.getString("matchId").orEmpty()
                val teamSlot = TeamSide.valueOf(invite.getString("teamSlot").orEmpty())
                val matchRef = firestore.collection(MATCHES_COLLECTION).document(matchId)
                val match = transaction.get(matchRef)
                require(match.exists()) { "Match not found" }
                val teamAClaim = match.get("teamAClaim") as? Map<*, *>
                val teamBClaim = match.get("teamBClaim") as? Map<*, *>
                require(teamAClaim?.get("userId") != uid && teamBClaim?.get("userId") != uid) { "User already joined this match" }
                val claimField = if (teamSlot == TeamSide.TEAM_A) "teamAClaim" else "teamBClaim"
                require(match.get(claimField) == null) { "Team slot already claimed" }
                val profile = transaction.get(firestore.collection(USERS_COLLECTION).document(uid))
                val name = profile.getString("name")?.trim().takeUnless { it.isNullOrBlank() } ?: "SportsXtreme Player"
                val replacementId = if (teamSlot == TeamSide.TEAM_A) "dA1" else "dB1"
                transaction.update(matchRef, mapOf(
                    claimField to mapOf("userId" to uid, "displayName" to name, "replacedDummyPlayerId" to replacementId, "claimedAt" to FieldValue.serverTimestamp()),
                    "updatedAtEpochMs" to System.currentTimeMillis()
                ))
                transaction.update(inviteRef, mapOf("status" to MatchInviteStatus.CLAIMED.name, "claimedByUserId" to uid, "claimedAt" to FieldValue.serverTimestamp()))
                com.example.sportsxtreme.domain.model.ClaimedMatchInvite(matchId, teamSlot)
            }
        }
        result
    }.fold(onSuccess = { Resource.Success(it) }, onFailure = { Resource.Error(it.message ?: "Unable to claim invitation") })

    private suspend fun <T> awaitTask(factory: () -> com.google.android.gms.tasks.Task<T>): T = suspendCancellableCoroutine { continuation ->
        factory().addOnSuccessListener { value ->
            if (continuation.isActive) continuation.resumeWith(Result.success(value))
        }.addOnFailureListener { error ->
            if (continuation.isActive) continuation.resumeWith(Result.failure(error))
        }
    }

    private suspend fun getInvite(inviteId: String): MatchInvite? = suspendCancellableCoroutine { continuation ->
        firestore.collection(INVITES_COLLECTION).document(inviteId).get()
            .addOnSuccessListener { snapshot ->
                continuation.resume(if (snapshot.exists()) snapshot.toInvite() else null)
            }
            .addOnFailureListener { continuation.resumeWith(Result.failure(it)) }
    }

    private suspend fun createInvite(invite: MatchInvite) = suspendCancellableCoroutine<InviteCreateResult> { continuation ->
        firestore.collection(INVITES_COLLECTION).document(invite.inviteId)
            .set(invite.toPayload())
            .addOnSuccessListener { continuation.resume(InviteCreateResult.CREATED) }
            .addOnFailureListener { error ->
                // The Phase 3A rules allow create but reject updates. A denied
                // write can therefore mean this deterministic slot already exists.
                if (error is FirebaseFirestoreException && error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                    continuation.resume(InviteCreateResult.ALREADY_EXISTS)
                } else {
                    continuation.resumeWith(Result.failure(error))
                }
            }
    }

    private fun MatchInvite.toPayload(): Map<String, Any?> = mapOf(
        "inviteId" to inviteId,
        "matchId" to matchId,
        "teamSlot" to teamSlot.name,
        "organiserId" to organiserId,
        "tokenHash" to tokenHash,
        "status" to status.name,
        "createdAt" to Timestamp(createdAtEpochMs / 1000, ((createdAtEpochMs % 1000) * 1_000_000).toInt()),
        "expiresAt" to Timestamp(expiresAtEpochMs / 1000, ((expiresAtEpochMs % 1000) * 1_000_000).toInt()),
        "claimedByUserId" to null,
        "claimedAt" to null
    )

    private fun com.google.firebase.firestore.DocumentSnapshot.toInvite() = MatchInvite(
        inviteId = getString("inviteId").orEmpty().ifBlank { id },
        matchId = getString("matchId").orEmpty(),
        teamSlot = TeamSide.valueOf(getString("teamSlot").orEmpty()),
        organiserId = getString("organiserId").orEmpty(),
        tokenHash = getString("tokenHash").orEmpty(),
        status = MatchInviteStatus.valueOf(getString("status").orEmpty()),
        createdAtEpochMs = getTimestamp("createdAt")?.toDate()?.time ?: 0L,
        expiresAtEpochMs = getTimestamp("expiresAt")?.toDate()?.time ?: 0L,
        claimedByUserId = getString("claimedByUserId"),
        claimedAtEpochMs = getTimestamp("claimedAt")?.toDate()?.time
    )

    private fun newToken(): String = ByteArray(TOKEN_BYTES).also(secureRandom::nextBytes).let { bytes ->
        Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }

    private fun invitationUrl(token: String) = "$INVITE_URL_PREFIX$token"
    private fun tokenPreferenceKey(inviteId: String) = "raw_token_$inviteId"

    private companion object {
        const val INVITES_COLLECTION = "matchInvites"
        const val MATCHES_COLLECTION = "matches"
        const val USERS_COLLECTION = "users"
        const val PREFERENCES_NAME = "match_invite_tokens"
        const val INVITE_URL_PREFIX = "https://sportsxtreme-95fbb.web.app/join?invite="
        const val TOKEN_BYTES = 32
        const val INVITE_TTL_MILLIS = 24 * 60 * 60 * 1000L
        val secureRandom = SecureRandom()
    }

    private enum class InviteCreateResult { CREATED, ALREADY_EXISTS }
}
