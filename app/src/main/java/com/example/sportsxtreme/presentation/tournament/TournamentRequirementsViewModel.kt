package com.example.sportsxtreme.presentation.tournament

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.Tournament
import com.example.sportsxtreme.domain.model.TournamentRequirements
import com.example.sportsxtreme.domain.repository.TournamentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TournamentRequirementsViewModel @Inject constructor(
    private val repository: TournamentRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun save(tournament: Tournament, requirements: TournamentRequirements) {
        if (tournament.id.isBlank()) {
            _uiState.value = UiState.Error("Tournament ID is missing. Please create the tournament again.")
            return
        }
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val result = repository.updateTournamentRequirements(tournament.id, requirements)) {
                is Resource.Success -> _uiState.value = UiState.Saved(tournament)
                is Resource.Error -> _uiState.value = UiState.Error(result.message ?: "Unable to save requirements")
                is Resource.Loading -> _uiState.value = UiState.Loading
            }
        }
    }

    fun reset() { _uiState.value = UiState.Idle }

    sealed class UiState {
        data object Idle : UiState()
        data object Loading : UiState()
        data class Saved(val tournament: Tournament) : UiState()
        data class Error(val message: String) : UiState()
    }
}
