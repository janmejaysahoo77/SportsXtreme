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
    private var deliveryInFlight = false
    private var undoInFlight = false
    private var awaitingNextBowler = false
    private var latestRecordedState: MatchState? = null
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

    fun recordRuns(runs: Int, shotDirection: String? = null, metadata: Map<String, String> = emptyMap()) =
        recordDelivery(
            runsOffBat = runs,
            metadata = if (shotDirection != null) metadata + ("shotDirection" to shotDirection) else metadata
        )

    fun recordExtra(type: ExtraType) = recordDelivery(
        extras = listOf(ExtraRun(type, 1)),
        isLegalDelivery = type !in setOf(ExtraType.WIDE, ExtraType.NO_BALL)
    )

    fun recordObstructingField(batterId: String, deliveryType: ExtraType, runsScored: Int) {
        val state = scoringState() ?: return
        if (batterId != state.striker?.id && batterId != state.nonStriker?.id) return
        if (deliveryType !in setOf(ExtraType.WIDE, ExtraType.NO_BALL, ExtraType.BYE, ExtraType.LEG_BYE)) return
        if (runsScored !in 0..5) return

        val runsOffBat = if (deliveryType == ExtraType.NO_BALL) runsScored else 0
        val extras = when (deliveryType) {
            ExtraType.WIDE -> listOf(ExtraRun(ExtraType.WIDE, runsScored + 1))
            ExtraType.NO_BALL -> listOf(ExtraRun(ExtraType.NO_BALL, 1))
            ExtraType.BYE, ExtraType.LEG_BYE -> listOf(ExtraRun(deliveryType, runsScored))
            else -> emptyList()
        }
        recordDelivery(
            runsOffBat = runsOffBat,
            extras = extras,
            dismissal = Dismissal(DismissalType.OBSTRUCTING_THE_FIELD, batterId),
            isLegalDelivery = deliveryType !in setOf(ExtraType.WIDE, ExtraType.NO_BALL),
            metadata = mapOf(
                "dismissalVariant" to "Obstructing the field",
                "obstructingDeliveryType" to deliveryType.name,
                "obstructingRunsScored" to runsScored.toString()
            )
        )
    }

    fun recordRunOut(
        batterId: String,
        fielder1Id: String,
        fielder2Id: String?,
        directHit: Boolean,
        deliveryType: ExtraType,
        runsScored: Int
    ) {
        val state = scoringState() ?: return
        if (batterId != state.striker?.id && batterId != state.nonStriker?.id) return
        if (fielder1Id.isBlank() || fielder1Id == fielder2Id) return
        if (directHit && fielder2Id != null) return
        if (!directHit && (fielder2Id == null || fielder2Id.isBlank())) return
        if (deliveryType !in setOf(ExtraType.WIDE, ExtraType.NO_BALL, ExtraType.BYE, ExtraType.LEG_BYE)) return
        if (runsScored !in 0..5) return

        val runsOffBat = if (deliveryType == ExtraType.NO_BALL) runsScored else 0
        val extras = when (deliveryType) {
            ExtraType.WIDE -> listOf(ExtraRun(ExtraType.WIDE, runsScored + 1))
            ExtraType.NO_BALL -> listOf(ExtraRun(ExtraType.NO_BALL, 1))
            ExtraType.BYE, ExtraType.LEG_BYE -> listOf(ExtraRun(deliveryType, runsScored))
            else -> emptyList()
        }
        recordDelivery(
            runsOffBat = runsOffBat,
            extras = extras,
            dismissal = Dismissal(
                type = DismissalType.RUN_OUT,
                dismissedPlayerId = batterId,
                assistedByPlayerIds = listOfNotNull(fielder1Id, fielder2Id)
            ),
            isLegalDelivery = deliveryType !in setOf(ExtraType.WIDE, ExtraType.NO_BALL),
            metadata = mapOf(
                "dismissalVariant" to "Run out",
                "directHit" to directHit.toString(),
                "runOutDeliveryType" to deliveryType.name,
                "runOutRunsScored" to runsScored.toString()
            )
        )
    }

    fun recordWicket(type: DismissalType, variant: String, fielderId: String?, optionChecked: Boolean) {
        val state = scoringState() ?: return
        val retiredHurtSheet = type == DismissalType.RETIRED_HURT && variant == "Retired hurt"
        val dismissedPlayerId = if (retiredHurtSheet) {
            fielderId?.takeIf { it == state.striker?.id || it == state.nonStriker?.id } ?: return
        } else {
            state.striker?.id ?: return
        }
        if (type == DismissalType.CAUGHT && variant == "Caught" && fielderId == null) return
        if (type == DismissalType.STUMPED && fielderId == null) return
        val assistingPlayers = when {
            retiredHurtSheet -> emptyList()
            fielderId != null -> listOf(fielderId)
            variant.equals("Caught & bowled", ignoreCase = true) -> listOfNotNull(state.bowler?.id)
            else -> emptyList()
        }
        val wideStumping = type == DismissalType.STUMPED && optionChecked
        val wideHitWicket = type == DismissalType.HIT_WICKET && optionChecked
        val noBallHitBallTwice = type == DismissalType.HIT_BALL_TWICE && optionChecked
        val dontCountBall = retiredHurtSheet && optionChecked
        val metadata = mapOf("dismissalVariant" to variant) + when (type) {
            DismissalType.CAUGHT -> mapOf("brilliantCatch" to optionChecked.toString())
            DismissalType.STUMPED -> mapOf("wideBall" to wideStumping.toString())
            DismissalType.HIT_WICKET -> mapOf("wideBall" to wideHitWicket.toString())
            DismissalType.HIT_BALL_TWICE -> mapOf("noBall" to noBallHitBallTwice.toString())
            DismissalType.RETIRED_HURT -> mapOf("dontCountBall" to dontCountBall.toString())
            else -> emptyMap()
        }
        recordDelivery(
            dismissal = Dismissal(type, dismissedPlayerId, assistingPlayers),
            extras = when {
                wideStumping || wideHitWicket -> listOf(ExtraRun(ExtraType.WIDE, 1))
                noBallHitBallTwice -> listOf(ExtraRun(ExtraType.NO_BALL, 1))
                else -> emptyList()
            },
            isLegalDelivery = !wideStumping && !wideHitWicket && !noBallHitBallTwice && !dontCountBall,
            metadata = metadata
        )
    }

    fun undoLastBall() {
        if (undoInFlight || deliveryInFlight) return
        val state = scoringState() ?: return
        undoInFlight = true
        viewModelScope.launch {
            try {
                when (val result = undoBall(state.matchId, LOCAL_SCORER_ID)) {
                    is Resource.Success -> {
                        latestRecordedState = result.data
                        awaitingNextBowler = false
                    }
                    else -> Unit
                }
            } finally {
                undoInFlight = false
            }
        }
    }

    fun loadPlayers(onLoaded: (List<Player>, List<Player>) -> Unit) {
        val state = scoringState() ?: return
        val battingTeamId = state.battingTeam?.teamId ?: return
        val bowlingTeamId = state.bowlingTeam?.teamId ?: return
        viewModelScope.launch {
            val batting = (teamRepository.getTeam(battingTeamId) as? Resource.Success)?.data?.players.orEmpty()
            val bowling = (teamRepository.getTeam(bowlingTeamId) as? Resource.Success)?.data?.players.orEmpty()
            onLoaded(batting, bowling)
        }
    }

    fun selectNextBatter(playerId: String, onError: (String) -> Unit = {}) {
        // The observable match flow can briefly lag behind the just-recorded wicket.
        // Use the freshest local state so replacement selection doesn't run against
        // the old pair of batters and silently fail with "No batter slot is available".
        val state = scoringState() ?: return
        viewModelScope.launch {
            when (val result = matchRepository.selectNextBatter(state.matchId, playerId)) {
                is Resource.Error -> onError(result.message ?: "Unable to select batter")
                is Resource.Success -> {
                    latestRecordedState = result.data
                }
                is Resource.Loading -> onError("Player selection is still saving. Please try again shortly.")
            }
        }
    }

    fun selectNextBowler(playerId: String, onSelected: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val state = scoringState() ?: return
        viewModelScope.launch {
            when (val result = matchRepository.selectNextBowler(state.matchId, playerId)) {
                is Resource.Error -> onError(result.message ?: "Unable to select bowler")
                is Resource.Success -> {
                    latestRecordedState = result.data ?: return@launch
                    awaitingNextBowler = false
                    onSelected()
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun startSecondInnings(strikerId: String, nonStrikerId: String, bowlerId: String, onError: (String) -> Unit = {}) {
        val state = scoringState() ?: return
        viewModelScope.launch {
            when (val result = matchRepository.startSecondInnings(state.matchId, strikerId, nonStrikerId, bowlerId)) {
                is Resource.Error -> onError(result.message ?: "Unable to start second innings")
                is Resource.Success -> {
                    latestRecordedState = result.data
                    awaitingNextBowler = false
                }
                is Resource.Loading -> Unit
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
        isLegalDelivery: Boolean = true,
        metadata: Map<String, String> = emptyMap()
    ) {
        if (deliveryInFlight || undoInFlight || awaitingNextBowler) return
        val state = scoringState() ?: return
        val innings = state.currentInnings ?: return
        if (innings.status != com.example.sportsxtreme.domain.model.InningsStatus.LIVE) return
        // The flow can lag behind the last saved ball. Never allow another tap to
        // reuse the previous bowler once an over boundary has been reached.
        val previousOverBowlerId = state.currentOverEvents
            .lastOrNull { it.isLegalDelivery }
            ?.takeIf { state.currentOverEvents.count { event -> event.isLegalDelivery } >= 6 }
            ?.bowlerId
        if (state.legalBalls > 0 && state.legalBalls % 6 == 0 &&
            state.bowler?.id == previousOverBowlerId) {
            awaitingNextBowler = true
            return
        }
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
            previousEventId = previousEvent?.ballId,
            metadata = metadata
        )
        deliveryInFlight = true
        viewModelScope.launch {
            try {
                when (val result = recordBall(event)) {
                    is Resource.Success -> {
                        val updated = result.data ?: return@launch
                        latestRecordedState = updated
                        if (event.isLegalDelivery && updated.legalBalls > 0 && updated.legalBalls % 6 == 0) {
                            awaitingNextBowler = true
                        }
                    }
                    else -> Unit
                }
            } finally {
                deliveryInFlight = false
            }
        }
    }

    private fun currentState(): MatchState? = uiState.value.matchState

    private fun scoringState(): MatchState? {
        val observed = currentState()
        val recorded = latestRecordedState ?: return observed
        return if (observed == null || recorded.updatedAtEpochMs > observed.updatedAtEpochMs) recorded else observed
    }

    private companion object {
        const val LOCAL_SCORER_ID = "local_scorer"
    }
}
