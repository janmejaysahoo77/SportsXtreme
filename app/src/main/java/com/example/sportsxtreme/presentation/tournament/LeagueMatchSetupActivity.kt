 package com.example.sportsxtreme.presentation.tournament

import com.example.sportsxtreme.R
import com.example.sportsxtreme.presentation.ui.theme.*
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
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.example.sportsxtreme.domain.repository.TournamentRepository
import com.example.sportsxtreme.domain.model.Tournament
import com.example.sportsxtreme.common.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LeagueMatchSetupActivity : ComponentActivity() {
    @Inject lateinit var tournamentRepository: TournamentRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        val tournamentId = intent.getStringExtra("tournament_id").orEmpty()
        val fallbackTournamentName = intent.getStringExtra(EXTRA_TOURNAMENT_NAME).orEmpty()
        val matchId = intent.getStringExtra(StartMatchActivity.EXTRA_SCHEDULE_MATCH_ID).orEmpty()
        val isTournamentMatchFlow = intent.getBooleanExtra(EXTRA_TOURNAMENT_MATCH_FLOW, false)
        val tournamentState = mutableStateOf<Tournament?>(null)
        val selectedStage = mutableStateOf("League Stage")
        val isSavingStage = mutableStateOf(false)
        val stageSaveError = mutableStateOf<String?>(null)
        if (tournamentId.isNotBlank()) {
            lifecycleScope.launch {
                when (val result = tournamentRepository.getTournament(tournamentId)) {
                    is Resource.Success -> tournamentState.value = result.data
                    is Resource.Error, is Resource.Loading -> Unit
                }
            }
        }
        setContent {
            val tournament = tournamentState.value
            val stage = selectedStage.value
            val saving = isSavingStage.value
            val saveError = stageSaveError.value
            LeagueMatchSetupScreen(
                tournament = tournament,
                fallbackTournamentName = fallbackTournamentName,
                selectedStage = stage,
                isSavingStage = saving,
                stageSaveError = saveError,
                onBack = { finish() },
                onStageSelected = { selected ->
                    selectedStage.value = selected
                    if (matchId.isNotBlank()) {
                        lifecycleScope.launch {
                            isSavingStage.value = true
                            stageSaveError.value = null
                            val saveResult = runCatching {
                                FirebaseFirestore.getInstance().collection("matches").document(matchId)
                                    .update(
                                        mapOf(
                                            "roundName" to selected,
                                            "selectedStage" to selected,
                                            "tournamentName" to tournament?.name.orEmpty(),
                                            "tournamentLocation" to (tournament?.city?.ifBlank { tournament.requirements.location }.orEmpty()),
                                            "tournamentTeamCount" to tournament?.requirements?.numberOfTeams.orEmpty()
                                        )
                                    ).await()
                            }
                            if (saveResult.isFailure) {
                                stageSaveError.value = "Could not save stage. Please try again."
                                Toast.makeText(this@LeagueMatchSetupActivity, "Could not save selected stage", Toast.LENGTH_SHORT).show()
                            }
                            isSavingStage.value = false
                        }
                    }
                },
                onContinue = {
                    startActivity(
                        Intent(this, SelectPlayingTeamsActivity::class.java)
                            .putExtra(SelectPlayingTeamsActivity.EXTRA_TOURNAMENT_SCHEDULE_FLOW, !isTournamentMatchFlow)
                            .putExtra(SelectPlayingTeamsActivity.EXTRA_TOURNAMENT_MATCH_FLOW, isTournamentMatchFlow)
                            .putExtra(SelectPlayingTeamsActivity.EXTRA_TOURNAMENT_ID, tournamentId)
                            .putExtra(SelectPlayingTeamsActivity.EXTRA_TOURNAMENT_NAME, tournament?.name ?: fallbackTournamentName)
                            .putExtra(SelectPlayingTeamsActivity.EXTRA_MATCH_ID, matchId)
                            .putExtra(SelectPlayingTeamsActivity.EXTRA_SELECTED_STAGE, stage)
                    )
                }
            )
        }
    }

    companion object {
        const val EXTRA_TOURNAMENT_NAME = "tournament_name"
        const val EXTRA_TOURNAMENT_MATCH_FLOW = "tournament_match_flow"
    }
}

private val SetupAccent = XtremeLime
private val SetupBg = XtremeBgBlue
private val SetupPanel = XtremeCardBlue
private val SetupCard = XtremeCardBlue
private val SetupMuted = XtremeMuted

@Composable
private fun LeagueMatchSetupScreen(
    tournament: Tournament?,
    fallbackTournamentName: String,
    selectedStage: String,
    isSavingStage: Boolean,
    stageSaveError: String?,
    onBack: () -> Unit,
    onStageSelected: (String) -> Unit,
    onContinue: () -> Unit
) {
    val stages = listOf(
        SetupStage("League Stage", if (selectedStage == "League Stage") "SELECTED" else "AVAILABLE ROUND", SetupGlyph.LEAGUE, R.drawable.tournamentlogo, true),
        SetupStage("Semi Final", if (selectedStage == "Semi Final") "SELECTED" else "AVAILABLE ROUND", SetupGlyph.FLAME, R.drawable.otherball, true),
        SetupStage("Final", if (selectedStage == "Final") "SELECTED" else "AVAILABLE ROUND", SetupGlyph.TROPHY, R.drawable.finalicon, true)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SetupBg)
            .drawBehind {
                drawCircle(Color(0x191C5DFF), radius = size.width * 0.72f, center = Offset(size.width * 0.9f, size.height * 0.92f))
                drawCircle(Color(0x102A3B13), radius = size.width * 0.64f, center = Offset(size.width * 0.02f, size.height * 0.14f))
            }
    ) {
        SetupTopBar(onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                "Select the tournament round to begin scoring.",
                color = SetupMuted,
                fontSize = 13.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.Medium
            )
            SetupTournamentCard(tournament, fallbackTournamentName)
            SetupSectionTitle("TOURNAMENT PROGRESSION")
            SetupProgressionCard(selectedStage)
            SetupSectionTitle("SELECT STAGE")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                stages.forEach { stage ->
                    StageSelectCard(
                        stage = stage,
                        selected = selectedStage == stage.title,
                        onClick = { onStageSelected(stage.title) }
                    )
                }
            }
            if (isSavingStage) {
                Text("Saving selected stage…", color = SetupAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            stageSaveError?.let { Text(it, color = Color(0xFFFF8B8B), fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            SetupInfoCard()
            Spacer(Modifier.height(72.dp))
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 18.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        SetupContinueButton(onContinue, enabled = !isSavingStage && stageSaveError == null)
    }
}

@Composable
private fun SetupTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(SetupPanel)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SetupArrow(Modifier.size(23.dp).clickable(onClick = onBack), tint = Color.White)
        Text(
            "MATCH SETUP",
            color = Color.White,
            fontSize = 22.sp,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 14.dp).weight(1f),
            maxLines = 1
        )
        SetupHelp(Modifier.size(23.dp))
    }
}

@Composable
private fun SetupTournamentCard(tournament: Tournament?, fallbackTournamentName: String) {
    val location = tournament?.city?.takeIf { it.isNotBlank() }
        ?: tournament?.requirements?.location?.takeIf { it.isNotBlank() }
        ?: "Location not set"
    val teamCount = tournament?.requirements?.numberOfTeams?.takeIf { it.isNotBlank() } ?: "—"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(172.dp)
            .drawBehind {
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.56f),
                    topLeft = Offset(0f, 13.dp.toPx()),
                    size = Size(size.width, size.height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx(), 16.dp.toPx())
                )
                drawRoundRect(
                    color = SetupAccent.copy(alpha = 0.12f),
                    topLeft = Offset(0f, 5.dp.toPx()),
                    size = Size(size.width, size.height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx(), 16.dp.toPx())
                )
            }
            .shadow(26.dp, RoundedCornerShape(16.dp), clip = false)
            .clip(RoundedCornerShape(16.dp))
            .background(SetupCard)
            .drawBehind {
                drawCircle(Color.White.copy(alpha = 0.14f), radius = size.width * 0.38f, center = Offset(size.width * 0.98f, -size.height * 0.05f))
                drawCircle(SetupAccent.copy(alpha = 0.10f), radius = size.width * 0.36f, center = Offset(size.width * 0.9f, size.height * 0.08f))
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 7.dp, height = 138.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SetupAccent)
        )
        Column(modifier = Modifier.padding(start = 15.dp).weight(1f)) {
            Text(tournament?.name?.ifBlank { "Tournament" } ?: fallbackTournamentName.ifBlank { "Loading tournament…" }, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SetupAccent)
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text("ACTIVE SEASON 2024", color = Color(0xFF111604), fontSize = 9.sp, fontWeight = FontWeight.Black)
            }
            Row(modifier = Modifier.padding(top = 32.dp), verticalAlignment = Alignment.CenterVertically) {
                SetupGlyphIcon(SetupGlyph.PIN, Modifier.size(19.dp), SetupAccent)
                Text(location, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 8.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                SetupGlyphIcon(SetupGlyph.TEAMS, Modifier.padding(start = 22.dp).size(19.dp), SetupAccent)
                Text("$teamCount Teams", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 7.dp), maxLines = 1)
            }
            Row(modifier = Modifier.padding(top = 23.dp), verticalAlignment = Alignment.CenterVertically) {
                SetupGlyphIcon(SetupGlyph.TROPHY, Modifier.size(18.dp), SetupAccent)
                Text(tournament?.requirements?.tournamentFormat?.ifBlank { "Tournament" } ?: "Tournament", color = Color.White, fontSize = 16.5.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 8.dp))
            }
        }
        Box(
            modifier = Modifier
                .size(64.dp)
                .shadow(16.dp, RoundedCornerShape(12.dp), clip = false)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF273623), Color(0xFF07101A)),
                        radius = 62f
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.tournamentlogo),
                contentDescription = "Tournament logo",
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun SetupSectionTitle(text: String) {
    Text(text, color = Color.White, fontSize = 15.5.sp, fontWeight = FontWeight.Black, letterSpacing = 0.sp)
}

@Composable
private fun SetupProgressionCard(selectedStage: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
            .drawBehind {
                drawRoundRect(
                    Color.Black.copy(alpha = 0.42f),
                    topLeft = Offset(0f, 9.dp.toPx()),
                    size = Size(size.width, size.height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(14.dp.toPx(), 14.dp.toPx())
                )
            }
            .shadow(20.dp, RoundedCornerShape(14.dp), clip = false)
            .clip(RoundedCornerShape(14.dp))
            .background(SetupCard)
            .padding(horizontal = 26.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ProgressionNode("LEAGUE STAGE", R.drawable.tournamentlogo, selectedStage == "League Stage")
        ProgressionConnector()
        ProgressionNode("SEMI FINAL", R.drawable.otherball, selectedStage == "Semi Final")
        ProgressionConnector()
        ProgressionNode("FINAL", R.drawable.finalicon, selectedStage == "Final")
    }
}

@Composable
private fun ProgressionNode(label: String, iconRes: Int, active: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(if (active) 48.dp else 38.dp)
                .clip(CircleShape)
                .background(if (active) SetupAccent else Color(0xFF111D2E)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = label,
                modifier = Modifier
                    .size(if (active) 34.dp else 28.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
        Text(label, color = if (active) SetupAccent else Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 8.dp), maxLines = 1)
    }
}

@Composable
private fun ProgressionConnector() {
    Box(
        modifier = Modifier
            .height(1.dp)
            .size(width = 25.dp, height = 1.dp)
            .background(Color(0xFF566A2F))
    )
}

@Composable
private fun StageSelectCard(stage: SetupStage, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp)
            .drawBehind {
                drawRoundRect(
                    Color.Black.copy(alpha = 0.38f),
                    topLeft = Offset(0f, 7.dp.toPx()),
                    size = Size(size.width, size.height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx(), 12.dp.toPx())
                )
            }
            .shadow(16.dp, RoundedCornerShape(12.dp), clip = false)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Color(0xFF14200B) else SetupCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(if (selected) SetupAccent else Color(0xFF172233)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(stage.iconRes),
                contentDescription = stage.title,
                modifier = Modifier
                    .size(25.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
        Column(modifier = Modifier.padding(start = 13.dp).weight(1f)) {
            Text(stage.title, color = if (selected) Color.White else Color(0xFFBBC4C8), fontSize = 18.sp, fontWeight = FontWeight.Black, maxLines = 1)
            Text(stage.status, color = if (selected) SetupAccent else Color(0xFF75818C), fontSize = 9.sp, fontWeight = FontWeight.Black, maxLines = 1)
        }
        if (selected) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(SetupAccent),
                contentAlignment = Alignment.Center
            ) {
                SetupCheck(Modifier.size(14.dp), Color(0xFF111604))
            }
        }
    }
}

@Composable
private fun SetupInfoCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp)
            .drawBehind {
                drawRoundRect(
                    Color.Black.copy(alpha = 0.34f),
                    topLeft = Offset(0f, 7.dp.toPx()),
                    size = Size(size.width, size.height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx(), 12.dp.toPx())
                )
            }
            .shadow(16.dp, RoundedCornerShape(12.dp), clip = false)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF071123))
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(18.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(15.dp)) {
                drawCircle(Color(0xFF007BFF), radius = size.minDimension * 0.43f)
                drawLine(Color.White, Offset(size.width * 0.5f, size.height * 0.45f), Offset(size.width * 0.5f, size.height * 0.72f), strokeWidth = 1.3.dp.toPx(), cap = StrokeCap.Round)
                drawCircle(Color.White, radius = size.minDimension * 0.04f, center = Offset(size.width * 0.5f, size.height * 0.29f))
            }
        }
        Text(
            "Choose a stage above to initialize the match scoreboard. Scores will be recorded under the selected round.",
            color = Color(0xFFD7E3EA),
            fontSize = 11.2.sp,
            lineHeight = 14.5.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 13.dp)
        )
    }
}

@Composable
private fun SetupContinueButton(onClick: () -> Unit, enabled: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp)
            .shadow(22.dp, RoundedCornerShape(12.dp), clip = false)
            .clip(RoundedCornerShape(12.dp))
            .background(SetupAccent)
            .clickable(enabled = enabled, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(Modifier.size(15.dp)) {
            drawCircle(Color.Transparent, radius = size.minDimension * 0.42f, style = Stroke(width = 1.5.dp.toPx()))
            val path = Path().apply {
                moveTo(size.width * 0.42f, size.height * 0.32f)
                lineTo(size.width * 0.68f, size.height * 0.5f)
                lineTo(size.width * 0.42f, size.height * 0.68f)
                close()
            }
            drawCircle(Color(0xFF111604), radius = size.minDimension * 0.43f, style = Stroke(width = 1.5.dp.toPx()))
            drawPath(path, Color(0xFF111604))
        }
        Text("CONTINUE", color = if (enabled) Color(0xFF111604) else Color(0xFF536040), fontSize = 14.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 10.dp))
    }
}

@Composable
private fun SetupArrow(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.9.dp.toPx(), cap = StrokeCap.Round)
        val path = Path().apply {
            moveTo(size.width * 0.65f, size.height * 0.25f)
            lineTo(size.width * 0.36f, size.height * 0.5f)
            lineTo(size.width * 0.65f, size.height * 0.75f)
        }
        drawPath(path, tint, style = stroke)
    }
}

@Composable
private fun SetupHelp(modifier: Modifier) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(Color.White, radius = size.minDimension * 0.35f, style = stroke)
        drawLine(Color.White, Offset(size.width * 0.5f, size.height * 0.34f), Offset(size.width * 0.5f, size.height * 0.55f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawCircle(Color.White, radius = size.minDimension * 0.035f, center = Offset(size.width * 0.5f, size.height * 0.68f))
    }
}

@Composable
private fun SetupCheck(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round)
        val path = Path().apply {
            moveTo(size.width * 0.25f, size.height * 0.53f)
            lineTo(size.width * 0.42f, size.height * 0.68f)
            lineTo(size.width * 0.75f, size.height * 0.35f)
        }
        drawPath(path, tint, style = stroke)
    }
}

@Composable
private fun SetupGlyphIcon(glyph: SetupGlyph, modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
        when (glyph) {
            SetupGlyph.PIN -> {
                drawCircle(tint, radius = size.minDimension * 0.2f, center = Offset(size.width * 0.5f, size.height * 0.38f), style = stroke)
                val path = Path().apply {
                    moveTo(size.width * 0.5f, size.height * 0.88f)
                    cubicTo(size.width * 0.18f, size.height * 0.55f, size.width * 0.24f, size.height * 0.16f, size.width * 0.5f, size.height * 0.14f)
                    cubicTo(size.width * 0.76f, size.height * 0.16f, size.width * 0.82f, size.height * 0.55f, size.width * 0.5f, size.height * 0.88f)
                }
                drawPath(path, tint, style = stroke)
            }
            SetupGlyph.TEAMS -> {
                drawCircle(tint, radius = size.minDimension * 0.16f, center = Offset(size.width * 0.38f, size.height * 0.36f), style = stroke)
                drawCircle(tint, radius = size.minDimension * 0.13f, center = Offset(size.width * 0.66f, size.height * 0.41f), style = stroke)
                drawArc(tint, 205f, 130f, false, topLeft = Offset(size.width * 0.14f, size.height * 0.52f), size = Size(size.width * 0.5f, size.height * 0.36f), style = stroke)
                drawArc(tint, 205f, 130f, false, topLeft = Offset(size.width * 0.46f, size.height * 0.56f), size = Size(size.width * 0.36f, size.height * 0.27f), style = stroke)
            }
            SetupGlyph.TROPHY -> {
                drawRoundRect(tint, topLeft = Offset(size.width * 0.3f, size.height * 0.16f), size = Size(size.width * 0.4f, size.height * 0.36f), style = stroke)
                drawArc(tint, 90f, 165f, false, topLeft = Offset(size.width * 0.1f, size.height * 0.2f), size = Size(size.width * 0.28f, size.height * 0.28f), style = stroke)
                drawArc(tint, -75f, 165f, false, topLeft = Offset(size.width * 0.62f, size.height * 0.2f), size = Size(size.width * 0.28f, size.height * 0.28f), style = stroke)
                drawLine(tint, Offset(size.width * 0.5f, size.height * 0.52f), Offset(size.width * 0.5f, size.height * 0.75f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.35f, size.height * 0.78f), Offset(size.width * 0.65f, size.height * 0.78f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            SetupGlyph.LEAGUE -> {
                drawLine(tint, Offset(size.width * 0.3f, size.height * 0.78f), Offset(size.width * 0.72f, size.height * 0.25f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawRoundRect(tint, topLeft = Offset(size.width * 0.54f, size.height * 0.13f), size = Size(size.width * 0.28f, size.height * 0.34f), style = stroke)
                drawCircle(tint, radius = size.minDimension * 0.09f, center = Offset(size.width * 0.29f, size.height * 0.78f), style = stroke)
            }
            SetupGlyph.FLAME -> {
                val path = Path().apply {
                    moveTo(size.width * 0.52f, size.height * 0.1f)
                    cubicTo(size.width * 0.85f, size.height * 0.42f, size.width * 0.72f, size.height * 0.86f, size.width * 0.5f, size.height * 0.9f)
                    cubicTo(size.width * 0.22f, size.height * 0.84f, size.width * 0.14f, size.height * 0.52f, size.width * 0.38f, size.height * 0.32f)
                    cubicTo(size.width * 0.44f, size.height * 0.5f, size.width * 0.58f, size.height * 0.43f, size.width * 0.52f, size.height * 0.1f)
                }
                drawPath(path, tint, style = stroke)
            }
            SetupGlyph.EMBLEM -> {
                val hex = Path().apply {
                    moveTo(size.width * 0.5f, size.height * 0.08f)
                    lineTo(size.width * 0.82f, size.height * 0.26f)
                    lineTo(size.width * 0.82f, size.height * 0.65f)
                    lineTo(size.width * 0.5f, size.height * 0.84f)
                    lineTo(size.width * 0.18f, size.height * 0.65f)
                    lineTo(size.width * 0.18f, size.height * 0.26f)
                    close()
                }
                drawPath(hex, tint, style = stroke)
                drawCircle(tint, radius = size.minDimension * 0.17f, center = Offset(size.width * 0.5f, size.height * 0.46f), style = stroke)
                drawLine(tint, Offset(size.width * 0.35f, size.height * 0.64f), Offset(size.width * 0.65f, size.height * 0.64f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
        }
    }
}

private data class SetupStage(val title: String, val status: String, val glyph: SetupGlyph, val iconRes: Int, val enabled: Boolean)

private enum class SetupGlyph {
    PIN,
    TEAMS,
    TROPHY,
    LEAGUE,
    FLAME,
    EMBLEM
}
