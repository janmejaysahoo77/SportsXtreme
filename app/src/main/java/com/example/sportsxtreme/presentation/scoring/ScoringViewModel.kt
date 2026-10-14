package com.example.sportsxtreme.presentation.scoring

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.BallEvent
import com.example.sportsxtreme.domain.model.Dismissal
import com.example.sportsxtreme.domain.model.DismissalType
import com.example.sportsxtreme.domain.model.ExtraRun
import com.example.sportsxtreme.domain.model.ExtraType
import com.example.sportsxtreme.domain.model.InningsScorecard
import com.example.sportsxtreme.domain.model.MatchState
import com.example.sportsxtreme.domain.model.SyncState
import com.example.sportsxtreme.domain.usecase.ObserveMatchStateUseCase
import com.example.sportsxtreme.domain.usecase.ObserveScorecardUseCase
import com.example.sportsxtreme.domain.usecase.RecordBallUseCase
import com.example.sportsxtreme.domain.usecase.UndoBallUseCase
import com.example.sportsxtreme.domain.repository.MatchRepository
import com.example.sportsxtreme.domain.repository.TeamRepository
import com.example.sportsxtreme.domain.model.Player
import com.example.sportsxtreme.data.di.AuthDependencies
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ScoringUiState(
    val isLoading: Boolean = true,
    val scorecard: InningsScorecard? = null,
    val matchState: MatchState? = null,
    val errorMessage: String? = null,
    val scorerName: String = "Scorer"
)

@HiltViewModel
class ScoringViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeScorecard: ObserveScorecardUseCase,
    observeMatchState: ObserveMatchStateUseCase,
    private val recordBall: RecordBallUseCase,
    private val undoBall: UndoBallUseCase,
    private val matchRepository: MatchRepository,
    private val teamRepository: TeamRepository
) : ViewModel() {
    private val _scorerName = kotlinx.coroutines.flow.MutableStateFlow("Scorer")
    private val scorerNameFlow: StateFlow<String> = _scorerName

    init {
        viewModelScope.launch {
            val user = FirebaseAuth.getInstance().currentUser ?: return@launch
            val profileName = runCatching {
                AuthDependencies.authUseCases().getUserProfile(user.uid).data?.name?.trim().orEmpty()
            }.getOrDefault("")
            _scorerName.value = profileName.ifBlank { user.displayName?.trim().orEmpty().ifBlank { "Scorer" } }
        }
    }

    private val matchId = savedStateHandle.get<String>("match_id").orEmpty()
    private val inningsId = savedStateHandle.get<String>("inningsId").orEmpty()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ScoringUiState> = if (matchId.isBlank() || inningsId.isBlank()) {
        flowOf(ScoringUiState(isLoading = false, errorMessage = "Match or innings id is missing"))
    } else {
        observeMatchState(matchId).flatMapLatest { matchState ->
            val activeInningsId = (matchState as? Resource.Success)?.data?.currentInnings?.id ?: inningsId
            combine(observeScorecard(matchId, activeInningsId), scorerNameFlow) { scorecard, scorerName ->
                val scorecardError = (scorecard as? Resource.Error)?.message
                val matchStateError = (matchState as? Resource.Error)?.message
                ScoringUiState(
                    isLoading = scorecard is Resource.Loading || matchState is Resource.Loading,
                    scorecard = (scorecard as? Resource.Success)?.data,
                    matchState = (matchState as? Resource.Success)?.data,
                    errorMessage = scorecardError ?: matchStateError,
                    scorerName = scorerName
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ScoringUiState()
    )

    fun recordRuns(runs: Int) = recordDelivery(runsOffBat = runs)

    fun recordExtra(type: ExtraType) = recordDelivery(
        extras = listOf(ExtraRun(type, 1)),
        isLegalDelivery = type !in setOf(ExtraType.WIDE, ExtraType.NO_BALL)
    )

    fun recordWicket() = recordDelivery(
        dismissal = Dismissal(DismissalType.BOWLED, currentState()?.striker?.id.orEmpty())
    )

    fun undoLastBall() {
        val state = currentState() ?: return
        viewModelScope.launch { undoBall(state.matchId, LOCAL_SCORER_ID) }
    }

    fun loadPlayers(onLoaded: (List<Player>, List<Player>) -> Unit) {
        val state = currentState() ?: return
        val battingTeamId = state.battingTeam?.teamId ?: return
        val bowlingTeamId = state.bowlingTeam?.teamId ?: return
        viewModelScope.launch {
            val batting = (teamRepository.getTeam(battingTeamId) as? Resource.Success)?.data?.players.orEmpty()
            val bowling = (teamRepository.getTeam(bowlingTeamId) as? Resource.Success)?.data?.players.orEmpty()
            onLoaded(batting, bowling)
        }
    }

    fun selectNextBatter(playerId: String, onError: (String) -> Unit = {}) {
        val state = currentState() ?: return
        viewModelScope.launch {
            when (val result = matchRepository.selectNextBatter(state.matchId, playerId)) {
                is Resource.Error -> onError(result.message ?: "Unable to select batter")
                else -> Unit
            }
        }
    }

    fun startSecondInnings(strikerId: String, nonStrikerId: String, bowlerId: String, onError: (String) -> Unit = {}) {
        val state = currentState() ?: return
        viewModelScope.launch {
            when (val result = matchRepository.startSecondInnings(state.matchId, strikerId, nonStrikerId, bowlerId)) {
                is Resource.Error -> onError(result.message ?: "Unable to start second innings")
                else -> Unit
            }
        }
    }

    fun finishMatch(onFinished: () -> Unit, onError: (String) -> Unit = {}) {
        val state = currentState() ?: run {
            onError("Match state is not ready. Please reopen the match and try again.")
            return
        }
        viewModelScope.launch {
            when (val result = matchRepository.finishMatch(state.matchId)) {
                is Resource.Success -> onFinished()
                is Resource.Error -> onError(result.message ?: "Unable to finish match")
                is Resource.Loading -> onError("Match is still finishing. Please try again shortly.")
            }
        }
    }

    private fun recordDelivery(
        runsOffBat: Int = 0,
        extras: List<ExtraRun> = emptyList(),
        dismissal: Dismissal? = null,
        isLegalDelivery: Boolean = true
    ) {
        val state = currentState() ?: return
        val innings = state.currentInnings ?: return
        if (innings.status != com.example.sportsxtreme.domain.model.InningsStatus.LIVE) return
        val strikerId = state.striker?.id ?: return
        val nonStrikerId = state.nonStriker?.id ?: return
        val bowlerId = state.bowler?.id ?: return
        val now = System.currentTimeMillis()
        val previousEvent = state.currentOverEvents.lastOrNull()
        val event = BallEvent(
            ballId = UUID.randomUUID().toString(),
            matchId = state.matchId,
            inningsId = innings.id,
            inningsNumber = innings.number,
            sequenceNumber = (previousEvent?.sequenceNumber ?: state.legalBalls.toLong()) + 1,
            overNumber = state.legalBalls / 6,
            ballNumber = state.currentOverEvents.size + 1,
            legalBallNumber = state.legalBalls % 6 + 1,
            battingTeamId = innings.battingTeamId,
            bowlingTeamId = innings.bowlingTeamId,
            strikerId = strikerId,
            nonStrikerId = nonStrikerId,
            bowlerId = bowlerId,
            runsOffBat = runsOffBat,
            extras = extras,
            dismissal = dismissal,
            isLegalDelivery = isLegalDelivery,
            recordedByUserId = LOCAL_SCORER_ID,
            timestampEpochMs = now,
            syncState = SyncState.PENDING,
            previousEventId = previousEvent?.ballId
        )
        viewModelScope.launch { recordBall(event) }
    }

    private fun currentState(): MatchState? = uiState.value.matchState

    private companion object {
        const val LOCAL_SCORER_ID = "local_scorer"
    }
}
