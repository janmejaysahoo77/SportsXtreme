package com.example.sportsxtreme.presentation.tournament

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stadium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.sportsxtreme.R
import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.Tournament
import com.example.sportsxtreme.domain.usecase.MatchUseCases
import com.example.sportsxtreme.presentation.match.SelectPlayingTeamsActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@AndroidEntryPoint
class ScheduleMatchActivity : ComponentActivity() {
    @Inject lateinit var matchUseCases: MatchUseCases
    private val tournamentViewModel: TournamentFlowViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)

        val tournamentId = intent.getStringExtra(EXTRA_TOURNAMENT_ID).orEmpty()
        val matchId = intent.getStringExtra(EXTRA_MATCH_ID).orEmpty()
        val teamAId = intent.getStringExtra(SelectPlayingTeamsActivity.EXTRA_TEAM_A_ID).orEmpty()
        val teamBId = intent.getStringExtra(SelectPlayingTeamsActivity.EXTRA_TEAM_B_ID).orEmpty()
        val passedTeamAName = intent.getStringExtra(SelectPlayingTeamsActivity.EXTRA_TEAM_A_NAME).orEmpty()
        val passedTeamBName = intent.getStringExtra(SelectPlayingTeamsActivity.EXTRA_TEAM_B_NAME).orEmpty()
        tournamentViewModel.load(tournamentId)

        setContent {
            val tournament by tournamentViewModel.tournament.collectAsState()
            val registeredTeams = rememberRegisteredTournamentTeams(tournamentId)
            var selectedTeamAId by rememberSaveable { mutableStateOf(teamAId) }
            var selectedTeamBId by rememberSaveable { mutableStateOf(teamBId) }
            var selectedTeamAName by rememberSaveable { mutableStateOf(passedTeamAName) }
            var selectedTeamBName by rememberSaveable { mutableStateOf(passedTeamBName) }
            val teamAName = registeredTeams.firstOrNull { it.id == selectedTeamAId }?.name
                ?.takeIf { it.isNotBlank() } ?: selectedTeamAName.ifBlank { "Team A" }
            val teamBName = registeredTeams.firstOrNull { it.id == selectedTeamBId }?.name
                ?.takeIf { it.isNotBlank() } ?: selectedTeamBName.ifBlank { "Team B" }
            val teamPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode != RESULT_OK) return@rememberLauncherForActivityResult
                val data = result.data ?: return@rememberLauncherForActivityResult
                val slot = data.getStringExtra(SelectPlayingTeamsActivity.EXTRA_TEAM_SLOT).orEmpty()
                val pickedId = data.getStringExtra(SelectPlayingTeamsActivity.EXTRA_SELECTED_TEAM_ID).orEmpty()
                val pickedName = data.getStringExtra(SelectPlayingTeamsActivity.EXTRA_SELECTED_TEAM_NAME).orEmpty()
                if (pickedId.isBlank() || pickedName.isBlank()) return@rememberLauncherForActivityResult
                val otherTeamId = if (slot == "A") selectedTeamBId else selectedTeamAId
                if (slot !in setOf("A", "B")) return@rememberLauncherForActivityResult
                if (pickedId == otherTeamId) {
                    Toast.makeText(this@ScheduleMatchActivity, "Choose a different team for each side", Toast.LENGTH_SHORT).show()
                    return@rememberLauncherForActivityResult
                }
                if (slot == "A") {
                    selectedTeamAId = pickedId
                    selectedTeamAName = pickedName
                } else {
                    selectedTeamBId = pickedId
                    selectedTeamBName = pickedName
                }
            }
            fun openTeamPicker(slot: String) {
                if (tournamentId.isBlank()) {
                    Toast.makeText(this@ScheduleMatchActivity, "Tournament details are unavailable", Toast.LENGTH_SHORT).show()
                    return
                }
                teamPickerLauncher.launch(
                    Intent(this@ScheduleMatchActivity, com.example.sportsxtreme.presentation.match.RegisteredTeamsOfATournament::class.java)
                        .putExtra(SelectPlayingTeamsActivity.EXTRA_TOURNAMENT_ID, tournamentId)
                        .putExtra(SelectPlayingTeamsActivity.EXTRA_TEAM_SLOT, slot)
                        .putExtra(SelectPlayingTeamsActivity.EXTRA_TEAM_A_ID, selectedTeamAId)
                        .putExtra(SelectPlayingTeamsActivity.EXTRA_TEAM_A_NAME, teamAName)
                        .putExtra(SelectPlayingTeamsActivity.EXTRA_TEAM_B_ID, selectedTeamBId)
                        .putExtra(SelectPlayingTeamsActivity.EXTRA_TEAM_B_NAME, teamBName)
                )
            }

            var dateMillis by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }
            var matchTime by rememberSaveable { mutableStateOf("08:00 PM") }
            var venue by rememberSaveable { mutableStateOf("") }
            var showVenueDialog by rememberSaveable { mutableStateOf(false) }
            var isSaving by rememberSaveable { mutableStateOf(false) }
            var showSuccess by rememberSaveable { mutableStateOf(false) }
            var venueDraft by rememberSaveable { mutableStateOf("") }

            LaunchedEffect(tournament?.id) {
                val current = tournament ?: return@LaunchedEffect
                if (venue.isBlank()) {
                    venue = current.ground.trim()
                        .ifBlank { current.requirements.location.trim() }
                        .ifBlank { current.city.trim() }
                }
            }

            Box(Modifier.fillMaxSize()) {
                ScheduleMatchScreen(
                    tournament = tournament,
                    registeredTeamCount = registeredTeams.size,
                    teamA = teamAName,
                    teamB = teamBName,
                    date = formatScheduleDate(dateMillis),
                    time = matchTime,
                    venue = venue,
                    isSaving = isSaving,
                    onBack = { finish() },
                    onSelectTeamA = { openTeamPicker("A") },
                    onSelectTeamB = { openTeamPicker("B") },
                    onDateClick = { showDatePicker(dateMillis) { dateMillis = it } },
                    onTimeClick = { showTimePicker(matchTime) { matchTime = it } },
                    onVenueClick = {
                        venueDraft = venue
                        showVenueDialog = true
                    },
                    onSchedule = {
                        if (tournamentId.isBlank() || matchId.isBlank()) {
                            Toast.makeText(this@ScheduleMatchActivity, "Tournament or match details are missing", Toast.LENGTH_LONG).show()
                        } else if (tournament == null) {
                            Toast.makeText(this@ScheduleMatchActivity, "Tournament details are still loading", Toast.LENGTH_SHORT).show()
                        } else if (selectedTeamAId.isBlank() || selectedTeamBId.isBlank() || selectedTeamAId == selectedTeamBId) {
                            Toast.makeText(this@ScheduleMatchActivity, "Choose two different registered teams", Toast.LENGTH_LONG).show()
                        } else if (venue.isBlank()) {
                            Toast.makeText(this@ScheduleMatchActivity, "Enter a match venue", Toast.LENGTH_SHORT).show()
                        } else if (!isSaving) {
                            isSaving = true
                            lifecycleScope.launch {
                                try {
                                    val scheduledAt = combineDateAndTime(dateMillis, matchTime)
                                    when (val result = matchUseCases.updateMatchDetails(
                                        matchId = matchId,
                                        venue = venue.trim(),
                                        matchDateEpochMs = scheduledAt,
                                        matchTime = matchTime
                                    )) {
                                        is Resource.Success -> {
                                            val currentTournament = tournament
                                            val tournamentLocation = listOfNotNull(
                                                currentTournament?.ground?.takeIf { it.isNotBlank() },
                                                currentTournament?.city?.takeIf { it.isNotBlank() },
                                                currentTournament?.requirements?.location?.takeIf { it.isNotBlank() }
                                            ).distinct().joinToString(" · ")
                                            FirebaseFirestore.getInstance().collection("matches").document(matchId)
                                                .set(
                                                    mapOf(
                                                        "tournamentId" to tournamentId,
                                                        "tournamentName" to currentTournament?.name.orEmpty(),
                                                        "tournamentLocation" to tournamentLocation,
                                                        "teamAId" to selectedTeamAId,
                                                        "teamAName" to teamAName,
                                                        "teamBId" to selectedTeamBId,
                                                        "teamBName" to teamBName,
                                                        "matchDateEpochMs" to scheduledAt,
                                                        "matchDate" to formatScheduleDate(scheduledAt),
                                                        "matchTime" to matchTime,
                                                        "venue" to venue.trim(),
                                                        "scheduleStatus" to "SCHEDULED",
                                                        "updatedAt" to FieldValue.serverTimestamp()
                                                    ),
                                                    SetOptions.merge()
                                                ).await()
                                            showSuccess = true
                                        }
                                        is Resource.Error -> {
                                            isSaving = false
                                            Toast.makeText(
                                                this@ScheduleMatchActivity,
                                                result.message ?: "Unable to schedule match",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                        is Resource.Loading -> Unit
                                    }
                                } catch (error: Exception) {
                                    isSaving = false
                                    Toast.makeText(
                                        this@ScheduleMatchActivity,
                                        error.message ?: "Unable to save scheduled match",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    }
                )

                if (showVenueDialog) {
                    AlertDialog(
                        onDismissRequest = { showVenueDialog = false },
                        title = { Text("Match venue") },
                        text = {
                            OutlinedTextField(
                                value = venueDraft,
                                onValueChange = { venueDraft = it },
                                label = { Text("Ground or venue") },
                                singleLine = true
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                venue = venueDraft.trim()
                                showVenueDialog = false
                            }) { Text("Save") }
                        },
                        dismissButton = { TextButton(onClick = { showVenueDialog = false }) { Text("Cancel") } }
                    )
                }

                if (showSuccess) {
                    ScheduleSuccessOverlay(
                        tournamentName = tournament?.name.orEmpty().ifBlank { "Tournament" },
                        onFinished = {
                            startActivity(
                                Intent(this@ScheduleMatchActivity, RegisterTournamentFinalPageActivity::class.java)
                                    .putExtra(RegisterTournamentFinalPageActivity.EXTRA_TOURNAMENT_ID, tournamentId)
                                    .putExtra(RegisterTournamentFinalPageActivity.EXTRA_INITIAL_TAB, 2)
                                    .putExtra(RegisterTournamentFinalPageActivity.EXTRA_INITIAL_MATCH_TAB, 1)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            )
                            finish()
                        }
                    )
                }
            }
        }
    }

    private fun showDatePicker(currentDate: Long, onSelected: (Long) -> Unit) {
        val calendar = Calendar.getInstance().apply { timeInMillis = currentDate }
        DatePickerDialog(
            this,
            { _, year, month, day ->
                onSelected(Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, day)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker(currentTime: String, onSelected: (String) -> Unit) {
        val parsed = runCatching { SimpleDateFormat("hh:mm a", Locale.getDefault()).parse(currentTime) }.getOrNull()
        val calendar = Calendar.getInstance().apply { if (parsed != null) time = parsed }
        TimePickerDialog(
            this,
            { _, hour, minute ->
                val time = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                }.time
                onSelected(SimpleDateFormat("hh:mm a", Locale.getDefault()).format(time))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).show()
    }

    companion object {
        const val EXTRA_TOURNAMENT_ID = "tournament_id"
        const val EXTRA_MATCH_ID = "match_id"
    }
}

private data class RegisteredScheduleTeam(val id: String, val name: String)

@Composable
private fun rememberRegisteredTournamentTeams(tournamentId: String): List<RegisteredScheduleTeam> {
    var teams by remember(tournamentId) { mutableStateOf(emptyList<RegisteredScheduleTeam>()) }
    DisposableEffect(tournamentId) {
        if (tournamentId.isBlank()) return@DisposableEffect onDispose { }
        val registration = FirebaseFirestore.getInstance().collection("tournaments")
            .document(tournamentId).collection("teams")
            .addSnapshotListener { snapshot, _ ->
                teams = snapshot?.documents.orEmpty().map { document ->
                    val name = document.getString("teamName")?.trim().orEmpty()
                        .ifBlank { document.getString("name")?.trim().orEmpty() }
                        .ifBlank { document.id }
                    RegisteredScheduleTeam(document.id, name)
                }.sortedBy { it.name.lowercase() }
            }
        onDispose { registration.remove() }
    }
    return teams
}

private val ScheduleNeon = Color(0xFFC6FF00)
private val ScheduleBg = Color(0xFF020914)
private val SchedulePanel = Color(0xFF071525)
private val ScheduleCard = Color(0xFF09192A)
private val ScheduleMuted = Color(0xFF91A0AF)

@Composable
private fun ScheduleMatchScreen(
    tournament: Tournament?,
    registeredTeamCount: Int,
    teamA: String,
    teamB: String,
    date: String,
    time: String,
    venue: String,
    isSaving: Boolean,
    onBack: () -> Unit,
    onSelectTeamA: () -> Unit,
    onSelectTeamB: () -> Unit,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    onVenueClick: () -> Unit,
    onSchedule: () -> Unit
) {
    Box(
        Modifier.fillMaxSize().background(ScheduleBg).drawBehind {
            drawRect(Brush.verticalGradient(listOf(Color(0xFF0A151B), Color(0xFF071116), Color(0xFF020609))))
            drawRect(Brush.radialGradient(listOf(Color(0x2545E9FF), Color(0x1045E9FF), Color.Transparent), center = Offset(size.width * .08f, size.height * .12f), radius = size.width * .85f))
            drawRect(Brush.radialGradient(listOf(Color(0x20C7FF1A), Color(0x0CC7FF1A), Color.Transparent), center = Offset(size.width * .94f, size.height * .58f), radius = size.width * .72f))
        }
    ) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            ScheduleTopBar(onBack, tournament, registeredTeamCount)
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(13.dp)
            ) {
                ScheduleSectionLabel("1", "Select Teams", "Choose two teams for this match")
                MatchTeams(teamA, teamB, onSelectTeamA, onSelectTeamB)
                ScheduleSectionLabel("2", "Match Details", "Set the date, time and venue")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ScheduleDetailTile(Icons.Default.CalendarMonth, "Match Date", date, Color(0xFF25C966), onDateClick, Modifier.weight(1f))
                    ScheduleDetailTile(Icons.Default.Schedule, "Match Time", time, Color(0xFFFFB800), onTimeClick, Modifier.weight(1f))
                    ScheduleDetailTile(Icons.Default.LocationOn, "Match Venue", venue.ifBlank { "Add venue" }, Color(0xFFAA5CFF), onVenueClick, Modifier.weight(1f))
                }
                ScheduleSectionLabel("3", "Match Preview", "Review the match details before scheduling")
                SchedulePreview(teamA, teamB, date, time, venue, tournament?.requirements?.tournamentFormat.orEmpty())
                ScheduleActionButton(
                    label = if (isSaving) "SCHEDULING…" else "SCHEDULE MATCH",
                    enabled = !isSaving,
                    onClick = onSchedule,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )
            }
        }
    }
}

@Composable
private fun ScheduleTopBar(onBack: () -> Unit, tournament: Tournament?, registeredTeamCount: Int) {
    Box(
        Modifier.fillMaxWidth().height(205.dp)
            .background(Color(0xFF0A101C))
            .drawBehind {
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(ScheduleNeon.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(size.width / 2f, -50f),
                        radius = size.height * 0.6f
                    )
                )
            }
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().height(101.dp).padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ArrowBack, "Back", tint = Color.White, modifier = Modifier.size(25.dp).clickable(onClick = onBack))
                Column(Modifier.padding(start = 16.dp)) {
                    Row {
                        Text("Schedule ", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Black)
                        Text("Match", color = ScheduleNeon, fontSize = 25.sp, fontWeight = FontWeight.Black)
                    }
                    Text("Set the teams, date, time and venue to begin the match", color = Color(0xFFBBC7D2), fontSize = 10.sp)
                }
            }
            TournamentBanner(tournament, registeredTeamCount, Modifier.padding(horizontal = 15.dp))
        }
    }
}

@Composable
private fun TournamentBanner(tournament: Tournament?, registeredTeamCount: Int, modifier: Modifier = Modifier) {
    val title = tournament?.name?.takeIf { it.isNotBlank() } ?: "Loading tournament…"
    val place = listOfNotNull(tournament?.city?.takeIf { it.isNotBlank() }, tournament?.ground?.takeIf { it.isNotBlank() })
        .joinToString(" · ").ifBlank { tournament?.requirements?.location.orEmpty().ifBlank { "Tournament location" } }
    val season = tournament?.startDate?.takeIf { it.isNotBlank() } ?: "Tournament fixture"
    val bannerShape = RoundedCornerShape(16.dp)
    Box(modifier.fillMaxWidth().height(91.dp).shadow(12.dp, bannerShape, clip = false, ambientColor = Color(0x553C9FFF), spotColor = Color(0x663C9FFF)).clip(bannerShape).background(Color(0xB2071525))) {
        tournament?.bannerUrl?.takeIf(String::isNotBlank)?.let { bannerUrl ->
            AsyncImage(model = bannerUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }
        Box(Modifier.fillMaxSize().background(Color(0xA8071525)).padding(13.dp)) {
        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(52.dp).clip(RoundedCornerShape(13.dp)).background(Color(0xFF102438)), contentAlignment = Alignment.Center) {
            val logoUrl = tournament?.logoUrl.orEmpty()
            if (logoUrl.isNotBlank()) {
                AsyncImage(model = logoUrl, contentDescription = "$title logo", modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(13.dp)), contentScale = ContentScale.Crop)
            } else {
                Icon(Icons.Default.EmojiEvents, null, tint = ScheduleNeon, modifier = Modifier.size(31.dp))
            }
        }
        Column(Modifier.padding(start = 10.dp).weight(1f)) {
            Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(season, color = Color(0xFF132000), fontSize = 7.sp, fontWeight = FontWeight.Black,
                modifier = Modifier.padding(top = 3.dp).clip(RoundedCornerShape(6.dp)).background(ScheduleNeon).padding(horizontal = 6.dp, vertical = 2.dp),
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(Modifier.padding(top = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = ScheduleNeon, modifier = Modifier.size(11.dp))
                Text(place, color = ScheduleMuted, fontSize = 10.sp, modifier = Modifier.padding(start = 2.dp).weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Icon(Icons.Default.Groups, null, tint = ScheduleNeon, modifier = Modifier.padding(start = 7.dp).size(11.dp))
                Text("$registeredTeamCount Teams", color = ScheduleMuted, fontSize = 9.sp, modifier = Modifier.padding(start = 2.dp))
            }
        }
        }
        }
    }
}

@Composable
private fun ScheduleSectionLabel(number: String, title: String, subtitle: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(32.dp).clip(CircleShape).background(ScheduleNeon), contentAlignment = Alignment.Center) {
            Text(number, color = Color(0xFF102000), fontSize = 17.sp, fontWeight = FontWeight.Black)
        }
        Column(Modifier.padding(start = 10.dp).weight(1f)) {
            Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
            if (subtitle.isNotBlank()) Text(subtitle, color = ScheduleMuted, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun MatchTeams(teamA: String, teamB: String, onSelectTeamA: () -> Unit, onSelectTeamB: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        ScheduleTeamCard("TEAM A", teamA, Color(0xFF168BFF), onSelectTeamA, Modifier.weight(1f))
        Text("VS", color = ScheduleNeon, fontSize = 15.sp, fontWeight = FontWeight.Black)
        ScheduleTeamCard("TEAM B", teamB, Color(0xFFFF5A27), onSelectTeamB, Modifier.weight(1f))
    }
}

@Composable
private fun ScheduleTeamCard(label: String, name: String, emblem: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(16.dp)
    Column(modifier.shadow(12.dp, shape, clip = false, ambientColor = emblem.copy(alpha = .28f), spotColor = emblem.copy(alpha = .42f)).clip(shape).background(ScheduleCard).clickable(onClick = onClick).padding(horizontal = 9.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.padding(top = 3.dp).size(52.dp).shadow(6.dp, CircleShape, clip = false, ambientColor = emblem.copy(alpha = .2f), spotColor = emblem.copy(alpha = .28f)).clip(CircleShape).background(Color(0xFF030B15)), contentAlignment = Alignment.Center) {
            Text(name.take(2).uppercase(), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
        }
        Text(name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp))
        Text("Registered team", color = ScheduleMuted, fontSize = 8.sp)
        Row(Modifier.fillMaxWidth().padding(top = 9.dp).clip(RoundedCornerShape(9.dp)).background(Color(0xFF102237)).padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(name, color = Color(0xFFD3DFE8), fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun ScheduleDetailTile(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String, accent: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(16.dp)
    Column(modifier.height(112.dp).shadow(12.dp, shape, clip = false, ambientColor = accent.copy(alpha = .24f), spotColor = accent.copy(alpha = .36f)).clip(shape).background(accent.copy(alpha = .08f))
        .clickable(onClick = onClick).padding(11.dp), verticalArrangement = Arrangement.SpaceBetween) {
        Box(Modifier.size(38.dp).shadow(5.dp, RoundedCornerShape(12.dp), clip = false, ambientColor = accent.copy(alpha = .18f), spotColor = accent.copy(alpha = .25f)).clip(RoundedCornerShape(12.dp)).background(Color(0xFF071525)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = accent, modifier = Modifier.size(21.dp))
        }
        Column {
            Text(title, color = ScheduleMuted, fontSize = 9.sp, maxLines = 1)
            Text(value, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun ScheduleDetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, caption: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(11.dp)
    Row(Modifier.fillMaxWidth().height(54.dp).shadow(8.dp, shape, clip = false, ambientColor = Color(0x443C9FFF), spotColor = Color(0x553C9FFF)).clip(shape).background(ScheduleCard).clickable(onClick = onClick).padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF172A20)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = ScheduleNeon, modifier = Modifier.size(17.dp))
        }
        Column(Modifier.padding(start = 10.dp).weight(1f)) {
            Text(value, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(caption, color = ScheduleMuted, fontSize = 8.sp, modifier = Modifier.padding(top = 2.dp))
        }
        Icon(Icons.Default.ChevronRight, null, tint = Color(0xFFB0C1CD), modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun SchedulePreview(teamA: String, teamB: String, date: String, time: String, venue: String, format: String) {
    Box(
        Modifier.fillMaxWidth()
            .shadow(14.dp, RoundedCornerShape(16.dp), clip = false, ambientColor = Color(0x3345E9FF), spotColor = Color(0x5545E9FF))
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0A101C))
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(ScheduleNeon.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(size.width / 2f, -30f),
                        radius = size.height * 1.1f
                    )
                )
            }
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            PreviewTag("🏆 ${format.ifBlank { "TOURNAMENT" }.uppercase()}", Modifier.weight(1f))
            PreviewTag("●  SCHEDULED", Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth().padding(top = 17.dp), verticalAlignment = Alignment.CenterVertically) {
            ScheduleTeamBadge(teamA, Color(0xFF168BFF))
            Text(teamA, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f).padding(start = 5.dp))
            Text("VS", color = ScheduleNeon, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 4.dp))
            Text(teamB, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.End, modifier = Modifier.weight(1f).padding(end = 5.dp))
            ScheduleTeamBadge(teamB, Color(0xFFFF5A27))
        }
        Row(Modifier.fillMaxWidth().padding(top = 17.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xDD061321)).padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            PreviewMeta(date.uppercase(), Icons.Default.CalendarMonth, Modifier.weight(1f))
            PreviewMeta(time, Icons.Default.Schedule, Modifier.weight(1f))
            PreviewMeta(venue.uppercase().ifBlank { "VENUE TBD" }, Icons.Default.LocationOn, Modifier.weight(1f))
        }
        }
        }
}

@Composable
private fun ScheduleTeamBadge(name: String, color: Color) {
    Box(Modifier.size(28.dp).shadow(5.dp, CircleShape, clip = false, ambientColor = color.copy(alpha = .2f), spotColor = color.copy(alpha = .3f)).clip(CircleShape).background(Color(0xFF020914)), contentAlignment = Alignment.Center) {
        Text(name.take(2).uppercase(), color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun PreviewMeta(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = ScheduleMuted, modifier = Modifier.size(10.dp))
        Text(text, color = ScheduleMuted, fontSize = 6.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 3.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun PreviewTag(text: String, modifier: Modifier) {
    Text(text, color = ScheduleNeon, fontSize = 8.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, modifier = modifier.clip(RoundedCornerShape(7.dp)).background(Color(0xFF253610)).padding(vertical = 6.dp))
}

@Composable
private fun ScheduleActionButton(label: String, enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().height(48.dp).shadow(18.dp, RoundedCornerShape(11.dp)).clip(RoundedCornerShape(11.dp)).background(ScheduleNeon).clickable(enabled = enabled, onClick = onClick), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        Icon(Icons.Default.CalendarMonth, null, tint = Color(0xFF102000), modifier = Modifier.size(17.dp))
        Text(label, color = Color(0xFF102000), fontSize = 12.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 8.dp))
        if (!enabled) Text("…", color = Color(0xFF102000), fontSize = 12.sp, fontWeight = FontWeight.Black)
        else Icon(Icons.Default.ArrowForward, null, tint = Color(0xFF102000), modifier = Modifier.padding(start = 9.dp).size(16.dp))
    }
}

@Composable
private fun ScheduleSuccessOverlay(tournamentName: String, onFinished: () -> Unit) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.success))
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val player = MediaPlayer.create(context, R.raw.success_sound)
        player?.start()
        kotlinx.coroutines.delay(2300)
        player?.release()
        onFinished()
    }
    Box(Modifier.fillMaxSize().background(Color(0xF20D1B2A)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            LottieAnimation(composition = composition, iterations = 1, modifier = Modifier.size(210.dp))
            Spacer(Modifier.height(18.dp))
            Text("Match Scheduled!", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Text(tournamentName, color = Color(0xFFB6C3CF), fontSize = 13.sp, modifier = Modifier.padding(top = 7.dp))
        }
    }
}

private fun formatScheduleDate(epochMillis: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(epochMillis))

private fun combineDateAndTime(dateMillis: Long, timeText: String): Long {
    val calendar = Calendar.getInstance().apply { timeInMillis = dateMillis }
    val parsedTime = runCatching { SimpleDateFormat("hh:mm a", Locale.getDefault()).parse(timeText) }.getOrNull()
    if (parsedTime != null) {
        val time = Calendar.getInstance().apply { this.time = parsedTime }
        calendar.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY))
        calendar.set(Calendar.MINUTE, time.get(Calendar.MINUTE))
    }
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}
