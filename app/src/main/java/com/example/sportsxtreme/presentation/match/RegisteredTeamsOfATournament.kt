package com.example.sportsxtreme.presentation.match

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.example.sportsxtreme.R
import com.google.firebase.firestore.FirebaseFirestore

class RegisteredTeamsOfATournament : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        val tournamentId = intent.getStringExtra(SelectPlayingTeamsActivity.EXTRA_TOURNAMENT_ID).orEmpty()
        val slot = intent.getStringExtra(SelectPlayingTeamsActivity.EXTRA_TEAM_SLOT).orEmpty()
        setContent {
            val teams = rememberTournamentTeams(tournamentId)
            var selected by remember { mutableStateOf<RegisteredTournamentTeam?>(null) }
            RegisteredTeamsScreen(
                teams = teams,
                selected = selected,
                onSelect = { selected = it },
                onDone = {
                    selected?.let { team ->
                        setResult(RESULT_OK, intent
                            .putExtra(SelectPlayingTeamsActivity.EXTRA_SELECTED_TEAM_ID, team.id)
                            .putExtra(SelectPlayingTeamsActivity.EXTRA_SELECTED_TEAM_NAME, team.name)
                            .putExtra(SelectPlayingTeamsActivity.EXTRA_TEAM_SLOT, slot))
                        finish()
                    }
                },
                onBack = { finish() }
            )
        }
    }
}

private data class RegisteredTournamentTeam(val id: String, val name: String)

@Composable
private fun rememberTournamentTeams(tournamentId: String): List<RegisteredTournamentTeam> {
    var teams by remember(tournamentId) { mutableStateOf(emptyList<RegisteredTournamentTeam>()) }
    DisposableEffect(tournamentId) {
        if (tournamentId.isBlank()) return@DisposableEffect onDispose { }
        val listener = FirebaseFirestore.getInstance().collection("tournaments")
            .document(tournamentId).collection("teams")
            .addSnapshotListener { snapshots, _ ->
                teams = snapshots?.documents.orEmpty().map { document ->
                    val name = document.getString("teamName")?.trim().orEmpty().ifBlank { "Team" }
                    RegisteredTournamentTeam(document.id, name)
                }.sortedBy { it.name.lowercase() }
            }
        onDispose { listener.remove() }
    }
    return teams
}

@Composable
private fun RegisteredTeamsScreen(
    teams: List<RegisteredTournamentTeam>,
    selected: RegisteredTournamentTeam?,
    onSelect: (RegisteredTournamentTeam) -> Unit,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    val bg = Color(0xFF020914)
    val accent = Color(0xFFC6FF00)
    Column(Modifier.fillMaxSize().background(bg).padding(18.dp)) {
        Text("‹   REGISTERED TEAMS", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp,
            modifier = Modifier.clickable(onClick = onBack).padding(vertical = 14.dp))
        Text("Choose one team for this match", color = Color(0xFF91A0AF), fontSize = 12.sp)
        Spacer(Modifier.height(18.dp))
        if (teams.isEmpty()) {
            Text("No teams are registered for this tournament yet.", color = Color.White, fontSize = 14.sp)
        } else {
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(teams, key = { it.id }) { team ->
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .background(if (selected?.id == team.id) accent.copy(alpha = .16f) else Color(0xFF09192A))
                            .clickable { onSelect(team) }.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(team.name, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        if (selected?.id == team.id) Text("SELECTED", color = accent, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
        Button(onClick = onDone, enabled = selected != null, modifier = Modifier.fillMaxWidth()) {
            Text("DONE")
        }
    }
}
