package com.example.sportsxtreme.presentation.clubs

import android.graphics.Color
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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

class OwnedClubPageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = Color.rgb(3, 9, 18)
        window.navigationBarColor = Color.rgb(3, 9, 18)
        setContent { OwnedClubPage(::finish) }
    }
}

private val OwnedBg = UiColor(3, 9, 18)
private val OwnedPanel = UiColor(10, 21, 36)
private val Accent = UiColor(198, 255, 13)
private val OwnedMuted = UiColor(169, 180, 194)

@Composable
private fun OwnedClubPage(onBack: () -> Unit) = Column(Modifier.fillMaxSize().background(OwnedBg).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
    val context = LocalContext.current
    Spacer(Modifier.height(14.dp))
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        NavCircle("<", onBack)
        Spacer(Modifier.weight(1f))
        Text("♧", color = UiColor.White, fontSize = 23.sp)
        Spacer(Modifier.width(17.dp))
        NavCircle("⋮")
    }
    Spacer(Modifier.height(13.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(70.dp).clip(RoundedCornerShape(13.dp)).background(Brush.linearGradient(listOf(UiColor(20, 28, 20), UiColor(4, 8, 15)))), contentAlignment = Alignment.Center) {
            Text("VICTORY\nCRICKET\nCLUB", color = UiColor.White, fontSize = 10.sp, lineHeight = 11.sp, fontWeight = FontWeight.Bold)
        }
        Column(Modifier.padding(start = 14.dp)) {
            Text("Victory Cricket Club  ✦", color = UiColor.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Box(Modifier.clip(RoundedCornerShape(9.dp)).background(UiColor(48, 64, 13)).padding(horizontal = 9.dp, vertical = 4.dp)) { Text("♧  Club Owner", color = Accent, fontSize = 9.sp) }
        }
    }
    Spacer(Modifier.height(24.dp))
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        SectionTitle("Club Overview", Modifier.weight(1f))
        Text("View All Stats  >", color = Accent, fontSize = 9.sp, modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(OwnedPanel).padding(horizontal = 9.dp, vertical = 5.dp))
    }
    Spacer(Modifier.height(12.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) { StatCard("♧", "Members", "486", Modifier.weight(1f)); StatCard("♣", "Squads", "5", Modifier.weight(1f)); StatCard("♜", "Tournaments", "6", Modifier.weight(1f)) }
    Spacer(Modifier.height(10.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) { StatCard("♛", "Venues", "3", Modifier.weight(1f)); StatCard("◉", "Matches", "145", Modifier.weight(1f)); StatCard("▣", "Season", "2026", Modifier.weight(1f)) }
    Spacer(Modifier.height(20.dp))
    SectionTitle("Club Management")
    Spacer(Modifier.height(10.dp))
    ManagementRow("♧", "Members", "Manage club members, invitations\nand roles.") {
        context.startActivity(Intent(context, ClubMemberPageActivity::class.java))
    }
    ManagementRow("♣", "Squads", "Manage Senior, Women's, Under-19\nand Under-16 squads.") {
        context.startActivity(Intent(context, SquadInClubActivity::class.java))
    }
    ManagementRow("♜", "Tournaments", "Create and manage internal and\nexternal tournaments.") {
        context.startActivity(Intent(context, ClubsTournamentPlayedActivity::class.java))
    }
    ManagementRow("▣", "Matches", "Schedule, score and\nmanage fixtures.") {
        context.startActivity(Intent(context, MatchesPlayedByClubActivity::class.java))
    }
    ManagementRow("▥", "Analytics", "View club performance, squad\nand tournament analytics.") {
        context.startActivity(Intent(context, ClubAnalyticsActivity::class.java))
    }
    ManagementRow("⚙", "More", "Announcements, club profile,\nsettings and other tools.")
    Spacer(Modifier.height(25.dp))
}

@Composable
private fun NavCircle(label: String, onClick: (() -> Unit)? = null) = Box(Modifier.size(28.dp).clip(CircleShape).background(UiColor(17, 30, 46)).clickable { onClick?.invoke() }, contentAlignment = Alignment.Center) { Text(label, color = UiColor.White, fontSize = 18.sp) }

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) = Text("|  $text", color = UiColor.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = modifier)

@Composable
private fun StatCard(icon: String, label: String, value: String, modifier: Modifier) = Card(modifier, shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = OwnedPanel)) {
    Column(Modifier.height(86.dp).fillMaxWidth().padding(top = 11.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, color = Accent, fontSize = 21.sp); Spacer(Modifier.height(2.dp)); Text(label, color = OwnedMuted, fontSize = 9.sp); Text(value, color = UiColor.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ManagementRow(icon: String, title: String, body: String, onClick: (() -> Unit)? = null) = Card(Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { onClick?.invoke() }, shape = RoundedCornerShape(9.dp), colors = CardDefaults.cardColors(containerColor = OwnedPanel)) {
    Row(Modifier.height(62.dp).padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(42.dp).clip(RoundedCornerShape(8.dp)).background(UiColor(18, 35, 52)), contentAlignment = Alignment.Center) { Text(icon, color = Accent, fontSize = 23.sp) }
        Column(Modifier.weight(1f).padding(start = 13.dp)) { Text(title, color = UiColor.White, fontSize = 13.sp, fontWeight = FontWeight.Bold); Text(body, color = OwnedMuted, fontSize = 9.sp, lineHeight = 11.sp) }
        Text(">", color = UiColor.White, fontSize = 27.sp)
    }
}
