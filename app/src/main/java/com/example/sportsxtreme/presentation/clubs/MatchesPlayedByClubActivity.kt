package com.example.sportsxtreme.presentation.clubs

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color as UiColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

class MatchesPlayedByClubActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = Color.rgb(3, 9, 18)
        window.navigationBarColor = Color.rgb(3, 9, 18)
        setContent { ClubMatchesPage(::finish) }
    }
}

private val MatchBg = UiColor(3, 9, 18)
private val MatchPanel = UiColor(10, 21, 36)
private val MatchLime = UiColor(198, 255, 13)
private val MatchMuted = UiColor(176, 186, 199)
private data class LiveMatch(val title: String, val left: String, val leftScore: String, val right: String, val rightScore: String, val viewers: String, val venue: String)

@Composable
private fun ClubMatchesPage(onBack: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val matches = listOf(
        LiveMatch("Senior Team vs Titans CC", "SENIOR TEAM", "128/4\n(16.2 Overs)", "TITANS CC", "142/6\n(20 Overs)", "2.1k Watching", "Kalinga Cricket Ground, Bhubaneswar"),
        LiveMatch("Women's Team vs Queens XI", "WOMEN'S TEAM", "86/2\n(10 Overs)", "QUEENS XI", "-\n(Yet to Bat)", "1.3k Watching", "Barabati Stadium, Cuttack"),
        LiveMatch("U19 Team vs Rising Stars", "U19 TEAM", "98/3\n(14.1 Overs)", "RISING STARS", "-\n(Yet to Bat)", "856 Watching", "Green Field Stadium, Bhubaneswar"),
        LiveMatch("Senior Team vs Royal Warriors", "SENIOR TEAM", "67/1\n(8.0 Overs)", "ROYAL WARRIORS", "-\n(Yet to Bat)", "642 Watching", "Kalinga Cricket Ground, Bhubaneswar"),
        LiveMatch("Women's Team vs Power Women", "WOMEN'S TEAM", "45/0\n(4.2 Overs)", "POWER WOMEN", "-\n(Yet to Bat)", "523 Watching", "Barabati Stadium, Cuttack")
    )
    Column(Modifier.fillMaxSize().background(MatchBg).verticalScroll(rememberScrollState()).padding(horizontal = 12.dp)) {
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth().height(55.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(31.dp).clip(RoundedCornerShape(7.dp)).background(MatchPanel).clickable { onBack() }, contentAlignment = Alignment.Center) { Text("<", color = UiColor.White, fontSize = 22.sp) }
            Box(Modifier.size(43.dp).padding(start = 10.dp).background(UiColor(24, 30, 20)), contentAlignment = Alignment.Center) { Text("VC", color = MatchLime, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
            Column(Modifier.weight(1f).padding(start = 10.dp)) { Text("Matches", color = UiColor.White, fontSize = 17.sp, fontWeight = FontWeight.Bold); Text("Victory Cricket Club  ✦", color = MatchMuted, fontSize = 10.sp) }
            Text("♧", color = UiColor.White, fontSize = 24.sp)
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth().height(36.dp).clip(RoundedCornerShape(7.dp)).background(UiColor(5, 14, 27))) {
            MatchTab("Live", selectedTab == 0, Modifier.weight(1f)) { selectedTab = 0 }
            MatchTab("Upcoming", selectedTab == 1, Modifier.weight(1f)) { selectedTab = 1 }
            MatchTab("Completed", selectedTab == 2, Modifier.weight(1f)) { selectedTab = 2 }
        }
        Spacer(Modifier.height(12.dp))
        if (selectedTab == 0) matches.forEach { LiveMatchCard(it); Spacer(Modifier.height(8.dp)) }
        else Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) { Text(if (selectedTab == 1) "No upcoming matches" else "No completed matches", color = MatchMuted, fontSize = 14.sp) }
    }
}

@Composable
private fun MatchTab(text: String, selected: Boolean, modifier: Modifier, click: () -> Unit) = Box(modifier.fillMaxHeight().clip(RoundedCornerShape(7.dp)).then(if (selected) Modifier.background(UiColor(20, 34, 12)) else Modifier).clickable { click() }, contentAlignment = Alignment.Center) { Text(text, color = if (selected) MatchLime else MatchMuted, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) }

@Composable
private fun LiveMatchCard(match: LiveMatch) = Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = MatchPanel)) {
    Column(Modifier.fillMaxWidth().padding(10.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text("LIVE", color = UiColor.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clip(RoundedCornerShape(3.dp)).background(UiColor(126, 29, 34)).padding(horizontal = 5.dp, vertical = 3.dp)); Text(match.title, color = UiColor.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f).padding(start = 6.dp)); Text("◉ ${match.viewers}", color = MatchLime, fontSize = 8.sp, modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(UiColor(27, 57, 16)).padding(horizontal = 6.dp, vertical = 4.dp)) }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) { TeamScore("ST", match.left, match.leftScore); Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("LIVE", color = UiColor.White, fontSize = 7.sp, modifier = Modifier.clip(RoundedCornerShape(3.dp)).background(UiColor(126, 29, 34)).padding(horizontal = 5.dp, vertical = 3.dp)); Text("vs", color = MatchMuted, fontSize = 9.sp) }; TeamScore("TT", match.right, match.rightScore) }
        Spacer(Modifier.height(7.dp)); Row(Modifier.fillMaxWidth()) { Text("•  ${match.venue}", color = MatchMuted, fontSize = 8.sp, modifier = Modifier.weight(1f)); Text(">", color = MatchMuted, fontSize = 15.sp) }
    }
}

@Composable
private fun TeamScore(monogram: String, team: String, score: String) = Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(105.dp)) { Box(Modifier.size(31.dp).clip(RoundedCornerShape(7.dp)).background(UiColor(13, 38, 77)), contentAlignment = Alignment.Center) { Text(monogram, color = UiColor.White, fontSize = 10.sp, fontWeight = FontWeight.Bold) }; Text(score, color = UiColor.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, lineHeight = 16.sp); Text(team, color = MatchMuted, fontSize = 7.sp) }
