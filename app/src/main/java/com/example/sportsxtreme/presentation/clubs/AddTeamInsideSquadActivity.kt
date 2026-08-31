package com.example.sportsxtreme.presentation.clubs

import android.graphics.Color
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color as UiColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

class AddTeamInsideSquadActivity : ComponentActivity() {
    companion object { const val EXTRA_SQUAD_NAME = "extra_squad_name" }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = Color.rgb(3, 9, 18)
        window.navigationBarColor = Color.rgb(3, 9, 18)
        setContent { SquadTeamsPage(intent.getStringExtra(EXTRA_SQUAD_NAME) ?: "Senior Men", ::finish) }
    }
}

private val TeamBg = UiColor(3, 9, 18)
private val TeamPanel = UiColor(10, 21, 36)
private val TeamAccent = UiColor(198, 255, 13)
private val TeamMuted = UiColor(190, 196, 207)

@Composable
private fun SquadTeamsPage(squad: String, onBack: () -> Unit) = Column(Modifier.fillMaxSize().background(TeamBg).verticalScroll(rememberScrollState()).padding(horizontal = 33.dp)) {
    Spacer(Modifier.height(51.dp))
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(71.dp).clip(RoundedCornerShape(15.dp)).background(TeamPanel).border(1.dp, UiColor(28, 47, 67), RoundedCornerShape(15.dp)).clickable { onBack() }, contentAlignment = Alignment.Center) { Text("<", color = UiColor.White, fontSize = 51.sp) }
        Text(squad, color = UiColor.White, fontSize = 35.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 31.dp))
        Spacer(Modifier.weight(1f)); Text("⋮", color = UiColor.White, fontSize = 39.sp)
    }
    Spacer(Modifier.height(45.dp))
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = TeamPanel)) {
        Row(Modifier.fillMaxWidth().height(210.dp).padding(horizontal = 35.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(130.dp).clip(CircleShape).border(2.dp, UiColor(53, 76, 46), CircleShape), contentAlignment = Alignment.Center) { Text("♧", color = TeamAccent, fontSize = 70.sp) }
            Column(Modifier.padding(start = 25.dp)) { Text("$squad Squad", color = UiColor.White, fontSize = 34.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(16.dp)); Text("5", color = TeamAccent, fontSize = 31.sp, fontWeight = FontWeight.Bold); Text(" Teams  •  ", color = TeamMuted, fontSize = 28.sp, modifier = Modifier.offset(x = 36.dp, y = (-35).dp)); Text("90", color = TeamAccent, fontSize = 31.sp, fontWeight = FontWeight.Bold, modifier = Modifier.offset(x = 142.dp, y = (-71).dp)); Text(" Players", color = TeamMuted, fontSize = 28.sp, modifier = Modifier.offset(x = 181.dp, y = (-106).dp)) }
        }
    }
    Spacer(Modifier.height(51.dp))
    Text("|", color = TeamAccent, fontSize = 43.sp, modifier = Modifier.offset(y = 8.dp)); Text("Teams", color = UiColor.White, fontSize = 31.sp, fontWeight = FontWeight.Bold, modifier = Modifier.offset(x = 28.dp, y = (-35).dp))
    val captains = listOf("Rahul", "Amit", "Suresh", "Vikas", "Nilesh")
    val context = androidx.compose.ui.platform.LocalContext.current
    captains.forEachIndexed { index, captain ->
        TeamRow("${squad.removeSuffix(" Men").removeSuffix("'s")} Team ${('A'.code + index).toChar()}", captain) { teamName ->
            context.startActivity(Intent(context, PlayerInsideSquadOfAClub::class.java).putExtra(PlayerInsideSquadOfAClub.EXTRA_TEAM_NAME, teamName))
        }
        Spacer(Modifier.height(20.dp))
    }
    Box(Modifier.fillMaxWidth().height(109.dp).clip(RoundedCornerShape(15.dp)).border(2.dp, TeamAccent, RoundedCornerShape(15.dp)), contentAlignment = Alignment.Center) { Text("+   Create New Team", color = TeamAccent, fontSize = 28.sp, fontWeight = FontWeight.Bold) }
    Spacer(Modifier.height(100.dp))
}

@Composable
private fun TeamRow(name: String, captain: String, onClick: (String) -> Unit) = Card(Modifier.clickable { onClick(name) }, shape = RoundedCornerShape(19.dp), colors = CardDefaults.cardColors(containerColor = TeamPanel)) {
    Row(Modifier.fillMaxWidth().height(181.dp).padding(horizontal = 35.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(112.dp).clip(CircleShape).border(2.dp, UiColor(53, 76, 46), CircleShape), contentAlignment = Alignment.Center) { Text("♧", color = TeamAccent, fontSize = 61.sp) }
        Column(Modifier.weight(1f).padding(start = 26.dp)) { Text(name, color = UiColor.White, fontSize = 30.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(17.dp)); Text("18", color = TeamAccent, fontSize = 25.sp, fontWeight = FontWeight.Bold); Text(" Players  •  Captain $captain", color = TeamMuted, fontSize = 24.sp, modifier = Modifier.offset(x = 38.dp, y = (-29).dp)) }
        Text(">", color = UiColor.White, fontSize = 56.sp)
    }
}
