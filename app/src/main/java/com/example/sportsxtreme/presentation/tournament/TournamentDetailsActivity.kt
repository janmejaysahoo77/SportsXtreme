package com.example.sportsxtreme.presentation.tournament

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.example.sportsxtreme.R
import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.BallType
import com.example.sportsxtreme.domain.model.MatchFormat
import com.example.sportsxtreme.domain.repository.TournamentRepository
import com.example.sportsxtreme.domain.usecase.MatchUseCases
import com.example.sportsxtreme.presentation.match.SelectPlayingTeamsActivity
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@AndroidEntryPoint
class TournamentDetailsActivity : ComponentActivity() {
    @Inject lateinit var tournamentRepository: TournamentRepository
    @Inject lateinit var matchUseCases: MatchUseCases

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)

        val tournamentId = intent.getStringExtra(EXTRA_TOURNAMENT_ID).orEmpty()
        val matchId = intent.getStringExtra(EXTRA_MATCH_ID).orEmpty()
        val tournamentName = intent.getStringExtra(EXTRA_TOURNAMENT_NAME).orEmpty()
        val selectedStage = intent.getStringExtra(EXTRA_SELECTED_STAGE).orEmpty()
        val isTournamentMatchFlow = intent.getBooleanExtra(EXTRA_TOURNAMENT_MATCH_FLOW, false)
        val tournamentState = mutableStateOf<com.example.sportsxtreme.domain.model.Tournament?>(null)

        if (tournamentId.isNotBlank()) {
            lifecycleScope.launch {
                when (val result = tournamentRepository.getTournament(tournamentId)) {
                    is Resource.Success -> tournamentState.value = result.data
                    is Resource.Error, is Resource.Loading -> Unit
                }
            }
        }

        setContent {
            var isSaving by remember { mutableStateOf(false) }
            TournamentDetailsScreen(
                tournamentName = tournamentState.value?.name?.ifBlank { tournamentName } ?: tournamentName,
                initialGround = tournamentState.value?.ground.orEmpty()
                    .ifBlank { tournamentState.value?.city.orEmpty() }
                    .ifBlank { tournamentState.value?.requirements?.location.orEmpty() },
                initialBallType = tournamentState.value?.ballType.orEmpty(),
                isSaving = isSaving,
                onBack = { finish() },
                onContinue = { overs, powerplayOvers, maxOversPerBowler, ballType, ground ->
                    if (isSaving) return@TournamentDetailsScreen
                    isSaving = true
                    lifecycleScope.launch {
                        try {
                            if (matchId.isNotBlank()) {
                                when (val result = matchUseCases.updateMatchSettings(
                                    matchId = matchId,
                                    format = MatchFormat.T20,
                                    ballType = ballType,
                                    overs = overs,
                                    maxOversPerBowler = maxOversPerBowler
                                )) {
                                    is Resource.Error -> {
                                        isSaving = false
                                        Toast.makeText(this@TournamentDetailsActivity, result.message ?: "Could not save match settings", Toast.LENGTH_LONG).show()
                                        return@launch
                                    }
                                    is Resource.Loading -> {
                                        isSaving = false
                                        return@launch
                                    }
                                    is Resource.Success -> Unit
                                }
                                FirebaseFirestore.getInstance().collection("matches").document(matchId)
                                    .update(
                                        mapOf(
                                            "venue" to ground.trim(),
                                            "powerplayOvers" to powerplayOvers,
                                            "tournamentId" to tournamentId,
                                            "tournamentName" to tournamentName,
                                            "roundName" to selectedStage,
                                            "selectedStage" to selectedStage
                                        )
                                    ).await()
                            }
                            startActivity(
                                Intent(this@TournamentDetailsActivity, SelectPlayingTeamsActivity::class.java)
                                    .putExtra(SelectPlayingTeamsActivity.EXTRA_TOURNAMENT_SCHEDULE_FLOW, !isTournamentMatchFlow)
                                    .putExtra(SelectPlayingTeamsActivity.EXTRA_TOURNAMENT_MATCH_FLOW, isTournamentMatchFlow)
                                    .putExtra(SelectPlayingTeamsActivity.EXTRA_TOURNAMENT_ID, tournamentId)
                                    .putExtra(SelectPlayingTeamsActivity.EXTRA_TOURNAMENT_NAME, tournamentName)
                                    .putExtra(SelectPlayingTeamsActivity.EXTRA_MATCH_ID, matchId)
                                    .putExtra(SelectPlayingTeamsActivity.EXTRA_SELECTED_STAGE, selectedStage)
                            )
                            finish()
                        } catch (error: Exception) {
                            isSaving = false
                            Toast.makeText(this@TournamentDetailsActivity, error.message ?: "Could not save tournament match details", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            )
        }
    }

    companion object {
        const val EXTRA_TOURNAMENT_ID = "tournament_id"
        const val EXTRA_TOURNAMENT_NAME = "tournament_name"
        const val EXTRA_MATCH_ID = "match_id"
        const val EXTRA_SELECTED_STAGE = "selected_stage"
        const val EXTRA_TOURNAMENT_MATCH_FLOW = "tournament_match_flow"
    }
}

private val TournamentDetailsAccent = Color(0xFFC1FF00)
private val TournamentDetailsBg = Color(0xFF0A101C)
private val TournamentDetailsCard = Color(0xFF101B2A)
private val TournamentDetailsStroke = Color(0xFF26394B)
private val TournamentDetailsMuted = Color(0xFFAAB6C4)

@Composable
private fun TournamentDetailsScreen(
    tournamentName: String,
    initialGround: String,
    initialBallType: String,
    isSaving: Boolean,
    onBack: () -> Unit,
    onContinue: (Int, Int, Int, BallType, String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var oversChoice by remember { mutableStateOf("20") }
    var customOvers by remember { mutableStateOf("") }
    var powerplayChoice by remember { mutableStateOf("6") }
    var customPowerplay by remember { mutableStateOf("") }
    var bowlerChoice by remember { mutableStateOf("4") }
    var customBowlerOvers by remember { mutableStateOf("") }
    var ground by remember { mutableStateOf(initialGround) }
    var ballType by remember { mutableStateOf(initialBallType.toBallType()) }
    var ballTypeSelected by remember { mutableStateOf(false) }
    var customTarget by remember { mutableStateOf<String?>(null) }
    var customDraft by remember { mutableStateOf("") }
    var groundDraft by remember { mutableStateOf(initialGround) }
    var groundEditing by remember { mutableStateOf(false) }
    LaunchedEffect(initialGround) {
        if (ground.isBlank() && initialGround.isNotBlank()) {
            ground = initialGround
            if (!groundEditing) groundDraft = initialGround
        }
    }
    LaunchedEffect(initialBallType) {
        if (!ballTypeSelected && initialBallType.isNotBlank()) ballType = initialBallType.toBallType()
    }

    val totalOvers = if (oversChoice == "Custom") customOvers.toIntOrNull() else oversChoice.toIntOrNull()
    val powerplayOvers = if (powerplayChoice == "Custom") customPowerplay.toIntOrNull() else powerplayChoice.toIntOrNull()
    val maxBowlerOvers = if (bowlerChoice == "Custom") customBowlerOvers.toIntOrNull() else bowlerChoice.toIntOrNull()
    val valid = totalOvers != null && totalOvers > 0 && powerplayOvers != null && powerplayOvers in 1..(totalOvers ?: 0) &&
        maxBowlerOvers != null && maxBowlerOvers > 0 && ground.isNotBlank() && customTarget == null && !groundEditing
    val customDraftValue = customDraft.toIntOrNull()
    val canSaveCustom = customDraftValue != null && customDraftValue > 0 &&
        (customTarget != "powerplay" || totalOvers == null || customDraftValue <= totalOvers)

    fun beginCustomEntry(target: String, existing: String) {
        if (groundEditing) {
            groundEditing = false
            groundDraft = ground
        }
        customTarget = target
        customDraft = existing
    }

    fun saveCustomEntry() {
        val value = customDraft.toIntOrNull() ?: return
        if (value < 1 || (customTarget == "powerplay" && totalOvers != null && value > totalOvers)) return
        when (customTarget) {
            "overs" -> {
                customOvers = value.toString()
                oversChoice = "Custom"
                val currentPowerplay = powerplayChoice.toIntOrNull()
                if (currentPowerplay != null && currentPowerplay > value) {
                    if (value in listOf(2, 4, 6)) powerplayChoice = value.toString()
                    else {
                        powerplayChoice = "Custom"
                        customPowerplay = value.toString()
                    }
                }
            }
            "powerplay" -> {
                customPowerplay = value.toString()
                powerplayChoice = "Custom"
            }
            "bowler" -> {
                customBowlerOvers = value.toString()
                bowlerChoice = "Custom"
            }
        }
        customTarget = null
        customDraft = ""
    }

    Box(
        modifier = Modifier.fillMaxSize().background(TournamentDetailsBg).drawBehind {
            drawRect(
                Brush.radialGradient(
                    listOf(TournamentDetailsAccent.copy(alpha = 0.15f), Color.Transparent),
                    center = Offset(size.width / 2f, -50f),
                    radius = size.height * 0.6f
                )
            )
        }
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().height(58.dp).background(Color(0xE90B1626)).padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DetailsArrow(Modifier.size(22.dp).clickable(onClick = onBack), false)
                Text("TOURNAMENT DETAILS", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f).padding(start = 14.dp), maxLines = 1)
                if (customTarget != null || groundEditing) {
                    DetailsActionIcon(
                        modifier = Modifier.size(22.dp).clickable {
                            if (groundEditing) {
                                groundDraft = ground
                                groundEditing = false
                            } else {
                                customTarget = null
                                customDraft = ""
                            }
                        },
                        checked = false,
                        enabled = true
                    )
                    DetailsActionIcon(
                        modifier = Modifier.padding(start = 14.dp).size(22.dp).clickable(
                            enabled = if (groundEditing) groundDraft.isNotBlank() else canSaveCustom
                        ) {
                            if (groundEditing) {
                                ground = groundDraft.trim()
                                groundDraft = ground
                                groundEditing = false
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            } else {
                                saveCustomEntry()
                            }
                        },
                        checked = true,
                        enabled = if (groundEditing) groundDraft.isNotBlank() else canSaveCustom
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 14.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text(tournamentName.ifBlank { "Tournament Match" }, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("Set the match conditions before selecting teams.", color = TournamentDetailsMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp))
                }
                DetailsSection("TOTAL OVERS") {
                    DetailsChoices(listOf("5", "10", "20", "Custom"), if (customTarget == "overs") "Custom" else oversChoice) {
                        if (it == "Custom") {
                            beginCustomEntry("overs", customOvers)
                        } else {
                            oversChoice = it
                            val newOvers = it.toIntOrNull()
                            val currentPowerplay = powerplayChoice.toIntOrNull()
                            if (newOvers != null && currentPowerplay != null && currentPowerplay > newOvers) {
                                if (newOvers in listOf(2, 4, 6)) powerplayChoice = newOvers.toString()
                                else {
                                    powerplayChoice = "Custom"
                                    customPowerplay = newOvers.toString()
                                }
                            }
                        }
                    }
                    if (customTarget == "overs") DetailsNumberField("Enter total overs", customDraft) { customDraft = it }
                    else if (oversChoice == "Custom") Text("Custom total: $customOvers overs", color = TournamentDetailsMuted, fontSize = 12.sp)
                }
                DetailsSection("POWERPLAY OVERS") {
                    val powerplayChoices = listOf("2", "4", "6").filter { totalOvers == null || it.toInt() <= totalOvers } + "Custom"
                    DetailsChoices(powerplayChoices, if (customTarget == "powerplay") "Custom" else powerplayChoice) {
                        if (it == "Custom") beginCustomEntry("powerplay", customPowerplay) else powerplayChoice = it
                    }
                    if (customTarget == "powerplay") DetailsNumberField("Powerplay overs", customDraft) { customDraft = it }
                    else if (powerplayChoice == "Custom") Text("Custom powerplay: $customPowerplay overs", color = TournamentDetailsMuted, fontSize = 12.sp)
                    if (powerplayOvers != null && totalOvers != null && powerplayOvers > totalOvers) {
                        Text("Powerplay overs cannot exceed total overs.", color = Color(0xFFFF8585), fontSize = 12.sp)
                    }
                }
                DetailsSection("MAX OVERS PER BOWLER") {
                    DetailsChoices(listOf("2", "4", "6", "Custom"), if (customTarget == "bowler") "Custom" else bowlerChoice) {
                        if (it == "Custom") beginCustomEntry("bowler", customBowlerOvers) else bowlerChoice = it
                    }
                    if (customTarget == "bowler") DetailsNumberField("Overs per bowler", customDraft) { customDraft = it }
                    else if (bowlerChoice == "Custom") Text("Custom limit: $customBowlerOvers overs", color = TournamentDetailsMuted, fontSize = 12.sp)
                }
                DetailsSection("BALL TYPE (Enter If Changed)") {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TournamentBallCard("TENNIS", BallType.TENNIS, R.drawable.tennisball, ballType, Modifier.weight(1f)) { ballType = BallType.TENNIS; ballTypeSelected = true }
                        TournamentBallCard("LEATHER", BallType.LEATHER, R.drawable.leatherball, ballType, Modifier.weight(1f)) { ballType = BallType.LEATHER; ballTypeSelected = true }
                        TournamentBallCard("TAPE", BallType.OTHER, R.drawable.otherball, ballType, Modifier.weight(1f)) { ballType = BallType.OTHER; ballTypeSelected = true }
                    }
                }
                DetailsSection("GROUND (Enter If Changed)") {
                    OutlinedTextField(
                        value = if (groundEditing) groundDraft else ground,
                        onValueChange = {
                            customTarget = null
                            customDraft = ""
                            groundEditing = true
                            groundDraft = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Tournament ground", color = TournamentDetailsMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = TournamentDetailsAccent,
                            unfocusedBorderColor = TournamentDetailsStroke,
                            cursorColor = TournamentDetailsAccent
                        )
                    )
                }
                Spacer(Modifier.height(84.dp))
            }
        }
        Box(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Transparent, TournamentDetailsBg, TournamentDetailsBg)))
                .padding(horizontal = 14.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(28.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFFD8FF38), TournamentDetailsAccent, Color(0xFF9BFA00))))
                    .clickable(enabled = valid && !isSaving) {
                        onContinue(totalOvers!!, powerplayOvers!!, maxBowlerOvers!!, ballType, ground.trim())
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(if (isSaving) "SAVING DETAILS…" else "CONTINUE TO TEAM SETUP", color = Color(0xFF101904), fontSize = 14.sp, fontWeight = FontWeight.Black)
                DetailsArrow(Modifier.padding(start = 10.dp).size(18.dp), true)
            }
        }
    }
}

@Composable
private fun DetailsSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
        Column(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(TournamentDetailsCard)
                .border(1.dp, TournamentDetailsStroke, RoundedCornerShape(14.dp)).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) { content() }
    }
}

@Composable
private fun DetailsChoices(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        options.forEach { option ->
            Box(
                modifier = Modifier.weight(1f).height(40.dp).clip(RoundedCornerShape(20.dp))
                    .background(if (selected == option) TournamentDetailsAccent else Color(0xFF0A1420))
                    .border(1.dp, if (selected == option) TournamentDetailsAccent else TournamentDetailsStroke, RoundedCornerShape(20.dp))
                    .clickable { onSelect(option) },
                contentAlignment = Alignment.Center
            ) {
                Text(option, color = if (selected == option) Color(0xFF142008) else Color(0xFFBAC4C0), fontSize = 12.sp, fontWeight = FontWeight.Black, maxLines = 1)
            }
        }
    }
}

@Composable
private fun DetailsNumberField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.filter(Char::isDigit).take(3)) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label, color = TournamentDetailsMuted) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = TournamentDetailsAccent,
            unfocusedBorderColor = TournamentDetailsStroke,
            cursorColor = TournamentDetailsAccent
        )
    )
}

@Composable
private fun TournamentBallCard(label: String, value: BallType, icon: Int, selected: BallType, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier.height(88.dp).clip(RoundedCornerShape(10.dp))
            .background(if (selected == value) Color(0xFF172513) else Color(0xFF0A1420))
            .border(1.dp, if (selected == value) TournamentDetailsAccent else TournamentDetailsStroke, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(painterResource(icon), contentDescription = null, modifier = Modifier.size(40.dp), contentScale = ContentScale.Fit)
        Text(label, color = if (selected == value) TournamentDetailsAccent else TournamentDetailsMuted, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 5.dp))
    }
}

@Composable
private fun DetailsArrow(modifier: Modifier, forward: Boolean) {
    androidx.compose.foundation.Canvas(modifier) {
        val stroke = Stroke(width = 1.9.dp.toPx(), cap = StrokeCap.Round)
        val path = Path().apply {
            if (forward) {
                moveTo(size.width * 0.35f, size.height * 0.25f)
                lineTo(size.width * 0.65f, size.height * 0.5f)
                lineTo(size.width * 0.35f, size.height * 0.75f)
            } else {
                moveTo(size.width * 0.65f, size.height * 0.25f)
                lineTo(size.width * 0.35f, size.height * 0.5f)
                lineTo(size.width * 0.65f, size.height * 0.75f)
            }
        }
        drawPath(path, Color.White, style = stroke)
    }
}

@Composable
private fun DetailsActionIcon(modifier: Modifier, checked: Boolean, enabled: Boolean) {
    androidx.compose.foundation.Canvas(modifier) {
        val tint = when {
            !enabled -> TournamentDetailsMuted.copy(alpha = 0.45f)
            checked -> TournamentDetailsAccent
            else -> Color.White
        }
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        if (checked) {
            val path = Path().apply {
                moveTo(size.width * 0.2f, size.height * 0.52f)
                lineTo(size.width * 0.43f, size.height * 0.74f)
                lineTo(size.width * 0.82f, size.height * 0.27f)
            }
            drawPath(path, tint, style = stroke)
        } else {
            drawLine(tint, Offset(size.width * 0.25f, size.height * 0.25f), Offset(size.width * 0.75f, size.height * 0.75f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            drawLine(tint, Offset(size.width * 0.75f, size.height * 0.25f), Offset(size.width * 0.25f, size.height * 0.75f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        }
    }
}

private fun String.toBallType(): BallType = when {
    contains("TENNIS", ignoreCase = true) -> BallType.TENNIS
    contains("TAPE", ignoreCase = true) || contains("OTHER", ignoreCase = true) -> BallType.OTHER
    else -> BallType.LEATHER
}
