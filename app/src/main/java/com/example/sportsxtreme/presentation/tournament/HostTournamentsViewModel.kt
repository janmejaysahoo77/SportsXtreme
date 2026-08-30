package com.example.sportsxtreme.presentation.tournament

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.Tournament
import com.example.sportsxtreme.domain.repository.TournamentRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HostTournamentsViewModel @Inject constructor(
    private val repository: TournamentRepository,
    firebaseAuth: FirebaseAuth
) : ViewModel() {
    sealed class UiState {
        object Loading : UiState()
        data class Content(val tournaments: List<Tournament>) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        val hostUid = firebaseAuth.currentUser?.uid
        if (hostUid.isNullOrBlank()) {
            _uiState.value = UiState.Content(emptyList())
        } else {
            viewModelScope.launch {
                repository.observeHostTournaments(hostUid).collect { result ->
                    _uiState.value = when (result) {
                        is Resource.Success -> UiState.Content(result.data.orEmpty())
                        is Resource.Error -> UiState.Error(result.message ?: "Unable to load your tournaments")
                        is Resource.Loading -> UiState.Loading
                    }
                }
            }
        }
    }
}
