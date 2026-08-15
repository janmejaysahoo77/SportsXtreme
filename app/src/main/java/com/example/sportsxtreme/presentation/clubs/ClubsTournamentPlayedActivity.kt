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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color as UiColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

class ClubsTournamentPlayedActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = Color.rgb(3, 9, 18)
        window.navigationBarColor = Color.rgb(3, 9, 18)
        setContent { TournamentDashboard(::finish) }
    }
}

private val TourBg = UiColor(3, 9, 18)
private val TourPanel = UiColor(10, 21, 36)
private val TourAccent = UiColor(198, 255, 13)
private val TourMuted = UiColor(174, 184, 197)

@Composable
private fun TournamentDashboard(onBack: () -> Unit) {
    var category by remember { mutableIntStateOf(0) }
    var status by remember { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize().background(TourBg).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(11.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TopCircle("<", onBack)
            Column(Modifier.weight(1f).padding(start = 10.dp)) { Text("Tournament Dashboard", color = UiColor.White, fontSize = 17.sp, fontWeight = FontWeight.Bold); Text("VICTORY CRICKET CLUB", color = TourAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold) }
            TopCircle("♧"); Spacer(Modifier.width(8.dp)); TopCircle("⋮")
        }
        Spacer(Modifier.height(12.dp)); Text("ⓘ  Manage your Club and Inter-Club tournaments.", color = TourMuted, fontSize = 9.sp, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(TourPanel).padding(10.dp))
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) { Metric("◉", "3", "LIVE", Modifier.weight(1f)); Metric("▣", "2", "UPCOMING", Modifier.weight(1f)); Metric("♜", "12", "COMPLETED", Modifier.weight(1f)); Metric("♧", "48", "PARTICIPATING TEAMS", Modifier.weight(1f)) }
        Spacer(Modifier.height(14.dp)); Box(Modifier.fillMaxWidth().height(37.dp).clip(RoundedCornerShape(12.dp)).background(TourAccent), contentAlignment = Alignment.Center) { Text("⊕   Create Tournament", color = UiColor.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
        Spacer(Modifier.height(14.dp)); Row(Modifier.fillMaxWidth()) { Box(Modifier.weight(1f).height(34.dp).clip(RoundedCornerShape(18.dp)).background(TourPanel).padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) { Text("⌕  Search tournaments...", color = TourMuted, fontSize = 9.sp) }; Spacer(Modifier.width(10.dp)); Box(Modifier.height(34.dp).clip(RoundedCornerShape(18.dp)).background(TourPanel).padding(horizontal = 15.dp), contentAlignment = Alignment.Center) { Text("≡  Filter", color = UiColor.White, fontSize = 10.sp) } }
        Spacer(Modifier.height(12.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) { TournamentFilter("▦  All", category == 0, Modifier.weight(1f)) { category = 0 }; TournamentFilter("⌂  Club Tournament", category == 1, Modifier.weight(1.45f)) { category = 1 }; TournamentFilter("◉  Inter-Club Tournament", category == 2, Modifier.weight(1.7f)) { category = 2 } }
        Spacer(Modifier.height(10.dp)); Row(Modifier.fillMaxWidth().height(31.dp).clip(RoundedCornerShape(17.dp)).background(UiColor(9, 16, 26)).padding(2.dp)) { TournamentStatus("◉  LIVE", status == 0, Modifier.weight(1f)) { status = 0 }; TournamentStatus("▣  UPCOMING", status == 1, Modifier.weight(1f)) { status = 1 }; TournamentStatus("▣  COMPLETED", status == 2, Modifier.weight(1f)) { status = 2 } }
        Spacer(Modifier.height(12.dp))
        TournamentListCard("Summer Cricket League 2026", "Club Tournament", "12 Teams", "18 Matches", "12 Jul - 28 Jul", "75%")
        TournamentListCard("Odisha Champions Cup", "Inter-Club Tournament", "8 Teams", "10 Matches", "14 Jul - 30 Jul", "50%")
        TournamentListCard("Victory U19 League", "Club Tournament", "6 Teams", "9 Matches", "10 Jul - 25 Jul", "20%")
        Spacer(Modifier.height(36.dp))
    }
}

@Composable
private fun TopCircle(text: String, click: (() -> Unit)? = null) = Box(Modifier.size(31.dp).clip(CircleShape).background(TourPanel).clickable { click?.invoke() }, contentAlignment = Alignment.Center) { Text(text, color = UiColor.White, fontSize = 19.sp) }
@Composable
private fun Metric(icon: String, value: String, label: String, modifier: Modifier) = Card(modifier, shape = RoundedCornerShape(9.dp), colors = CardDefaults.cardColors(containerColor = TourPanel)) { Column(Modifier.height(82.dp).padding(top = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(icon, color = TourAccent, fontSize = 16.sp); Text(value, color = UiColor.White, fontSize = 24.sp, fontWeight = FontWeight.Bold); Text(label, color = TourMuted, fontSize = 7.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center) } }
@Composable
private fun TournamentFilter(text: String, active: Boolean, modifier: Modifier, click: () -> Unit) = Box(modifier.height(35.dp).clip(RoundedCornerShape(19.dp)).then(if (active) Modifier.background(TourAccent) else Modifier.background(TourPanel)).clickable { click() }, contentAlignment = Alignment.Center) { Text(text, color = if (active) UiColor.Black else UiColor.White, fontSize = 8.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal) }
@Composable
private fun TournamentStatus(text: String, active: Boolean, modifier: Modifier, click: () -> Unit) = Box(modifier.fillMaxHeight().clip(RoundedCornerShape(16.dp)).then(if (active) Modifier.background(TourAccent) else Modifier).clickable { click() }, contentAlignment = Alignment.Center) { Text(text, color = if (active) UiColor.Black else TourMuted, fontSize = 8.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal) }
@Composable
private fun TournamentListCard(title: String, type: String, teams: String, matches: String, dates: String, progress: String) =
    Card(Modifier.fillMaxWidth().padding(bottom = 9.dp),
        shape = RoundedCornerShape(9.dp),
        colors = CardDefaults.cardColors(containerColor = TourPanel)) {
        Column(Modifier.padding(12.dp)) {
            Row {
                Box(Modifier.size(58.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(UiColor(26, 42, 30)),
                    contentAlignment = Alignment.Center) {
                    Text("VC", color = TourAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold) }; Column(Modifier.weight(1f).padding(start = 10.dp)) { Text(title, color = UiColor.White, fontSize = 13.sp, fontWeight = FontWeight.Bold); Text("⌂  $type", color = TourAccent, fontSize = 9.sp); Text("Only squads from Victory Cricket Club participate.", color = TourMuted, fontSize = 7.sp) }; Text("◉ LIVE", color = TourAccent, fontSize = 8.sp, modifier = Modifier.clip(RoundedCornerShape(7.dp)).background(UiColor(29, 58, 15)).padding(horizontal = 6.dp, vertical = 4.dp)) }; Spacer(Modifier.height(10.dp)); Text("♧ $teams     ⚡ $matches     ▣ $dates", color = TourMuted, fontSize = 8.sp); Spacer(Modifier.height(10.dp)); Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)).background(UiColor(54, 68, 79))) { Box(Modifier.fillMaxWidth(if (progress == "75%") .75f else if (progress == "50%") .5f else .2f).fillMaxHeight().background(TourAccent)) }; Text(progress, color = TourAccent, fontSize = 8.sp, modifier = Modifier.padding(horizontal = 10.dp)); Box(Modifier.height(28.dp).clip(RoundedCornerShape(7.dp)).border(1.dp, TourAccent, RoundedCornerShape(7.dp)).padding(horizontal = 8.dp), contentAlignment = Alignment.Center) { Text("View Tournament  >", color = UiColor.White, fontSize = 8.sp, fontWeight = FontWeight.Bold) } } } }
