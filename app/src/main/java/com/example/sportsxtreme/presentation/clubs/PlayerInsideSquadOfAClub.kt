package com.example.sportsxtreme.presentation.clubs

import android.graphics.Color
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
import androidx.compose.ui.graphics.Color as UiColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

class PlayerInsideSquadOfAClub : ComponentActivity() {
    companion object { const val EXTRA_TEAM_NAME = "extra_team_name" }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = Color.rgb(3, 9, 18)
        window.navigationBarColor = Color.rgb(3, 9, 18)
        setContent { TeamPlayersPage(intent.getStringExtra(EXTRA_TEAM_NAME) ?: "Senior Team A", ::finish) }
    }
}

private val PlayersBg = UiColor(3, 9, 18)
private val PlayersPanel = UiColor(10, 21, 36)
private val PlayersAccent = UiColor(198, 255, 13)
private val PlayersMuted = UiColor(176, 186, 199)
private data class TeamPlayer(val number: String, val jersey: String, val name: String, val position: String)

@Composable
private fun TeamPlayersPage(teamName: String, onBack: () -> Unit) = Column(Modifier.fillMaxSize().background(PlayersBg).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp)) {
    Spacer(Modifier.height(18.dp))
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(31.dp).clip(RoundedCornerShape(7.dp)).background(PlayersPanel).border(1.dp, UiColor(27, 46, 66), RoundedCornerShape(7.dp)).clickable { onBack() }, contentAlignment = Alignment.Center) { Text("<", color = UiColor.White, fontSize = 22.sp) }
        Text(teamName, color = UiColor.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 18.dp).weight(1f))
        Text("⋮", color = UiColor.White, fontSize = 24.sp)
    }
    Spacer(Modifier.height(12.dp))
    Row(Modifier.fillMaxWidth().height(67.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(63.dp).clip(RoundedCornerShape(9.dp)).background(UiColor(9, 20, 35)), contentAlignment = Alignment.Center) { Text("STA", color = PlayersAccent, fontSize = 23.sp, fontWeight = FontWeight.Bold) }
        Column(Modifier.weight(1f).padding(start = 12.dp)) { Text(teamName, color = UiColor.White, fontSize = 13.sp, fontWeight = FontWeight.Bold); Text("Senior Men Squad", color = PlayersMuted, fontSize = 9.sp) }
        Box(Modifier.clip(RoundedCornerShape(10.dp)).background(UiColor(20, 43, 16)).border(1.dp, PlayersAccent, RoundedCornerShape(10.dp)).padding(horizontal = 10.dp, vertical = 5.dp)) { Text("• Active", color = PlayersAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold) }
    }
    Spacer(Modifier.height(20.dp))
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("|", color = PlayersAccent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Players", color = UiColor.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f).padding(start = 7.dp))
        Box(Modifier.height(28.dp).clip(RoundedCornerShape(6.dp)).background(PlayersAccent).padding(horizontal = 12.dp), contentAlignment = Alignment.Center) { Text("♧  Add Player", color = UiColor.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold) }
    }
    Spacer(Modifier.height(12.dp))
    val players = listOf(
        TeamPlayer("01", "07", "Rahul Kumar", "Right Hand Batsman"), TeamPlayer("02", "18", "Amit Sharma", "Right Arm Fast Bowler"), TeamPlayer("03", "45", "Suresh Nayak", "Wicket Keeper"), TeamPlayer("04", "22", "Vivek Sharma", "Right Hand Batsman"), TeamPlayer("05", "31", "Nilesh Rao", "Right Arm Off Break"), TeamPlayer("06", "11", "Arjun Patil", "Left Hand Batsman"), TeamPlayer("07", "09", "Karan Singh", "Right Arm Fast Bowler"), TeamPlayer("08", "27", "Vivek Sharma", "Right Arm Medium Fast"), TeamPlayer("09", "63", "Rahul Das", "All Rounder"), TeamPlayer("10", "04", "Amit Kumar", "Right Hand Batsman")
    )
    players.forEachIndexed { index, player -> PlayerRow(player, if (index == 0) "Captain" else if (index == 1) "Vice Captain" else null); Spacer(Modifier.height(6.dp)) }
    Spacer(Modifier.height(20.dp))
}

@Composable
private fun PlayerRow(player: TeamPlayer, badge: String?) = Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = PlayersPanel)) {
    Row(Modifier.fillMaxWidth().height(50.dp).padding(horizontal = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(29.dp).clip(CircleShape).background(UiColor(19, 35, 41)), contentAlignment = Alignment.Center) { Text(player.number, color = PlayersAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
        Box(Modifier.size(39.dp).padding(start = 6.dp).clip(CircleShape).background(UiColor(38, 54, 70)), contentAlignment = Alignment.Center) { Text(player.name.take(1), color = UiColor.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
        Text(player.jersey, color = PlayersAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(35.dp).padding(start = 10.dp))
        Column(Modifier.weight(1f)) { Row(verticalAlignment = Alignment.CenterVertically) { Text(player.name, color = UiColor.White, fontSize = 11.sp, fontWeight = FontWeight.Bold); if (badge != null) Text(badge, color = PlayersAccent, fontSize = 7.sp, modifier = Modifier.padding(start = 8.dp).border(1.dp, PlayersAccent, RoundedCornerShape(4.dp)).padding(horizontal = 5.dp, vertical = 2.dp)) }; Text(player.position, color = PlayersMuted, fontSize = 8.sp) }
        Text(">", color = PlayersMuted, fontSize = 23.sp)
    }
}
