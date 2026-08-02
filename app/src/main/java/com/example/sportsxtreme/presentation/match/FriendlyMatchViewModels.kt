package com.example.sportsxtreme.presentation.match

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.Match
import com.example.sportsxtreme.domain.model.MatchType
import com.example.sportsxtreme.domain.model.Player
import com.example.sportsxtreme.domain.model.TossDecision
import com.example.sportsxtreme.domain.repository.TeamRepository
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

data class SelectedTeam(
    val id: String,
    val name: String
)

data class TeamSelectionUiState(
    val isLoading: Boolean = true,
    val match: Match? = null,
    val selectedTeamA: SelectedTeam? = null,
    val selectedTeamB: SelectedTeam? = null,
    val errorMessage: String? = null
)

class TeamSelectionViewModel(
    matchId: String,
    private val matchUseCases: MatchUseCases,
    initialTeamA: SelectedTeam?,
    initialTeamB: SelectedTeam?
) : ViewModel() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _uiState = MutableStateFlow(
        TeamSelectionUiState(
            selectedTeamA = initialTeamA,
            selectedTeamB = initialTeamB
        )
    )

    val uiState: StateFlow<TeamSelectionUiState> = _uiState.asStateFlow()

    init {
        if (matchId.isBlank()) {
            _uiState.value = TeamSelectionUiState(isLoading = false, errorMessage = "Match id is missing")
        } else {
            scope.launch {
                matchUseCases.observeMatch(matchId).collect { result ->
                    _uiState.value = when (result) {
                        is Resource.Success -> _uiState.value.copy(
                            isLoading = false,
                            match = result.data,
                            errorMessage = null
                        )
                        is Resource.Error -> _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message ?: "Unable to load match"
                        )
                        is Resource.Loading -> _uiState.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    override fun onCleared() {
        scope.cancel()
    }

    fun setSelectedTeamA(team: SelectedTeam) {
        _uiState.value = _uiState.value.copy(selectedTeamA = team)
    }

    fun setSelectedTeamB(team: SelectedTeam) {
        _uiState.value = _uiState.value.copy(selectedTeamB = team)
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
        fun factory(
            matchId: String,
            matchUseCases: MatchUseCases,
            initialTeamA: SelectedTeam?,
            initialTeamB: SelectedTeam?
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    require(modelClass.isAssignableFrom(TeamSelectionViewModel::class.java))
                    return TeamSelectionViewModel(matchId, matchUseCases, initialTeamA, initialTeamB) as T
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

data class TossUiState(
    val isLoading: Boolean = true,
    val match: Match? = null,
    val errorMessage: String? = null,
    val selectedWinnerId: String? = null,
    val selectedDecision: com.example.sportsxtreme.domain.model.TossDecision? = null
)

class TossViewModel(
    matchId: String,
    private val matchUseCases: MatchUseCases
) : ViewModel() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _uiState = MutableStateFlow(TossUiState())

    val uiState: StateFlow<TossUiState> = _uiState.asStateFlow()

    init {
        if (matchId.isBlank()) {
            _uiState.value = TossUiState(isLoading = false, errorMessage = "Match id is missing")
        } else {
            scope.launch {
                matchUseCases.observeMatch(matchId).collect { result ->
                    _uiState.value = when (result) {
                        is Resource.Success -> _uiState.value.copy(isLoading = false, match = result.data)
                        is Resource.Error -> _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message ?: "Unable to load match"
                        )
                        is Resource.Loading -> _uiState.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    override fun onCleared() {
        scope.cancel()
    }

    fun selectWinner(teamId: String) {
        _uiState.value = _uiState.value.copy(selectedWinnerId = teamId)
    }

    fun selectDecision(decision: com.example.sportsxtreme.domain.model.TossDecision) {
        _uiState.value = _uiState.value.copy(selectedDecision = decision)
    }

    fun saveToss(
        matchId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val winnerId = _uiState.value.selectedWinnerId
        val decision = _uiState.value.selectedDecision
        if (winnerId.isNullOrBlank() || decision == null) {
            onError("Please select toss winner and their decision")
            return
        }
        scope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val toss = com.example.sportsxtreme.domain.model.Toss(
                winnerTeamId = winnerId,
                decision = decision,
                completedByUserId = "current_user", // Simplification
                completedAtEpochMs = System.currentTimeMillis()
            )
            when (val result = matchUseCases.saveToss(matchId, toss)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, match = result.data)
                    onSuccess()
                }
                is Resource.Error -> {
                    val msg = result.message ?: "Failed to save toss"
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
                    require(modelClass.isAssignableFrom(TossViewModel::class.java))
                    return TossViewModel(matchId, matchUseCases) as T
                }
            }
    }
}

data class OpeningPlayersUiState(
    val isLoading: Boolean = true,
    val match: Match? = null,
    val battingTeamPlayers: List<Player> = emptyList(),
    val bowlingTeamPlayers: List<Player> = emptyList(),
    val selectedStrikerId: String? = null,
    val selectedNonStrikerId: String? = null,
    val selectedBowlerId: String? = null,
    val errorMessage: String? = null
) {
    val battingTeamName: String get() {
        val match = match ?: return ""
        val toss = match.toss ?: return match.teamA.name
        return if (toss.decision == TossDecision.BAT) {
            if (toss.winnerTeamId == match.teamA.teamId) match.teamA.name else match.teamB.name
        } else {
            if (toss.winnerTeamId == match.teamA.teamId) match.teamB.name else match.teamA.name
        }
    }
    val bowlingTeamName: String get() {
        val match = match ?: return ""
        val toss = match.toss ?: return match.teamB.name
        return if (toss.decision == TossDecision.FIELD) {
            if (toss.winnerTeamId == match.teamA.teamId) match.teamA.name else match.teamB.name
        } else {
            if (toss.winnerTeamId == match.teamA.teamId) match.teamB.name else match.teamA.name
        }
    }
}

class OpeningPlayersViewModel(
    matchId: String,
    private val matchUseCases: MatchUseCases,
    private val teamRepository: TeamRepository
) : ViewModel() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _uiState = MutableStateFlow(OpeningPlayersUiState())

    val uiState: StateFlow<OpeningPlayersUiState> = _uiState.asStateFlow()

    init {
        if (matchId.isBlank()) {
            _uiState.value = OpeningPlayersUiState(isLoading = false, errorMessage = "Match id is missing")
        } else {
            scope.launch {
                matchUseCases.observeMatch(matchId).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            val match = result.data!!
                            _uiState.value = _uiState.value.copy(isLoading = false, match = match)
                            loadPlayersForTeams(match)
                        }
                        is Resource.Error -> _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message ?: "Unable to load match"
                        )
                        is Resource.Loading -> _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    private fun loadPlayersForTeams(match: Match) {
        val toss = match.toss ?: return
        val battingTeamId = if (toss.decision == TossDecision.BAT) {
            toss.winnerTeamId
        } else {
            if (toss.winnerTeamId == match.teamA.teamId) match.teamB.teamId else match.teamA.teamId
        }
        val bowlingTeamId = if (battingTeamId == match.teamA.teamId) match.teamB.teamId else match.teamA.teamId

        scope.launch(Dispatchers.IO) {
            val battingResult = teamRepository.getTeam(battingTeamId)
            val bowlingResult = teamRepository.getTeam(bowlingTeamId)
            val battingPlayers = (battingResult as? Resource.Success)?.data?.players ?: emptyList()
            val bowlingPlayers = (bowlingResult as? Resource.Success)?.data?.players ?: emptyList()
            _uiState.value = _uiState.value.copy(
                battingTeamPlayers = battingPlayers,
                bowlingTeamPlayers = bowlingPlayers
            )
        }
    }

    override fun onCleared() {
        scope.cancel()
    }

    fun selectStriker(playerId: String) {
        _uiState.value = _uiState.value.copy(selectedStrikerId = playerId)
    }

    fun selectNonStriker(playerId: String) {
        _uiState.value = _uiState.value.copy(selectedNonStrikerId = playerId)
    }

    fun selectBowler(playerId: String) {
        _uiState.value = _uiState.value.copy(selectedBowlerId = playerId)
    }

    fun saveOpeningPlayers(
        matchId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val strikerId = _uiState.value.selectedStrikerId
        val nonStrikerId = _uiState.value.selectedNonStrikerId
        val bowlerId = _uiState.value.selectedBowlerId

        if (strikerId.isNullOrBlank()) { onError("Please select a striker"); return }
        if (nonStrikerId.isNullOrBlank()) { onError("Please select a non-striker"); return }
        if (bowlerId.isNullOrBlank()) { onError("Please select a bowler"); return }
        if (strikerId == nonStrikerId) { onError("Striker and Non-Striker must be different players"); return }

        scope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = matchUseCases.selectOpeningPlayers(matchId, strikerId, nonStrikerId, bowlerId)) {
                is Resource.Success -> {
                    when (val startResult = matchUseCases.startMatch(matchId)) {
                        is Resource.Success -> {
                            _uiState.value = _uiState.value.copy(isLoading = false)
                            onSuccess()
                        }
                        is Resource.Error -> {
                            val msg = startResult.message ?: "Failed to start match"
                            _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = msg)
                            onError(msg)
                        }
                        is Resource.Loading -> Unit
                    }
                }
                is Resource.Error -> {
                    val msg = result.message ?: "Failed to save opening players"
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = msg)
                    onError(msg)
                }
                is Resource.Loading -> Unit
            }
        }
    }

    companion object {
        fun factory(matchId: String, matchUseCases: MatchUseCases, teamRepository: TeamRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    require(modelClass.isAssignableFrom(OpeningPlayersViewModel::class.java))
                    return OpeningPlayersViewModel(matchId, matchUseCases, teamRepository) as T
                }
            }
    }
}
