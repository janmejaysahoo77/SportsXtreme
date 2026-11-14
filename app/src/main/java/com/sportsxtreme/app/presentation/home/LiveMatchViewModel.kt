package com.sportsxtreme.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sportsxtreme.app.common.Resource
import com.sportsxtreme.app.domain.model.LiveMatch
import com.sportsxtreme.app.domain.repository.LiveMatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

/**
 * UI state for the live matches section of the Home Screen.
 */
data class LiveMatchesUiState(
    val isLoading: Boolean = true,
    val isOffline: Boolean = false,
    val errorMessage: String? = null,
    val matches: List<LiveMatch> = emptyList(),
    val recentMatches: List<LiveMatch> = emptyList()
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
            launch {
                delay(8_000L)
                _uiState.update { state ->
                    if (state.isLoading) state.copy(
                        isLoading = false,
                        errorMessage = "Live matches are taking too long to load"
                    ) else state
                }
            }
            liveMatchRepository.observeLiveMatches().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val matches = resource.data.orEmpty()
                        val liveMatches = matches
                            .filter { match -> match.isActivelyPlaying }
                            .sortedByDescending { match -> match.updatedAtEpochMs }
                        val recentMatches = matches
                            .filter { match ->
                                match.isCompleted || match.isAbandoned || match.isPaused ||
                                    match.status == com.sportsxtreme.app.domain.model.MatchStatus.INNINGS_BREAK
                            }
                            .sortedByDescending { match -> match.updatedAtEpochMs }
                            .take(5)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isOffline = false,
                                errorMessage = null,
                                matches = liveMatches,
                                recentMatches = recentMatches
                            )
                        }
                    }

                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }

                    is Resource.Error -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            // Keep showing cached matches while offline.
                            isOffline = it.matches.isNotEmpty() || it.recentMatches.isNotEmpty(),
                            errorMessage = resource.message
                        )
                    }
                }
            }
        }
    }
}
