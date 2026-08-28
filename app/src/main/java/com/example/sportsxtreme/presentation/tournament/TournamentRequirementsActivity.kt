package com.example.sportsxtreme.presentation.tournament

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
import android.os.Bundle
import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.media.MediaPlayer
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.example.sportsxtreme.domain.model.Tournament
import com.example.sportsxtreme.domain.model.TournamentRequirements
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@AndroidEntryPoint
class TournamentRequirementsActivity : ComponentActivity() {
    private val viewModel: TournamentRequirementsViewModel by viewModels()
    private var locationCallback: ((String) -> Unit)? = null
    private val locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) fetchLocation()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        setContent {
            TournamentRequirementsScreen(
                summary = TournamentRequirementSummary(
                    tournamentId = intent.getStringExtra(EXTRA_TOURNAMENT_ID).orEmpty(),
                    date = intent.getStringExtra(EXTRA_START_DATE).orEmpty(),
                    matchForm = intent.getStringExtra(EXTRA_MATCH_FORM).orEmpty(),
                    ballType = intent.getStringExtra(EXTRA_BALL_TYPE).orEmpty()
                ),
                viewModel = viewModel,
                onSaved = { tournamentId, needsOfficials ->
                    val destination = if (needsOfficials) {
                        Intent(this, LeagueTournamentFlowActivity::class.java)
                            .putExtra(LeagueTournamentFlowActivity.EXTRA_TOURNAMENT_ID, tournamentId)
                    } else {
                        Intent(this, RegisterTournamentFinalPageActivity::class.java)
                            .putExtra(RegisterTournamentFinalPageActivity.EXTRA_TOURNAMENT_ID, tournamentId)
                    }
                    startActivity(destination)
                },
                onBack = { finish() },
                onLocationClick = { callback -> requestLocation(callback) }
            )
        }
    }

    companion object {
        const val EXTRA_START_DATE = "tournament_start_date"
        const val EXTRA_TOURNAMENT_ID = "tournament_id"
        const val EXTRA_MATCH_FORM = "tournament_match_form"
        const val EXTRA_BALL_TYPE = "tournament_ball_type"
    }

    private fun requestLocation(callback: (String) -> Unit) {
        locationCallback = callback
        val granted = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (granted) fetchLocation() else locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    @Suppress("MissingPermission")
    private fun fetchLocation() {
        val manager = getSystemService(LOCATION_SERVICE) as LocationManager
        val location = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER).mapNotNull { runCatching { manager.getLastKnownLocation(it) }.getOrNull() }.maxByOrNull { it.time } ?: return
        lifecycleScope.launch(Dispatchers.IO) {
            val address = runCatching { @Suppress("DEPRECATION") Geocoder(this@TournamentRequirementsActivity, Locale.getDefault()).getFromLocation(location.latitude, location.longitude, 1)?.firstOrNull() }.getOrNull()
            val properLocation = address?.getAddressLine(0)?.trim()?.takeIf { it.isNotBlank() }
                ?: listOfNotNull(address?.featureName, address?.thoroughfare, address?.subLocality, address?.locality, address?.adminArea)
                    .distinct().joinToString(", ")
                    .ifBlank { "%.5f, %.5f".format(Locale.US, location.latitude, location.longitude) }
            withContext(Dispatchers.Main) { locationCallback?.invoke(properLocation); locationCallback = null }
        }
    }
}

private data class TournamentRequirementSummary(
    val tournamentId: String,
    val date: String,
    val matchForm: String,
    val ballType: String
)

private val ReqAccent = Color(0xFFC1FF00)
private val ReqBg = Color(0xFF010509)
private val ReqPanel = Color(0xFF0B111C)
private val ReqField = Color(0xFF111828)
private val ReqStroke = Color(0xFF2E3950)
private val ReqMuted = Color(0xFF8E9C9A)
private val ReqCyan = Color(0xFF4DE9FF)
private val ReqGold = Color(0xFFFFB84D)

@Composable
private fun TournamentRequirementsScreen(
    summary: TournamentRequirementSummary,
    viewModel: TournamentRequirementsViewModel,
    onSaved: (String, Boolean) -> Unit,
    onBack: () -> Unit,
    onLocationClick: ((String) -> Unit) -> Unit
) {
    var requirements by remember { mutableStateOf(TournamentRequirements()) }
    var showSuccessAnimation by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is TournamentRequirementsViewModel.UiState.Saved) {
            val savedTournament = (uiState as TournamentRequirementsViewModel.UiState.Saved).tournament
            viewModel.reset()
            if (requirements.needsOfficials) {
                onSaved(savedTournament.id, true)
            } else {
                showSuccessAnimation = true
            }
        }
    }
    Box(Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReqBg)
            .drawBehind {
                drawCircle(Color(0x332C4D11), radius = size.width * 0.78f, center = Offset(size.width, size.height * 0.16f))
                drawCircle(Color(0x1A00D2FF), radius = size.width * 0.58f, center = Offset(0f, size.height * 0.78f))
            }
    ) {
        RequirementsTopBar(onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            RequirementHeroCard()
            RequirementStatsRow(summary)
            TournamentDetailsSection(requirements, onLocationClick) { requirements = it }
            WinningPrizeSection(requirements) { requirements = it }
            FormatSection(requirements) { requirements = it }
            NotesSection(requirements) { requirements = it }
            SmartFeaturesSection(
                requirements = requirements,
                onRequirementsChange = { requirements = it }
            )
            TermsPanel()
            if (uiState is TournamentRequirementsViewModel.UiState.Error) {
                Text((uiState as TournamentRequirementsViewModel.UiState.Error).message, color = Color.Red, fontSize = 13.sp)
            }
            ContinueButton(
                showContinue = requirements.needsOfficials,
                isSaving = uiState is TournamentRequirementsViewModel.UiState.Loading,
                onClick = {
                    viewModel.save(
                        Tournament(
                            id = summary.tournamentId,
                            startDate = summary.date,
                            matchForm = summary.matchForm,
                            ballType = summary.ballType
                        ),
                        requirements
                    )
                }
            )
        }
    }
    if (showSuccessAnimation) {
        SuccessAnimationOverlay(
            onFinished = { onSaved(summary.tournamentId, false) }
        )
    }
    }
}

@Composable
private fun SuccessAnimationOverlay(onFinished: () -> Unit) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.success))
    val context = androidx.compose.ui.platform.LocalContext.current
    val cheerPlayer = remember { MediaPlayer.create(context, R.raw.success_sound) }
    var cheerReleased by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        cheerPlayer.start()
    }
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            if (!cheerReleased) {
                if (cheerPlayer.isPlaying) cheerPlayer.stop()
                cheerPlayer.release()
                cheerReleased = true
            }
        }
    }
    LaunchedEffect(composition) {
        if (composition != null) {
            kotlinx.coroutines.delay(2200)
            if (cheerPlayer.isPlaying) {
                cheerPlayer.stop()
            }
            cheerPlayer.release()
            cheerReleased = true
            onFinished()
        }
    }
    Box(
        modifier = Modifier.fillMaxSize().background(ReqBg.copy(alpha = 0.96f)),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            iterations = 1,
            modifier = Modifier.size(260.dp)
        )
    }
}

@Composable
private fun RequirementsTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF07101A), Color(0xFF0D1C22), Color(0xFF07101A))
                )
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ReqArrowIcon(modifier = Modifier.size(30.dp).clickable(onClick = onBack), right = false)
        Text(
            "Team Requirements",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(Color(0x1FC1FF00))
                .border(1.dp, Color(0x55C1FF00), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("2", color = ReqAccent, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun RequirementHeroCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(178.dp)
            .shadow(16.dp, RoundedCornerShape(20.dp), clip = false)
            .drawBehind {
                drawCircle(ReqAccent.copy(alpha = 0.13f), radius = size.width * 0.36f, center = Offset(size.width * 0.12f, size.height * 1.02f))
                drawCircle(ReqCyan.copy(alpha = 0.08f), radius = size.width * 0.27f, center = Offset(size.width * 0.94f, size.height * 0.08f))
            }
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF17260D), Color(0xFF0B1A22), Color(0xFF111523))
                )
            )
            .border(1.2.dp, ReqAccent.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
    ) {
        Image(
            painter = painterResource(id = R.drawable.prizeimage_onboarding4),
            contentDescription = "Tournament prize",
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .height(178.dp)
                .width(190.dp),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xF007100B), Color(0xD007100B), Color(0x3307100B))
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 18.dp, end = 120.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Finalize setup", color = ReqAccent, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
            Text("Rules, prizes and entries", color = Color.White, fontSize = 25.sp, lineHeight = 28.sp, fontWeight = FontWeight.Black)
            Text(
                "Set team limits, entry fee, prize pool and public discovery options.",
                color = Color(0xFFC5D0CC),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
        StepBadge(modifier = Modifier.align(Alignment.TopEnd).padding(14.dp))
    }
}

@Composable
private fun StepBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xE60D1420))
            .border(1.dp, Color(0x664DE9FF), RoundedCornerShape(18.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("STEP 2 OF 2", color = ReqCyan, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun RequirementStatsRow(summary: TournamentRequirementSummary) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatCard(summary.date.ifBlank { "Not set" }, "Date", ReqAccent, Modifier.weight(1f))
        StatCard(summary.matchForm.ifBlank { "Not set" }, "Match Form", ReqCyan, Modifier.weight(1f))
        StatCard(summary.ballType.ifBlank { "Not set" }, "Ball Type", ReqGold, Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(value: String, label: String, accent: Color, modifier: Modifier) {
    Column(
        modifier = modifier
            .height(82.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp), clip = false)
            .drawBehind {
                drawCircle(accent.copy(alpha = 0.1f), radius = size.width * 0.5f, center = Offset(size.width * 0.5f, size.height * 0.05f))
            }
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF101827), Color(0xFF0B111C))))
            .border(1.dp, accent.copy(alpha = 0.58f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = accent, fontSize = if (value.length > 7) 15.sp else 21.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(label, color = Color(0xFFBFCBC7), fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun TournamentDetailsSection(requirements: TournamentRequirements, onLocationClick: ((String) -> Unit) -> Unit, onChange: (TournamentRequirements) -> Unit) {
    ReqSection("Tournament Details", "ENTRY") {
        ReqInput("Tournament Location", "Bhubaneswar, Odisha", requirements.location, trailingIcon = { androidx.compose.material3.Icon(painterResource(R.drawable.baseline_edit_location_24), "Use current location", tint = ReqCyan, modifier = Modifier.size(20.dp).clickable { onLocationClick { onChange(requirements.copy(location = it)) } }) }) { onChange(requirements.copy(location = it)) }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ReqInput("Entry Fee", "Rs 999", requirements.entryFee, Modifier.weight(1f)) { onChange(requirements.copy(entryFee = it)) }
            ReqInput("Number of Teams", "10", requirements.numberOfTeams, Modifier.weight(1f)) { onChange(requirements.copy(numberOfTeams = it)) }
        }
        ReqInput("Match Duration", "How many hours is one match?", requirements.matchDuration) { onChange(requirements.copy(matchDuration = it)) }
        InfoPanel("Only the organizer can edit team information after the tournament is created.")
    }
}

@Composable
private fun WinningPrizeSection(requirements: TournamentRequirements, onChange: (TournamentRequirements) -> Unit) {
    ReqSection("Winning Prize", "REWARDS", iconRes = R.drawable.tournamentlogo) {
        ChipRowReq(listOf("Cash", "Trophy", "Both"), requirements.prizeType) { onChange(requirements.copy(prizeType = it)) }
        ReqInput("Prize Pool", "Rs 50,000", requirements.prizePool) { onChange(requirements.copy(prizePool = it)) }
        ReqInput("Runner-up Prize", "Rs 10,000", requirements.runnerUpPrize) { onChange(requirements.copy(runnerUpPrize = it)) }
    }
}

@Composable
private fun FormatSection(requirements: TournamentRequirements, onChange: (TournamentRequirements) -> Unit) {
    ReqSection("Tournament Format", "FIXTURES") {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SelectBox("League", selected = requirements.tournamentFormat == "League", modifier = Modifier.weight(1f)) { onChange(requirements.copy(tournamentFormat = "League")) }
            SelectBox("Knockout", selected = requirements.tournamentFormat == "Knockout", modifier = Modifier.weight(1f)) { onChange(requirements.copy(tournamentFormat = "Knockout")) }
        }
        InfoPanel("Knockout teams are eliminated after one loss. League format keeps all teams in the race longer.")
    }
}

@Composable
private fun NotesSection(requirements: TournamentRequirements, onChange: (TournamentRequirements) -> Unit) {
    ReqSection("Tournament Notes", "RULES") {
        ReqInput("Notes", "Add rules, prize breakdown, reporting time, dress code, or important instructions", requirements.notes, minHeight = 96.dp) { onChange(requirements.copy(notes = it)) }
    }
}

@Composable
private fun SmartFeaturesSection(
    requirements: TournamentRequirements,
    onRequirementsChange: (TournamentRequirements) -> Unit
) {
    ReqSection("Smart Features", "PUBLIC FEED", iconRes = R.drawable.organiserss) {
        ToggleLine("Invite all the players of my previous tournaments", "Notify past players and teams instantly.", requirements.invitePreviousPlayers) {
            onRequirementsChange(requirements.copy(invitePreviousPlayers = !requirements.invitePreviousPlayers))
        }
        ToggleLine(
            text = "Do you need officials? Umpire, scorer, streamer",
            helper = "Post to the officials feed and find match staff faster.",
            active = requirements.needsOfficials,
            onClick = { onRequirementsChange(requirements.copy(needsOfficials = !requirements.needsOfficials)) }
        )
    }
}

@Composable
private fun TermsPanel() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(7.dp, RoundedCornerShape(16.dp), clip = false)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF101A14), Color(0xFF0B111C))))
            .border(1.dp, Color(0x994F6B1F), RoundedCornerShape(16.dp))
            .padding(15.dp),
        verticalAlignment = Alignment.Top
    ) {
        SmallBadge(active = true)
        Text(
            "I agree to all SportsXtreme terms and conditions for creating and managing this tournament.",
            color = Color.White,
            fontSize = 13.sp,
            lineHeight = 17.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

@Composable
private fun ContinueButton(showContinue: Boolean, isSaving: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .shadow(12.dp, RoundedCornerShape(18.dp), clip = false)
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.horizontalGradient(listOf(ReqAccent, Color(0xFFDFFF6C), ReqGold)))
            .clickable(enabled = !isSaving, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(if (isSaving) "Saving..." else if (showContinue) "Continue" else "Done", color = Color(0xFF111604), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun ReqSection(
    title: String,
    endBadge: String? = null,
    iconRes: Int? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionIcon(iconRes)
            Text(
                title,
                color = Color.White,
                fontSize = 17.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(start = 8.dp).weight(1f)
            )
            endBadge?.let {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF17240B))
                        .border(1.dp, Color(0x705C7E22), RoundedCornerShape(14.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(it, color = ReqAccent, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(17.dp), clip = false)
                .drawBehind {
                    drawCircle(ReqCyan.copy(alpha = 0.04f), radius = size.width * 0.42f, center = Offset(size.width * 0.9f, 0f))
                }
                .clip(RoundedCornerShape(17.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF101827), ReqPanel)))
                .border(1.dp, Color(0xFF31405C), RoundedCornerShape(17.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp),
            content = content
        )
    }
}

@Composable
private fun ReqInput(label: String, placeholder: String, value: String, modifier: Modifier = Modifier, minHeight: Dp = 52.dp, trailingIcon: (@Composable (() -> Unit))? = null, onValueChange: (String) -> Unit) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(label, color = Color(0xFF99A9A5), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
            singleLine = minHeight <= 56.dp,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(minHeight)
                        .clip(RoundedCornerShape(13.dp))
                        .background(Brush.verticalGradient(listOf(Color(0xFF172033), ReqField)))
                        .border(1.dp, Color(0xFF42526F), RoundedCornerShape(13.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = if (minHeight > 56.dp) Alignment.TopStart else Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(placeholder, color = Color(0xFF768784), fontSize = 14.sp, lineHeight = 18.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                    innerTextField()
                    trailingIcon?.let { icon -> Box(Modifier.align(Alignment.CenterEnd)) { icon() } }
                }
            }
        )
    }
}

@Composable
private fun ChipRowReq(labels: List<String>, selectedLabel: String, onLabelSelected: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        labels.forEachIndexed { index, label ->
            Box(
                modifier = Modifier
                    .height(38.dp)
                    .clip(RoundedCornerShape(19.dp))
                    .background(if (label == selectedLabel) ReqAccent else Color(0xFF1A2231))
                    .border(1.dp, if (label == selectedLabel) Color(0xFFDFFF6C) else Color(0xFF34405A), RoundedCornerShape(19.dp))
                    .clickable { onLabelSelected(label) }
                    .padding(horizontal = 17.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(label, color = if (label == selectedLabel) Color(0xFF111604) else Color(0xFFBBC7C4), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun SelectBox(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) ReqAccent else Color(0xFF1A2231))
            .border(1.dp, if (selected) Color(0xFFDFFF6C) else Color(0xFF34405A), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (selected) Color(0xFF111604) else Color.White, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun InfoPanel(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF162010))
            .border(1.dp, Color(0x805C7E22), RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SmallBadge(active = true)
        Text(text, color = Color(0xFFD8E1DA), fontSize = 12.sp, lineHeight = 16.sp, modifier = Modifier.padding(start = 10.dp))
    }
}

@Composable
private fun ToggleLine(text: String, helper: String, active: Boolean, onClick: (() -> Unit)? = null) {
    Row(
        modifier = if (onClick != null) Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(4.dp) else Modifier,
        verticalAlignment = Alignment.Top
    ) {
        SmallBadge(active = active)
        Column(modifier = Modifier.padding(start = 10.dp)) {
            Text(text, color = Color.White, fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.ExtraBold)
            Text(helper, color = ReqMuted, fontSize = 12.sp, lineHeight = 16.sp, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun SmallBadge(active: Boolean) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(if (active) ReqAccent else Color.Transparent)
            .border(1.dp, if (active) ReqAccent else Color(0xFF3B455B), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (active) CheckMark(Modifier.size(15.dp), Color(0xFF111604))
    }
}

@Composable
private fun SectionIcon(iconRes: Int?) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(Brush.radialGradient(listOf(Color(0x334DE9FF), Color(0xFF101827))))
            .border(1.dp, ReqCyan.copy(alpha = 0.52f), RoundedCornerShape(11.dp))
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        if (iconRes == null) {
            SectionGlyph()
        } else {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun SectionGlyph() {
    Canvas(Modifier.fillMaxSize()) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        drawLine(ReqAccent, Offset(size.width * 0.2f, size.height * 0.28f), Offset(size.width * 0.8f, size.height * 0.28f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(ReqCyan, Offset(size.width * 0.2f, size.height * 0.5f), Offset(size.width * 0.7f, size.height * 0.5f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(ReqGold, Offset(size.width * 0.2f, size.height * 0.72f), Offset(size.width * 0.58f, size.height * 0.72f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}

@Composable
private fun CheckMark(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.18f, size.height * 0.54f), Offset(size.width * 0.42f, size.height * 0.76f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.42f, size.height * 0.76f), Offset(size.width * 0.84f, size.height * 0.24f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}

@Composable
private fun ReqArrowIcon(modifier: Modifier, right: Boolean) {
    Canvas(modifier) {
        val tint = Color(0xFFC7D2CF)
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        val path = Path().apply {
            if (right) {
                moveTo(size.width * 0.35f, size.height * 0.25f)
                lineTo(size.width * 0.62f, size.height * 0.5f)
                lineTo(size.width * 0.35f, size.height * 0.75f)
            } else {
                moveTo(size.width * 0.65f, size.height * 0.25f)
                lineTo(size.width * 0.38f, size.height * 0.5f)
                lineTo(size.width * 0.65f, size.height * 0.75f)
            }
        }
        drawPath(path, tint, style = stroke)
    }
}
