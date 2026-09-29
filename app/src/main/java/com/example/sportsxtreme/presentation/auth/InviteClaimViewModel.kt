package com.example.sportsxtreme.presentation.auth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.repository.MatchInviteRepository
import com.example.sportsxtreme.domain.model.TeamSide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@HiltViewModel
class InviteClaimViewModel @Inject constructor(
    private val repository: MatchInviteRepository,
    private val functions: FirebaseFunctions,
    private val firestore: FirebaseFirestore,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    fun resolveMatchInvite(token: String, onFinished: (Resource<ResolvedMatchInvitation>) -> Unit) {
        viewModelScope.launch {
            val result = runCatching {
                val response = awaitCallable("resolveMatchInvite", mapOf("token" to token)) as? Map<*, *>
                    ?: error("Invalid match invitation response")
                ResolvedMatchInvitation(
                    matchId = response["matchId"] as? String ?: error("Match id is missing"),
                    teamSide = TeamSide.valueOf(response["teamSlot"] as? String ?: error("Team slot is missing"))
                )
            }.fold(
                onSuccess = { Resource.Success(it) },
                onFailure = { Resource.Error(it.message ?: "Unable to open match invitation") }
            )
            onFinished(result)
        }
    }

    fun claim(token: String, onFinished: (Resource<com.example.sportsxtreme.domain.model.ClaimedMatchInvite>) -> Unit) {
        if (savedStateHandle.get<String>("claiming_token") == token) return
        savedStateHandle["claiming_token"] = token
        viewModelScope.launch {
            val result = repository.claimInvite(token)
            savedStateHandle["claiming_token"] = null
            onFinished(result)
        }
    }

    fun claimTeamInvite(token: String, onFinished: (Resource<TeamInviteJoin>) -> Unit) {
        if (savedStateHandle.get<String>("claiming_team_token") == token) return
        savedStateHandle["claiming_team_token"] = token
        viewModelScope.launch {
            val result = runCatching {
                val response = awaitCallable("joinTeamInvite", mapOf("token" to token))
                val data = response as? Map<*, *> ?: error("Invalid invitation response")
                val teamId = data["teamId"] as? String ?: error("Invalid invitation response")
                val teamName = (data["teamName"] as? String)
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }
                    ?: fetchTeamName(teamId)
                TeamInviteJoin(
                    teamId = teamId,
                    teamName = teamName.ifBlank { "the team" },
                    alreadyMember = data["alreadyMember"] as? Boolean ?: false
                )
            }.fold(
                onSuccess = { Resource.Success(it) },
                onFailure = { Resource.Error(it.message ?: "Unable to join team") }
            )
            savedStateHandle["claiming_team_token"] = null
            onFinished(result)
        }
    }

    private suspend fun awaitCallable(name: String, payload: Map<String, String>): Any? =
        suspendCancellableCoroutine { continuation ->
            functions.getHttpsCallable(name).call(payload)
                .addOnSuccessListener { result ->
                    if (continuation.isActive) continuation.resume(result.data)
                }
                .addOnFailureListener { error ->
                    if (continuation.isActive) continuation.resumeWith(Result.failure(error))
                }
        }

    private suspend fun fetchTeamName(teamId: String): String =
        suspendCancellableCoroutine { continuation ->
            firestore.collection(TEAMS_COLLECTION).document(teamId).get()
                .addOnSuccessListener { team ->
                    val name = team.getString("teamName")
                        ?.trim()
                        ?.takeIf { it.isNotEmpty() }
                        ?: team.getString("name")
                            ?.trim()
                            ?.takeIf { it.isNotEmpty() }
                        ?: "the team"
                    if (continuation.isActive) continuation.resume(name)
                }
                .addOnFailureListener {
                    if (continuation.isActive) continuation.resume("the team")
                }
        }

    private companion object {
        const val TEAMS_COLLECTION = "teams"
    }
}

data class ResolvedMatchInvitation(val matchId: String, val teamSide: TeamSide)

data class TeamInviteJoin(
    val teamId: String,
    val teamName: String,
    val alreadyMember: Boolean
)
