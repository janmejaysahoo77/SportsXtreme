package com.example.sportsxtreme.presentation.tournament

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TeamInviteUiModel(
    val teamId: String,
    val teamName: String,
    val memberCount: Int,
    val isCaptain: Boolean,
    val avatarLetter: String,
    val avatarGradient: List<Color>
)

sealed interface TournamentTeamUiState {
    object Loading : TournamentTeamUiState
    data class Success(val teams: List<TeamInviteUiModel>) : TournamentTeamUiState
    data class Error(val message: String) : TournamentTeamUiState
}

@HiltViewModel
class TournamentTeamInviteViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val functions: FirebaseFunctions
) : ViewModel() {

    private val _uiState = MutableStateFlow<TournamentTeamUiState>(TournamentTeamUiState.Loading)
    val uiState: StateFlow<TournamentTeamUiState> = _uiState.asStateFlow()

    private val _registrationState = MutableStateFlow<RegistrationState>(RegistrationState.Idle)
    val registrationState: StateFlow<RegistrationState> = _registrationState.asStateFlow()

    fun loadTeams(uid: String) {
        // Use snapshot listener to get cached data first, then live updates
        firestore.collection("teams")
            .whereArrayContains("memberIds", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.value = TournamentTeamUiState.Error(error.message ?: "Failed to load teams")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    viewModelScope.launch(Dispatchers.Default) {
                        val parsedTeams = snapshot.documents.mapNotNull { doc ->
                            val members = doc.get("members") as? List<*> ?: emptyList<Any>()
                            val mine = members.filterIsInstance<Map<*, *>>().firstOrNull { it["userId"] == uid }
                            val roles = mine?.get("roles") as? List<*> ?: emptyList<Any>()
                            
                            val isManager = roles.any { it == "CAPTAIN" || it == "ADMIN" }
                            if (isManager) {
                                val name = doc.getString("teamName") ?: doc.getString("name") ?: "Team"
                                val firstLetter = name.firstOrNull()?.uppercase() ?: "T"
                                val gradient = generateGradientForTeam(doc.id)
                                
                                TeamInviteUiModel(
                                    teamId = doc.id,
                                    teamName = name,
                                    memberCount = members.size,
                                    isCaptain = true,
                                    avatarLetter = firstLetter,
                                    avatarGradient = gradient
                                )
                            } else {
                                null
                            }
                        }.sortedBy { it.teamName }
                        
                        _uiState.value = TournamentTeamUiState.Success(parsedTeams)
                    }
                }
            }
    }

    fun registerTeam(token: String, teamId: String, teamName: String) {
        _registrationState.value = RegistrationState.Loading(teamId)
        
        functions.getHttpsCallable("joinTournamentInvite")
            .call(mapOf("token" to token, "teamId" to teamId))
            .addOnSuccessListener {
                _registrationState.value = RegistrationState.Success(teamName)
            }
            .addOnFailureListener { error ->
                _registrationState.value = RegistrationState.Error(error.message ?: "Unable to register team")
            }
    }

    fun resetRegistrationState() {
        _registrationState.value = RegistrationState.Idle
    }

    private fun generateGradientForTeam(teamId: String): List<Color> {
        val hash = teamId.hashCode().times(2654435761).toUInt().toInt() // Knuth's multiplicative hash
        val hue1 = Math.abs(hash % 360).toFloat()
        val hue2 = (hue1 + 40 + Math.abs((hash shr 8) % 60)) % 360
        
        return listOf(
            Color.hsv(hue1, 0.8f, 0.9f),
            Color.hsv(hue2, 0.9f, 0.7f)
        )
    }
}

sealed interface RegistrationState {
    object Idle : RegistrationState
    data class Loading(val teamId: String) : RegistrationState
    data class Success(val teamName: String) : RegistrationState
    data class Error(val message: String) : RegistrationState
}
