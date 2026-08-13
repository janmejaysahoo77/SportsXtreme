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
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat

class SquadInClubActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = Color.rgb(3, 9, 18)
        window.navigationBarColor = Color.rgb(3, 9, 18)
        setContent { SquadsPage(::finish) }
    }
}

private val SquadBg = UiColor(3, 9, 18)
private val SquadPanel = UiColor(10, 21, 36)
private val SquadLime = UiColor(198, 255, 13)
private val SquadMuted = UiColor(169, 180, 194)

@Composable
private fun SquadsPage(onBack: () -> Unit) = Column(Modifier.fillMaxSize().background(SquadBg).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp)) {
    val context = LocalContext.current
    Spacer(Modifier.height(19.dp))
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(37.dp).clip(RoundedCornerShape(8.dp)).background(SquadPanel).border(1.dp, UiColor(30, 49, 69), RoundedCornerShape(8.dp)).clickable { onBack() }, contentAlignment = Alignment.Center) { Text("<", color = UiColor.White, fontSize = 25.sp) }
        Text("Squads", color = UiColor.White, fontSize = 19.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f).padding(start = 19.dp))
        Box(Modifier.size(32.dp).clip(CircleShape).border(2.dp, SquadLime, CircleShape), contentAlignment = Alignment.Center) { Text("+", color = SquadLime, fontSize = 26.sp) }
    }
    Spacer(Modifier.height(30.dp))
    Row(Modifier.fillMaxWidth().height(70.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(70.dp).clip(RoundedCornerShape(15.dp)).background(SquadPanel).border(1.dp, UiColor(25, 43, 61), RoundedCornerShape(15.dp)), contentAlignment = Alignment.Center) { Text("♧", color = SquadLime, fontSize = 38.sp) }
        Column(Modifier.padding(start = 17.dp)) { Text("Squads", color = UiColor.White, fontSize = 19.sp, fontWeight = FontWeight.Bold); Text("Squads are categories of teams\nin your club.", color = SquadMuted, fontSize = 12.sp, lineHeight = 17.sp) }
    }
    Spacer(Modifier.height(27.dp))
    SquadRow("♜", "Senior Men", "5 Teams", context)
    Spacer(Modifier.height(15.dp))
    SquadRow("♙", "Women's", "3 Teams", context)
    Spacer(Modifier.height(15.dp))
    SquadRow("♙", "Under-19 Boys", "4 Teams", context)
    Spacer(Modifier.height(15.dp))
    SquadRow("♙", "Under-16 Boys", "2 Teams", context)
    Spacer(Modifier.height(16.dp))
    Box(Modifier.fillMaxWidth().height(53.dp).clip(RoundedCornerShape(9.dp)).border(1.dp, SquadLime, RoundedCornerShape(9.dp)), contentAlignment = Alignment.Center) { Text("+  Create New Squad", color = SquadLime, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
}

@Composable
private fun SquadRow(icon: String, title: String, teams: String, context: android.content.Context) = Card(Modifier.clickable { context.startActivity(Intent(context, AddTeamInsideSquadActivity::class.java).putExtra(AddTeamInsideSquadActivity.EXTRA_SQUAD_NAME, title)) }, shape = RoundedCornerShape(9.dp), colors = CardDefaults.cardColors(containerColor = SquadPanel)) {
    Row(Modifier.fillMaxWidth().height(89.dp).padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(55.dp).clip(RoundedCornerShape(11.dp)).background(UiColor(12, 27, 43)).border(1.dp, UiColor(26, 46, 65), RoundedCornerShape(11.dp)), contentAlignment = Alignment.Center) { Text(icon, color = SquadLime, fontSize = 30.sp) }
        Column(Modifier.weight(1f).padding(start = 16.dp)) { Text(title, color = UiColor.White, fontSize = 16.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(4.dp)); Text(teams, color = SquadMuted, fontSize = 13.sp) }
        Text(">", color = UiColor.White, fontSize = 28.sp)
    }
}
