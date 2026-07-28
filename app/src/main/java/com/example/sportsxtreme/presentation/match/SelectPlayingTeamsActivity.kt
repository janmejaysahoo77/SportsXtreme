package com.example.sportsxtreme.presentation.match

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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.example.sportsxtreme.domain.model.Match
import com.example.sportsxtreme.domain.usecase.MatchUseCases
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.delay

@AndroidEntryPoint
class SelectPlayingTeamsActivity : ComponentActivity() {
    @Inject lateinit var matchUseCases: MatchUseCases

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        val matchId = intent.getStringExtra(EXTRA_MATCH_ID).orEmpty()
        val teamA = intent.toSelectedTeam(EXTRA_TEAM_A_ID, EXTRA_TEAM_A_NAME)
        val teamB = intent.toSelectedTeam(EXTRA_TEAM_B_ID, EXTRA_TEAM_B_NAME)
        val viewModel: TeamSelectionViewModel by viewModels {
            TeamSelectionViewModel.factory(matchId, matchUseCases)
        }
        setContent {
            val uiState by viewModel.uiState.collectAsState()
            SelectPlayingTeamsScreen(
                match = uiState.match,
                teamA = teamA,
                teamB = teamB,
                onBack = { finish() },
                onOpenStartMatchPreview = { selectedTeamA, selectedTeamB ->
                    startActivity(
                        Intent(this, StartMatchPreviewActivity::class.java)
                            .putExtra(EXTRA_MATCH_ID, matchId)
                            .putExtra(EXTRA_TEAM_A_ID, selectedTeamA.id)
                            .putExtra(EXTRA_TEAM_A_NAME, selectedTeamA.name)
                            .putExtra(EXTRA_TEAM_B_ID, selectedTeamB.id)
                            .putExtra(EXTRA_TEAM_B_NAME, selectedTeamB.name)
                    )
                    finish()
                },
                onSelectTeamA = {
                    startActivity(
                        Intent(this, SelectTeamAorBActivity::class.java)
                            .putExtra(SelectTeamAorBActivity.EXTRA_TEAM_SLOT, "A")
                            .putExtra(EXTRA_MATCH_ID, matchId)
                            .putExtra(EXTRA_SELECTED_TEAM_ID, teamA?.id)
                            .putExtras(intent.copyTeamSelectionExtras())
                    )
                },
                onSelectTeamB = {
                    startActivity(
                        Intent(this, SelectTeamAorBActivity::class.java)
                            .putExtra(SelectTeamAorBActivity.EXTRA_TEAM_SLOT, "B")
                            .putExtra(EXTRA_MATCH_ID, matchId)
                            .putExtra(EXTRA_SELECTED_TEAM_ID, teamB?.id)
                            .putExtras(intent.copyTeamSelectionExtras())
                    )
                }
            )
        }
    }

    companion object {
        const val EXTRA_MATCH_ID = "match_id"
        const val EXTRA_TEAM_SLOT = "team_slot"
        const val EXTRA_SELECTED_TEAM_ID = "selected_team_id"
        const val EXTRA_SELECTED_TEAM_NAME = "selected_team_name"
        const val EXTRA_TEAM_A_ID = "team_a_id"
        const val EXTRA_TEAM_A_NAME = "team_a_name"
        const val EXTRA_TEAM_B_ID = "team_b_id"
        const val EXTRA_TEAM_B_NAME = "team_b_name"
    }
}

private data class SelectedTeam(
    val id: String,
    val name: String
)

private fun Intent.toSelectedTeam(idKey: String, nameKey: String): SelectedTeam? {
    val id = getStringExtra(idKey).orEmpty()
    val name = getStringExtra(nameKey).orEmpty()
    return if (id.isBlank() || name.isBlank()) null else SelectedTeam(id, name)
}

private fun Intent.copyTeamSelectionExtras(): Bundle = Bundle().apply {
    putString(SelectPlayingTeamsActivity.EXTRA_TEAM_A_ID, getStringExtra(SelectPlayingTeamsActivity.EXTRA_TEAM_A_ID))
    putString(SelectPlayingTeamsActivity.EXTRA_TEAM_A_NAME, getStringExtra(SelectPlayingTeamsActivity.EXTRA_TEAM_A_NAME))
    putString(SelectPlayingTeamsActivity.EXTRA_TEAM_B_ID, getStringExtra(SelectPlayingTeamsActivity.EXTRA_TEAM_B_ID))
    putString(SelectPlayingTeamsActivity.EXTRA_TEAM_B_NAME, getStringExtra(SelectPlayingTeamsActivity.EXTRA_TEAM_B_NAME))
}

private val TeamsAccent = Color(0xFFD6EF7B)
private val TeamsBg = Color(0xFF01060D)
private val TeamsPanel = Color(0xFF07101A)
private val TeamsCard = Color(0xFF0A1422)
private val TeamsStroke = Color(0xFF25324A)
private val TeamsBlue = Color(0xFF1D67FF)
private val TeamsMuted = Color(0xFF93A3AA)

@Composable
private fun SelectPlayingTeamsScreen(
    match: Match?,
    teamA: SelectedTeam?,
    teamB: SelectedTeam?,
    onBack: () -> Unit,
    onOpenStartMatchPreview: (SelectedTeam, SelectedTeam) -> Unit,
    onSelectTeamA: () -> Unit,
    onSelectTeamB: () -> Unit
) {
    val bothTeamsSelected = teamA != null && teamB != null
    var showStartMatchFade by rememberSaveable { mutableStateOf(false) }
    var previewOpened by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(bothTeamsSelected) {
        showStartMatchFade = false
        if (bothTeamsSelected) {
            delay(250)
            showStartMatchFade = true
            delay(2500)
            if (!previewOpened) {
                previewOpened = true
                onOpenStartMatchPreview(requireNotNull(teamA), requireNotNull(teamB))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TeamsBg)
            .drawBehind {
                repeat(14) { index ->
                    val y = size.height * (0.09f + index * 0.052f)
                    drawLine(
                        color = Color(0x0D9BB2BA),
                        start = Offset(0f, y),
                        end = Offset(size.width, y + size.width * 0.05f),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                drawCircle(Color(0x101B5BFF), radius = size.width * 0.5f, center = Offset(size.width * 0.5f, size.height * 0.42f))
            }
    ) {
        TeamsTopBar(onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TeamsInfoCard(match)
            Spacer(Modifier.height(34.dp))
            AnimatedContent(
                targetState = bothTeamsSelected,
                transitionSpec = {
                    (fadeIn(tween(420)) + slideInVertically(tween(420)) { it / 4 }) togetherWith
                        fadeOut(tween(220))
                },
                label = "team-selection-layout"
            ) { showMatchup ->
                if (showMatchup) {
                    SelectedTeamsMatchup(teamA = requireNotNull(teamA), teamB = requireNotNull(teamB))
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        TeamSlot(team = teamA, emptyLabel = "SELECT TEAM A", onClick = onSelectTeamA)
                        VersusBadge(Modifier.padding(vertical = 24.dp))
                        TeamSlot(team = teamB, emptyLabel = "SELECT TEAM B", onClick = onSelectTeamB)
                    }
                }
            }
            StartMatchFadeAnimationWillOpen(visible = showStartMatchFade)
            MatchPreviewCard(teamA = teamA, teamB = teamB, modifier = Modifier.padding(top = 39.dp))
            Spacer(Modifier.height(42.dp))
        }
    }
}

@Composable
private fun TeamsTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp)
            .background(TeamsPanel)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TeamsArrow(Modifier.size(23.dp).clickable(onClick = onBack), tint = Color.White)
        Text(
            "SELECT PLAYING TEAMS",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 14.dp).weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        TeamsHelp(Modifier.size(20.dp))
    }
}

@Composable
private fun TeamsInfoCard(match: Match?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(TeamsCard)
            .border(1.dp, Color(0xFF17243A), RoundedCornerShape(8.dp))
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(Color(0xFF073D94)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.size(13.dp)) {
                drawCircle(TeamsBlue, radius = size.minDimension * 0.43f)
                drawCircle(Color.White, radius = size.minDimension * 0.08f)
            }
        }
        Text(
            match?.let {
                "${it.title} • ${it.status.name.replace('_', ' ')}\n${it.teamA.name} vs ${it.teamB.name}"
            } ?: "Loading match…",
            color = Color(0xFFD3E0E6),
            fontSize = 8.5.sp,
            lineHeight = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 13.dp).weight(1f)
        )
    }
}

@Composable
private fun TeamSlot(team: SelectedTeam?, emptyLabel: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(104.dp)
                .clickable(onClick = onClick)
                .drawBehind {
                    drawCircle(Color(0x331E79FF), radius = size.minDimension * 0.48f, style = Stroke(width = 1.dp.toPx()))
                    drawCircle(Color(0x665A6B33), radius = size.minDimension * 0.42f, style = Stroke(width = 1.3.dp.toPx()))
                    drawCircle(Color(0xFF081119), radius = size.minDimension * 0.38f)
                },
            contentAlignment = Alignment.Center
        ) {
            if (team == null) {
                Text("+", color = TeamsAccent, fontSize = 38.sp, fontWeight = FontWeight.Light)
            } else {
                TemporaryTeamLogo(team = team, modifier = Modifier.size(68.dp))
            }
        }
        Box(
            modifier = Modifier
                .padding(top = 13.dp)
                .height(32.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(TeamsAccent)
                .clickable(onClick = onClick)
                .padding(horizontal = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(team?.name ?: emptyLabel, color = Color(0xFF111604), fontSize = 8.sp, fontWeight = FontWeight.Black, maxLines = 1)
        }
    }
}

@Composable
private fun VersusBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(72.dp)
            .drawBehind {
                drawCircle(Color(0x33366DFF), radius = size.minDimension * 0.5f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF244FAD), Color(0xFF081326)),
                        radius = size.minDimension * 0.55f
                    ),
                    radius = size.minDimension * 0.43f
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text("VS", color = TeamsAccent, fontSize = 23.sp, fontWeight = FontWeight.Black, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
    }
}

@Composable
private fun MatchPreviewCard(teamA: SelectedTeam?, teamB: SelectedTeam?, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(142.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF07101A))
            .border(1.dp, Color(0xFF3E4B2A), RoundedCornerShape(12.dp))
            .padding(top = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("MATCH PREVIEW", color = TeamsAccent, fontSize = 8.sp, fontWeight = FontWeight.Black)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 44.dp, vertical = 17.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            PreviewTeamIcon(teamA)
            PreviewTeamIcon(teamB)
        }
        Text(
            if (teamA != null && teamB != null) {
                "${teamA.name} vs ${teamB.name}"
            } else {
                "Select two teams to create the\nmatch"
            },
            color = TeamsAccent,
            fontSize = 9.sp,
            lineHeight = 12.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PreviewTeamIcon(team: SelectedTeam?) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .drawBehind {
                drawCircle(Color(0x335B6E34), radius = size.minDimension * 0.48f)
                drawCircle(Color(0xFF2F3E22), radius = size.minDimension * 0.39f, style = Stroke(width = 1.dp.toPx()))
                drawCircle(Color(0xFF111A21), radius = size.minDimension * 0.32f)
            },
        contentAlignment = Alignment.Center
    ) {
        if (team == null) TeamBenchIcon(Modifier.size(23.dp), TeamsAccent)
        else TemporaryTeamLogo(team = team, modifier = Modifier.size(33.dp))
    }
}

@Composable
private fun SelectedTeamsMatchup(teamA: SelectedTeam, teamB: SelectedTeam) {
    val floatingTransition = rememberInfiniteTransition(label = "team-logo-float")
    val logoOffset by floatingTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(950), RepeatMode.Reverse),
        label = "team-a-logo-offset"
    )

    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MatchupTeam(team = teamA, verticalOffset = logoOffset, modifier = Modifier.weight(1f))
        VersusBadge(Modifier.size(62.dp))
        MatchupTeam(team = teamB, verticalOffset = -logoOffset, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun MatchupTeam(team: SelectedTeam, verticalOffset: Float, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        TemporaryTeamLogo(team = team, modifier = Modifier.size(86.dp).offset(y = verticalOffset.dp))
        Text(
            team.name,
            color = TeamsAccent,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 11.dp)
        )
    }
}

@Composable
private fun TemporaryTeamLogo(team: SelectedTeam, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .drawBehind {
                drawCircle(Color(0x334E9AFF), radius = size.minDimension * 0.5f)
                drawCircle(Color(0xFF111A21), radius = size.minDimension * 0.39f)
            },
        contentAlignment = Alignment.Center
    ) {
        TeamBenchIcon(Modifier.size(34.dp), TeamsAccent)
        Text(
            team.name.take(2).uppercase(),
            color = Color.White,
            fontSize = 8.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp)
        )
    }
}

@Composable
private fun StartMatchFadeAnimationWillOpen(visible: Boolean) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(600)) + scaleIn(initialScale = 0.92f, animationSpec = tween(600))
    ) {
        Text(
            "MATCH READY",
            color = TeamsAccent,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(top = 24.dp)
        )
    }
}

@Composable
internal fun TeamBenchIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        drawRoundRect(
            color = tint,
            topLeft = Offset(size.width * 0.18f, size.height * 0.38f),
            size = Size(size.width * 0.64f, size.height * 0.28f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx()),
            style = stroke
        )
        drawLine(tint, Offset(size.width * 0.28f, size.height * 0.66f), Offset(size.width * 0.28f, size.height * 0.84f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.72f, size.height * 0.66f), Offset(size.width * 0.72f, size.height * 0.84f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.12f, size.height * 0.84f), Offset(size.width * 0.88f, size.height * 0.84f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.12f, center = Offset(size.width * 0.33f, size.height * 0.22f), style = stroke)
        drawCircle(tint, radius = size.minDimension * 0.12f, center = Offset(size.width * 0.67f, size.height * 0.22f), style = stroke)
    }
}

@Composable
private fun TeamsArrow(modifier: Modifier, tint: Color) {
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
private fun TeamsHelp(modifier: Modifier) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(Color(0xFFAAB7B4), radius = size.minDimension * 0.38f, style = stroke)
        drawArc(Color(0xFFAAB7B4), 210f, 215f, false, style = stroke)
        drawCircle(Color(0xFFAAB7B4), radius = size.minDimension * 0.035f, center = Offset(size.width * 0.5f, size.height * 0.68f))
    }
}
