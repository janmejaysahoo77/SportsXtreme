package com.example.sportsxtreme.presentation.clubs

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color as UiColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import kotlinx.coroutines.launch

class ClubAnalyticsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = Color.rgb(4, 10, 20)
        window.navigationBarColor = Color.rgb(4, 10, 20)
        setContent { AnalyticsPage(::finish) }
    }
}

private val AnalyticsBg = UiColor(4, 10, 20)
private val AnalyticsPanel = UiColor(11, 22, 37)
private val AnalyticsAccent = UiColor(190, 255, 24)
private val AnalyticsMuted = UiColor(166, 178, 194)

@Composable
private fun AnalyticsPage(onBack: () -> Unit) {
    val pager = rememberPagerState(initialPage = 0) { 3 }
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize().background(AnalyticsBg)) {
        Row(Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("<", color = UiColor.White, fontSize = 23.sp, modifier = Modifier.clickable { onBack() })
            Text("Club Analytics", color = UiColor.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f).padding(start = 14.dp))
            Text("≡", color = UiColor.White, fontSize = 20.sp); Spacer(Modifier.width(14.dp)); Text("⋮", color = UiColor.White, fontSize = 22.sp)
        }
        Row(Modifier.fillMaxWidth().height(39.dp).padding(horizontal = 10.dp).clip(RoundedCornerShape(20.dp)).background(UiColor(14, 18, 28)).padding(3.dp)) {
            AnalyticsTab("Overview", pager.currentPage == 0, Modifier.weight(1f)) { scope.launch { pager.animateScrollToPage(0) } }
            AnalyticsTab("Teams", pager.currentPage == 1, Modifier.weight(1f)) { scope.launch { pager.animateScrollToPage(1) } }
            AnalyticsTab("Tournaments", pager.currentPage == 2, Modifier.weight(1f)) { scope.launch { pager.animateScrollToPage(2) } }
        }
        Spacer(Modifier.height(11.dp))
        HorizontalPager(state = pager, modifier = Modifier.fillMaxSize()) { page ->
            when (page) { 0 -> OverviewTab(); 1 -> TeamsTab(); else -> TournamentsTab() }
        }
    }
}

@Composable
private fun AnalyticsTab(label: String, selected: Boolean, modifier: Modifier, click: () -> Unit) = Box(modifier.fillMaxHeight().clip(RoundedCornerShape(18.dp)).then(if (selected) Modifier.background(AnalyticsAccent) else Modifier).clickable { click() }, contentAlignment = Alignment.Center) { Text(label, color = if (selected) UiColor.Black else AnalyticsMuted, fontSize = 10.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) }

@Composable
private fun OverviewTab() = Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 10.dp)) {
    Text("Club Overview  ⓘ", color = UiColor.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(10.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) { OverviewStat("TOTAL TEAMS", "5", Modifier.weight(1f)); OverviewStat("TOTAL MEMBERS", "82", Modifier.weight(1f)) }
    Spacer(Modifier.height(9.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) { OverviewStat("INTERNAL TOURN.", "6", Modifier.weight(1f)); OverviewStat("EXTERNAL TOURN.", "3", Modifier.weight(1f)) }
    Spacer(Modifier.height(9.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) { OverviewStat("MATCHES ORGANIZED", "145", Modifier.weight(1f)); OverviewStat("ACTIVE SEASON", "2026", Modifier.weight(1f)) }
    Spacer(Modifier.height(18.dp)); Text("Quick Insights", color = UiColor.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(9.dp)); Insight("◉", "Best Performing Team", "Senior Team   75% Win Rate"); Insight("♜", "Latest Tournament Champion", "Senior Team"); Insight("♧", "Top Run Scorer", "Rahul Das  (982 Runs)")
    Spacer(Modifier.height(17.dp)); Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text("Monthly Activity", color = UiColor.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); Text("This Year⌄", color = UiColor.White, fontSize = 9.sp, modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(UiColor(59, 57, 69)).padding(horizontal = 11.dp, vertical = 5.dp)) }
    Spacer(Modifier.height(10.dp)); ActivityChart(); Spacer(Modifier.height(25.dp))
}

@Composable
private fun OverviewStat(label: String, value: String, modifier: Modifier) = Card(modifier, shape = RoundedCornerShape(7.dp), colors = CardDefaults.cardColors(containerColor = AnalyticsPanel)) { Column(Modifier.height(74.dp).padding(9.dp)) { Text(label, color = AnalyticsMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.weight(1f)); Text(value, color = if (value == "5") AnalyticsAccent else UiColor.White, fontSize = 26.sp, fontWeight = FontWeight.Bold) } }

@Composable
private fun Insight(icon: String, label: String, value: String) = Card(Modifier.fillMaxWidth().padding(bottom = 8.dp), shape = RoundedCornerShape(7.dp), colors = CardDefaults.cardColors(containerColor = AnalyticsPanel)) { Row(Modifier.height(50.dp).padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(28.dp).clip(CircleShape).background(UiColor(25, 47, 20)), contentAlignment = Alignment.Center) { Text(icon, color = AnalyticsAccent, fontSize = 14.sp) }; Column(Modifier.weight(1f).padding(start = 10.dp)) { Text(label, color = AnalyticsMuted, fontSize = 8.sp); Text(value, color = UiColor.White, fontSize = 11.sp, fontWeight = FontWeight.Bold) }; Text(">", color = AnalyticsMuted, fontSize = 19.sp) } }

@Composable
private fun ActivityChart() = Card(shape = RoundedCornerShape(7.dp), colors = CardDefaults.cardColors(containerColor = AnalyticsPanel)) { Row(Modifier.fillMaxWidth().height(156.dp).padding(18.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween) { listOf(45, 68, 39, 78, 98, 62, 108).forEachIndexed { index, height -> Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) { Box(Modifier.width(9.dp).height(height.dp).background(AnalyticsAccent)); Spacer(Modifier.height(6.dp)); Text(listOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL")[index], color = AnalyticsMuted, fontSize = 7.sp) } } } }

@Composable
private fun TeamsTab() = Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 8.dp)) {
    Text("Team Performance", color = UiColor.White, fontSize = 16.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(12.dp))
    TeamPerformance("Senior Team", "Rahul Das", "18", "40", "30", "75%", "1", "1", true)
    TeamPerformance("Women's Team", "Priya Sharma", "16", "22", "15", "68%", "1", "0")
    TeamPerformance("Under-19 Team", "Aarav Nayak", "17", "32", "23", "72%", "1", "2")
    TeamPerformance("Under-16 Team", "Suman Rout", "15", "28", "18", "64%", "1", "0")
    Spacer(Modifier.height(25.dp))
}

@Composable
private fun TeamPerformance(name: String, captain: String, players: String, matches: String, wins: String, rate: String, internal: String, external: String, best: Boolean = false) = Card(Modifier.fillMaxWidth().padding(bottom = 10.dp).border(1.dp, UiColor(39, 65, 54), RoundedCornerShape(9.dp)), shape = RoundedCornerShape(9.dp), colors = CardDefaults.cardColors(containerColor = AnalyticsPanel)) { Column(Modifier.padding(13.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(40.dp).clip(CircleShape).border(1.dp, UiColor(52, 74, 86), CircleShape), contentAlignment = Alignment.Center) { Text("⬟", color = AnalyticsAccent, fontSize = 18.sp) }; Column(Modifier.weight(1f).padding(start = 12.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Text(name, color = UiColor.White, fontSize = 13.sp, fontWeight = FontWeight.Bold); if (best) Text(" BEST TEAM", color = UiColor.Black, fontSize = 6.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 6.dp).clip(RoundedCornerShape(4.dp)).background(AnalyticsAccent).padding(horizontal = 4.dp, vertical = 2.dp)) }; Text("Captain: $captain", color = AnalyticsMuted, fontSize = 9.sp) }; Text(">", color = AnalyticsMuted, fontSize = 20.sp) }; Spacer(Modifier.height(11.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { SmallMetric("PLAYERS", players, Modifier.weight(1f)); SmallMetric("MATCHES", matches, Modifier.weight(1f)); SmallMetric("WINS", wins, Modifier.weight(1f)); SmallMetric("WIN RATE", rate, Modifier.weight(1f), accent = true) }; Spacer(Modifier.height(11.dp)); Row(Modifier.fillMaxWidth()) { Text("Internal Titles: $internal", color = AnalyticsMuted, fontSize = 8.sp, modifier = Modifier.weight(1f)); Text("External Titles: $external", color = AnalyticsMuted, fontSize = 8.sp) } } }

@Composable
private fun SmallMetric(label: String, value: String, modifier: Modifier, accent: Boolean = false) = Box(modifier.height(48.dp).clip(RoundedCornerShape(7.dp)).background(UiColor(20, 25, 35)).padding(7.dp)) { Column { Text(label, color = AnalyticsMuted, fontSize = 6.sp); Text(value, color = if (accent) AnalyticsAccent else UiColor.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) } }

@Composable
private fun TournamentsTab() = Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 8.dp)) {
    Row(Modifier.fillMaxWidth().height(36.dp).clip(RoundedCornerShape(19.dp)).background(UiColor(14, 18, 28)).padding(3.dp)) { Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { Text("All", color = AnalyticsMuted, fontSize = 9.sp) }; Box(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(17.dp)).border(1.dp, AnalyticsAccent, RoundedCornerShape(17.dp)), contentAlignment = Alignment.Center) { Text("Internal", color = AnalyticsAccent, fontSize = 9.sp) }; Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { Text("External", color = AnalyticsMuted, fontSize = 9.sp) } }
    Spacer(Modifier.height(22.dp)); Text("Internal Tournaments  ⓘ", color = UiColor.White, fontSize = 14.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(10.dp))
    TournamentCard("Victory Premier\nLeague 2026", "Club Internal Tournament", "LIVE", "4", "12", "Senior Team", "Under-19 Team")
    TournamentCard("Monsoon Cup\n2025", "Club Internal\nTournament", "COMPLETED", "5", "16", "Women's Team", "Senior Team")
    Box(Modifier.fillMaxWidth().height(40.dp).clip(RoundedCornerShape(7.dp)).background(UiColor(18, 22, 30)), contentAlignment = Alignment.Center) { Text("View All Internal Tournaments   >", color = AnalyticsAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
    Spacer(Modifier.height(24.dp)); Text("External Tournaments", color = UiColor.White, fontSize = 14.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(10.dp)); Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = AnalyticsPanel)) { Row(Modifier.height(62.dp).padding(10.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(35.dp).background(UiColor(42, 48, 54))); Column(Modifier.weight(1f).padding(start = 10.dp)) { Text("Summer Cup 2025", color = UiColor.White, fontSize = 10.sp, fontWeight = FontWeight.Bold); Text("Hosted by: District C.A.", color = AnalyticsMuted, fontSize = 7.sp) }; Text("COMPLETED   >", color = AnalyticsMuted, fontSize = 7.sp) } }
    Spacer(Modifier.height(25.dp))
}

@Composable
private fun TournamentCard(title: String, subtitle: String, status: String, teams: String, matches: String, champion: String, runnerUp: String) = Card(Modifier.fillMaxWidth().padding(bottom = 12.dp).border(1.dp, UiColor(39, 65, 54), RoundedCornerShape(13.dp)), shape = RoundedCornerShape(13.dp), colors = CardDefaults.cardColors(containerColor = AnalyticsPanel)) { Column(Modifier.padding(14.dp)) { Row { Box(Modifier.size(35.dp).background(UiColor(38, 48, 59))); Column(Modifier.weight(1f).padding(start = 10.dp)) { Text(title, color = UiColor.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, lineHeight = 15.sp); Text(subtitle, color = AnalyticsMuted, fontSize = 8.sp) }; Text("● $status", color = if (status == "LIVE") AnalyticsAccent else AnalyticsMuted, fontSize = 7.sp, modifier = Modifier.clip(RoundedCornerShape(8.dp)).border(1.dp, AnalyticsMuted, RoundedCornerShape(8.dp)).padding(horizontal = 7.dp, vertical = 4.dp)) }; Spacer(Modifier.height(12.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { SmallMetric("TEAMS", teams, Modifier.weight(1f), true); SmallMetric("MATCHES", matches, Modifier.weight(1f), true) }; Spacer(Modifier.height(8.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { SmallMetric("CHAMPION", champion, Modifier.weight(1f)); SmallMetric("RUNNER UP", runnerUp, Modifier.weight(1f)) } } }
