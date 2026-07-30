package com.example.sportsxtreme.presentation.match

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.Match
import com.example.sportsxtreme.domain.model.MatchType
import com.example.sportsxtreme.domain.usecase.MatchUseCases
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StartMatchUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface StartMatchEvent {
    data object NavigateToFriendlySetup : StartMatchEvent
    data class ShowMessage(val message: String) : StartMatchEvent
}

class StartMatchViewModel(
    private val matchUseCases: MatchUseCases
) : ViewModel() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _uiState = MutableStateFlow(StartMatchUiState())
    private val _events = MutableSharedFlow<StartMatchEvent>()

    val uiState: StateFlow<StartMatchUiState> = _uiState.asStateFlow()
    val events: SharedFlow<StartMatchEvent> = _events.asSharedFlow()

    fun continueWith(matchType: MatchType) {
        if (_uiState.value.isLoading) return
        if (matchType == MatchType.FRIENDLY) {
            scope.launch {
                _events.emit(StartMatchEvent.NavigateToFriendlySetup)
            }
            return
        }

        scope.launch {
            _uiState.value = _uiState.value.copy(errorMessage = "Coming Soon")
            _events.emit(StartMatchEvent.ShowMessage("Coming Soon"))
        }
    }

    override fun onCleared() {
        scope.cancel()
    }

    private suspend fun publishError(message: String) {
        _uiState.value = StartMatchUiState(errorMessage = message)
        _events.emit(StartMatchEvent.ShowMessage(message))
    }

    companion object {
        fun factory(matchUseCases: MatchUseCases): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    require(modelClass.isAssignableFrom(StartMatchViewModel::class.java))
                    return StartMatchViewModel(matchUseCases) as T
                }
            }
    }
}

data class TeamSelectionUiState(
    val isLoading: Boolean = true,
    val match: Match? = null,
    val errorMessage: String? = null
)

class TeamSelectionViewModel(
    matchId: String,
    private val matchUseCases: MatchUseCases
) : ViewModel() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _uiState = MutableStateFlow(TeamSelectionUiState())

    val uiState: StateFlow<TeamSelectionUiState> = _uiState.asStateFlow()

    init {
        if (matchId.isBlank()) {
            _uiState.value = TeamSelectionUiState(isLoading = false, errorMessage = "Match id is missing")
        } else {
            scope.launch {
                matchUseCases.observeMatch(matchId).collect { result ->
                    _uiState.value = when (result) {
                        is Resource.Success -> TeamSelectionUiState(match = result.data)
                        is Resource.Error -> TeamSelectionUiState(
                            isLoading = false,
                            errorMessage = result.message ?: "Unable to load match"
                        )
                        is Resource.Loading -> TeamSelectionUiState(isLoading = true)
                    }
                }
            }
        }
    }

    override fun onCleared() {
        scope.cancel()
    }

    fun updateMatchTeams(
        matchId: String,
        teamAId: String,
        teamBId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (teamAId.isBlank() || teamBId.isBlank()) {
            onError("Both Team A and Team B must be selected")
            return
        }
        if (teamAId == teamBId) {
            onError("Team A and Team B cannot be the same")
            return
        }
        scope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = matchUseCases.updateMatchTeams(matchId, teamAId, teamBId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, match = result.data)
                    onSuccess()
                }
                is Resource.Error -> {
                    val msg = result.message ?: "Failed to update match teams"
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = msg)
                    onError(msg)
                }
                is Resource.Loading -> Unit
            }
        }
    }

    companion object {
        fun factory(matchId: String, matchUseCases: MatchUseCases): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    require(modelClass.isAssignableFrom(TeamSelectionViewModel::class.java))
                    return TeamSelectionViewModel(matchId, matchUseCases) as T
                }
            }
    }
}

data class StartMatchPreviewUiState(
    val isLoading: Boolean = true,
    val match: Match? = null,
    val errorMessage: String? = null
)

class StartMatchPreviewViewModel(
    matchId: String,
    private val matchUseCases: MatchUseCases
) : ViewModel() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _uiState = MutableStateFlow(StartMatchPreviewUiState())

    val uiState: StateFlow<StartMatchPreviewUiState> = _uiState.asStateFlow()

    init {
        if (matchId.isBlank()) {
            _uiState.value = StartMatchPreviewUiState(isLoading = false, errorMessage = "Match id is missing")
        } else {
            scope.launch {
                matchUseCases.observeMatch(matchId).collect { result ->
                    _uiState.value = when (result) {
                        is Resource.Success -> StartMatchPreviewUiState(isLoading = false, match = result.data)
                        is Resource.Error -> StartMatchPreviewUiState(
                            isLoading = false,
                            errorMessage = result.message ?: "Unable to load match preview"
                        )
                        is Resource.Loading -> StartMatchPreviewUiState(isLoading = true)
                    }
                }
            }
        }
    }

    override fun onCleared() {
        scope.cancel()
    }

    companion object {
        fun factory(matchId: String, matchUseCases: MatchUseCases): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    require(modelClass.isAssignableFrom(StartMatchPreviewViewModel::class.java))
                    return StartMatchPreviewViewModel(matchId, matchUseCases) as T
                }
            }
    }
}
