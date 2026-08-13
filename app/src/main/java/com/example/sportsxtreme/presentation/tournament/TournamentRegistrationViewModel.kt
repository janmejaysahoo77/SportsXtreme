package com.example.sportsxtreme.presentation.tournament

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.Tournament
import com.example.sportsxtreme.domain.usecase.CreateTournamentUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TournamentRegistrationViewModel @Inject constructor(
    private val createTournamentUseCase: CreateTournamentUseCase,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _tournamentState = MutableStateFlow(Tournament())
    val tournamentState: StateFlow<Tournament> = _tournamentState.asStateFlow()

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        // Set hostUid when initialized
        firebaseAuth.currentUser?.let { user ->
            _tournamentState.value = _tournamentState.value.copy(
                hostUid = user.uid,
                email = user.email ?: "",
                phone = user.phoneNumber ?: ""
            )
        }
    }

    fun updateField(field: Field, value: String) {
        val current = _tournamentState.value
        _tournamentState.value = when (field) {
            Field.NAME -> current.copy(name = value)
            Field.CITY -> current.copy(city = value)
            Field.GROUND -> current.copy(ground = value)
            Field.ORGANIZER_NAME -> current.copy(organizerName = value)
            Field.PHONE -> current.copy(phone = value)
            Field.EMAIL -> current.copy(email = value)
            Field.START_DATE -> current.copy(startDate = value)
        }
    }

    fun updateMode(type: String) {
        _tournamentState.value = _tournamentState.value.copy(type = type)
    }

    fun updateBallType(ballType: String) {
        _tournamentState.value = _tournamentState.value.copy(ballType = ballType)
    }

    fun updateMatchForm(matchForm: String) {
        _tournamentState.value = _tournamentState.value.copy(matchForm = matchForm)
    }

    fun updateLookingForTeams(looking: Boolean) {
        _tournamentState.value = _tournamentState.value.copy(lookingForTeams = looking)
    }

    fun saveTournament() {
        if (_tournamentState.value.name.isBlank()) {
            _uiState.value = UiState.Error("Tournament Name is required")
            return
        }
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            when (val result = createTournamentUseCase(_tournamentState.value)) {
                is Resource.Success -> {
                    _tournamentState.value = result.data ?: _tournamentState.value
                    _uiState.value = UiState.Success
                }
                is Resource.Error -> {
                    _uiState.value = UiState.Error(result.message ?: "Failed to create tournament")
                }
                is Resource.Loading -> {
                    _uiState.value = UiState.Loading
                }
            }
        }
    }

    fun resetUiState() {
        _uiState.value = UiState.Idle
    }

    enum class Field {
        NAME, CITY, GROUND, ORGANIZER_NAME, PHONE, EMAIL, START_DATE
    }

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        object Success : UiState()
        data class Error(val message: String) : UiState()
    }
}
