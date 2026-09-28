package com.example.sportsxtreme.presentation.scoring

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.example.sportsxtreme.R
import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.data.sync.PausedScoringExpiryScheduler
import com.example.sportsxtreme.domain.model.Match
import com.example.sportsxtreme.domain.repository.MatchRepository
import com.example.sportsxtreme.domain.repository.TournamentRepository
import com.example.sportsxtreme.presentation.match.SelectPlayingTeamsActivity
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@AndroidEntryPoint
class QuickScoringActivity : ComponentActivity() {
    @Inject lateinit var matchRepository: MatchRepository
    @Inject lateinit var tournamentRepository: TournamentRepository
    @Inject lateinit var firestore: FirebaseFirestore
    @Inject lateinit var expiryScheduler: PausedScoringExpiryScheduler

    private val resumableMatch = mutableStateOf<Match?>(null)
    private val tournamentName = mutableStateOf<String?>(null)
    private val matchStage = mutableStateOf<String?>(null)
    private val isLoading = mutableStateOf(true)
    private val isResuming = mutableStateOf(false)
    private val isDeleting = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)

        lifecycleScope.launch {
            matchRepository.observeLatestScoringMatch().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val match = result.data ?: return@collect
                        if (System.currentTimeMillis() - match.updatedAtEpochMs >= RESUME_WINDOW_MS) {
                            matchRepository.expirePausedScoringMatch(match.id, match.updatedAtEpochMs)
                            resumableMatch.value = null
                        } else {
                            resumableMatch.value = match
                            loadTournamentAndStage(match)
                        }
                    }
                    is Resource.Error -> resumableMatch.value = null
                    is Resource.Loading -> Unit
                }
                isLoading.value = false
            }
        }

        setContent {
            QuickScoringScreen(
                match = resumableMatch.value,
                tournamentName = tournamentName.value,
                stage = matchStage.value,
                isLoading = isLoading.value,
                isResuming = isResuming.value,
                isDeleting = isDeleting.value,
                onBack = { finish() },
                onResume = ::resumeMatch,
                onDelete = ::deleteMatch
            )
        }
    }

    private suspend fun loadTournamentAndStage(match: Match) {
        tournamentName.value = match.tournamentId?.let { id ->
            (tournamentRepository.getTournament(id) as? Resource.Success)?.data?.name
        }
        matchStage.value = runCatching {
            val data = firestore.collection("matches").document(match.id).get().await()
            data.getString("roundName") ?: data.getString("selectedStage")
        }.getOrNull()
    }

    private fun resumeMatch(match: Match) {
        if (isResuming.value) return
        val inningsId = match.innings.lastOrNull()?.id
        if (inningsId.isNullOrBlank()) return
        isResuming.value = true
        lifecycleScope.launch {
            if (matchRepository.resumeScoring(match.id)) {
                expiryScheduler.cancel(match.id)
                startActivity(
                    Intent(this@QuickScoringActivity, MainScoringActivity::class.java)
                        .putExtra(SelectPlayingTeamsActivity.EXTRA_MATCH_ID, match.id)
                        .putExtra(MainScoringActivity.EXTRA_INNINGS_ID, inningsId)
                )
            }
            isResuming.value = false
        }
    }

    private fun deleteMatch(match: Match) {
        if (isDeleting.value) return
        isDeleting.value = true
        lifecycleScope.launch {
            when (val result = matchRepository.deleteMatch(match.id)) {
                is Resource.Success -> {
                    expiryScheduler.cancel(match.id)
                    resumableMatch.value = null
                }
                is Resource.Error -> Toast.makeText(
                    this@QuickScoringActivity,
                    result.message ?: "Unable to delete match",
                    Toast.LENGTH_LONG
                ).show()
                is Resource.Loading -> Unit
            }
            isDeleting.value = false
        }
    }

    companion object {
        private const val RESUME_WINDOW_MS = 2 * 60 * 60 * 1000L
    }
}

private val QuickAccent = Color(0xFFC1FF00)
private val QuickBg = Color(0xFF020A15)
private val QuickPanel = Color(0xFF07121E)
private val QuickCard = Color(0xFF0B1827)
private val QuickStroke = Color(0xFF26364A)
private val QuickMuted = Color(0xFFAAB6C4)

@Composable
private fun QuickScoringScreen(
    match: Match?,
    tournamentName: String?,
    stage: String?,
    isLoading: Boolean,
    isResuming: Boolean,
    isDeleting: Boolean,
    onBack: () -> Unit,
    onResume: (Match) -> Unit,
    onDelete: (Match) -> Unit
) {
    Box(
        Modifier.fillMaxSize().background(QuickBg).drawBehind {
            drawCircle(Color(0x22125E8E), size.width * .62f, Offset(size.width * .94f, size.height * .12f))
            drawCircle(Color(0x182D5414), size.width * .58f, Offset(size.width * .05f, size.height * .72f))
        }
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().height(60.dp).background(QuickPanel).padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(34.dp).clip(CircleShape).clickable(onClick = onBack), contentAlignment = Alignment.Center) {
                    Text("‹", color = Color.White, fontSize = 31.sp, fontWeight = FontWeight.Light)
                }
                Column(Modifier.padding(start = 11.dp)) {
                    Text("QUICK SCORING", color = QuickMuted, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                    Text("Resume your match", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                }
            }
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("PICK UP WHERE YOU LEFT OFF", color = QuickAccent, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
                when {
                    isLoading -> Text("Checking for a paused match…", color = QuickMuted, fontSize = 14.sp)
                    match == null -> EmptyQuickScoringCard()
                    else -> ResumableMatchCard(
                        match = match,
                        tournamentName = tournamentName,
                        stage = stage,
                        isResuming = isResuming,
                        isDeleting = isDeleting,
                        onResume = { onResume(match) },
                        onDelete = { onDelete(match) }
                    )
                }
                Text("Paused matches are available to resume for up to 2 hours.", color = QuickMuted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun ResumableMatchCard(
    match: Match,
    tournamentName: String?,
    stage: String?,
    isResuming: Boolean,
    isDeleting: Boolean,
    onResume: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val date = remember(match.matchDateEpochMs, match.createdAtEpochMs) {
        SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(Date(match.matchDateEpochMs ?: match.createdAtEpochMs))
    }
    val time = match.matchTime?.takeIf { it.isNotBlank() } ?: remember(match.createdAtEpochMs) {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(match.createdAtEpochMs))
    }
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(QuickCard)
            .border(1.dp, QuickStroke, RoundedCornerShape(22.dp)).padding(17.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(tournamentName ?: match.title, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${match.teamA.name}  vs  ${match.teamB.name}", color = QuickMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Column(horizontalAlignment = Alignment.End) {
                Box {
                    IconButton(onClick = { menuExpanded = true }, enabled = !isDeleting) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Match options", tint = Color.White)
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Delete match") },
                            onClick = {
                                menuExpanded = false
                                showDeleteConfirmation = true
                            }
                        )
                    }
                }
                Text("PAUSED", color = QuickAccent, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.clip(CircleShape).background(Color(0x2238B42B)).padding(horizontal = 10.dp, vertical = 7.dp))
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MatchInfoTile("STAGE", stage ?: "Innings ${match.innings.lastOrNull()?.number ?: 1}", Modifier.weight(1f))
            MatchInfoTile("DATE", date, Modifier.weight(1f))
        }
        MatchInfoTile("TIME", time, Modifier.fillMaxWidth())
        Box(
            Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(14.dp)).background(QuickAccent).clickable(enabled = !isResuming && !isDeleting, onClick = onResume),
            contentAlignment = Alignment.Center
        ) {
            Text(if (isResuming) "REOPENING SCORING…" else "RESUME SCORING", color = QuickBg, fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = .6.sp)
        }
    }
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete this match?") },
            text = { Text("This permanently deletes the match, its scorecards, and scoring data.") },
            confirmButton = {
                TextButton(
                    enabled = !isDeleting,
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete()
                    }
                ) { Text(if (isDeleting) "Deleting…" else "Delete", color = Color(0xFFB3261E)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }, enabled = !isDeleting) {
                    Text("Cancel", color = QuickMuted)
                }
            }
        )
    }
}

@Composable
private fun MatchInfoTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(13.dp)).background(QuickPanel).padding(horizontal = 12.dp, vertical = 11.dp)) {
        Text(label, color = QuickMuted, fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        Spacer(Modifier.height(5.dp))
        Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun EmptyQuickScoringCard() {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(QuickCard)
            .border(1.dp, QuickStroke, RoundedCornerShape(20.dp)).padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Text("NO PAUSED MATCH", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
        Text("Start a match and begin scoring. If you leave the scoring screen, it will appear here so you can resume.", color = QuickMuted, fontSize = 13.sp, lineHeight = 19.sp)
    }
}
