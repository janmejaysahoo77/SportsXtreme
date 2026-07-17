package com.example.sportsxtreme.presentation.home

import com.example.sportsxtreme.R
import com.example.sportsxtreme.presentation.tournament.*
import com.example.sportsxtreme.presentation.components.*
import com.example.sportsxtreme.presentation.auth.*
import com.example.sportsxtreme.presentation.scoring.*
import com.example.sportsxtreme.presentation.match.*
import com.example.sportsxtreme.presentation.media.*
import com.example.sportsxtreme.presentation.home.*
import com.example.sportsxtreme.presentation.team.*
import com.example.sportsxtreme.presentation.profile.*
import com.example.sportsxtreme.presentation.store.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val CricketAccent = Color(0xFFC1FF00)
private val CricketBg = Color(0xFF010509)
private val CricketPanel = Color(0xFF08111D)
private val CricketCard = Color(0xFF091424)
private val CricketMuted = Color(0xFF94A19E)
private val CricketStroke = Color(0x3D6A7480)
private val CricketBlue = Color(0xFF2ED8FF)

private val cricketTabs = listOf("Matches", "Tournaments", "Teams", "Stats", "History")

private data class CricketMatch(
    val type: String,
    val title: String,
    val status: String,
    val left: String,
    val right: String,
    val score: String,
    val meta: String,
    val result: String? = null,
    val live: Boolean = false,
    val accent: Color = CricketAccent
)

private data class CricketTeam(val name: String, val initials: String, val location: String, val accent: Color)

private data class CricketTournament(
    val name: String,
    val date: String,
    val location: String,
    val status: String,
    val accent: Color,
    val visual: TournamentVisual
)

private enum class TournamentVisual { NEON, FIELD, TROPHY, SLAM }

private val sampleMatches = listOf(
    CricketMatch(
        type = "T20 FRIENDLY MATCH",
        title = "Neon Syndicate vs Thunder Strikers",
        status = "Upcoming",
        left = "Syndicate",
        right = "Thunder",
        score = "VS",
        meta = "Today, 6:30 PM",
        accent = CricketBlue
    ),
    CricketMatch(
        type = "CORPORATE LEAGUE",
        title = "Phoenix XI vs Titans CC",
        status = "Live soon",
        left = "Phoenix XI",
        right = "Titans CC",
        score = "9:00",
        meta = "Tomorrow",
        live = true
    ),
    CricketMatch(
        type = "WEEKEND CUP - FINAL",
        title = "Warriors CC vs Blue Hawks",
        status = "Finished",
        left = "Warriors CC",
        right = "Blue Hawks",
        score = "184/6 (20)",
        meta = "POTM: R Sharma",
        result = "Warriors won by 18 runs",
        accent = Color(0xFFFFCA64)
    ),
    CricketMatch(
        type = "INTER COLLEGE TROPHY",
        title = "SITAM CSE vs Apex College",
        status = "Scheduled",
        left = "SITAM CSE",
        right = "Apex College",
        score = "VS",
        meta = "League fixture",
        accent = Color(0xFFFF8FB0)
    ),
    CricketMatch(
        type = "PRACTICE MATCH",
        title = "Challengers vs Royals",
        status = "Result",
        left = "Challengers",
        right = "Royals",
        score = "128/10",
        meta = "Royals won by 5 wickets",
        result = "Royals won by 5 wickets"
    )
)

private val teams = listOf(
    CricketTeam("Neon Strikers", "NS", "Odisha", Color(0xFF1E6CF1)),
    CricketTeam("Cyber Titans", "CT", "Odisha", Color(0xFF7B32D9)),
    CricketTeam("Quantum XI", "QX", "Odisha", Color(0xFF079F89)),
    CricketTeam("Velocity Vipers", "VV", "Odisha", Color(0xFFD51D49)),
    CricketTeam("Royal Mavericks", "RM", "Odisha", Color(0xFFFFB340))
)

private val tournaments = listOf(
    CricketTournament("Neon Pro League", "20 Jun - 30 Jun 2026", "BHUBANESWAR", "UPCOMING", CricketBlue, TournamentVisual.NEON),
    CricketTournament("Corporate Cricket Cup", "15 Jun - 25 Jun 2026", "HYDERABAD", "LIVE", CricketAccent, TournamentVisual.FIELD),
    CricketTournament("Balisahi Premier League (BPL)", "03 Jun - 05 Jun 2026", "ODISHA", "PAST", Color(0xFF88909A), TournamentVisual.TROPHY),
    CricketTournament("Summer Slam T20", "10 Jul - 20 Jul 2026", "MUMBAI", "SCHEDULED", Color(0xFFBFD8FF), TournamentVisual.SLAM)
)

@Composable
fun MyCricketScreen(onMenuClick: () -> Unit = {}, onStartMatch: () -> Unit = {}) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CricketBg)
            .drawBehind {
                drawCircle(Color(0x1F00D2FF), radius = size.width * 0.65f, center = Offset(size.width * 0.05f, size.height * 0.16f))
                drawCircle(Color(0x242D4211), radius = size.width * 0.7f, center = Offset(size.width * 0.98f, size.height * 0.36f))
                drawCircle(Color(0x16007AFF), radius = size.width * 0.55f, center = Offset(size.width * 0.45f, size.height * 0.92f))
            }
    ) {
        CricketTopStrip(onMenuClick)
        CricketTabs(selectedTab = selectedTab, onSelect = { selectedTab = it })
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    item { StartMatchPrompt(onStartMatch) }
                    item { SegmentPills(listOf("You", "Playing", "Network", "All"), active = 0) }
                    items(sampleMatches) { match -> MatchCard(match) }
                }
                1 -> {
                    item { HostTournamentPrompt() }
                    item { SegmentPills(listOf("Your", "Participate", "Network"), active = 0) }
                    items(tournaments) { tournament -> TournamentCard(tournament) }
                }
                2 -> {
                    item { CreateTeamPrompt() }
                    item { TeamFilterPills() }
                    item { QuickSearchBar() }
                    item { ActiveTeamsHeader() }
                    items(teams) { team -> TeamCard(team) }
                }
                3 -> {
                    item { SectionTitle("Performance Hub", "Live numbers from your cricket universe") }
                    item { StatsMatrix() }
                    item { MomentumCard() }
                }
                else -> {
                    item { SectionTitle("Match History", "Recent scorecards and completed fixtures") }
                    items(sampleMatches.filter { it.status == "Finished" || it.status == "Result" }) { match -> MatchCard(match) }
                    item { TimelineCard() }
                }
            }
        }
    }
}

@Composable
private fun CricketTopStrip(onMenuClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(Brush.horizontalGradient(listOf(Color(0xFF050D11), Color(0xFF030A16))))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeaderMenuButton(onMenuClick)
        Spacer(Modifier.width(14.dp))
        Column {
            Text("My Cricket", color = Color.White, fontSize = 16.sp, lineHeight = 18.sp, fontWeight = FontWeight.ExtraBold)
            Text("Match command center", color = CricketMuted, fontSize = 8.5.sp, lineHeight = 10.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .height(25.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(CricketAccent)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("PRO", color = Color(0xFF081007), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
        }
        Spacer(Modifier.width(12.dp))
        HeaderBell()
    }
}

@Composable
private fun CricketTabs(selectedTab: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(Color(0xFF040A12))
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(19.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        cricketTabs.forEachIndexed { index, label ->
            Column(
                modifier = Modifier
                    .height(40.dp)
                    .clickable { onSelect(index) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    label,
                    color = if (index == selectedTab) CricketAccent else Color(0xFFC5D1CE),
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    Modifier
                        .width(if (index == selectedTab) 34.dp else 0.dp)
                        .height(2.dp)
                        .background(CricketAccent)
                )
            }
        }
    }
}

@Composable
private fun StartMatchPrompt(onStartMatch: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF101827), Color(0xFF07101B), Color(0xFF101E14))))
            .border(1.dp, Color(0x304E5B64), RoundedCornerShape(11.dp))
            .padding(13.dp)
    ) {
        Column {
            Text("Ready to organize your next match?", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
            Text("Setup custom matches, invite teams, and track live scores effortlessly.", color = CricketMuted, fontSize = 9.sp, lineHeight = 12.sp, modifier = Modifier.padding(top = 5.dp))
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .height(34.dp)
                    .width(145.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .background(CricketAccent)
                    .clickable(onClick = onStartMatch),
                contentAlignment = Alignment.Center
            ) {
                Text("Start Match", color = Color(0xFF0A1204), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun SegmentPills(labels: List<String>, active: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        labels.forEachIndexed { index, label ->
            Box(
                modifier = Modifier
                    .height(29.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(if (index == active) Color(0x221B2604) else Color(0xFF121A28))
                    .border(1.dp, if (index == active) CricketAccent else CricketStroke, RoundedCornerShape(15.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(label, color = if (index == active) CricketAccent else Color(0xFFD7DFE5), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun MatchCard(match: CricketMatch) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF0B1728), Color(0xFF070E19), Color(0xFF0D1812))))
            .border(1.dp, if (match.live) CricketAccent.copy(alpha = 0.65f) else CricketStroke, RoundedCornerShape(11.dp))
            .padding(13.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(match.type, color = match.accent, fontSize = 7.5.sp, lineHeight = 9.sp, fontWeight = FontWeight.ExtraBold)
                    Text(match.title, color = Color.White, fontSize = 12.sp, lineHeight = 15.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                StatusBadge(match.status, match.live)
            }
            Spacer(Modifier.height(17.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TeamNode(match.left, match.accent, Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(82.dp)) {
                    Text(match.score, color = Color.White, fontSize = if (match.score == "VS") 18.sp else 16.sp, fontWeight = FontWeight.ExtraBold, fontStyle = if (match.score == "VS") FontStyle.Italic else FontStyle.Normal)
                    Text(match.meta, color = CricketAccent, fontSize = 8.sp, lineHeight = 10.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
                }
                TeamNode(match.right, match.accent, Modifier.weight(1f))
            }
            match.result?.let {
                Spacer(Modifier.height(14.dp))
                Text(it, color = CricketAccent, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun TeamNode(name: String, accent: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(45.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF101827))
                .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            CricketCrest(accent)
        }
        Text(name, color = Color.White, fontSize = 8.sp, lineHeight = 10.sp, fontWeight = FontWeight.Bold, maxLines = 2, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 7.dp))
    }
}

@Composable
private fun CricketCrest(accent: Color) {
    Canvas(Modifier.size(28.dp)) {
        val path = Path().apply {
            moveTo(size.width * 0.5f, size.height * 0.08f)
            lineTo(size.width * 0.82f, size.height * 0.22f)
            lineTo(size.width * 0.73f, size.height * 0.72f)
            quadraticTo(size.width * 0.5f, size.height * 0.93f, size.width * 0.27f, size.height * 0.72f)
            lineTo(size.width * 0.18f, size.height * 0.22f)
            close()
        }
        drawPath(path, accent.copy(alpha = 0.2f))
        drawPath(path, accent, style = Stroke(width = 1.3.dp.toPx(), cap = StrokeCap.Round))
        drawCircle(Color.White.copy(alpha = 0.18f), radius = size.minDimension * 0.13f, center = center)
    }
}

@Composable
private fun StatusBadge(label: String, live: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(7.dp))
            .background(if (live) Color(0x221B2604) else Color(0xFF142036))
            .border(1.dp, if (live) CricketAccent else Color(0xFF314057), RoundedCornerShape(7.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (live) CricketAccent else Color(0xFFD8E1EA), fontSize = 7.5.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)) {
        Text(title, color = Color.White, fontSize = 19.sp, lineHeight = 22.sp, fontWeight = FontWeight.ExtraBold)
        Text(subtitle, color = CricketMuted, fontSize = 9.5.sp, lineHeight = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 7.dp))
    }
}

@Composable
private fun HostTournamentPrompt() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(116.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF07111F), Color(0xFF081221), Color(0xFF061018))))
            .border(1.dp, Color(0x253E4C60), RoundedCornerShape(10.dp))
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color(0xFF0B1F42))
                .border(1.dp, Color(0xFF1D5EF7), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.size(20.dp)) {
                val tint = Color(0xFFBFD8FF)
                drawCircle(tint.copy(alpha = 0.2f), radius = size.minDimension * 0.46f, center = center)
                drawCircle(tint, radius = size.minDimension * 0.36f, center = center, style = Stroke(width = 1.4.dp.toPx()))
                drawLine(tint, Offset(size.width * 0.5f, size.height * 0.28f), Offset(size.width * 0.5f, size.height * 0.72f), strokeWidth = 1.4.dp.toPx(), cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.28f, size.height * 0.5f), Offset(size.width * 0.72f, size.height * 0.5f), strokeWidth = 1.4.dp.toPx(), cap = StrokeCap.Round)
            }
        }
        Spacer(Modifier.width(13.dp))
        Text(
            text = "Want to host\na\ntournament?",
            color = Color.White,
            fontSize = 19.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .height(38.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(CricketAccent)
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Register", color = Color(0xFF0A1204), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun TournamentCard(tournament: CricketTournament) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF060D18))
            .border(1.dp, if (tournament.status == "LIVE") CricketAccent else CricketStroke, RoundedCornerShape(10.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(151.dp)
                .background(Color(0xFF07111D))
        ) {
            TournamentVisualPanel(tournament)
            TournamentStatusTag(
                label = tournament.status,
                live = tournament.status == "LIVE",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 10.dp, end = 10.dp)
            )
            Text(
                tournament.name,
                color = Color.White,
                fontSize = 13.sp,
                lineHeight = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 12.dp, end = 12.dp, bottom = 13.dp)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(57.dp)
                .background(Brush.horizontalGradient(listOf(Color(0xFF050A12), Color(0xFF07111E), tournament.accent.copy(alpha = 0.12f))))
                .padding(start = 12.dp, end = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                TournamentMetaLine(tournament.date, calendar = true)
                TournamentMetaLine(tournament.location, calendar = false)
            }
            TournamentArrow(tournament.accent)
        }
    }
}

@Composable
private fun TournamentVisualPanel(tournament: CricketTournament) {
    Canvas(Modifier.fillMaxSize()) {
        val base = when (tournament.visual) {
            TournamentVisual.NEON -> Color(0xFF08345A)
            TournamentVisual.FIELD -> Color(0xFF2E4B18)
            TournamentVisual.TROPHY -> Color(0xFF23272D)
            TournamentVisual.SLAM -> Color(0xFF0A2C54)
        }
        drawRect(base.copy(alpha = 0.68f))
        drawRect(Color.Black.copy(alpha = if (tournament.visual == TournamentVisual.TROPHY) 0.62f else 0.32f))
        drawCircle(tournament.accent.copy(alpha = 0.2f), radius = size.width * 0.55f, center = Offset(size.width * 0.78f, size.height * 0.18f))
        drawCircle(CricketBlue.copy(alpha = 0.14f), radius = size.width * 0.46f, center = Offset(size.width * 0.15f, size.height * 0.04f))

        when (tournament.visual) {
            TournamentVisual.FIELD -> drawFieldScene(tournament.accent)
            TournamentVisual.TROPHY -> drawTrophyScene()
            TournamentVisual.SLAM -> drawSlamScene(tournament.accent)
            TournamentVisual.NEON -> drawNeonStadium(tournament.accent)
        }

        drawRect(
            Brush.verticalGradient(
                listOf(Color.Transparent, Color.Black.copy(alpha = 0.18f), Color.Black.copy(alpha = 0.82f)),
                startY = size.height * 0.28f,
                endY = size.height
            )
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawNeonStadium(accent: Color) {
    val stroke = 1.dp.toPx()
    repeat(12) { index ->
        val y = size.height * (0.12f + index * 0.045f)
        drawLine(Color.White.copy(alpha = 0.08f), Offset(0f, y), Offset(size.width, y + 18.dp.toPx()), strokeWidth = stroke)
    }
    repeat(8) { index ->
        val x = size.width * (index / 7f)
        drawLine(accent.copy(alpha = 0.2f), Offset(x, size.height * 0.1f), Offset(size.width * 0.5f, size.height * 0.72f), strokeWidth = stroke)
    }
    drawOval(
        color = Color.White.copy(alpha = 0.16f),
        topLeft = Offset(size.width * 0.1f, size.height * 0.46f),
        size = Size(size.width * 0.8f, size.height * 0.38f),
        style = Stroke(width = 1.4.dp.toPx())
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFieldScene(accent: Color) {
    drawOval(
        color = Color(0xFF6D8F27).copy(alpha = 0.55f),
        topLeft = Offset(size.width * 0.04f, size.height * 0.48f),
        size = Size(size.width * 0.92f, size.height * 0.43f)
    )
    drawOval(
        color = Color(0xFF141B0F).copy(alpha = 0.34f),
        topLeft = Offset(size.width * 0.18f, size.height * 0.56f),
        size = Size(size.width * 0.64f, size.height * 0.25f),
        style = Stroke(width = 1.2.dp.toPx())
    )
    repeat(4) { index ->
        val x = size.width * (0.16f + index * 0.23f)
        drawCircle(Color.White.copy(alpha = 0.72f), radius = 4.5.dp.toPx(), center = Offset(x, size.height * 0.18f))
        drawLine(Color.White.copy(alpha = 0.18f), Offset(x, size.height * 0.2f), Offset(size.width * 0.5f, size.height * 0.58f), strokeWidth = 1.dp.toPx())
    }
    drawLine(accent.copy(alpha = 0.7f), Offset(size.width * 0.47f, size.height * 0.64f), Offset(size.width * 0.54f, size.height * 0.64f), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTrophyScene() {
    val cup = Color.White.copy(alpha = 0.28f)
    val left = size.width * 0.42f
    val top = size.height * 0.23f
    val w = size.width * 0.16f
    val h = size.height * 0.34f
    drawRoundRect(cup, Offset(left, top), Size(w, h * 0.56f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx()), style = Stroke(width = 2.dp.toPx()))
    drawLine(cup, Offset(size.width * 0.5f, top + h * 0.56f), Offset(size.width * 0.5f, top + h * 0.82f), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
    drawLine(cup, Offset(size.width * 0.44f, top + h * 0.84f), Offset(size.width * 0.56f, top + h * 0.84f), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
    drawLine(cup, Offset(size.width * 0.38f, top + h), Offset(size.width * 0.62f, top + h), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
    repeat(9) { index ->
        val y = size.height * (0.18f + index * 0.07f)
        drawLine(Color.White.copy(alpha = 0.035f), Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSlamScene(accent: Color) {
    repeat(9) { index ->
        val y = size.height * (0.18f + index * 0.065f)
        drawLine(Color.White.copy(alpha = 0.06f), Offset(size.width * 0.02f, y), Offset(size.width * 0.92f, y + 8.dp.toPx()), strokeWidth = 1.dp.toPx())
    }
    repeat(10) { index ->
        val startX = size.width * (0.12f + index * 0.075f)
        drawLine(
            color = if (index % 2 == 0) accent.copy(alpha = 0.72f) else CricketAccent.copy(alpha = 0.58f),
            start = Offset(startX, size.height * 0.78f),
            end = Offset(startX + size.width * 0.22f, size.height * 0.28f),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun TournamentStatusTag(label: String, live: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(3.dp))
            .background(if (live) CricketAccent else Color(0xFFDDE7FF))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color(0xFF071007), fontSize = 7.5.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun TournamentMetaLine(text: String, calendar: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.size(10.dp)) {
            val tint = Color(0xFFB7C4C8)
            if (calendar) {
                drawRect(tint.copy(alpha = 0.18f), topLeft = Offset(size.width * 0.16f, size.height * 0.22f), size = Size(size.width * 0.68f, size.height * 0.62f), style = Stroke(width = 1.dp.toPx()))
                drawLine(tint, Offset(size.width * 0.16f, size.height * 0.4f), Offset(size.width * 0.84f, size.height * 0.4f), strokeWidth = 1.dp.toPx())
            } else {
                val path = Path().apply {
                    moveTo(size.width * 0.5f, size.height * 0.92f)
                    cubicTo(size.width * 0.2f, size.height * 0.58f, size.width * 0.25f, size.height * 0.18f, size.width * 0.5f, size.height * 0.18f)
                    cubicTo(size.width * 0.75f, size.height * 0.18f, size.width * 0.8f, size.height * 0.58f, size.width * 0.5f, size.height * 0.92f)
                }
                drawPath(path, tint, style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round))
                drawCircle(tint, radius = 1.dp.toPx(), center = Offset(size.width * 0.5f, size.height * 0.43f))
            }
        }
        Spacer(Modifier.width(6.dp))
        Text(text, color = Color(0xFFD7E0E3), fontSize = 8.sp, lineHeight = 10.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
    }
}

@Composable
private fun TournamentArrow(accent: Color) {
    Canvas(Modifier.size(24.dp)) {
        drawLine(accent, Offset(size.width * 0.36f, size.height * 0.2f), Offset(size.width * 0.68f, size.height * 0.5f), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
        drawLine(accent, Offset(size.width * 0.68f, size.height * 0.5f), Offset(size.width * 0.36f, size.height * 0.8f), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
    }
}

@Composable
private fun CreateTeamPrompt() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(105.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(Color(0xFF08101C))
            .border(1.dp, Color(0x263D4C5F), RoundedCornerShape(13.dp))
            .drawBehind {
                drawLine(
                    color = CricketAccent,
                    start = Offset(1.dp.toPx(), 9.dp.toPx()),
                    end = Offset(1.dp.toPx(), size.height - 9.dp.toPx()),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
            .padding(start = 17.dp, end = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Want to create a new\nteam?", color = Color.White, fontSize = 18.sp, lineHeight = 22.sp, fontWeight = FontWeight.ExtraBold)
            Text(
                "Start your own legacy in the\ncommunity.",
                color = Color(0xFFD1D8DE),
                fontSize = 14.sp,
                lineHeight = 19.sp,
                modifier = Modifier.padding(top = 7.dp)
            )
        }
        Box(
            modifier = Modifier
                .height(39.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(CricketAccent)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Create", color = Color(0xFF071007), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun TeamFilterPills() {
    val labels = listOf("Your teams", "Opponents", "Following")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        labels.forEachIndexed { index, label ->
            Box(
                modifier = Modifier
                    .height(42.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(if (index == 0) CricketAccent else Color(0xFF151A24))
                    .border(1.dp, if (index == 0) Color(0xFFE5FF66) else Color(0xFF3A4352), RoundedCornerShape(22.dp))
                    .padding(horizontal = if (index == 0) 22.dp else 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    color = if (index == 0) Color(0xFF071007) else Color(0xFFE1E7EC),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun QuickSearchBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(Color(0xFF151A21))
            .border(1.dp, Color(0xFF3B4451), RoundedCornerShape(13.dp))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(Modifier.size(17.dp)) {
                val tint = Color(0xFF98A4B3)
                drawCircle(tint, radius = size.minDimension * 0.31f, center = Offset(size.width * 0.43f, size.height * 0.43f), style = Stroke(width = 1.7.dp.toPx()))
                drawLine(tint, Offset(size.width * 0.66f, size.height * 0.66f), Offset(size.width * 0.88f, size.height * 0.88f), strokeWidth = 1.7.dp.toPx(), cap = StrokeCap.Round)
            }
            Spacer(Modifier.width(10.dp))
            Text("Quick search", color = Color(0xFFD8DEE7), fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ActiveTeamsHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 37.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(5.dp)
                .height(21.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(CricketAccent)
        )
        Spacer(Modifier.width(7.dp))
        Text("ACTIVE TEAMS", color = CricketAccent, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun TeamCard(team: CricketTeam) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(69.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF09111E))
            .border(1.dp, Color(0x272E3A4A), RoundedCornerShape(12.dp))
            .padding(start = 14.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(team.accent)
                .border(1.dp, Color.White.copy(alpha = 0.22f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(Color.White.copy(alpha = 0.12f), radius = size.minDimension * 0.48f, center = Offset(size.width * 0.34f, size.height * 0.24f))
                drawCircle(Color.Black.copy(alpha = 0.12f), radius = size.minDimension * 0.48f, center = Offset(size.width * 0.7f, size.height * 0.72f))
            }
            Text(team.initials, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(team.name, color = Color.White, fontSize = 15.sp, lineHeight = 17.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(modifier = Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Canvas(Modifier.size(12.dp)) {
                    val tint = Color(0xFFB9C3CB)
                    val path = Path().apply {
                        moveTo(size.width * 0.5f, size.height * 0.92f)
                        cubicTo(size.width * 0.2f, size.height * 0.58f, size.width * 0.25f, size.height * 0.18f, size.width * 0.5f, size.height * 0.18f)
                        cubicTo(size.width * 0.75f, size.height * 0.18f, size.width * 0.8f, size.height * 0.58f, size.width * 0.5f, size.height * 0.92f)
                    }
                    drawPath(path, tint, style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round))
                    drawCircle(tint, radius = 1.1.dp.toPx(), center = Offset(size.width * 0.5f, size.height * 0.43f))
                }
                Spacer(Modifier.width(4.dp))
                Text(team.location, color = Color(0xFFD1D8DE), fontSize = 12.sp, lineHeight = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
        Text(":", color = CricketAccent, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun StatsMatrix() {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.fillMaxWidth()) {
            StatTile("Matches", "42", "+8 this month", CricketAccent, Modifier.weight(1f))
            StatTile("Win Rate", "68%", "last 12 games", CricketBlue, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.fillMaxWidth()) {
            StatTile("Runs", "5.8K", "team total", Color(0xFFFFCA64), Modifier.weight(1f))
            StatTile("Wickets", "214", "all squads", Color(0xFFFF8FB0), Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, note: String, accent: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .height(108.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(CricketPanel)
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(11.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = CricketMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Text(value, color = Color.White, fontSize = 25.sp, lineHeight = 28.sp, fontWeight = FontWeight.ExtraBold)
        Text(note, color = accent, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun MomentumCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(132.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF07101A), Color(0xFF0B2038), Color(0xFF172608))))
            .border(1.dp, CricketStroke, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column {
            Text("AI Momentum", color = CricketAccent, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
            Text("Syndicate form is climbing", color = Color.White, fontSize = 17.sp, lineHeight = 20.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(top = 7.dp))
            Text("Bowling economy improved by 11% across the last five matches.", color = CricketMuted, fontSize = 10.sp, lineHeight = 14.sp, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
private fun TimelineCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .background(CricketPanel)
            .border(1.dp, CricketStroke, RoundedCornerShape(11.dp))
            .padding(13.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Saved scorecards", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
        listOf("Phoenix XI beat Titans CC", "Royals chased 132 in 18.4 overs", "Warriors CC lifted Weekend Cup").forEach {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(7.dp).clip(CircleShape).background(CricketAccent))
                Spacer(Modifier.width(9.dp))
                Text(it, color = Color(0xFFDDE7E3), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun HeaderMenuButton(onMenuClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(7.dp))
            .clickable(onClick = onMenuClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(18.dp)) {
            val tint = Color(0xFF8E9E99)
            val stroke = 1.8.dp.toPx()
            drawLine(tint, Offset(size.width * 0.16f, size.height * 0.28f), Offset(size.width * 0.84f, size.height * 0.28f), strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(tint, Offset(size.width * 0.16f, size.height * 0.5f), Offset(size.width * 0.84f, size.height * 0.5f), strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(tint, Offset(size.width * 0.16f, size.height * 0.72f), Offset(size.width * 0.84f, size.height * 0.72f), strokeWidth = stroke, cap = StrokeCap.Round)
        }
    }
}

@Composable
private fun HeaderBell() {
    Canvas(Modifier.size(21.dp)) {
        val tint = Color(0xFF8E9E99)
        val stroke = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round)
        val bell = Path().apply {
            moveTo(size.width * 0.28f, size.height * 0.61f)
            cubicTo(size.width * 0.3f, size.height * 0.34f, size.width * 0.38f, size.height * 0.22f, size.width * 0.5f, size.height * 0.22f)
            cubicTo(size.width * 0.62f, size.height * 0.22f, size.width * 0.7f, size.height * 0.34f, size.width * 0.72f, size.height * 0.61f)
            lineTo(size.width * 0.8f, size.height * 0.74f)
            lineTo(size.width * 0.2f, size.height * 0.74f)
            close()
        }
        drawPath(bell, tint, style = stroke)
        drawLine(tint, Offset(size.width * 0.43f, size.height * 0.84f), Offset(size.width * 0.57f, size.height * 0.84f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}
