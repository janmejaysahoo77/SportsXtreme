package com.example.sportsxtreme.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.LiveMatch
import com.example.sportsxtreme.domain.repository.LiveMatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for the live matches section of the Home Screen.
 */
data class LiveMatchesUiState(
    val isLoading: Boolean = true,
    val isOffline: Boolean = false,
    val errorMessage: String? = null,
    val matches: List<LiveMatch> = emptyList()
)

@HiltViewModel
class LiveMatchViewModel @Inject constructor(
    private val liveMatchRepository: LiveMatchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LiveMatchesUiState())
    val uiState: StateFlow<LiveMatchesUiState> = _uiState.asStateFlow()

    init {
        observeLiveMatches()
    }

    /**
     * Collects the repository flow which combines the Room cache (instant
     * offline render) with the Firestore SnapshotListener (real-time updates).
     */
    private fun observeLiveMatches() {
        viewModelScope.launch {
            liveMatchRepository.observeLiveMatches().collect { resource ->
                when (resource) {
                    is Resource.Success -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            isOffline = false,
                            errorMessage = null,
                            matches = resource.data.orEmpty().sortedByDescending { match -> match.updatedAtEpochMs }
                        )
                    }

                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }

                    is Resource.Error -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            // Keep showing cached matches while offline.
                            isOffline = it.matches.isNotEmpty(),
                            errorMessage = resource.message
                        )
                    }
                }
            }
        }
    }
}