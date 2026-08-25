package com.example.sportsxtreme.presentation.clubs

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

class ViewClubPageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = Color.rgb(4, 12, 20)
        window.navigationBarColor = Color.rgb(4, 12, 20)
        setContent { ViewClubPage(::finish) }
    }
}

private val ClubPageBg = UiColor(4, 12, 20)
private val ClubPagePanel = UiColor(10, 22, 34)
private val ClubPageLime = UiColor(190, 255, 24)
private val ClubPageMuted = UiColor(164, 173, 181)

@Composable
private fun ViewClubPage(onBack: () -> Unit) = Column(Modifier.fillMaxSize().background(ClubPageBg).verticalScroll(rememberScrollState())) {
    ClubHero(onBack)
    Column(Modifier.padding(horizontal = 12.dp)) {
        ClubStats()
        Spacer(Modifier.height(8.dp))
        ClubTabs()
        Spacer(Modifier.height(10.dp))
        AboutClub()
        Spacer(Modifier.height(10.dp))
        NextMatch()
        Spacer(Modifier.height(10.dp))
        RecentResults()
        Spacer(Modifier.height(10.dp))
        Announcements()
        Spacer(Modifier.height(12.dp))
        Text("Chat with Club", color = UiColor.Black, textAlign = TextAlign.Center, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(7.dp)).background(ClubPageLime).padding(vertical = 11.dp))
        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun ClubHero(onBack: () -> Unit) = Box(Modifier.fillMaxWidth().height(180.dp).background(Brush.verticalGradient(listOf(UiColor(10, 44, 74), ClubPageBg)))) {
    Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        RoundButton("‹", onBack); RoundButton("◌"); RoundButton("⋮")
    }
    Row(Modifier.align(Alignment.BottomStart).padding(horizontal = 18.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(82.dp).clip(CircleShape).background(UiColor(25, 43, 23)), contentAlignment = Alignment.Center) { Text("WARRIORS\nCC", color = ClubPageLime, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text("Warriors Cricket Club", color = UiColor.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text("●  Cuttack, Odisha", color = ClubPageMuted, fontSize = 9.sp)
            Spacer(Modifier.height(6.dp))
            Text("♙  Your Role: Player", color = ClubPageLime, fontSize = 9.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("Join Request", color = UiColor.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(ClubPageLime).padding(horizontal = 11.dp, vertical = 9.dp))
            Spacer(Modifier.height(4.dp)); Text("Request to join this club", color = ClubPageMuted, fontSize = 6.sp)
        }
    }
}

@Composable
private fun RoundButton(label: String, onClick: (() -> Unit)? = null) = Text(label, color = UiColor.White, fontSize = 23.sp, textAlign = TextAlign.Center, modifier = Modifier.size(29.dp).clip(RoundedCornerShape(7.dp)).background(ClubPagePanel).clickable { onClick?.invoke() }.padding(top = 1.dp))

@Composable
private fun ClubStats() = Card(shape = RoundedCornerShape(7.dp), colors = CardDefaults.cardColors(containerColor = ClubPagePanel)) {
    Row(Modifier.fillMaxWidth().padding(vertical = 11.dp), horizontalArrangement = Arrangement.SpaceEvenly) { ClubStat("♧", "Members", "486"); ClubStat("◯", "Teams", "5"); ClubStat("♜", "Tournaments", "12"); ClubStat("▣", "Founded", "2021") }
}

@Composable
private fun ClubStat(symbol: String, label: String, value: String) = Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(symbol, color = ClubPageMuted, fontSize = 15.sp); Text(label, color = ClubPageMuted, fontSize = 7.sp); Text(value, color = UiColor.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) }

@Composable
private fun ClubTabs() = Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(7.dp)).background(ClubPagePanel), horizontalArrangement = Arrangement.SpaceEvenly) { listOf("⌂\nOverview", "♧\nTeams", "▣\nMatches", "♜\nTournaments", "♙\nPlayers", "▧\nMedia").forEachIndexed { index, title -> Text(title, color = if (index == 0) ClubPageLime else ClubPageMuted, textAlign = TextAlign.Center, fontSize = 7.sp, lineHeight = 12.sp, modifier = Modifier.padding(vertical = 8.dp)) } }

@Composable
private fun AboutClub() = InfoCard("About Club") { Text("Warriors Cricket Club is a passionate cricket community dedicated to excellence, teamwork and sportsmanship. Join us in our journey to victory.", color = ClubPageMuted, fontSize = 9.sp, lineHeight = 13.sp); Spacer(Modifier.height(12.dp)); Row { Text("⌂  Home Ground\nBarabati Stadium, Cuttack", color = ClubPageLime, fontSize = 7.sp, lineHeight = 12.sp, modifier = Modifier.weight(1f)); Text("▣  Established\n15 Aug 2021", color = ClubPageLime, fontSize = 7.sp, lineHeight = 12.sp, modifier = Modifier.weight(1f)) } }

@Composable
private fun NextMatch() = InfoCard("Next Match") { Row(verticalAlignment = Alignment.CenterVertically) { TeamMark("WARRIORS", Modifier.weight(1f)); Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) { Text("VS", color = UiColor.White, fontSize = 10.sp); Text("10 May 2026\n06:00 PM", color = ClubPageMuted, fontSize = 8.sp, textAlign = TextAlign.Center) }; TeamMark("KINGS", Modifier.weight(1f)) }; Text("⌖  Barabati Stadium, Cuttack", color = ClubPageMuted, fontSize = 7.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) }

@Composable
private fun TeamMark(name: String, modifier: Modifier) = Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) { Box(Modifier.size(36.dp).clip(CircleShape).background(UiColor(33, 51, 30)), contentAlignment = Alignment.Center) { Text(name.take(1), color = ClubPageLime, fontWeight = FontWeight.Bold) }; Text(name, color = UiColor.White, fontSize = 7.sp) }

@Composable
private fun RecentResults() = InfoCard("Recent Results", "View All  →") { Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("WON\nby 28 runs", "WON\nby 6 wickets", "WON\nby 45 runs", "LOST\nby 12 runs").forEach { result -> Text(result, color = if (result.startsWith("LOST")) UiColor(255, 137, 137) else ClubPageLime, fontSize = 7.sp, lineHeight = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f).clip(RoundedCornerShape(6.dp)).background(UiColor(18, 31, 41)).padding(vertical = 12.dp)) } } }

@Composable
private fun Announcements() = InfoCard("Latest Announcements", "View All  →") { Announcement("Practice session on Sunday", "All players must be present at 7:00 AM", "02 May 2026"); Spacer(Modifier.height(8.dp)); Announcement("New Team Kit Unveiled!", "Check out our new kit for the upcoming season.", "01 May 2026") }

@Composable
private fun Announcement(title: String, body: String, date: String) = Row(verticalAlignment = Alignment.CenterVertically) { Text("▰", color = ClubPageLime, fontSize = 22.sp); Spacer(Modifier.width(8.dp)); Column(Modifier.weight(1f)) { Text(title, color = UiColor.White, fontSize = 9.sp, fontWeight = FontWeight.Bold); Text(body, color = ClubPageMuted, fontSize = 7.sp) }; Text(date, color = ClubPageMuted, fontSize = 7.sp) }

@Composable
private fun InfoCard(title: String, action: String? = null, content: @Composable () -> Unit) = Card(shape = RoundedCornerShape(7.dp), colors = CardDefaults.cardColors(containerColor = ClubPagePanel)) { Column(Modifier.fillMaxWidth().padding(11.dp)) { Row(Modifier.fillMaxWidth()) { Text(title, color = UiColor.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); if (action != null) Text(action, color = ClubPageLime, fontSize = 8.sp, fontWeight = FontWeight.Bold) }; Spacer(Modifier.height(9.dp)); content() } }
