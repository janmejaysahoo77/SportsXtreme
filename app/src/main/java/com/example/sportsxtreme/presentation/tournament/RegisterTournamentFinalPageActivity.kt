package com.example.sportsxtreme.presentation.tournament

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.example.sportsxtreme.R
import com.example.sportsxtreme.domain.model.Tournament
import com.example.sportsxtreme.presentation.home.HomeScreenView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterTournamentFinalPageActivity : ComponentActivity() {
    private val viewModel: TournamentFlowViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        val tournamentId = intent.getStringExtra(EXTRA_TOURNAMENT_ID).orEmpty()
        viewModel.load(tournamentId)
        setContent {
            RegisterTournamentFinalPage(
                tournament = viewModel.tournament.collectAsState().value,
                onBack = { finish() }
            )
        }
    }

    companion object { const val EXTRA_TOURNAMENT_ID = "tournament_id" }
}

private val FinalBg = Color(0xFF08111F)
private val FinalPanel = Color(0xFF121E31)
private val FinalPanelLight = Color(0xFF1A2940)
private val FinalAccent = Color(0xFFC1FF00)
private val FinalMuted = Color(0xFF9AA6BA)
private val FinalDivider = Color(0xFF24334C)

@Composable
private fun RegisterTournamentFinalPage(tournament: Tournament?, onBack: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var swipeDistance by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Teams", "Matches", "Points")
    Column(Modifier.fillMaxSize().background(FinalBg)) {
        FinalTopBar(onBack)
        FinalTournamentHeader(tournament)
        FinalTournamentTabs(tabs, selectedTab) { selectedTab = it }
        Box(
            Modifier.fillMaxSize().pointerInput(selectedTab) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dragAmount -> swipeDistance += dragAmount.toInt() },
                    onDragEnd = {
                        when {
                            swipeDistance <= -80 && selectedTab < tabs.lastIndex -> selectedTab++
                            swipeDistance >= 80 && selectedTab > 0 -> selectedTab--
                        }
                        swipeDistance = 0
                    },
                    onDragCancel = { swipeDistance = 0 }
                )
            }
        ) {
            when (selectedTab) {
                0 -> AboutTab(tournament)
                1 -> TeamsTab()
                2 -> MatchesTab(tournament)
                else -> PointsTab()
            }
        }
    }
}

@Composable
private fun FinalTournamentTabs(
    tabs: List<String>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .height(56.dp)
            .shadow(12.dp, RoundedCornerShape(28.dp), clip = false)
            .clip(RoundedCornerShape(28.dp))
            .background(FinalPanel)
            .border(1.dp, FinalDivider, RoundedCornerShape(28.dp))
            .horizontalScroll(rememberScrollState())
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        tabs.forEachIndexed { index, label ->
            val selected = selectedTab == index
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(23.dp))
                    .background(
                        if (selected) Brush.horizontalGradient(listOf(FinalAccent.copy(alpha = .20f), FinalAccent.copy(alpha = .07f)))
                        else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                    )
                    .border(1.dp, if (selected) FinalAccent.copy(alpha = .7f) else Color.Transparent, RoundedCornerShape(23.dp))
                    .clickable { onTabSelected(index) }
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    label.uppercase(),
                    color = if (selected) FinalAccent else FinalMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(5.dp))
                Box(
                    Modifier.width(if (selected) 18.dp else 0.dp)
                        .height(4.dp)
                        .shadow(if (selected) 8.dp else 0.dp, RoundedCornerShape(2.dp), clip = false)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (selected) FinalAccent else Color.Transparent)
                )
            }
        }
    }
}

@Composable
private fun FinalTopBar(onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(FinalPanelLight).clickable(onClick = onBack), contentAlignment = Alignment.Center) {
            BackGlyph(Modifier.size(21.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text("Tournament hub", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("Manage every detail in one place", color = FinalMuted, fontSize = 10.sp)
        }
        Spacer(Modifier.weight(1f))
        Box(Modifier.size(40.dp).clip(CircleShape).background(FinalPanelLight), contentAlignment = Alignment.Center) {
            Text("⋮", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FinalTournamentHeader(tournament: Tournament?) {
    val tournamentName = tournament?.name?.ifBlank { "Tournament" } ?: "Loading tournament…"
    val tournamentDate = tournament?.startDate?.ifBlank { "Date to be announced" } ?: ""
    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(58.dp).clip(RoundedCornerShape(18.dp)).background(Color(0xFFECF8D2)).border(1.dp, FinalAccent.copy(alpha = .7f), RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
            Text("🏏", fontSize = 23.sp)
        }
        Column(Modifier.padding(start = 13.dp).weight(1f)) {
            Text(tournamentName, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(3.dp))
            Text(tournamentDate, color = FinalMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("DRAFT", color = FinalAccent, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Text("19 views", color = FinalMuted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun AboutTab(tournament: Tournament?) {
    val requirements = tournament?.requirements
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OverviewStatsCard(
            teams = requirements?.numberOfTeams?.ifBlank { "0" } ?: "0",
            matches = "0",
            startDate = tournament?.startDate?.ifBlank { "Not set" } ?: "Not set",
            location = listOf(tournament?.ground, tournament?.city).filterNotNull().filter { it.isNotBlank() }.joinToString(", ").ifBlank { "Not set" }
        )
        SetupHelpCard()
        TournamentPromoCard()
        OrganizerCard(tournament)
        TournamentDetailsCard(tournament)
        TeamDetailsCard(tournament)
        TournamentQrCard()
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun OverviewStatsCard(teams: String, matches: String, startDate: String, location: String) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).background(FinalPanel).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(Modifier.fillMaxWidth()) {
            OverviewMetric("TEAMS", teams, Modifier.weight(1f))
            OverviewMetric("MATCHES", matches, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth()) {
            OverviewMetric("START DATE", startDate, Modifier.weight(1f))
            OverviewMetric("LOCATION", location, Modifier.weight(1f))
        }
    }
}

@Composable
private fun OverviewMetric(label: String, value: String, modifier: Modifier) {
    Column(modifier) {
        Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(3.dp))
        Text(label, color = FinalMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
private fun SetupHelpCard() {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).background(FinalPanel).padding(14.dp)
    ) {
        Text("Tournament setup guide/help", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(13.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(FinalDivider))
        Row(Modifier.fillMaxWidth().padding(top = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Help videos", color = FinalMuted, fontSize = 13.sp, modifier = Modifier.weight(1f))
            Box(Modifier.clip(RoundedCornerShape(6.dp)).border(1.dp, FinalAccent.copy(alpha = .85f), RoundedCornerShape(6.dp)).padding(horizontal = 18.dp, vertical = 6.dp)) {
                Text("View", color = FinalAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(17.dp))
        Text("SportsXtreme helpline", color = FinalMuted, fontSize = 13.sp)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            HelpContactButton("Call", Modifier.weight(1f))
            HelpContactButton("WhatsApp", Modifier.weight(1f))
        }
    }
}

@Composable
private fun TournamentPromoCard() {
    Box(
        Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(13.dp)).background(FinalPanel)
    ) {
        Image(
            painter = painterResource(R.drawable.batsman_onboarding2),
            contentDescription = "SportsXtreme tournament promotion",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun OrganizerCard(tournament: Tournament?) {
    val organizerName = tournament?.organizerName?.ifBlank { "Organizer" } ?: "Organizer"
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).background(FinalPanel).padding(14.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(38.dp).clip(CircleShape).background(FinalPanelLight), contentAlignment = Alignment.Center) {
                Text(organizerName.take(1).uppercase(), color = FinalAccent, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.padding(start = 11.dp).weight(1f)) {
                Text("Organizer", color = FinalMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(2.dp))
                Text(organizerName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text("›", color = FinalMuted, fontSize = 24.sp)
        }
    }
}

@Composable
private fun TournamentDetailsCard(tournament: Tournament?) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).background(FinalPanel).padding(14.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Tournament details", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("✎ Edit", color = FinalAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(13.dp))
        OverviewDetail("Name", tournament?.name?.ifBlank { "Not set" } ?: "Not set")
        OverviewDetail("Date", tournament?.startDate?.ifBlank { "Not set" } ?: "Not set")
        OverviewDetail("Grounds", listOf(tournament?.ground, tournament?.city).filterNotNull().filter { it.isNotBlank() }.joinToString(", ").ifBlank { "Not set" }, valueColor = FinalAccent)
        OverviewDetail("Ball type", tournament?.ballType?.ifBlank { "Not set" } ?: "Not set")
        OverviewDetail("Category", tournament?.type?.ifBlank { "Tournament" } ?: "Tournament")
        OverviewDetail("Tournament ID", tournament?.id?.ifBlank { "Not available" } ?: "Not available")
    }
}

@Composable
private fun TeamDetailsCard(tournament: Tournament?) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).background(FinalPanel).padding(14.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Team details", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("✎ Edit", color = FinalAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(13.dp))
        OverviewDetail("Entry fee", tournament?.requirements?.entryFee?.ifBlank { "0" } ?: "0")
        OverviewDetail("Team capacity", tournament?.requirements?.numberOfTeams?.ifBlank { "Not set" } ?: "Not set")
    }
}

@Composable
private fun OverviewDetail(label: String, value: String, valueColor: Color = Color.White) {
    Column(Modifier.padding(bottom = 10.dp)) {
        Text(label, color = FinalMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(2.dp))
        Text(value, color = valueColor, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun TournamentQrCard() {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).background(FinalPanel).padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Tournament QR code", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("↗  ↶", color = FinalMuted, fontSize = 16.sp)
        }
        Spacer(Modifier.height(16.dp))
        Box(Modifier.size(142.dp).clip(RoundedCornerShape(10.dp)).background(Color.White).padding(8.dp)) {
            Image(
                painter = painterResource(R.drawable.tournamentlogo),
                contentDescription = "Temporary QR code placeholder",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
        Spacer(Modifier.height(14.dp))
        Text("Let cricketers find this tournament\neasily with QR code.", color = FinalMuted, fontSize = 12.sp, textAlign = TextAlign.Center, lineHeight = 17.sp)
    }
}

@Composable
private fun HelpContactButton(label: String, modifier: Modifier) {
    Box(modifier.height(36.dp).clip(RoundedCornerShape(7.dp)).background(FinalPanelLight), contentAlignment = Alignment.Center) {
        Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AboutStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(14.dp)).background(FinalPanel).border(1.dp, FinalDivider, RoundedCornerShape(14.dp)).padding(11.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(5.dp))
        Text(label, color = FinalMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun AboutCard(title: String, content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(FinalPanel).border(1.dp, FinalDivider, RoundedCornerShape(16.dp)).padding(15.dp)
    ) {
        Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun AboutDetail(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = FinalMuted, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun MatchesTab(tournament: Tournament?) {
    val context = LocalContext.current
    val homeCardFactory = remember(context) { HomeScreenView(context) }
    val tournamentName = tournament?.name?.ifBlank { "Tournament" } ?: "Tournament"

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Matches", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Black)
                Text("Live tournament scorecards", color = FinalMuted, fontSize = 12.sp)
            }
            Box(Modifier.clip(RoundedCornerShape(16.dp)).background(FinalAccent.copy(alpha = .13f)).padding(horizontal = 11.dp, vertical = 7.dp)) {
                Text("2 LIVE", color = FinalAccent, fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
        }
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AndroidView(
                factory = {
                    homeCardFactory.createHeroScoreCard(
                        context = it,
                        league = tournamentName.uppercase(),
                        round = "Match 01"
                    )
                },
                modifier = Modifier.fillMaxWidth().height(272.dp)
            )
            AndroidView(
                factory = {
                    homeCardFactory.createHeroScoreCard(
                        context = it,
                        league = tournamentName.uppercase(),
                        round = "Match 02",
                        leftName = "BBS",
                        leftScore = "96/2",
                        leftOvers = "11.3 OV",
                        rightName = "KDP",
                        rightScore = "94/7",
                        rightOvers = "15.0 OV",
                        target = "148",
                        rrr = "7.86",
                        win = "BBS 68%",
                        note = "BBS need 52 from 51 balls"
                    )
                },
                modifier = Modifier.fillMaxWidth().height(272.dp)
            )
        }
        Text(
            "Tap a scorecard to open the full match details.",
            color = FinalMuted,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 18.dp)
        )
    }
}

@Composable
private fun TeamsTab() {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Teams", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text("Build your lineup before fixtures begin", color = FinalMuted, fontSize = 12.sp)
            }
            Box(Modifier.clip(RoundedCornerShape(20.dp)).background(FinalAccent.copy(alpha = .13f)).padding(horizontal = 11.dp, vertical = 7.dp)) {
                Text("0 TEAMS", color = FinalAccent, fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
        }
        Spacer(Modifier.height(18.dp))
        Box(Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(14.dp)).background(FinalPanel), contentAlignment = Alignment.CenterStart) {
            Text("⌕   Search teams", color = FinalMuted, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 16.dp))
        }
        Spacer(Modifier.height(22.dp))
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(FinalPanel).padding(horizontal = 20.dp, vertical = 26.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(84.dp).clip(CircleShape).background(FinalAccent.copy(alpha = .12f)), contentAlignment = Alignment.Center) {
                    Text("01", color = FinalAccent, fontSize = 26.sp, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.height(20.dp))
                Text("Invite captains to add teams", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                Spacer(Modifier.height(9.dp))
                Text("Share one link and let captains submit their\nteams and player details directly.", color = FinalMuted, fontSize = 13.sp, lineHeight = 19.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(24.dp))
                FinalActionButton("SHARE INVITE LINK", filled = true)
                Text("OR", color = Color(0xFF65718A), fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(vertical = 17.dp))
                FinalActionButton("ADD A TEAM MANUALLY", filled = false)
            }
        }
        Spacer(Modifier.height(20.dp))
        Text("You can edit teams and players anytime before\npublishing the tournament.", color = FinalMuted, fontSize = 12.sp, textAlign = TextAlign.Center, lineHeight = 17.sp)
    }
}

@Composable
private fun PointsTab() {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Points table", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Black)
                Text("Standings update after each match", color = FinalMuted, fontSize = 12.sp)
            }
            Box(Modifier.clip(RoundedCornerShape(16.dp)).background(FinalPanelLight).padding(horizontal = 11.dp, vertical = 7.dp)) {
                Text("LEAGUE", color = FinalAccent, fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
        }
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(FinalPanel).padding(14.dp)) {
            Row(Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                Text("TEAM", color = FinalMuted, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                Text("P", color = FinalMuted, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(30.dp), textAlign = TextAlign.Center)
                Text("W", color = FinalMuted, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(30.dp), textAlign = TextAlign.Center)
                Text("PTS", color = FinalMuted, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(38.dp), textAlign = TextAlign.Center)
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(FinalDivider))
            PointTableRow("Teams will appear here", "—", "—", "—", highlighted = true)
            PointTableRow("Play matches to unlock standings", "—", "—", "—", highlighted = false)
        }
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(FinalAccent.copy(alpha = .09f)).padding(16.dp)) {
            Text("Create fixtures after teams join. The points table will then update automatically.", color = Color(0xFFD9E5C0), fontSize = 13.sp, lineHeight = 18.sp)
        }
    }
}

@Composable
private fun PointTableRow(team: String, played: String, won: String, points: String, highlighted: Boolean) {
    Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(team, color = if (highlighted) Color.White else FinalMuted, fontSize = 12.sp, fontWeight = if (highlighted) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.weight(1f))
        Text(played, color = FinalMuted, fontSize = 12.sp, modifier = Modifier.width(30.dp), textAlign = TextAlign.Center)
        Text(won, color = FinalMuted, fontSize = 12.sp, modifier = Modifier.width(30.dp), textAlign = TextAlign.Center)
        Text(points, color = FinalAccent, fontSize = 12.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(38.dp), textAlign = TextAlign.Center)
    }
}

@Composable
private fun FinalActionButton(label: String, filled: Boolean, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(14.dp)).background(if (filled) FinalAccent else FinalPanelLight).border(if (filled) 0.dp else 1.dp, FinalAccent.copy(alpha = .55f), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
        Text(label, color = Color(0xFF101604).takeIf { filled } ?: FinalAccent, fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
    }
}

@Composable
private fun ComingSoonTab(label: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("$label will appear here once teams are added.", color = FinalMuted, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(32.dp))
    }
}

@Composable
private fun TeamGlyph(modifier: Modifier) = Canvas(modifier) {
    val stroke = Stroke(2.7.dp.toPx(), cap = StrokeCap.Round)
    val color = FinalAccent
    drawCircle(color, radius = size.width * .16f, center = androidx.compose.ui.geometry.Offset(size.width * .36f, size.height * .33f), style = stroke)
    drawCircle(color, radius = size.width * .16f, center = androidx.compose.ui.geometry.Offset(size.width * .67f, size.height * .33f), style = stroke)
    drawRoundRect(color, topLeft = androidx.compose.ui.geometry.Offset(size.width * .16f, size.height * .55f), size = androidx.compose.ui.geometry.Size(size.width * .68f, size.height * .3f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx()), style = stroke)
}

@Composable
private fun BackGlyph(modifier: Modifier) = Canvas(modifier) {
    val stroke = Stroke(2.dp.toPx(), cap = StrokeCap.Round)
    val c = Color.White
    drawLine(c, androidx.compose.ui.geometry.Offset(size.width * .75f, size.height * .5f), androidx.compose.ui.geometry.Offset(size.width * .2f, size.height * .5f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    drawLine(c, androidx.compose.ui.geometry.Offset(size.width * .2f, size.height * .5f), androidx.compose.ui.geometry.Offset(size.width * .48f, size.height * .22f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    drawLine(c, androidx.compose.ui.geometry.Offset(size.width * .2f, size.height * .5f), androidx.compose.ui.geometry.Offset(size.width * .48f, size.height * .78f), strokeWidth = stroke.width, cap = StrokeCap.Round)
}
