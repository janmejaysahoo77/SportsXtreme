package com.example.sportsxtreme.presentation.scoring

import com.example.sportsxtreme.R
import com.example.sportsxtreme.presentation.tournament.*
import com.example.sportsxtreme.presentation.components.*
import com.example.sportsxtreme.presentation.auth.*
import com.example.sportsxtreme.presentation.scoring.*
import com.example.sportsxtreme.presentation.match.*
import com.example.sportsxtreme.presentation.media.*
import com.example.sportsxtreme.presentation.home.*
import com.example.sportsxtreme.presentation.team.*
import com.example.sportsxtreme.presentation.profile.*
import com.example.sportsxtreme.presentation.store.*
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint
import com.example.sportsxtreme.data.sync.PausedScoringExpiryScheduler
import com.example.sportsxtreme.domain.repository.MatchRepository
import javax.inject.Inject
import kotlinx.coroutines.launch
import com.example.sportsxtreme.domain.model.Player
import com.example.sportsxtreme.domain.model.MatchTeam
import com.example.sportsxtreme.domain.model.DismissalType
import com.example.sportsxtreme.domain.model.ExtraType
import coil3.compose.AsyncImage

@AndroidEntryPoint
class MainScoringActivity : ComponentActivity() {
    private val scoringViewModel: ScoringViewModel by viewModels()
    @Inject lateinit var matchRepository: MatchRepository
    @Inject lateinit var expiryScheduler: PausedScoringExpiryScheduler
    private var isLeavingScoring = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = pauseScoringAndFinish()
        })
        setContent {
            val uiState by scoringViewModel.uiState.collectAsState()
            MainScoringScreen(
                uiState = uiState,
                scorerName = uiState.scorerName,
                onLoadPlayers = scoringViewModel::loadPlayers,
                onSelectNextBatter = { playerId, onError -> scoringViewModel.selectNextBatter(playerId, onError) },
                onSelectNextBowler = { playerId, onSelected, onError -> scoringViewModel.selectNextBowler(playerId, onSelected, onError) },
                onStartSecondInnings = scoringViewModel::startSecondInnings,
                onFinishMatch = {
                    scoringViewModel.finishMatch(
                        onFinished = { finish() },
                        onError = { message -> Toast.makeText(this, message, Toast.LENGTH_LONG).show() }
                    )
                },
                onUpdateTarget = scoringViewModel::updateInningsTarget,
                onApplyPenaltyRuns = scoringViewModel::applyPenaltyRuns,
                onAbandonMatch = ::abandonMatchAndFinish,
                onBack = ::pauseScoringAndFinish,
                onLeaveScoring = ::leaveScoringToHostQuickScoring,
                onRun = { runs, dir, meta -> scoringViewModel.recordRuns(runs, dir, meta) },
                onExtra = { type, runs -> scoringViewModel.recordExtra(type, runs) },
                onRecordOut = { type, variant, fielderId, brilliantCatch ->
                    scoringViewModel.recordWicket(type, variant, fielderId, brilliantCatch)
                },
                onRecordObstructingField = scoringViewModel::recordObstructingField,
                onRecordRunOut = scoringViewModel::recordRunOut,
                onUndo = scoringViewModel::undoLastBall
            )
        }
    }

    private fun pauseScoringAndFinish() {
        pauseScoringAndFinish(returnToHostQuickScoring = false)
    }

    private fun leaveScoringToHostQuickScoring() {
        pauseScoringAndFinish(returnToHostQuickScoring = true)
    }

    private fun abandonMatchAndFinish() {
        if (isLeavingScoring) return
        isLeavingScoring = true
        val matchId = intent.getStringExtra(com.example.sportsxtreme.presentation.match.SelectPlayingTeamsActivity.EXTRA_MATCH_ID).orEmpty()
        lifecycleScope.launch {
            val result = matchRepository.abandonMatch(matchId)
            if (result is com.example.sportsxtreme.common.Resource.Error) {
                isLeavingScoring = false
                Toast.makeText(this@MainScoringActivity, result.message ?: "Unable to abandon match", Toast.LENGTH_LONG).show()
                return@launch
            }
            expiryScheduler.cancel(matchId)
            startActivity(
                Intent(this@MainScoringActivity, com.example.sportsxtreme.presentation.auth.MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            )
            finish()
        }
    }

    private fun pauseScoringAndFinish(returnToHostQuickScoring: Boolean) {
        if (isLeavingScoring) return
        isLeavingScoring = true
        val matchId = intent.getStringExtra(com.example.sportsxtreme.presentation.match.SelectPlayingTeamsActivity.EXTRA_MATCH_ID).orEmpty()
        if (matchId.isBlank()) {
            if (returnToHostQuickScoring) {
                startActivity(
                    Intent(this, com.example.sportsxtreme.presentation.auth.MainActivity::class.java)
                        .addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        .putExtra(com.example.sportsxtreme.presentation.auth.MainActivity.EXTRA_OPEN_HOST_QUICK_SCORING, true)
                )
            }
            finish()
            return
        }
        lifecycleScope.launch {
            try {
                runCatching { matchRepository.pauseScoring(matchId) }
                    .getOrNull()
                    ?.let { pausedAt -> runCatching { expiryScheduler.schedule(matchId, pausedAt) } }
            } finally {
                if (returnToHostQuickScoring) {
                    startActivity(
                        Intent(this@MainScoringActivity, com.example.sportsxtreme.presentation.auth.MainActivity::class.java)
                            .addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            .putExtra(com.example.sportsxtreme.presentation.auth.MainActivity.EXTRA_OPEN_HOST_QUICK_SCORING, true)
                    )
                }
                finish()
            }
        }
    }

    companion object {
        const val EXTRA_INNINGS_ID = "inningsId"
    }
}

private val ScoringAccent = Color(0xFFC1FF00)
private val ScoringBg = Color(0xFF020A15)
private val ScoringPanel = Color(0xFF07101A)
private val ScoringCard = Color(0xFF0B1523)
private val ScoringStroke = Color(0xFF25314A)
private val ScoringMuted = Color(0xFFAAB6C4)
private val ScoringBlue = Color(0xFF00D2FF)
private val OutSheetBackground = Color(0xFF0A101C)

@Composable
private fun MainScoringScreen(
    uiState: ScoringUiState,
    scorerName: String,
    onLoadPlayers: ((List<Player>, List<Player>) -> Unit) -> Unit,
    onSelectNextBatter: (String, (String) -> Unit) -> Unit,
    onSelectNextBowler: (String, () -> Unit, (String) -> Unit) -> Unit,
    onStartSecondInnings: (String, String, String, (String) -> Unit) -> Unit,
    onFinishMatch: () -> Unit,
    onUpdateTarget: (Int, () -> Unit, (String) -> Unit) -> Unit,
    onApplyPenaltyRuns: (String, Int, () -> Unit, (String) -> Unit) -> Unit,
    onAbandonMatch: () -> Unit,
    onBack: () -> Unit,
    onLeaveScoring: () -> Unit,
    onRun: (Int, String?, Map<String, String>) -> Unit,
    onExtra: (com.example.sportsxtreme.domain.model.ExtraType, Int) -> Unit,
    onRecordOut: (DismissalType, String, String?, Boolean) -> Unit,
    onRecordObstructingField: (String, ExtraType, Int) -> Unit,
    onRecordRunOut: (String, String, String?, Boolean, ExtraType, Int) -> Unit,
    onUndo: () -> Unit
) {
    var showAdvanced by remember { mutableStateOf(false) }
    var showTargetDialog by remember { mutableStateOf(false) }
    var targetInput by remember { mutableStateOf("") }
    var targetError by remember { mutableStateOf<String?>(null) }
    var showPenaltyDialog by remember { mutableStateOf(false) }
    var selectedPenaltyTeamId by remember { mutableStateOf<String?>(null) }
    var penaltyEntryStep by remember { mutableStateOf(false) }
    var penaltyRunsInput by remember { mutableStateOf("") }
    var penaltyError by remember { mutableStateOf<String?>(null) }
    var showMatchBreakDialog by remember { mutableStateOf(false) }
    var showAbandonDialog by remember { mutableStateOf(false) }
    var showWagonWheel by remember { mutableStateOf(false) }
    var showBoundaryGround by remember { mutableStateOf(false) }
    var showOutPicker by remember { mutableStateOf(false) }
    var pendingCaughtVariant by remember { mutableStateOf<String?>(null) }
    var showStumpedSheet by remember { mutableStateOf(false) }
    var showRetiredHurtSheet by remember { mutableStateOf(false) }
    var showHitWicketSheet by remember { mutableStateOf(false) }
    var showHitBallTwiceSheet by remember { mutableStateOf(false) }
    var showObstructingFieldSheet by remember { mutableStateOf(false) }
    var showRunOutSheet by remember { mutableStateOf(false) }
    var pendingExtraType by remember { mutableStateOf<ExtraType?>(null) }
    var wagonRun by remember { mutableStateOf(0) }
    var battingPlayers by remember { mutableStateOf(emptyList<Player>()) }
    var bowlingPlayers by remember { mutableStateOf(emptyList<Player>()) }
    var secondStrikerId by remember { mutableStateOf<String?>(null) }
    var secondNonStrikerId by remember { mutableStateOf<String?>(null) }
    var secondBowlerId by remember { mutableStateOf<String?>(null) }
    var pickerError by remember { mutableStateOf<String?>(null) }
    var batterPickerError by remember { mutableStateOf<String?>(null) }
    var bowlerPickerCompletedOver by remember { mutableStateOf<Int?>(null) }
    val scorecard = uiState.scorecard
    val matchState = uiState.matchState
    val innings = matchState?.currentInnings
    var showLeaveScoringConfirmation by remember { mutableStateOf(false) }
    val needsReplacement = innings?.status?.name == "LIVE" &&
        (matchState?.striker == null || matchState.nonStriker == null)
    BackHandler(enabled = needsReplacement) {
        showLeaveScoringConfirmation = true
    }
    LaunchedEffect(innings?.id) {
        // Over numbers restart at zero in each innings, so the previous innings'
        // selected-over marker must not suppress this innings' bowler picker.
        bowlerPickerCompletedOver = null
    }
    LaunchedEffect(matchState?.matchId, matchState?.battingTeam?.teamId, matchState?.bowlingTeam?.teamId) {
        onLoadPlayers { batting, bowling ->
            battingPlayers = batting
            bowlingPlayers = bowling
        }
    }
    val striker = scorecard?.batting?.firstOrNull { it.playerId == matchState?.striker?.id }
    val nonStriker = scorecard?.batting?.firstOrNull { it.playerId == matchState?.nonStriker?.id }
    val bowler = scorecard?.bowling?.firstOrNull { it.playerId == matchState?.bowler?.id }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScoringBg)
            .drawBehind {
                drawCircle(Color(0x111B5BFF), radius = size.width * 0.64f, center = Offset(size.width * 0.58f, size.height * 0.18f))
                drawCircle(Color(0x111C3F14), radius = size.width * 0.62f, center = Offset(size.width * 0.24f, size.height * 0.72f))
            }
    ) {
        // ── Main content – single screen, no scroll ──
        Column(modifier = Modifier.fillMaxSize()) {
            ScoringTopBar(scorerName, onBack)
            Spacer(Modifier.weight(0.5f))
            // Score summary
            ScoreSummaryCard(
                score = "${scorecard?.summary?.totalScore ?: 0}/${scorecard?.summary?.wickets ?: 0}",
                overs = scorecard?.summary?.overs?.display ?: "0.0",
                fullOvers = scorecard?.summary?.scheduledOvers,
                chaseText = scorecard?.summary?.let { summary -> summary.target?.let { target ->
                    val runsNeeded = (target - summary.totalScore).coerceAtLeast(0)
                    val ballsRemaining = summary.scheduledOvers?.let { (it * 6 - summary.legalBalls).coerceAtLeast(0) }
                    ballsRemaining?.let { "$runsNeeded runs needed from $it ${if (it == 1) "ball" else "balls"}" }
                } },
                runRate = scorecard?.summary?.currentRunRate ?: 0.0,
                target = scorecard?.summary?.target,
                modifier = Modifier.padding(horizontal = 10.dp)
            )
            Spacer(Modifier.weight(1f))
            // Batters + Bowler in a vertical list
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BatterMini(
                    "STRIKER",
                    matchState?.striker?.displayName ?: "Select batter",
                    (striker?.runs ?: 0).toString(),
                    "(${striker?.balls ?: 0})",
                    striker?.fours ?: 0,
                    striker?.sixes ?: 0,
                    ScoringAccent,
                    Modifier
                )
                BatterMini(
                    "NON-STR",
                    matchState?.nonStriker?.displayName ?: "Select batter",
                    (nonStriker?.runs ?: 0).toString(),
                    "(${nonStriker?.balls ?: 0})",
                    nonStriker?.fours ?: 0,
                    nonStriker?.sixes ?: 0,
                    ScoringStroke,
                    Modifier
                )
                BowlerMini(matchState?.bowler?.displayName ?: "Select bowler", bowler?.overs?.display ?: "0.0", bowler?.economy ?: 0.0, bowler?.wickets ?: 0, bowler?.runsConceded ?: 0, Modifier)
            }
            Spacer(Modifier.weight(2f))
            // Current over
            CurrentOverCard(matchState?.currentOverEvents.orEmpty(), modifier = Modifier.padding(horizontal = 10.dp))
            Spacer(Modifier.height(8.dp))
            // Run pad
            RunPad(
                modifier = Modifier.padding(horizontal = 10.dp).padding(bottom = 4.dp),
                onRunClick = { runs ->
                    if (runs == 4 || runs == 6) {
                        wagonRun = runs
                        showBoundaryGround = true
                    } else {
                        onRun(runs, null, emptyMap())
                    }
                },
                onExtra = { type ->
                    if (type in setOf(ExtraType.WIDE, ExtraType.NO_BALL, ExtraType.BYE, ExtraType.LEG_BYE)) pendingExtraType = type
                    else onExtra(type, 0)
                },
                onWicket = { showOutPicker = true },
                onUndo = onUndo
            )
            // More actions bar
            MoreActionsBar(
                expanded = showAdvanced,
                onToggle = { showAdvanced = !showAdvanced }
            )
        }

        // ── Scrim + Advanced bottom sheet ──
        if (showAdvanced) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x88000000))
                    .clickable { showAdvanced = false }
            )
            AdvancedOptionsSheet(
                modifier = Modifier.align(Alignment.BottomCenter),
                onClose = { showAdvanced = false },
                canChangeTarget = innings?.number == 2,
                onChangeTarget = {
                    targetInput = scorecard?.summary?.target?.toString().orEmpty()
                    targetError = null
                    showTargetDialog = true
                    showAdvanced = false
                },
                onPenaltyRuns = {
                    selectedPenaltyTeamId = null
                    penaltyEntryStep = false
                    penaltyRunsInput = ""
                    penaltyError = null
                    showPenaltyDialog = true
                    showAdvanced = false
                },
                onMatchBreak = { showMatchBreakDialog = true; showAdvanced = false },
                onAbandonMatch = { showAbandonDialog = true; showAdvanced = false }
            )
        }

        if (showTargetDialog) {
            NumberEntryDialog(
                title = "CHANGE TARGET",
                subtitle = "Enter the new target for the second innings.",
                value = targetInput,
                onValueChange = { targetInput = it.filter(Char::isDigit); targetError = null },
                error = targetError,
                onCancel = { showTargetDialog = false },
                confirmLabel = "UPDATE TARGET",
                onConfirm = {
                    val target = targetInput.toIntOrNull()
                    if (target == null || target <= 0) targetError = "Enter a target greater than zero"
                    else onUpdateTarget(
                        target,
                        { showTargetDialog = false },
                        { targetError = it.ifBlank { null } }
                    )
                }
            )
        }

        if (showPenaltyDialog) {
            if (!penaltyEntryStep) {
                PenaltyTeamSelectionDialog(
                    teams = listOfNotNull(matchState?.battingTeam, matchState?.bowlingTeam),
                    selectedTeamId = selectedPenaltyTeamId,
                    onSelect = { selectedPenaltyTeamId = it },
                    onCancel = { showPenaltyDialog = false },
                    onNext = { if (selectedPenaltyTeamId != null) penaltyEntryStep = true }
                )
            } else {
                NumberEntryDialog(
                    title = "PENALTY RUNS",
                    subtitle = "Enter the penalty runs for the selected team.",
                    value = penaltyRunsInput,
                    onValueChange = { penaltyRunsInput = it.filter(Char::isDigit); penaltyError = null },
                    error = penaltyError,
                    onCancel = { showPenaltyDialog = false },
                    confirmLabel = "APPLY PENALTY",
                    onConfirm = {
                        val runs = penaltyRunsInput.toIntOrNull()
                        val teamId = selectedPenaltyTeamId
                        when {
                            runs == null || runs <= 0 -> penaltyError = "Enter penalty runs greater than zero"
                            teamId == null -> penaltyError = "Choose a team first"
                            else -> onApplyPenaltyRuns(
                                teamId,
                                runs,
                                { showPenaltyDialog = false },
                                { penaltyError = it.ifBlank { null } }
                            )
                        }
                    }
                )
            }
        }

        if (showMatchBreakDialog) {
            ConfirmCancelDialog(
                title = "MATCH BREAK",
                message = "Pause scoring and return to the host screen?",
                confirmLabel = "CONFIRM",
                onCancel = { showMatchBreakDialog = false },
                onConfirm = { showMatchBreakDialog = false; onLeaveScoring() }
            )
        }
        if (showAbandonDialog) {
            ConfirmCancelDialog(
                title = "ABANDON MATCH?",
                message = "Do you really want to abandon this match?",
                confirmLabel = "YES, ABANDON",
                destructive = true,
                onCancel = { showAbandonDialog = false },
                onConfirm = { showAbandonDialog = false; onAbandonMatch() }
            )
        }

        // ── Wagon Wheel bottom sheet ──
        if (showWagonWheel) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x88000000))
                    .clickable { showWagonWheel = false }
            )
            WagonWheelBottomSheet(
                runs = wagonRun,
                modifier = Modifier.align(Alignment.BottomCenter),
                onClose = { showWagonWheel = false }
            )
        }

        if (showBoundaryGround) {
            GroundSelectionDialog(
                runs = wagonRun,
                onConfirm = { direction, metadata ->
                    showBoundaryGround = false
                    onRun(wagonRun, direction, metadata)
                },
                onCancel = {
                    showBoundaryGround = false
                }
            )
        }

        if (showOutPicker) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color(0x88000000)).clickable { showOutPicker = false }
            )
            OutSelectionBottomSheet(
                modifier = Modifier.align(Alignment.BottomCenter),
                onSelect = { type, variant ->
                    showOutPicker = false
                    if (type == DismissalType.CAUGHT && variant == "Caught") {
                        pendingCaughtVariant = variant
                    } else if (type == DismissalType.STUMPED && variant == "Stumped") {
                        showStumpedSheet = true
                    } else if (type == DismissalType.RETIRED_HURT && variant == "Retired hurt") {
                        showRetiredHurtSheet = true
                    } else if (type == DismissalType.HIT_WICKET && variant == "Hit wicket") {
                        showHitWicketSheet = true
                    } else if (type == DismissalType.OBSTRUCTING_THE_FIELD) {
                        showObstructingFieldSheet = true
                    } else if (type == DismissalType.HIT_BALL_TWICE && variant == "Hit the ball twice") {
                        showHitBallTwiceSheet = true
                    } else if (type == DismissalType.RUN_OUT && variant == "Run out") {
                        showRunOutSheet = true
                    } else {
                        onRecordOut(type, variant, null, false)
                    }
                }
            )
        }

        pendingCaughtVariant?.let { variant ->
            CaughtOutBottomSheet(
                strikerName = matchState?.striker?.displayName.orEmpty(),
                fielders = bowlingPlayers,
                onDismiss = { pendingCaughtVariant = null },
                onConfirm = { fielderId, brilliantCatch ->
                    pendingCaughtVariant = null
                    onRecordOut(DismissalType.CAUGHT, variant, fielderId, brilliantCatch)
                }
            )
        }

        if (showStumpedSheet) {
            StumpedOutBottomSheet(
                strikerName = matchState?.striker?.displayName.orEmpty(),
                fielders = bowlingPlayers,
                onDismiss = { showStumpedSheet = false },
                onConfirm = { wicketKeeperId, wideBall ->
                    showStumpedSheet = false
                    onRecordOut(DismissalType.STUMPED, "Stumped", wicketKeeperId, wideBall)
                }
            )
        }

        if (showRetiredHurtSheet) {
            RetiredHurtBottomSheet(
                strikerName = matchState?.striker?.displayName.orEmpty(),
                nonStrikerName = matchState?.nonStriker?.displayName.orEmpty(),
                strikerId = matchState?.striker?.id,
                nonStrikerId = matchState?.nonStriker?.id,
                onDismiss = { showRetiredHurtSheet = false },
                onConfirm = { batterId, dontCountBall ->
                    showRetiredHurtSheet = false
                    onRecordOut(DismissalType.RETIRED_HURT, "Retired hurt", batterId, dontCountBall)
                }
            )
        }

        if (showHitWicketSheet) {
            HitWicketOutBottomSheet(
                strikerName = matchState?.striker?.displayName.orEmpty(),
                onDismiss = { showHitWicketSheet = false },
                onConfirm = { wideBall ->
                    showHitWicketSheet = false
                    onRecordOut(DismissalType.HIT_WICKET, "Hit wicket", null, wideBall)
                }
            )
        }

        if (showObstructingFieldSheet) {
            ObstructingFieldBottomSheet(
                strikerName = matchState?.striker?.displayName.orEmpty(),
                nonStrikerName = matchState?.nonStriker?.displayName.orEmpty(),
                strikerId = matchState?.striker?.id,
                nonStrikerId = matchState?.nonStriker?.id,
                onDismiss = { showObstructingFieldSheet = false },
                onConfirm = { batterId, deliveryType, runs ->
                    showObstructingFieldSheet = false
                    onRecordObstructingField(batterId, deliveryType, runs)
                }
            )
        }

        if (showHitBallTwiceSheet) {
            HitBallTwiceBottomSheet(
                strikerName = matchState?.striker?.displayName.orEmpty(),
                onDismiss = { showHitBallTwiceSheet = false },
                onConfirm = {
                    showHitBallTwiceSheet = false
                    onRecordOut(DismissalType.HIT_BALL_TWICE, "Hit the ball twice", null, false)
                }
            )
        }

        if (showRunOutSheet) {
            RunOutBottomSheet(
                strikerName = matchState?.striker?.displayName.orEmpty(),
                nonStrikerName = matchState?.nonStriker?.displayName.orEmpty(),
                strikerId = matchState?.striker?.id,
                nonStrikerId = matchState?.nonStriker?.id,
                fielders = bowlingPlayers,
                onDismiss = { showRunOutSheet = false },
                onConfirm = { batterId, fielder1Id, fielder2Id, directHit, deliveryType, runs ->
                    showRunOutSheet = false
                    onRecordRunOut(batterId, fielder1Id, fielder2Id, directHit, deliveryType, runs)
                }
            )
        }

        if (needsReplacement) {
            val alreadyUsed = scorecard?.batting.orEmpty().map { it.playerId }.toSet()
            val eligible = battingPlayers.filterNot { it.id in alreadyUsed }
            Dialog(onDismissRequest = { showLeaveScoringConfirmation = true },
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)) {
                ScoringSelectionPage(
                    title = "SELECT NEXT BATTER",
                    subtitle = "Choose a player to continue the innings",
                    players = eligible,
                    selectedId = null,
                    onSelect = { playerId ->
                        batterPickerError = null
                        onSelectNextBatter(playerId) { error -> batterPickerError = error.ifBlank { null } }
                    }
                )
                batterPickerError?.let {
                    Text(it, color = Color(0xFFFF6677), fontSize = 12.sp, modifier = Modifier.padding(top = 10.dp))
                }
            }
        }

        if (showLeaveScoringConfirmation && needsReplacement) {
            Dialog(
                onDismissRequest = { showLeaveScoringConfirmation = false },
                properties = DialogProperties(usePlatformDefaultWidth = true, dismissOnClickOutside = false)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                        .background(OutSheetBackground).border(1.dp, ScoringStroke, RoundedCornerShape(16.dp))
                        .padding(20.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text("Leave scoring?", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    Text(
                        "You are leaving the scoring screen. You can continue from the Quick Scoring tab on the host screen.",
                        color = ScoringMuted, fontSize = 14.sp, modifier = Modifier.padding(top = 10.dp)
                    )
                    Row(Modifier.fillMaxWidth().padding(top = 20.dp), horizontalArrangement = Arrangement.End) {
                        Text(
                            "NO", color = ScoringMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { showLeaveScoringConfirmation = false }.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                        Text(
                            "YES", color = ScoringAccent, fontSize = 13.sp, fontWeight = FontWeight.Black,
                            modifier = Modifier.clickable {
                                showLeaveScoringConfirmation = false
                                onLeaveScoring()
                            }.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }
            }
        }

        val completedOverNumber = innings?.legalBalls?.div(6)
        val previousOverBowlerId = remember(completedOverNumber) { matchState?.bowler?.id }
        val needsNextBowler = innings?.status?.name == "LIVE" && innings.legalBalls > 0 && innings.legalBalls % 6 == 0 && bowlerPickerCompletedOver != completedOverNumber
        val bowlerOverLimit = matchState?.maxOversPerBowler
        val eligibleNextBowlers = bowlingPlayers
            .filterNot { it.id == previousOverBowlerId }
            .filter { player ->
                val limit = bowlerOverLimit
                limit == null || (scorecard?.bowling?.firstOrNull { it.playerId == player.id }?.legalBalls ?: 0) + 6 <= limit * 6
            }
        if (needsNextBowler && !needsReplacement) {
            Dialog(onDismissRequest = onBack,
                properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)) {
                Column(modifier = Modifier.fillMaxSize().background(ScoringBg).verticalScroll(rememberScrollState()).padding(22.dp), verticalArrangement = Arrangement.Center) {
                    Text("OVER COMPLETE", color = ScoringAccent, fontSize = 22.sp, fontWeight = FontWeight.Black)
                    Text("Choose the bowler for the next over.", color = Color.White, modifier = Modifier.padding(top = 8.dp, bottom = 16.dp))
                    ScoringSelectionPage(
                        title = "NEXT BOWLER",
                        subtitle = bowlerOverLimit?.let { "Maximum $it over${if (it == 1) "" else "s"} per bowler; previous bowler cannot go consecutive" }
                            ?: "The previous bowler cannot bowl consecutive overs",
                        players = eligibleNextBowlers,
                        selectedId = null,
                        onSelect = { playerId ->
                            pickerError = null
                            onSelectNextBowler(playerId, { bowlerPickerCompletedOver = completedOverNumber }, { pickerError = it })
                        }
                    )
                    pickerError?.let { Text(it, color = Color(0xFFFF6677), modifier = Modifier.padding(top = 8.dp)) }
                }
            }
        }

        if (innings?.status?.name == "COMPLETED") {
            val firstInningsComplete = innings.number == 1
            if (firstInningsComplete) {
                Dialog(onDismissRequest = onBack, properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)) {
                    Column(modifier = Modifier.fillMaxSize().background(ScoringBg).verticalScroll(rememberScrollState()).padding(22.dp), verticalArrangement = Arrangement.Center) {
                        Text("INNINGS BREAK", color = ScoringAccent, fontSize = 22.sp, fontWeight = FontWeight.Black)
                        Text("${matchState?.battingTeam?.name.orEmpty()} finished on ${innings.score}/${innings.wickets}. Choose the chasing team's openers and bowler.", color = Color.White, modifier = Modifier.padding(top = 10.dp, bottom = 20.dp))
                        Text(
                            "UNDO LAST BALL",
                            color = ScoringAccent,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, ScoringAccent, RoundedCornerShape(10.dp))
                                .clickable { onUndo() }
                                .padding(14.dp)
                        )
                        ScoringSelectionPage("STRIKER", "Choose the opening striker", bowlingPlayers, secondStrikerId) { secondStrikerId = it }
                        Spacer(Modifier.height(10.dp))
                        ScoringSelectionPage("NON-STRIKER", "Choose the second opener", bowlingPlayers.filterNot { it.id == secondStrikerId }, secondNonStrikerId) { secondNonStrikerId = it }
                        Spacer(Modifier.height(10.dp))
                        ScoringSelectionPage("BOWLER", "Choose the opening bowler", battingPlayers, secondBowlerId) { secondBowlerId = it }
                        pickerError?.let { Text(it, color = Color(0xFFFF6677), modifier = Modifier.padding(top = 8.dp)) }
                        Text("START SECOND INNINGS", color = ScoringBg, fontWeight = FontWeight.Black,
                            modifier = Modifier.fillMaxWidth().padding(top = 18.dp).clip(RoundedCornerShape(10.dp)).background(ScoringAccent).clickable {
                                val striker = secondStrikerId
                                val nonStriker = secondNonStrikerId
                                val bowler = secondBowlerId
                                if (striker == null || nonStriker == null || bowler == null) pickerError = "Select both openers and a bowler"
                                else onStartSecondInnings(striker, nonStriker, bowler) { pickerError = it }
                            }.padding(16.dp), textAlign = TextAlign.Center)
                    }
                }
            } else {
                Dialog(onDismissRequest = onBack, properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)) {
                    Column(modifier = Modifier.fillMaxSize().background(ScoringBg).padding(24.dp), verticalArrangement = Arrangement.Center) {
                        Text("MATCH FINISHED", color = ScoringAccent, fontSize = 24.sp, fontWeight = FontWeight.Black)
                        Text("${matchState?.score ?: innings.score}/${innings.wickets} • ${innings.legalBalls / 6}.${innings.legalBalls % 6} overs", color = Color.White, modifier = Modifier.padding(vertical = 16.dp))
                        Text("DONE", color = ScoringBg, fontWeight = FontWeight.Black, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(ScoringAccent).clickable(onClick = onFinishMatch).padding(16.dp), textAlign = TextAlign.Center)
                    }
                }
            }
        }
        pendingExtraType?.let { type ->
            ExtraRunsDialog(
                type = type,
                onDismiss = { pendingExtraType = null },
                onSelect = { runs ->
                    pendingExtraType = null
                    onExtra(type, runs)
                }
            )
        }
    }
}

@Composable
private fun ExtraRunsDialog(type: ExtraType, onDismiss: () -> Unit, onSelect: (Int) -> Unit) {
    var customSelected by remember(type) { mutableStateOf(false) }
    var customRuns by remember(type) { mutableStateOf("") }
    var selectedRuns by remember(type) { mutableStateOf<Int?>(null) }
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                .background(OutSheetBackground).border(1.dp, ScoringStroke, RoundedCornerShape(16.dp)).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                when (type) {
                    ExtraType.NO_BALL -> "NO BALL RUNS"
                    ExtraType.WIDE -> "WIDE RUNS"
                    ExtraType.BYE -> "BYE RUNS"
                    ExtraType.LEG_BYE -> "LEG BYE RUNS"
                    else -> "EXTRA RUNS"
                },
                color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black
            )
            Text("Select runs completed", color = ScoringMuted, fontSize = 13.sp, modifier = Modifier.padding(top = 5.dp, bottom = 14.dp))
            val runChoices = if (type == ExtraType.BYE || type == ExtraType.LEG_BYE) 1..6 else 0..6
            runChoices.chunked(4).forEach { rowRuns ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowRuns.forEach { runs ->
                        val selected = !customSelected && selectedRuns == runs
                        Text("+$runs", color = if (selected) ScoringAccent else Color.White, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                                .background(if (selected) Color.Transparent else ScoringCard)
                                .border(if (selected) 1.dp else 0.dp, if (selected) ScoringAccent else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable { customSelected = false; selectedRuns = runs }.padding(vertical = 13.dp))
                    }
                    repeat(4 - rowRuns.size) { Spacer(Modifier.weight(1f)) }
                }
                Spacer(Modifier.height(8.dp))
            }
            if (customSelected) {
                BasicTextField(
                    value = customRuns,
                    onValueChange = { customRuns = it.filter(Char::isDigit).take(4); selectedRuns = null },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 16.sp),
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(ScoringCard).padding(12.dp),
                    decorationBox = { inner -> Box { if (customRuns.isEmpty()) Text("Enter runs", color = ScoringMuted); inner() } }
                )
            } else {
                Text("CUSTOM", color = ScoringAccent, fontWeight = FontWeight.Black,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).border(1.dp, ScoringAccent, RoundedCornerShape(8.dp))
                        .clickable { customSelected = true; selectedRuns = null }.padding(12.dp), textAlign = TextAlign.Center)
            }
            val runsToConfirm = if (customSelected) customRuns.toIntOrNull() else selectedRuns
            if (runsToConfirm != null) {
                Text("CONFIRM", color = ScoringBg, fontWeight = FontWeight.Black, modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                    .clip(RoundedCornerShape(8.dp)).background(ScoringAccent).clickable { onSelect(runsToConfirm) }.padding(12.dp), textAlign = TextAlign.Center)
            }
            Text("CANCEL", color = ScoringMuted, modifier = Modifier.padding(top = 12.dp).clickable(onClick = onDismiss).padding(6.dp))
        }
    }
}

private data class OutOption(val title: String, val type: DismissalType, val icon: Int?)

private val outOptions = listOf(
    OutOption("Bowled", DismissalType.BOWLED, R.drawable.bowled),
    OutOption("Caught", DismissalType.CAUGHT, R.drawable.caught),
    OutOption("Caught\nBehind", DismissalType.CAUGHT, R.drawable.stumped),
    OutOption("Caught &\nbowled", DismissalType.CAUGHT, R.drawable.caughtandbowled),
    OutOption("Run out", DismissalType.RUN_OUT, R.drawable.runout),
    OutOption("LBW", DismissalType.LBW, R.drawable.lbw),
    OutOption("Stumped", DismissalType.STUMPED, R.drawable.stumping),
    OutOption("Retired\nhurt", DismissalType.RETIRED_HURT, R.drawable.retiredhurt),
    OutOption("Run out\n(mankaded)", DismissalType.RUN_OUT, R.drawable.mankadd),
    OutOption("Hit\nwicket", DismissalType.HIT_WICKET, R.drawable.hitwicket),
    OutOption("Absent\nHurt", DismissalType.RETIRED_HURT, R.drawable.absenthurt),
    OutOption("Retired\nout", DismissalType.RETIRED_OUT, R.drawable.retiredout),
    OutOption("Hit the ball\ntwice", DismissalType.HIT_BALL_TWICE, R.drawable.hittheballtwice),
    OutOption("Obstructing\nthe field", DismissalType.OBSTRUCTING_THE_FIELD, R.drawable.obstructingthefield),
    OutOption("Timed out", DismissalType.TIMED_OUT, R.drawable.timeout),
    OutOption("Retired", DismissalType.RETIRED_OUT, null)
)

@Composable
private fun OutSelectionBottomSheet(
    modifier: Modifier = Modifier,
    onSelect: (DismissalType, String) -> Unit
) {
    var expanded by remember { mutableStateOf(true) }
    Column(
        modifier = modifier.fillMaxWidth().heightIn(max = 560.dp)
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(OutSheetBackground)
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(ScoringAccent.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(size.width / 2f, -50f),
                        radius = size.height * 0.6f
                    )
                )
            }
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.weight(1f).height(1.dp).background(ScoringStroke))
            Text("Select out type", color = Color.White, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 14.dp))
            Spacer(Modifier.weight(1f).height(1.dp).background(ScoringStroke))
        }
        (if (expanded) outOptions else outOptions.take(8)).chunked(4).forEach { rowOptions ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                rowOptions.forEach { option ->
                    Column(
                        modifier = Modifier.weight(1f).clickable { onSelect(option.type, option.title.replace('\n', ' ')) }.padding(vertical = 7.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            Modifier.size(68.dp).clip(CircleShape).then(
                                if (option.icon == null) {
                                    Modifier.background(Color(0xFF111A29)).border(1.dp, ScoringStroke, CircleShape)
                                } else {
                                    Modifier
                                }
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (option.icon != null) {
                                Image(
                                    painter = painterResource(option.icon),
                                    contentDescription = option.title.replace('\n', ' '),
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                                )
                            } else {
                                Text("•", color = ScoringMuted, fontSize = 34.sp, lineHeight = 34.sp)
                            }
                        }
                        Text(
                            option.title,
                            color = Color.White,
                            fontSize = 10.sp,
                            lineHeight = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 5.dp).heightIn(min = 26.dp)
                        )
                    }
                }
                repeat(4 - rowOptions.size) { Spacer(Modifier.weight(1f)) }
            }
        }
        Text(
            if (expanded) "Show less" else "Show all",
            color = ScoringAccent,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp).clickable { expanded = !expanded }
        )
    }
}

@Composable
private fun CaughtOutBottomSheet(
    strikerName: String,
    fielders: List<Player>,
    onDismiss: () -> Unit,
    onConfirm: (fielderId: String, brilliantCatch: Boolean) -> Unit
) {
    var fieldPickerExpanded by remember { mutableStateOf(false) }
    var selectedFielderId by remember { mutableStateOf<String?>(null) }
    var brilliantCatch by remember { mutableStateOf(false) }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().background(Color(0x99000000)).clickable(onClick = onDismiss))
        Column(
            modifier = Modifier.align(Alignment.BottomCenter)
                .fillMaxWidth()
                .heightIn(max = this@BoxWithConstraints.maxHeight * 0.94f)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(OutSheetBackground)
                .drawBehind {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(ScoringAccent.copy(alpha = 0.15f), Color.Transparent),
                            center = Offset(size.width / 2f, -50f),
                            radius = size.height * 0.6f
                        )
                    )
                }
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(Modifier.width(38.dp).height(4.dp).clip(CircleShape).background(ScoringStroke))
            Text("Caught", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 12.dp))

            Column(Modifier.fillMaxWidth().padding(top = 18.dp), horizontalAlignment = Alignment.Start) {
                Text("Who?", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                StrikerDismissalCard(strikerName)
            }

            Column(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalAlignment = Alignment.Start) {
                Text("Select fielder", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier.padding(top = 7.dp).size(136.dp).clip(RoundedCornerShape(9.dp))
                        .clickable { fieldPickerExpanded = !fieldPickerExpanded },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.caught),
                        contentDescription = "Choose fielder",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                    if (selectedFielderId != null) {
                        Box(Modifier.align(Alignment.TopEnd).size(25.dp).clip(CircleShape).background(ScoringAccent), contentAlignment = Alignment.Center) {
                            Text("✓", color = ScoringBg, fontSize = 15.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
                Text(
                    fielders.firstOrNull { it.id == selectedFielderId }?.displayName ?: "Tap icon to choose",
                    color = if (selectedFielderId == null) ScoringMuted else ScoringAccent,
                    fontSize = 11.sp,
                    modifier = Modifier.width(136.dp).padding(top = 4.dp),
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
                if (fieldPickerExpanded) {
                    Column(
                        Modifier.fillMaxWidth().heightIn(max = 160.dp).padding(top = 7.dp)
                            .clip(RoundedCornerShape(9.dp)).background(ScoringCard)
                            .verticalScroll(rememberScrollState()).padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        fielders.forEach { fielder ->
                            Text(
                                text = (if (fielder.id == selectedFielderId) "✓  " else "") + fielder.displayName,
                                color = if (fielder.id == selectedFielderId) ScoringAccent else Color.White,
                                modifier = Modifier.fillMaxWidth().clickable {
                                    selectedFielderId = fielder.id
                                    fieldPickerExpanded = false
                                }.padding(vertical = 9.dp)
                            )
                        }
                        if (fielders.isEmpty()) Text("No fielders available", color = ScoringMuted, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp).clip(RoundedCornerShape(8.dp))
                    .clickable { brilliantCatch = !brilliantCatch }.padding(vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(20.dp).border(1.dp, if (brilliantCatch) ScoringAccent else ScoringMuted, RoundedCornerShape(3.dp))
                        .background(if (brilliantCatch) ScoringAccent else Color.Transparent, RoundedCornerShape(3.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (brilliantCatch) Text("✓", color = ScoringBg, fontSize = 14.sp, fontWeight = FontWeight.Black)
                }
                Text("Brilliant catch", color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(start = 9.dp))
            }

            Box(
                Modifier.fillMaxWidth().height(48.dp).padding(top = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedFielderId != null) ScoringAccent else ScoringStroke)
                    .clickable(enabled = selectedFielderId != null) {
                        selectedFielderId?.let { onConfirm(it, brilliantCatch) }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("OUT", color = if (selectedFielderId != null) ScoringBg else ScoringMuted, fontSize = 14.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun StumpedOutBottomSheet(
    strikerName: String,
    fielders: List<Player>,
    onDismiss: () -> Unit,
    onConfirm: (wicketKeeperId: String, wideBall: Boolean) -> Unit
) {
    var keeperPickerExpanded by remember { mutableStateOf(false) }
    var selectedKeeperId by remember { mutableStateOf<String?>(null) }
    var wideBall by remember { mutableStateOf(false) }
    val designatedKeepers = fielders.filter { it.role.name == "WICKET_KEEPER" }
    val keeperOptions = designatedKeepers.ifEmpty { fielders }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().background(Color(0x99000000)).clickable(onClick = onDismiss))
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .heightIn(max = this@BoxWithConstraints.maxHeight * 0.94f)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(OutSheetBackground)
                .drawBehind {
                    drawRect(Brush.radialGradient(
                        colors = listOf(ScoringAccent.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(size.width / 2f, -50f),
                        radius = size.height * 0.6f
                    ))
                }
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(Modifier.width(38.dp).height(4.dp).clip(CircleShape).background(ScoringStroke))
            Text("Stumped", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 12.dp))

            Column(Modifier.fillMaxWidth().padding(top = 18.dp), horizontalAlignment = Alignment.Start) {
                Text("Who?", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                StrikerDismissalCard(strikerName)
            }

            Column(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalAlignment = Alignment.Start) {
                Text("Wicket-keeper", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Column(
                    modifier = Modifier.padding(top = 8.dp).width(150.dp)
                        .clip(RoundedCornerShape(9.dp)).background(ScoringCard)
                        .border(1.dp, ScoringStroke, RoundedCornerShape(9.dp))
                        .clickable { keeperPickerExpanded = !keeperPickerExpanded },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(Modifier.fillMaxWidth().height(132.dp).background(Color(0xFF111A29)), contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(R.drawable.wicketkeeper),
                            contentDescription = "Wicket-keeper profile",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().padding(5.dp).clip(RoundedCornerShape(7.dp))
                        )
                        if (selectedKeeperId != null) {
                            Box(Modifier.fillMaxSize().background(Color(0x6611A79D)), contentAlignment = Alignment.Center) {
                                Text("✓", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                    Text(
                        keeperOptions.firstOrNull { it.id == selectedKeeperId }?.displayName ?: "Tap to choose",
                        color = if (selectedKeeperId == null) ScoringMuted else Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 9.dp)
                    )
                    if (selectedKeeperId != null) {
                        Text("Wicket-keeper", color = ScoringMuted, fontSize = 10.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, modifier = Modifier.padding(bottom = 8.dp))
                    }
                }
                if (keeperPickerExpanded) {
                    Column(
                        Modifier.fillMaxWidth().heightIn(max = 150.dp).padding(top = 7.dp)
                            .clip(RoundedCornerShape(9.dp)).background(ScoringCard)
                            .verticalScroll(rememberScrollState()).padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        keeperOptions.forEach { keeper ->
                            Text(
                                text = (if (keeper.id == selectedKeeperId) "✓  " else "") + keeper.displayName,
                                color = if (keeper.id == selectedKeeperId) ScoringAccent else Color.White,
                                modifier = Modifier.fillMaxWidth().clickable {
                                    selectedKeeperId = keeper.id
                                    keeperPickerExpanded = false
                                }.padding(vertical = 9.dp)
                            )
                        }
                        if (keeperOptions.isEmpty()) Text("No wicket-keepers available", color = ScoringMuted, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp).clickable { wideBall = !wideBall }.padding(vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(20.dp).border(1.dp, if (wideBall) ScoringAccent else ScoringMuted, RoundedCornerShape(3.dp))
                        .background(if (wideBall) ScoringAccent else Color.Transparent, RoundedCornerShape(3.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (wideBall) Text("✓", color = ScoringBg, fontSize = 14.sp, fontWeight = FontWeight.Black)
                }
                Text("Wide Ball", color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(start = 9.dp))
            }

            Box(
                Modifier.fillMaxWidth().height(48.dp).padding(top = 8.dp).clip(RoundedCornerShape(8.dp))
                    .background(if (selectedKeeperId != null) ScoringAccent else ScoringStroke)
                    .clickable(enabled = selectedKeeperId != null) {
                        selectedKeeperId?.let { onConfirm(it, wideBall) }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("OUT", color = if (selectedKeeperId != null) ScoringBg else ScoringMuted, fontSize = 14.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun HitWicketOutBottomSheet(
    strikerName: String,
    onDismiss: () -> Unit,
    onConfirm: (wideBall: Boolean) -> Unit
) {
    var wideBall by remember { mutableStateOf(false) }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().background(Color(0x99000000)).clickable(onClick = onDismiss))
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .heightIn(max = this@BoxWithConstraints.maxHeight * 0.94f)
                .fillMaxHeight(0.94f)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(OutSheetBackground)
                .drawBehind {
                    drawRect(Brush.radialGradient(
                        colors = listOf(ScoringAccent.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(size.width / 2f, -50f),
                        radius = size.height * 0.6f
                    ))
                }
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(Modifier.align(Alignment.CenterHorizontally).width(38.dp).height(4.dp).clip(CircleShape).background(ScoringStroke))
            Text("Hit Wicket", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 12.dp))
            Text("Who?", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 22.dp))
            StrikerDismissalCard(strikerName)

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp).clickable { wideBall = !wideBall }.padding(vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(20.dp).border(1.dp, if (wideBall) ScoringAccent else ScoringMuted, RoundedCornerShape(3.dp))
                        .background(if (wideBall) ScoringAccent else Color.Transparent, RoundedCornerShape(3.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (wideBall) Text("✓", color = ScoringBg, fontSize = 14.sp, fontWeight = FontWeight.Black)
                }
                Text("Wide Ball", color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(start = 9.dp))
            }

            Spacer(Modifier.weight(1f))
            Box(
                Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(8.dp))
                    .background(ScoringAccent).clickable { onConfirm(wideBall) },
                contentAlignment = Alignment.Center
            ) {
                Text("OUT", color = ScoringBg, fontSize = 14.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun HitBallTwiceBottomSheet(
    strikerName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().background(Color(0x99000000)).clickable(onClick = onDismiss))
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .fillMaxHeight(0.94f).heightIn(max = this@BoxWithConstraints.maxHeight * 0.94f)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(OutSheetBackground)
                .drawBehind {
                    drawRect(Brush.radialGradient(
                        colors = listOf(ScoringAccent.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(size.width / 2f, -50f),
                        radius = size.height * 0.6f
                    ))
                }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(Modifier.align(Alignment.CenterHorizontally).width(38.dp).height(4.dp).clip(CircleShape).background(ScoringStroke))
            Text("Hit the Ball Twice", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 12.dp))
            Text("The striker will be out", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 22.dp))
            RetiredHurtPlayerCard(
                name = strikerName,
                role = "Striker",
                image = R.drawable.striker,
                selected = true,
                modifier = Modifier.padding(top = 6.dp).width(150.dp),
                onClick = {}
            )

            Spacer(Modifier.weight(1f))
            Box(
                Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(8.dp))
                    .background(ScoringAccent).clickable(onClick = onConfirm),
                contentAlignment = Alignment.Center
            ) {
                Text("OUT", color = ScoringBg, fontSize = 14.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun ObstructingFieldBottomSheet(
    strikerName: String,
    nonStrikerName: String,
    strikerId: String?,
    nonStrikerId: String?,
    onDismiss: () -> Unit,
    onConfirm: (batterId: String, deliveryType: ExtraType, runs: Int) -> Unit
) {
    var selectedBatterId by remember { mutableStateOf<String?>(null) }
    var selectedDeliveryType by remember { mutableStateOf<ExtraType?>(null) }
    var selectedRuns by remember { mutableStateOf(0) }
    val deliveryTypes = listOf(
        "WD" to ExtraType.WIDE,
        "NB" to ExtraType.NO_BALL,
        "Bye" to ExtraType.BYE,
        "LB" to ExtraType.LEG_BYE
    )

    BoxWithConstraints(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().background(Color(0x99000000)).clickable(onClick = onDismiss))
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .fillMaxHeight(0.94f).heightIn(max = this@BoxWithConstraints.maxHeight * 0.94f)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(OutSheetBackground)
                .drawBehind {
                    drawRect(Brush.radialGradient(
                        colors = listOf(ScoringAccent.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(size.width / 2f, -50f),
                        radius = size.height * 0.6f
                    ))
                }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(Modifier.width(38.dp).height(4.dp).clip(CircleShape).background(ScoringStroke))
            Text("Obstructing the field", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 10.dp))

            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
            ) {
                Text("Who?", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 18.dp, bottom = 6.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RetiredHurtPlayerCard(
                        name = strikerName, role = "Striker", image = R.drawable.striker,
                        selected = selectedBatterId == strikerId && strikerId != null,
                        modifier = Modifier.weight(1f),
                        onClick = { strikerId?.let { selectedBatterId = it } }
                    )
                    RetiredHurtPlayerCard(
                        name = nonStrikerName, role = "Non-striker", image = R.drawable.nonstriker,
                        selected = selectedBatterId == nonStrikerId && nonStrikerId != null,
                        modifier = Modifier.weight(1f),
                        onClick = { nonStrikerId?.let { selectedBatterId = it } }
                    )
                }

                Text("Delivery type", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 18.dp, bottom = 8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    deliveryTypes.forEach { (label, type) ->
                        OutChoiceChip(
                            label = label,
                            selected = selectedDeliveryType == type,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedDeliveryType = type }
                        )
                    }
                }

                Text("Runs scored", color = ScoringMuted, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, top = 10.dp, bottom = 6.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (0..5).forEach { runs ->
                        OutChoiceChip(
                            label = if (runs == 5) "+" else runs.toString(),
                            selected = selectedRuns == runs,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedRuns = runs }
                        )
                    }
                }
            }

            Box(
                Modifier.fillMaxWidth().height(48.dp).padding(top = 8.dp).clip(RoundedCornerShape(8.dp))
                    .background(if (selectedBatterId != null && selectedDeliveryType != null) ScoringAccent else ScoringStroke)
                    .clickable(enabled = selectedBatterId != null && selectedDeliveryType != null) {
                        val batterId = selectedBatterId
                        val deliveryType = selectedDeliveryType
                        if (batterId != null && deliveryType != null) onConfirm(batterId, deliveryType, selectedRuns)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("OUT", color = if (selectedBatterId != null && selectedDeliveryType != null) ScoringBg else ScoringMuted, fontSize = 14.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun OutChoiceChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier.height(38.dp).clip(RoundedCornerShape(20.dp))
            .background(if (selected) ScoringAccent else Color(0xFF182333))
            .border(1.dp, if (selected) ScoringAccent else ScoringStroke, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (selected) ScoringBg else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RunOutBottomSheet(
    strikerName: String,
    nonStrikerName: String,
    strikerId: String?,
    nonStrikerId: String?,
    fielders: List<Player>,
    onDismiss: () -> Unit,
    onConfirm: (batterId: String, fielder1Id: String, fielder2Id: String?, directHit: Boolean, deliveryType: ExtraType, runs: Int) -> Unit
) {
    var selectedBatterId by remember { mutableStateOf<String?>(null) }
    var selectedFielder1Id by remember { mutableStateOf<String?>(null) }
    var selectedFielder2Id by remember { mutableStateOf<String?>(null) }
    var directHit by remember { mutableStateOf(false) }
    var selectedDeliveryType by remember { mutableStateOf<ExtraType?>(null) }
    var selectedRuns by remember { mutableStateOf(0) }
    var fielderPicker by remember { mutableStateOf(0) }
    val deliveryTypes = listOf(
        "WD" to ExtraType.WIDE,
        "NB" to ExtraType.NO_BALL,
        "Bye" to ExtraType.BYE,
        "LB" to ExtraType.LEG_BYE
    )
    val canConfirm = selectedBatterId != null && selectedFielder1Id != null &&
        (directHit || selectedFielder2Id != null) && selectedDeliveryType != null

    BoxWithConstraints(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().background(Color(0x99000000)).clickable(onClick = onDismiss))
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .fillMaxHeight(0.94f).heightIn(max = this@BoxWithConstraints.maxHeight * 0.94f)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(OutSheetBackground)
                .drawBehind {
                    drawRect(Brush.radialGradient(
                        colors = listOf(ScoringAccent.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(size.width / 2f, -50f),
                        radius = size.height * 0.6f
                    ))
                }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(Modifier.width(38.dp).height(4.dp).clip(CircleShape).background(ScoringStroke))
            Text("Run Out", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 10.dp))

            Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())) {
                Text("Who?", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 14.dp, bottom = 6.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RetiredHurtPlayerCard(
                        name = strikerName, role = "Striker", image = R.drawable.nonstriker,
                        selected = selectedBatterId == strikerId && strikerId != null,
                        modifier = Modifier.weight(1f),
                        onClick = { strikerId?.let { selectedBatterId = it } }
                    )
                    RetiredHurtPlayerCard(
                        name = nonStrikerName, role = "Non-striker", image = R.drawable.striker,
                        selected = selectedBatterId == nonStrikerId && nonStrikerId != null,
                        modifier = Modifier.weight(1f),
                        onClick = { nonStrikerId?.let { selectedBatterId = it } }
                    )
                }

                Row(Modifier.fillMaxWidth().padding(top = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Select fielder", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Row(
                        modifier = Modifier.clickable {
                            directHit = !directHit
                            if (directHit) {
                                selectedFielder2Id = null
                                fielderPicker = 0
                            }
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(18.dp).border(1.dp, if (directHit) ScoringAccent else ScoringMuted, RoundedCornerShape(3.dp))
                                .background(if (directHit) ScoringAccent else Color.Transparent, RoundedCornerShape(3.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (directHit) Text("✓", color = ScoringBg, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                        Text("Direct hit", color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(start = 6.dp))
                    }
                }

                Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RunOutFielderCard(
                        title = "Fielder 1",
                        selectedName = fielders.firstOrNull { it.id == selectedFielder1Id }?.displayName,
                        image = R.drawable.fielder_one,
                        enabled = true,
                        modifier = Modifier.weight(1f),
                        onClick = { fielderPicker = if (fielderPicker == 1) 0 else 1 }
                    )
                    RunOutFielderCard(
                        title = "Fielder 2",
                        selectedName = fielders.firstOrNull { it.id == selectedFielder2Id }?.displayName,
                        image = R.drawable.fileder_two,
                        enabled = !directHit,
                        modifier = Modifier.weight(1f),
                        onClick = { fielderPicker = if (fielderPicker == 2) 0 else 2 }
                    )
                }
                if (fielderPicker != 0) {
                    val selectedOther = if (fielderPicker == 1) selectedFielder2Id else selectedFielder1Id
                    Column(
                        Modifier.fillMaxWidth().padding(top = 6.dp).heightIn(max = 140.dp)
                            .clip(RoundedCornerShape(8.dp)).background(ScoringCard)
                            .verticalScroll(rememberScrollState()).padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        fielders.filterNot { it.id == selectedOther }.forEach { player ->
                            Text(
                                (if (player.id == (if (fielderPicker == 1) selectedFielder1Id else selectedFielder2Id)) "✓  " else "") + player.displayName,
                                color = if (player.id == (if (fielderPicker == 1) selectedFielder1Id else selectedFielder2Id)) ScoringAccent else Color.White,
                                modifier = Modifier.fillMaxWidth().clickable {
                                    if (fielderPicker == 1) selectedFielder1Id = player.id else selectedFielder2Id = player.id
                                    fielderPicker = 0
                                }.padding(vertical = 8.dp)
                            )
                        }
                        if (fielders.isEmpty()) Text("No fielders available", color = ScoringMuted, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp))
                    }
                }

                Text("Delivery type", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 14.dp, bottom = 7.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    deliveryTypes.forEach { (label, type) ->
                        OutChoiceChip(label, selectedDeliveryType == type, Modifier.weight(1f)) { selectedDeliveryType = type }
                    }
                }
                Text("Runs scored", color = ScoringMuted, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 5.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (0..5).forEach { runs ->
                        OutChoiceChip(if (runs == 5) "+" else runs.toString(), selectedRuns == runs, Modifier.weight(1f)) { selectedRuns = runs }
                    }
                }
            }

            Box(
                Modifier.fillMaxWidth().height(48.dp).padding(top = 8.dp).clip(RoundedCornerShape(8.dp))
                    .background(if (canConfirm) ScoringAccent else ScoringStroke)
                    .clickable(enabled = canConfirm) {
                        val batterId = selectedBatterId
                        val fielder1Id = selectedFielder1Id
                        val deliveryType = selectedDeliveryType
                        if (batterId != null && fielder1Id != null && deliveryType != null && (directHit || selectedFielder2Id != null)) {
                            onConfirm(batterId, fielder1Id, selectedFielder2Id.takeUnless { directHit }, directHit, deliveryType, selectedRuns)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("OUT", color = if (canConfirm) ScoringBg else ScoringMuted, fontSize = 14.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun RunOutFielderCard(
    title: String,
    selectedName: String?,
    image: Int,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(8.dp))
            .background(if (enabled) ScoringCard else ScoringBg)
            .border(1.dp, if (selectedName != null) ScoringAccent else ScoringStroke, RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // These fielder assets are square cards. Keep the image viewport square
        // so Fit can show the complete artwork without shrinking it into a wide,
        // letterboxed strip or cropping off its neon frame.
        Box(Modifier.fillMaxWidth().aspectRatio(1f).background(Color(0xFF111A29)), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(image),
                contentDescription = title,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
            if (selectedName != null) Box(Modifier.fillMaxSize().background(Color(0x5511A79D)), contentAlignment = Alignment.Center) {
                Text("✓", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
            }
        }
        Text(selectedName ?: title, color = if (selectedName == null) ScoringMuted else Color.White, fontSize = 11.sp, maxLines = 1, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 5.dp))
    }
}

@Composable
private fun StrikerDismissalCard(strikerName: String) {
    Column(
        modifier = Modifier.padding(top = 8.dp).width(150.dp)
            .clip(RoundedCornerShape(9.dp)).background(ScoringCard)
            .border(1.dp, ScoringStroke, RoundedCornerShape(9.dp)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier.fillMaxWidth().height(132.dp).background(Color(0xFF111A29)), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(R.drawable.striker),
                contentDescription = "Striker ${strikerName.ifBlank { "" }}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().padding(5.dp).clip(RoundedCornerShape(7.dp))
            )
            Box(Modifier.fillMaxSize().background(Color(0x6611A79D)), contentAlignment = Alignment.Center) {
                Text("✓", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Black)
            }
        }
        Text(strikerName.ifBlank { "Striker" }, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 2, modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 7.dp))
        Text("Striker", color = ScoringMuted, fontSize = 10.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, modifier = Modifier.padding(bottom = 8.dp))
    }
}

@Composable
private fun RetiredHurtBottomSheet(
    strikerName: String,
    nonStrikerName: String,
    strikerId: String?,
    nonStrikerId: String?,
    onDismiss: () -> Unit,
    onConfirm: (batterId: String, dontCountBall: Boolean) -> Unit
) {
    var selectedBatterId by remember { mutableStateOf<String?>(null) }
    var dontCountBall by remember { mutableStateOf(true) }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().background(Color(0x99000000)).clickable(onClick = onDismiss))
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .heightIn(max = this@BoxWithConstraints.maxHeight * 0.94f)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(OutSheetBackground)
                .drawBehind {
                    drawRect(Brush.radialGradient(
                        colors = listOf(ScoringAccent.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(size.width / 2f, -50f),
                        radius = size.height * 0.6f
                    ))
                }
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(Modifier.width(38.dp).height(4.dp).clip(CircleShape).background(ScoringStroke))
            Text("Retired hurt", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 12.dp))
            Text("Who?", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 8.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RetiredHurtPlayerCard(
                    name = strikerName,
                    role = "Striker",
                    image = R.drawable.striker,
                    selected = selectedBatterId == strikerId && strikerId != null,
                    modifier = Modifier.weight(1f),
                    onClick = { strikerId?.let { selectedBatterId = it } }
                )
                RetiredHurtPlayerCard(
                    name = nonStrikerName,
                    role = "Non-striker",
                    image = R.drawable.nonstriker,
                    selected = selectedBatterId == nonStrikerId && nonStrikerId != null,
                    modifier = Modifier.weight(1f),
                    onClick = { nonStrikerId?.let { selectedBatterId = it } }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp).clickable { dontCountBall = !dontCountBall }.padding(vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(20.dp).border(1.dp, if (dontCountBall) ScoringAccent else ScoringMuted, RoundedCornerShape(3.dp))
                        .background(if (dontCountBall) ScoringAccent else Color.Transparent, RoundedCornerShape(3.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (dontCountBall) Text("✓", color = ScoringBg, fontSize = 14.sp, fontWeight = FontWeight.Black)
                }
                Text("Don't count the ball", color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(start = 9.dp))
            }

            Box(
                Modifier.fillMaxWidth().height(48.dp).padding(top = 8.dp).clip(RoundedCornerShape(8.dp))
                    .background(if (selectedBatterId != null) ScoringAccent else ScoringStroke)
                    .clickable(enabled = selectedBatterId != null) {
                        selectedBatterId?.let { onConfirm(it, dontCountBall) }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("Retired hurt", color = if (selectedBatterId != null) ScoringBg else ScoringMuted, fontSize = 14.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun RetiredHurtPlayerCard(
    name: String,
    role: String,
    image: Int,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(9.dp))
            .background(if (selected) Color(0xFF15352F) else ScoringCard)
            .border(1.dp, if (selected) ScoringAccent else ScoringStroke, RoundedCornerShape(9.dp))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier.fillMaxWidth().height(136.dp).background(Color(0xFF111A29)), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(image),
                contentDescription = "$role ${name.ifBlank { "" }}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().padding(5.dp).clip(RoundedCornerShape(7.dp))
            )
            if (selected) {
                Box(Modifier.fillMaxSize().background(Color(0x6611A79D)), contentAlignment = Alignment.Center) {
                    Text("✓", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Black)
                }
            }
        }
        Text(name.ifBlank { role }, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 2, modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 7.dp))
        Text(role, color = ScoringMuted, fontSize = 10.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, modifier = Modifier.padding(bottom = 8.dp))
    }
}

@Composable
private fun ScoringSelectionPage(title: String, subtitle: String, players: List<Player>, selectedId: String?, onSelect: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().heightIn(max = 620.dp).verticalScroll(rememberScrollState()).clip(RoundedCornerShape(12.dp)).background(ScoringCard).border(1.dp, ScoringStroke, RoundedCornerShape(12.dp)).padding(12.dp)) {
        Text(title, color = ScoringAccent, fontWeight = FontWeight.Black, fontSize = 12.sp)
        Text(subtitle, color = ScoringMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 3.dp, bottom = 6.dp))
        players.forEach { player ->
            Text(if (player.id == selectedId) "✓  ${player.displayName}" else player.displayName,
                color = if (player.id == selectedId) ScoringAccent else Color.White,
                modifier = Modifier.fillMaxWidth().clickable { onSelect(player.id) }.padding(vertical = 7.dp))
        }
        if (players.isEmpty()) Text("No eligible players available", color = ScoringMuted, fontSize = 11.sp)
    }
}

// ── Top Bar ──────────────────────────────────────────────
@Composable
private fun ScoringTopBar(scorerName: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(ScoringPanel)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ScoreArrow(Modifier.size(22.dp).clickable(onClick = onBack), Color.White)
        Column(modifier = Modifier.padding(start = 10.dp).weight(1f)) {
            Text("Scoring", color = ScoringMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(scorerName, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        ScoreHelpIcon(Modifier.size(20.dp), Color.White)
        ScoreGearIcon(Modifier.padding(start = 20.dp).size(20.dp), Color.White)
    }
}

// ── Score Summary ─────────────────────────────────────────
@Composable
private fun ScoreSummaryCard(score: String, overs: String, fullOvers: Int?, chaseText: String?, runRate: Double, target: Int?, modifier: Modifier = Modifier) {
    val liveAlpha by rememberInfiniteTransition(label = "liveBlink").animateFloat(
        initialValue = 1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = keyframes { durationMillis = 1800; 1f at 0; 0.25f at 900 },
        ),
        label = "liveAlpha"
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(ScoringCard)
            .border(1.dp, Color(0xFF3E5B1B), RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(score, color = ScoringAccent, fontSize = 32.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .height(20.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF31480F))
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$overs/${fullOvers?.let { "$it.0" } ?: "--"} OV", color = ScoringAccent, fontSize = 8.sp, fontWeight = FontWeight.Black)
                }
                Text("CRR  ${"%.2f".format(runRate)}", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 6.dp))
            }
        }
        if (chaseText != null) {
            Text(chaseText, color = ScoringAccent, fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 4.dp))
        }
        Row(
            modifier = Modifier
                .padding(top = 6.dp)
                .fillMaxWidth()
                .height(30.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF151E2E))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(target?.let { "Target: $it" } ?: "1st Innings", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
            Text("LIVE", color = Color(0xFFFF334F).copy(alpha = liveAlpha), fontSize = 10.sp, fontWeight = FontWeight.Black)
        }
    }
}

// ── Compact Batter / Bowler cards ────────────────────────
@Composable
private fun BatterMini(
    label: String,
    name: String,
    runs: String,
    balls: String,
    fours: Int,
    sixes: Int,
    accent: Color,
    modifier: Modifier
) {
    val isStriker = accent != ScoringStroke
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(78.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(ScoringCard)
            .border(if (isStriker) 1.5.dp else 1.dp, if (isStriker) accent else ScoringStroke, RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = if (isStriker) accent else ScoringMuted, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 0.8.sp)
            Text(name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 2.dp))
            Text("4s: $fours  6s: $sixes", color = ScoringMuted, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 2.dp))
        }
        Column(horizontalAlignment = Alignment.End) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(runs, color = if (isStriker) accent else Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                Text(" $balls", color = if (isStriker) accent.copy(alpha = 0.7f) else ScoringMuted, fontSize = 12.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(bottom = 4.dp))
            }
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF1E2A3A))
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("CHANGE", color = ScoringMuted, fontSize = 7.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun BowlerMini(name: String, overs: String, economy: Double, wickets: Int, runsConceded: Int, modifier: Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(78.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(ScoringCard)
            .border(1.5.dp, ScoringBlue, RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("BOWLER", color = ScoringBlue, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 0.8.sp)
            Text(name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 2.dp))
            Text("$overs Ov  Econ ${"%.1f".format(economy)}", color = ScoringMuted, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 2.dp))
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("$wickets/$runsConceded", color = ScoringBlue, fontSize = 28.sp, fontWeight = FontWeight.Black)
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF1E2A3A))
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("CHANGE", color = ScoringMuted, fontSize = 7.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

// ── Current Over ──────────────────────────────────────────
@Composable
private fun CurrentOverCard(events: List<com.example.sportsxtreme.domain.model.BallEvent>, modifier: Modifier = Modifier) {
    val lastEvent = events.lastOrNull()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(ScoringCard)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("CURRENT OVER", color = ScoringMuted, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.4.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("LAST BALL: ", color = ScoringMuted, fontSize = 7.sp, fontWeight = FontWeight.Black)
                Text(lastEvent?.let { currentOverBallLabel(it) } ?: "--", color = ScoringAccent, fontSize = 9.sp, fontWeight = FontWeight.Black)
            }
        }
        Row(
            modifier = Modifier.padding(top = 10.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            events.takeLast(6).map { event ->
                val extraLabel = currentOverExtraLabel(event)
                val label = extraLabel ?: event.totalRuns.toString()
                val color = if (extraLabel != null) null
                    else if (event.totalRuns == 4) ScoringBlue
                    else if (event.totalRuns == 6) ScoringAccent
                    else null
                Triple(label, color, extraLabel != null)
            }
                .let { values -> if (values.isEmpty()) List(6) { Triple("--", null, false) } else values }
                .forEachIndexed { index, (value, col, isExtra) ->
                val color = col ?: Color(0xFF222A3A)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (col != null) color.copy(alpha = 0.2f) else Color(0xFF161E2C))
                            .border(1.dp, color, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(value, color = if (col != null) color else Color.White, fontSize = if (isExtra) 9.sp else 14.sp, fontWeight = FontWeight.Black, maxLines = 1)
                    }
                    Text("B${index + 1}", color = ScoringMuted, fontSize = 7.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

private fun currentOverExtraLabel(event: com.example.sportsxtreme.domain.model.BallEvent): String? {
    val noBall = event.extras.firstOrNull { it.type == ExtraType.NO_BALL }
    if (noBall != null) {
        val batRuns = event.runsOffBat
        return if (batRuns > 0) "NB+$batRuns" else "NB"
    }
    val wide = event.extras.firstOrNull { it.type == ExtraType.WIDE }
    if (wide != null) {
        val additionalRuns = (wide.runs - 1).coerceAtLeast(0)
        return if (additionalRuns > 0) "WD+$additionalRuns" else "WD"
    }
    return null
}

private fun currentOverBallLabel(event: com.example.sportsxtreme.domain.model.BallEvent): String =
    currentOverExtraLabel(event) ?: "${event.totalRuns} Runs"

// ── Run Pad ───────────────────────────────────────────────
@Composable
private fun RunPad(
    modifier: Modifier = Modifier,
    onRunClick: (Int) -> Unit,
    onExtra: (com.example.sportsxtreme.domain.model.ExtraType) -> Unit,
    onWicket: () -> Unit,
    onUndo: () -> Unit
) {
    val rows = listOf(
        listOf("0", "1", "2"),
        listOf("3", "4\nFOUR", "6\nSIX"),
        listOf("WD", "NB", "BYE"),
        listOf("LB", "OUT", "UNDO")
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        rows.forEach { rowLabels ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowLabels.forEach { label ->
                    val run = label.substringBefore("\n").toIntOrNull()
                    ScoreKey(
                        label = label,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        onClick = when {
                            run != null && run in 0..6 && run != 5 -> { { onRunClick(run) } }
                            label == "WD" -> { { onExtra(com.example.sportsxtreme.domain.model.ExtraType.WIDE) } }
                            label == "NB" -> { { onExtra(com.example.sportsxtreme.domain.model.ExtraType.NO_BALL) } }
                            label == "BYE" -> { { onExtra(com.example.sportsxtreme.domain.model.ExtraType.BYE) } }
                            label == "LB" -> { { onExtra(com.example.sportsxtreme.domain.model.ExtraType.LEG_BYE) } }
                            label == "OUT" -> onWicket
                            label == "UNDO" -> onUndo
                            else -> null
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ScoreKey(label: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    val isFour = label.startsWith("4")
    val isSix = label.startsWith("6")
    val isOut = label == "OUT"
    val borderCol = when {
        isFour -> ScoringBlue
        isSix -> ScoringAccent
        isOut -> Color(0xFFFF7F7F)
        else -> Color.Transparent
    }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.94f else 1f, label = "scale")

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isFour -> Color(0xFF0D1E2E)
                    isSix -> Color(0xFF162010)
                    isOut -> Color(0xFF1F1015)
                    else -> ScoringCard
                }
            )
            .border(if (borderCol == Color.Transparent) 0.dp else 1.dp, borderCol, RoundedCornerShape(8.dp))
            .then(if (onClick != null) Modifier.clickable(interactionSource = interactionSource, indication = null, onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        val parts = label.split("\n")
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                parts[0],
                color = when {
                    isFour -> ScoringBlue
                    isSix -> ScoringAccent
                    isOut -> Color(0xFFFFB0B0)
                    else -> Color.White
                },
                fontSize = if (parts[0].length == 1) 18.sp else 11.sp,
                fontWeight = FontWeight.Black
            )
            if (parts.size > 1) Text(parts[1], color = if (isFour) ScoringBlue else ScoringAccent, fontSize = 7.sp, fontWeight = FontWeight.Black)
        }
    }
}

// ── More Actions Bar ──────────────────────────────────────
@Composable
private fun MoreActionsBar(expanded: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp)
            .background(Color(0xFF131D2D))
            .border(1.dp, ScoringStroke)
            .clickable(onClick = onToggle)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(Modifier.size(14.dp)) {
            val s = Stroke(1.5.dp.toPx(), cap = StrokeCap.Round)
            drawLine(Color(0xFFC1FF00), Offset(0f, size.height * 0.3f), Offset(size.width, size.height * 0.3f), s.width)
            drawLine(Color(0xFFC1FF00), Offset(size.width * 0.25f, size.height * 0.65f), Offset(size.width, size.height * 0.65f), s.width)
            drawLine(Color(0xFFC1FF00), Offset(size.width * 0.5f, size.height), Offset(size.width, size.height), s.width)
        }
        Text(
            "MORE ACTIONS",
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(start = 10.dp).weight(1f)
        )
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color(0xFF273040)),
            contentAlignment = Alignment.Center
        ) {
            Text(if (expanded) "∨" else "∧", color = ScoringAccent, fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
    }
}

// ── Advanced Options Sheet ────────────────────────────────
@Composable
private fun AdvancedOptionsSheet(
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
    canChangeTarget: Boolean,
    onChangeTarget: () -> Unit,
    onPenaltyRuns: () -> Unit,
    onMatchBreak: () -> Unit,
    onAbandonMatch: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(Color(0xFF0C1828))
            .border(1.dp, ScoringStroke, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(horizontal = 14.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Drag handle
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFF3F5068))
        )
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Advanced Match Options", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black)
                Text("Rarely used tournament controls.", color = ScoringMuted, fontSize = 9.sp, modifier = Modifier.padding(top = 2.dp))
            }
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E2D42))
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Text("✕", color = ScoringMuted, fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
        }
        Spacer(Modifier.height(16.dp))
        // Options grid
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionOption("Change\nTarget", false, Modifier.weight(1f), enabled = canChangeTarget, onClick = onChangeTarget) { ScoreTargetIcon(Modifier.size(18.dp), ScoringAccent) }
                ActionOption("Penalty Runs", false, Modifier.weight(1f), onClick = onPenaltyRuns) { ScorePlusIcon(Modifier.size(18.dp), ScoringAccent) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionOption("D/L Method", false, Modifier.weight(1f)) { ScoreBoltIcon(Modifier.size(18.dp), ScoringAccent) }
                ActionOption("Match Break", false, Modifier.weight(1f), onClick = onMatchBreak) { ScoreClockIcon(Modifier.size(18.dp), ScoringAccent) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionOption("Change\nScorer", false, Modifier.weight(1f)) { ScorePersonIcon(Modifier.size(18.dp), ScoringAccent) }
                ActionOption("Abandon\nMatch", true, Modifier.weight(1f), onClick = onAbandonMatch) { ScoreNoIcon(Modifier.size(18.dp), Color(0xFFFF5252)) }
            }
        }
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF172236))
                .border(1.dp, ScoringStroke, RoundedCornerShape(10.dp))
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center
        ) {
            Text("Close", color = ScoringMuted, fontSize = 13.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun ActionOption(
    label: String,
    danger: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
    icon: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(if (!enabled) Color(0xFF101723) else if (danger) Color(0xFF241320) else Color(0xFF111B2B))
            .border(1.dp, if (danger) Color(0xFF5A2035) else ScoringStroke, RoundedCornerShape(9.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (danger) Color(0xFF3E1E2D) else Color(0xFF182539)),
            contentAlignment = Alignment.Center
        ) { icon() }
        Text(
            label,
            color = if (danger) Color(0xFFFF6B6B) else Color.White,
            fontSize = 10.sp,
            lineHeight = 13.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 10.dp)
        )
    }
}

@Composable
private fun NumberEntryDialog(
    title: String,
    subtitle: String,
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    onCancel: () -> Unit,
    confirmLabel: String,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onCancel) {
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(OutSheetBackground)
                .border(1.dp, ScoringStroke, RoundedCornerShape(18.dp)).padding(20.dp)
        ) {
            Text(title, color = ScoringAccent, fontSize = 17.sp, fontWeight = FontWeight.Black)
            Text(subtitle, color = ScoringMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 7.dp, bottom = 16.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold),
                decorationBox = { inner ->
                    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp)).background(Color(0xFF101A28)).padding(14.dp)) {
                        if (value.isEmpty()) Text("Enter runs", color = ScoringMuted, fontSize = 15.sp)
                        inner()
                    }
                }
            )
            error?.let { Text(it, color = Color(0xFFFF6677), fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp)) }
            Row(Modifier.fillMaxWidth().padding(top = 18.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DialogActionButton("CANCEL", true, Modifier.weight(1f), onCancel, primary = false)
                DialogActionButton(confirmLabel, true, Modifier.weight(1f), onConfirm)
            }
        }
    }
}

@Composable
private fun PenaltyTeamSelectionDialog(
    teams: List<MatchTeam>,
    selectedTeamId: String?,
    onSelect: (String) -> Unit,
    onCancel: () -> Unit,
    onNext: () -> Unit
) {
    Dialog(onDismissRequest = onCancel) {
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(OutSheetBackground)
            .border(1.dp, ScoringStroke, RoundedCornerShape(18.dp)).padding(18.dp)) {
            Text("PENALTY RUNS", color = ScoringAccent, fontSize = 17.sp, fontWeight = FontWeight.Black)
            Text("Choose which team receives the penalty.", color = ScoringMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp, bottom = 16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                teams.take(2).forEach { team ->
                    val selected = selectedTeamId == team.teamId
                    Column(
                        Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                            .background(if (selected) ScoringAccent.copy(alpha = .12f) else Color(0xFF101A28))
                            .border(1.dp, if (selected) ScoringAccent else ScoringStroke, RoundedCornerShape(12.dp))
                            .clickable { onSelect(team.teamId) }.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(Modifier.size(54.dp).clip(CircleShape).background(Color(0xFF26364A)).border(2.dp, if (selected) ScoringAccent else ScoringStroke, CircleShape), contentAlignment = Alignment.Center) {
                            val logo = team.logoUrl
                            if (!logo.isNullOrBlank()) {
                                AsyncImage(model = logo, contentDescription = "${team.name} logo", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(CircleShape))
                            } else {
                                Text(team.shortName.takeIf(String::isNotBlank)?.take(3)?.uppercase() ?: team.name.take(1).uppercase(), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                            }
                        }
                        Text(team.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 2, modifier = Modifier.padding(top = 9.dp))
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 18.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DialogActionButton("CANCEL", true, Modifier.weight(1f), onCancel, primary = false)
                DialogActionButton("NEXT", selectedTeamId != null, Modifier.weight(1f), onNext)
            }
        }
    }
}

@Composable
private fun ConfirmCancelDialog(
    title: String,
    message: String,
    confirmLabel: String,
    destructive: Boolean = false,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onCancel) {
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(OutSheetBackground)
            .border(1.dp, if (destructive) Color(0xFF713040) else ScoringStroke, RoundedCornerShape(18.dp)).padding(20.dp)) {
            Text(title, color = if (destructive) Color(0xFFFF6A78) else ScoringAccent, fontSize = 17.sp, fontWeight = FontWeight.Black)
            Text(message, color = Color.White, fontSize = 13.sp, lineHeight = 19.sp, modifier = Modifier.padding(top = 9.dp, bottom = 18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DialogActionButton("CANCEL", true, Modifier.weight(1f), onCancel, primary = false)
                DialogActionButton(confirmLabel, true, Modifier.weight(1f), onConfirm, destructive)
            }
        }
    }
}

@Composable
private fun DialogActionButton(
    label: String,
    enabled: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
    destructive: Boolean = false,
    primary: Boolean = true
) {
    Box(
        modifier.height(46.dp).clip(RoundedCornerShape(9.dp))
            .background(when {
                !enabled -> Color(0xFF252E3A)
                destructive -> Color(0xFFB3263D)
                primary -> ScoringAccent
                else -> Color(0xFF252E3A)
            })
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (enabled && primary) ScoringBg else if (enabled) Color.White else ScoringMuted, fontSize = 11.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
    }
}

// ── Wagon Wheel Sheet ─────────────────────────────────────
@Composable
private fun WagonWheelBottomSheet(runs: Int, modifier: Modifier = Modifier, onClose: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(380.dp)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(Color(0xFFF2F4F6))
            .padding(top = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFFB0B8C2))
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("✕", color = Color(0xFF1B2024), fontSize = 20.sp, fontWeight = FontWeight.Medium, modifier = Modifier.clickable(onClick = onClose))
            Column(modifier = Modifier.padding(start = 14.dp).weight(1f)) {
                Text("Select shot direction", color = Color(0xFF888F99), fontSize = 11.sp)
                Text("$runs Run${if (runs != 1) "s" else ""}", color = Color(0xFF008E55), fontSize = 14.sp, fontWeight = FontWeight.Black)
            }
            Text("Skip Ball", color = Color(0xFF5E5E5E), fontSize = 12.sp, textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline)
        }
        WagonWheel(modifier = Modifier.size(300.dp).padding(bottom = 8.dp))
    }
}

@Composable
private fun WagonWheel(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val radius = size.minDimension * 0.49f
            val center = Offset(size.width / 2f, size.height / 2f)
            drawCircle(Brush.verticalGradient(listOf(Color(0xFF617E8B), Color(0xFF0A161D), Color(0xFF000000)), startY = center.y - radius, endY = center.y + radius), radius = radius, center = center)
            drawArc(Color(0xFF48B94C), 0f, 360f, true, topLeft = Offset(center.x - radius * 0.77f, center.y - radius * 0.77f), size = Size(radius * 1.54f, radius * 1.54f))
            drawCircle(Color(0x5535C846), radius = radius * 0.53f, center = center)
            repeat(8) { i ->
                val angle = Math.toRadians((i * 45 - 90).toDouble())
                drawLine(Color(0xCFE5EEF0), center, Offset((center.x + kotlin.math.cos(angle) * radius).toFloat(), (center.y + kotlin.math.sin(angle) * radius).toFloat()), strokeWidth = 1.dp.toPx())
            }
            drawCircle(Color(0x88DDE9E8), radius = radius * 0.72f, center = center, style = Stroke(width = 1.dp.toPx()))
            drawCircle(Color(0x88DDE9E8), radius = radius * 0.28f, center = center, style = Stroke(width = 1.dp.toPx()))
            drawLine(Color(0xFFFFD96A), center, Offset(center.x, center.y + radius * 0.16f), strokeWidth = 8.dp.toPx(), cap = StrokeCap.Square)
        }
        Text("Off", color = Color(0x3348D860), fontSize = 26.sp, fontWeight = FontWeight.Black, modifier = Modifier.offset(x = (-58).dp, y = 18.dp))
        Text("Leg", color = Color(0x3348D860), fontSize = 26.sp, fontWeight = FontWeight.Black, modifier = Modifier.offset(x = 62.dp, y = 18.dp))
    }
}

// ── Canvas Icons ──────────────────────────────────────────
@Composable
private fun ScoreArrow(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        val path = Path().apply {
            moveTo(size.width * 0.66f, size.height * 0.24f)
            lineTo(size.width * 0.34f, size.height * 0.5f)
            lineTo(size.width * 0.66f, size.height * 0.76f)
        }
        drawPath(path, tint, style = stroke)
    }
}

@Composable
private fun ScoreHelpIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.38f, center = Offset(size.width * 0.5f, size.height * 0.5f), style = stroke)
        drawArc(tint, 210f, 230f, false, topLeft = Offset(size.width * 0.33f, size.height * 0.22f), size = Size(size.width * 0.34f, size.height * 0.34f), style = stroke)
        drawCircle(tint, radius = size.minDimension * 0.035f, center = Offset(size.width * 0.5f, size.height * 0.72f))
    }
}

@Composable
private fun ScoreGearIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.22f, center = Offset(size.width * 0.5f, size.height * 0.5f), style = stroke)
        repeat(8) { index ->
            val angle = Math.toRadians((index * 45).toDouble())
            val start = Offset((size.width * 0.5f + kotlin.math.cos(angle) * size.width * 0.31f).toFloat(), (size.height * 0.5f + kotlin.math.sin(angle) * size.height * 0.31f).toFloat())
            val end = Offset((size.width * 0.5f + kotlin.math.cos(angle) * size.width * 0.4f).toFloat(), (size.height * 0.5f + kotlin.math.sin(angle) * size.height * 0.4f).toFloat())
            drawLine(tint, start, end, strokeWidth = stroke.width, cap = StrokeCap.Round)
        }
    }
}

@Composable
private fun ScoreTargetIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        drawCircle(tint, radius = size.minDimension * 0.35f, center = Offset(size.width * 0.5f, size.height * 0.5f), style = Stroke(width = 1.5.dp.toPx()))
        drawCircle(tint, radius = size.minDimension * 0.16f, center = Offset(size.width * 0.5f, size.height * 0.5f), style = Stroke(width = 1.5.dp.toPx()))
    }
}

@Composable
private fun ScorePlusIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        drawCircle(tint, radius = size.minDimension * 0.34f, center = Offset(size.width * 0.5f, size.height * 0.5f), style = Stroke(width = 1.5.dp.toPx()))
        drawLine(tint, Offset(size.width * 0.5f, size.height * 0.32f), Offset(size.width * 0.5f, size.height * 0.68f), strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.32f, size.height * 0.5f), Offset(size.width * 0.68f, size.height * 0.5f), strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
    }
}

@Composable
private fun ScoreBoltIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val path = Path().apply {
            moveTo(size.width * 0.58f, size.height * 0.08f)
            lineTo(size.width * 0.28f, size.height * 0.54f)
            lineTo(size.width * 0.52f, size.height * 0.54f)
            lineTo(size.width * 0.42f, size.height * 0.92f)
            lineTo(size.width * 0.76f, size.height * 0.43f)
            lineTo(size.width * 0.52f, size.height * 0.43f)
            close()
        }
        drawPath(path, tint)
    }
}

@Composable
private fun ScoreClockIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.35f, center = Offset(size.width * 0.5f, size.height * 0.5f), style = stroke)
        drawLine(tint, Offset(size.width * 0.5f, size.height * 0.5f), Offset(size.width * 0.5f, size.height * 0.3f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.5f, size.height * 0.5f), Offset(size.width * 0.66f, size.height * 0.58f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}

@Composable
private fun ScorePersonIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.12f, center = Offset(size.width * 0.5f, size.height * 0.33f), style = stroke)
        drawArc(tint, 205f, 130f, false, topLeft = Offset(size.width * 0.24f, size.height * 0.52f), size = Size(size.width * 0.52f, size.height * 0.36f), style = stroke)
    }
}

@Composable
private fun ScoreNoIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.35f, center = Offset(size.width * 0.5f, size.height * 0.5f), style = stroke)
        drawLine(tint, Offset(size.width * 0.28f, size.height * 0.28f), Offset(size.width * 0.72f, size.height * 0.72f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}

@Composable
private fun GroundSelectionDialog(
    runs: Int,
    onConfirm: (directionName: String, metadata: Map<String, String>) -> Unit,
    onCancel: () -> Unit
) {
    var selectedOffset by remember { mutableStateOf<Offset?>(null) }
    var selectedDirectionName by remember { mutableStateOf("Straight") }

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xEE081018)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.925f)
                    .heightIn(min = 300.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF101B2B))
                    .border(1.dp, ScoringStroke, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("SELECT SHOT DIRECTION", color = ScoringAccent, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
                        Text("$runs Runs Scored", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 2.dp))
                    }
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E2D42))
                            .clickable(onClick = onCancel),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✕", color = ScoringMuted, fontSize = 14.sp, fontWeight = FontWeight.Black)
                    }
                }

                Spacer(Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .size(340.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF000000))
                        .pointerInput(Unit) {
                            detectTapGestures { tapOffset ->
                                val normX = tapOffset.x / size.width
                                val normY = tapOffset.y / size.height
                                determineFieldPosition(normX, normY)?.let { direction ->
                                    selectedOffset = tapOffset
                                    selectedDirectionName = direction
                                }
                            }
                        }
                ) {
                    Image(
                        painter = painterResource(R.drawable.cricketgroundforscoring),
                        contentDescription = "Cricket ground boundary points",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val wicketStart = Offset(size.width * 0.5f, size.height * 0.50f)

                        drawCircle(Color(0xFFFFD96A), radius = 6.dp.toPx(), center = wicketStart)
                        drawCircle(Color.Black, radius = 3.dp.toPx(), center = wicketStart)

                        selectedOffset?.let { target ->
                            drawLine(
                                color = ScoringAccent,
                                start = wicketStart,
                                end = target,
                                strokeWidth = 3.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                            drawCircle(Color(0xFFC1FF00), radius = 8.dp.toPx(), center = target)
                            drawCircle(Color.Black, radius = 4.dp.toPx(), center = target)
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "Selected: $selectedDirectionName${if (selectedOffset != null) " (Tap ground to change)" else " (Tap ground to select direction)"}",
                    color = ScoringMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1B273A))
                            .border(1.dp, ScoringStroke, RoundedCornerShape(10.dp))
                            .clickable(onClick = onCancel),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Cancel", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ScoringAccent)
                            .clickable(onClick = {
                                val direction = selectedDirectionName
                                val meta = mapOf(
                                    "shotDirection" to direction,
                                    "shotX" to (selectedOffset?.x?.toString() ?: "0"),
                                    "shotY" to (selectedOffset?.y?.toString() ?: "0")
                                )
                                onConfirm(direction, meta)
                            }),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Confirm", color = ScoringBg, fontSize = 13.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

private data class FieldPositionHotspot(
    val name: String,
    val x: Float,
    val y: Float
)

// Positions are normalized to the labeled markers in cricketgroundforscoring.webp.
private val fieldPositionHotspots = listOf(
    FieldPositionHotspot("Third Man", 0.365f, 0.167f),
    FieldPositionHotspot("Deep Fine Leg", 0.632f, 0.165f),
    FieldPositionHotspot("Deep Square Leg", 0.778f, 0.315f),
    FieldPositionHotspot("Deep Mid Wicket", 0.779f, 0.567f),
    FieldPositionHotspot("Long On", 0.620f, 0.742f),
    FieldPositionHotspot("Long Off", 0.380f, 0.742f),
    FieldPositionHotspot("Deep Cover", 0.222f, 0.567f),
    FieldPositionHotspot("Deep Point", 0.226f, 0.315f)
)

private fun determineFieldPosition(normX: Float, normY: Float): String? {
    val dx = normX - 0.5f
    val dy = normY - 0.5f
    val distanceFromCenterSquared = dx * dx + dy * dy

    // Ignore taps outside the playing area; the artwork's boundary is a circle.
    val boundaryRadius = 0.45f
    if (distanceFromCenterSquared > boundaryRadius * boundaryRadius) return null

    // The pitch/wicket area has no directional label, so treat it as Straight.
    val straightRadius = 0.10f
    if (distanceFromCenterSquared <= straightRadius * straightRadius) return "Straight"

    // Resolve the tap to the closest labeled marker, matching the image layout
    // rather than assuming the field positions occupy equal angular slices.
    return fieldPositionHotspots.minByOrNull { hotspot ->
        val hotspotDx = normX - hotspot.x
        val hotspotDy = normY - hotspot.y
        hotspotDx * hotspotDx + hotspotDy * hotspotDy
    }?.name
}
