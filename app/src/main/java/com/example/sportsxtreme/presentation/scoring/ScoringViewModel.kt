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
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ScoringUiState(
    val isLoading: Boolean = true,
    val scorecard: InningsScorecard? = null,
    val matchState: MatchState? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class ScoringViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeScorecard: ObserveScorecardUseCase,
    observeMatchState: ObserveMatchStateUseCase,
    private val recordBall: RecordBallUseCase,
    private val undoBall: UndoBallUseCase
) : ViewModel() {
    private val matchId = savedStateHandle.get<String>("match_id").orEmpty()
    private val inningsId = savedStateHandle.get<String>("inningsId").orEmpty()

    val uiState: StateFlow<ScoringUiState> = if (matchId.isBlank() || inningsId.isBlank()) {
        flowOf(ScoringUiState(isLoading = false, errorMessage = "Match or innings id is missing"))
    } else {
        combine(observeScorecard(matchId, inningsId), observeMatchState(matchId)) { scorecard, matchState ->
            val scorecardError = (scorecard as? Resource.Error)?.message
            val matchStateError = (matchState as? Resource.Error)?.message
            ScoringUiState(
                isLoading = scorecard is Resource.Loading || matchState is Resource.Loading,
                scorecard = (scorecard as? Resource.Success)?.data,
                matchState = (matchState as? Resource.Success)?.data,
                errorMessage = scorecardError ?: matchStateError
            )
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

    private fun recordDelivery(
        runsOffBat: Int = 0,
        extras: List<ExtraRun> = emptyList(),
        dismissal: Dismissal? = null,
        isLegalDelivery: Boolean = true
    ) {
        val state = currentState() ?: return
        val innings = state.currentInnings ?: return
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
