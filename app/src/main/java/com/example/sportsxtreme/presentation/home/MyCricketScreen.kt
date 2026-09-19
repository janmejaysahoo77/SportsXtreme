package com.example.sportsxtreme.presentation.home

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sportsxtreme.R
import com.example.sportsxtreme.presentation.team.TeamProfileActivity
import com.example.sportsxtreme.presentation.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

private val CricketAccent = XtremeLime
private val CricketBg = XtremeBgBlue
private val CricketPanel = XtremeCardBlue
private val CricketCard = XtremeCardBlue
private val CricketMuted = XtremeMuted
private val CricketStroke = XtremeCardBorder
private val CricketBlue = Color(0xFF2ED8FF)

private val ClubCardBlack = XtremeCardBlue
private val ClubCardBorder = XtremeCardBorder
private val ClubMuted = XtremeMuted

private val cricketTabs = listOf("Matches", "Tournaments", "Teams", "Stats", "Highlights")

private data class CricketMatch(
    val type: String,
    val title: String,
    val status: String,
    val left: String,
    val leftScore: String? = null,
    val right: String,
    val rightScore: String? = null,
    val scoreCenter: String? = null,
    val meta: String,
    val location: String? = null,
    val result: String? = null,
    val potm: String? = null,
    val live: Boolean = false,
    val liveSoon: Boolean = false,
    val accent: Color = CricketAccent
)

private data class CricketTeam(
    val id: String,
    val name: String,
    val initials: String,
    val location: String,
    val accent: Color
)

private data class CricketTournament(
    val name: String,
    val date: String,
    val location: String,
    val status: String,
    val accent: Color,
    val visual: TournamentVisual
)

private enum class TournamentVisual { NEON, FIELD, TROPHY, SLAM }

@Composable
private fun rememberJoinedTeams(): List<CricketTeam> {
    var teams by remember { mutableStateOf(emptyList<CricketTeam>()) }
    DisposableEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            teams = emptyList()
            onDispose { }
        } else {
            val registration = FirebaseFirestore.getInstance().collection("teams")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        teams = emptyList()
                    } else {
                        teams = snapshot?.documents.orEmpty()
                            .filter { document -> document.belongsTo(userId) }
                            .map { document -> document.toCricketTeam() }
                            .sortedBy { it.name.lowercase() }
                    }
                }
            onDispose { registration.remove() }
        }
    }
    return teams
}

private fun DocumentSnapshot.belongsTo(userId: String): Boolean {
    if (getString("ownerUserId") == userId || getString("ownerId") == userId) return true

    val ids = (get("memberIds") as? List<*>)?.filterIsInstance<String>().orEmpty()
    if (userId in ids) return true

    val people = listOf("members", "players").flatMap { field ->
        (get(field) as? List<*>)?.filterIsInstance<Map<*, *>>().orEmpty()
    }
    return people.any { person ->
        person["userId"] == userId || person["linkedUserId"] == userId || person["playerId"] == userId
    }
}

private fun com.google.firebase.firestore.DocumentSnapshot.toCricketTeam(): CricketTeam {
    val name =
        getString("teamName").orEmpty().ifBlank { getString("name").orEmpty().ifBlank { id } }
    val location = getString("city").orEmpty().ifBlank {
        getString("cityTown").orEmpty()
            .ifBlank { getString("location").orEmpty().ifBlank { "Team" } }
    }
    val initials = name.split(Regex("\\s+")).filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercase() }.ifBlank { "TM" }
    val accents = listOf(
        Color(0xFF1E6CF1),
        Color(0xFF7B32D9),
        Color(0xFF079F89),
        Color(0xFFD51D49),
        Color(0xFFFFB340)
    )
    return CricketTeam(
        id,
        name,
        initials,
        location,
        accents[(id.hashCode() and Int.MAX_VALUE) % accents.size]
    )
}

private val sampleMatches = listOf(
    CricketMatch(
        type = "T20 FRIENDLY MATCH",
        title = "Neon Syndicate vs Thunder Strikers",
        status = "Upcoming",
        left = "Syndicate",
        right = "Thunder",
        scoreCenter = "VS",
        meta = "Today, 6:30 PM",
        accent = CricketBlue
    ),
    CricketMatch(
        type = "CORPORATE LEAGUE",
        title = "Phoenix XI vs Titans CC",
        status = "LIVE SOON",
        left = "Phoenix XI",
        right = "Titans CC",
        scoreCenter = "9:00 AM",
        meta = "Tomorrow",
        location = "Riverside Ground",
        liveSoon = true
    ),
    CricketMatch(
        type = "WEEKEND CUP • FINAL",
        title = "Warriors CC vs Blue Hawks",
        status = "Finished",
        left = "Warriors CC",
        leftScore = "184/6 (20)",
        right = "Blue Hawks",
        rightScore = "166/9 (20)",
        meta = "Warriors won by 18 runs",
        result = "Warriors won by 18 runs",
        potm = "R. Sharma (74)",
        accent = Color(0xFFFFCA64)
    ),
    CricketMatch(
        type = "INTER COLLEGE TROPHY",
        title = "SITAM CSE vs Apex College",
        status = "Scheduled",
        left = "SITAM CSE",
        right = "Apex College",
        scoreCenter = "VS",
        meta = "League fixture",
        accent = Color(0xFFFF8FB0)
    ),
    CricketMatch(
        type = "PRACTICE MATCH",
        title = "Challengers vs Royals",
        status = "Result",
        left = "Challengers",
        leftScore = "128/10",
        right = "Royals",
        rightScore = "131/5",
        meta = "Royals won by 5 wickets",
        result = "Royals won by 5 wickets"
    )
)

private val tournaments = listOf(
    CricketTournament(
        "Neon Pro League",
        "20 Jun - 30 Jun 2026",
        "BHUBANESWAR",
        "UPCOMING",
        CricketBlue,
        TournamentVisual.NEON
    ),
    CricketTournament(
        "Corporate Cricket Cup",
        "15 Jun - 25 Jun 2026",
        "HYDERABAD",
        "LIVE",
        CricketAccent,
        TournamentVisual.FIELD
    ),
    CricketTournament(
        "Balisahi Premier League (BPL)",
        "03 Jun - 05 Jun 2026",
        "ODISHA",
        "PAST",
        Color(0xFF88909A),
        TournamentVisual.TROPHY
    ),
    CricketTournament(
        "Summer Slam T20",
        "10 Jul - 20 Jul 2026",
        "MUMBAI",
        "SCHEDULED",
        Color(0xFFBFD8FF),
        TournamentVisual.SLAM
    )
)

@Composable
fun MyCricketScreen(
    onMenuClick: () -> Unit = {},
    onStartMatch: () -> Unit = {},
    initialTab: Int = 0
) {
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab) }
    val joinedTeams = rememberJoinedTeams()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CricketBg)
    ) {
        CricketTopStrip(onMenuClick, selectedTab)
        CricketTabs(selectedTab = selectedTab, onSelect = { selectedTab = it })
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 10.dp,
                top = 10.dp,
                end = 10.dp,
                bottom = 100.dp
            ),
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
                    item {
                        SegmentPills(
                            listOf("Your", "Participate", "Network"),
                            active = 0,
                            activeColor = CricketBlue
                        )
                    }
                    items(tournaments) { tournament -> TournamentCard(tournament) }
                }

                2 -> {
                    item { CreateTeamPrompt() }
                    item { TeamFilterPills() }
                    item { QuickSearchBar() }
                    item { ActiveTeamsHeader(joinedTeams.size) }
                    if (joinedTeams.isEmpty()) {
                        item { NoJoinedTeamsCard() }
                    } else {
                        items(joinedTeams) { team -> TeamCard(team) }
                    }
                }

                3 -> {
                    item { StatsProContent() }
                }

                else -> {
                    item {
                        SectionTitle(
                            "Match History",
                            "Recent scorecards and completed fixtures"
                        )
                    }
                    items(sampleMatches.filter { it.status == "Finished" || it.status == "Result" }) { match ->
                        MatchCard(
                            match
                        )
                    }
                    item { TimelineCard() }
                }
            }
            item {
                Spacer(
                    Modifier
                        .height(30.dp)
                )
            }
        }

    }
}


@Composable
private fun CricketTopStrip(onMenuClick: () -> Unit, selectedTab: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(CricketBg)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeaderMenuButton(onMenuClick)
        Spacer(Modifier.width(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Text("Sports", color = CricketAccent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Xtreme", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(12.dp))
            val proBadgeColor = CricketAccent
            ProBadge(proBadgeColor)
        }
        Image(painter = painterResource(R.drawable.telegram), contentDescription = "Messages", modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Image(painter = painterResource(R.drawable.ghanti), contentDescription = "Notifications", modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Image(painter = painterResource(R.drawable.user), contentDescription = "Profile", modifier = Modifier.size(20.dp).clip(CircleShape))
    }
}

@Composable
private fun ProBadge(backgroundColor: Color) = Box(
    modifier = Modifier
        .clip(RoundedCornerShape(12.dp))
        .background(backgroundColor)
        .padding(horizontal = 8.dp, vertical = 4.dp),
    contentAlignment = Alignment.Center
) {
    Text(
        "PRO @ ₹199",
        color = if (backgroundColor == CricketAccent) Color.Black else Color.White,
        fontSize = 10.sp,
        fontWeight = FontWeight.ExtraBold,
        maxLines = 1
    )
}

@Composable
private fun CricketTabs(selectedTab: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(CricketBg)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        cricketTabs.forEachIndexed { index, label ->
            Column(
                modifier = Modifier
                    .height(48.dp)
                    .clickable { onSelect(index) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    label,
                    color = if (index == selectedTab) CricketAccent else Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    fontWeight = if (index == selectedTab) FontWeight.Bold else FontWeight.Normal
                )
                if (index == selectedTab) {
                    Spacer(Modifier.height(4.dp))
                    Box(
                        Modifier
                            .width(20.dp)
                            .height(2.dp)
                            .background(CricketAccent)
                    )
                }
            }
        }
    }
}

@Composable
private fun StartMatchPrompt(onStartMatch: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CricketCard)
            .border(1.dp, CricketStroke, RoundedCornerShape(18.dp))
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Ready to organize your next match?",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Setup custom matches, invite teams, and track live scores effortlessly.",
                color = CricketMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .height(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CricketAccent)
                    .clickable(onClick = onStartMatch)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(R.drawable.baseline_add_24),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(Color.Black)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Start Match",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SegmentPills(
    labels: List<String>,
    active: Int,
    activeColor: Color = CricketAccent,
    onTabClick: (Int) -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        labels.forEachIndexed { index, label ->
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (index == active) activeColor else CricketPanel)
                    .border(
                        1.dp,
                        if (index == active) activeColor else CricketStroke,
                        RoundedCornerShape(18.dp)
                    )
                    .clickable { onTabClick(index) }
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    color = if (index == active) {
                        if (activeColor == CricketAccent) Color.Black else Color.White
                    } else {
                        Color.White.copy(alpha = 0.7f)
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun MatchCard(match: CricketMatch) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CricketCard)
            .border(1.dp, CricketStroke, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        match.type,
                        color = match.accent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        match.title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
                StatusBadge(match.status, match.live, match.liveSoon)
            }
            Spacer(Modifier.height(20.dp))

            when (match.status) {
                "Upcoming" -> UpcomingMatchLayout(match)
                "LIVE SOON" -> LiveSoonMatchLayout(match)
                "Finished" -> FinishedMatchLayout(match)
                "Scheduled" -> ScheduledMatchLayout(match)
                "Result" -> ResultMatchLayout(match)
                else -> UpcomingMatchLayout(match)
            }

            match.location?.let {
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(R.drawable.loca),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(it, color = CricketMuted, fontSize = 12.sp)
                }
            }

            match.result?.let {
                Spacer(Modifier.height(12.dp))
                Text(
                    it,
                    color = CricketAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic
                )
            }

            match.potm?.let {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "POTM: $it",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun UpcomingMatchLayout(match: CricketMatch) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TeamNode(match.left, match.accent, Modifier.weight(1f))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(100.dp)
        ) {
            Text(
                match.scoreCenter ?: "VS",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(match.meta, color = CricketAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        TeamNode(match.right, match.accent, Modifier.weight(1f))
    }
}

@Composable
private fun LiveSoonMatchLayout(match: CricketMatch) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TeamNode(match.left, match.accent, Modifier.weight(1f))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(100.dp)
        ) {
            Text("Tomorrow", color = CricketMuted, fontSize = 10.sp)
            Text(
                match.scoreCenter ?: "9:00",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text("AM", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        TeamNode(match.right, match.accent, Modifier.weight(1f))
    }
}

@Composable
private fun FinishedMatchLayout(match: CricketMatch) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ScoreRow(match.left, match.leftScore ?: "")
        ScoreRow(match.right, match.rightScore ?: "")
    }
}

@Composable
private fun ScheduledMatchLayout(match: CricketMatch) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        BigTeamNode(match.left, match.accent, Modifier.weight(1f))
        Text(
            "vs",
            color = Color.White.copy(alpha = 0.3f),
            fontSize = 16.sp,
            fontStyle = FontStyle.Italic
        )
        BigTeamNode(match.right, match.accent, Modifier.weight(1f))
    }
}

@Composable
private fun ResultMatchLayout(match: CricketMatch) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(match.left, color = Color.White, fontSize = 16.sp, modifier = Modifier.weight(1f))
            Text(
                match.leftScore ?: "",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(match.right, color = Color.White, fontSize = 16.sp, modifier = Modifier.weight(1f))
            Text(
                match.rightScore ?: "",
                color = CricketAccent,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.1f))
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                match.result ?: "",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ScoreRow(team: String, score: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.1f))
        )
        Spacer(Modifier.width(12.dp))
        Text(
            team,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Text(score, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BigTeamNode(name: String, accent: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(name.take(1), color = accent, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        Text(name, color = Color.White, fontSize = 12.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun TeamNode(name: String, accent: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.ball),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            name,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}


@Composable
private fun StatusBadge(label: String, live: Boolean, liveSoon: Boolean = false) {
    val bgColor = when {
        live -> CricketAccent.copy(alpha = 0.15f)
        liveSoon -> CricketAccent.copy(alpha = 0.1f)
        label == "Finished" -> Color.White.copy(alpha = 0.1f)
        else -> Color.White.copy(alpha = 0.05f)
    }

    val textColor = when {
        live -> CricketAccent
        liveSoon -> CricketAccent
        else -> Color.White.copy(alpha = 0.7f)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (liveSoon) {
                Box(modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(CricketAccent))
                Spacer(Modifier.width(6.dp))
            }
            Text(label, color = textColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String, showInfo: Boolean = false) {
    Row(
        modifier = Modifier.padding(top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = Color.White,
                fontSize = 19.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                subtitle,
                color = CricketMuted,
                fontSize = 9.5.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 7.dp)
            )
        }
        if (showInfo) {
            Image(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.6f))
            )
        }
    }
}

private data class StatItem(val label: String, val value: String)

@Composable
private fun StatsProContent() {
    var selectedStatTab by remember { mutableIntStateOf(0) }
    var isUnlocked by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(CricketPanel)
                .border(
                    1.dp,
                    Brush.verticalGradient(listOf(CricketAccent.copy(alpha = 0.5f), Color.Transparent)),
                    RoundedCornerShape(18.dp)
                )
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Want to improve your stats?",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Box(
                Modifier
                    .height(37.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(CricketAccent)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "ANALYSE",
                    color = Color.Black,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        SegmentPills(
            labels = listOf("Batting", "Bowling", "Fielding", "Captaincy"),
            active = selectedStatTab,
            onTabClick = { selectedStatTab = it }
        )

        Spacer(Modifier.height(28.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .blur(if (!isUnlocked) 15.dp else 0.dp)
                    .then(if (!isUnlocked) Modifier.alpha(0.5f) else Modifier)
            ) {
                StatCategoryContent(selectedStatTab)
            }

            if (!isUnlocked) {
                // Freeze overlay: intercepts all touches so background stats aren't selectable
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(enabled = false, onClick = { /* Intercept */ })
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(122.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(CricketPanel)
                            .border(1.dp, CricketStroke, RoundedCornerShape(28.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.tala),
                            contentDescription = "PRO locked",
                            modifier = Modifier.size(61.dp)
                        )
                    }
                    Spacer(Modifier.height(30.dp))
                    Text(
                        "Full stats, full story",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Track every run, wicket, and pattern that\ndefines your game with PRO.",
                        color = CricketMuted,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(34.dp))
                    Box(
                        Modifier
                            .height(60.dp)
                            .width(267.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(CricketAccent)
                            .clickable { isUnlocked = true }
                            .padding(horizontal = 22.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "PRO starting at ₹69",
                            color = Color(0xFF091002),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCategoryContent(tabIndex: Int) {
    val stats = when (tabIndex) {
        0 -> battingStats
        1 -> bowlingStats
        2 -> fieldingStats
        else -> captaincyStats
    }

    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        StatSection("Overall", stats)
        StatSection("Tennis ball", stats, isTennis = true)
    }
}

@Composable
private fun StatSection(title: String, stats: List<StatItem>, isTennis: Boolean = false) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            if (isTennis) {
                Image(
                    painter = painterResource(R.drawable.badmintonlogo),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
            } else {
                Box(
                    Modifier
                        .width(4.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF246CE6))
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.weight(1f)
            )
            if (!isTennis) {
                Box(
                    Modifier
                        .height(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.groups),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            colorFilter = ColorFilter.tint(CricketAccent)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Compare",
                            color = CricketAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Image(
                        painter = painterResource(R.drawable.network),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        colorFilter = ColorFilter.tint(CricketAccent)
                    )
                    Text("WW", color = CricketAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Image(
                        painter = painterResource(R.drawable.list),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.6f))
                    )
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        StatGrid(stats)
    }
}

@Composable
private fun StatGrid(items: List<StatItem>) {
    // Custom grid using Rows to avoid nested scrolling issues in LazyColumn if any
    val rows = items.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { item ->
                    StatBox(item, Modifier.weight(1f))
                }
                // Fill empty slots if last row has fewer than 3 items
                repeat(3 - rowItems.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun StatBox(item: StatItem, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .height(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CricketPanel)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            item.value,
            color = if (item.value.contains("%")) CricketAccent else Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            item.label,
            color = CricketMuted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private val battingStats = listOf(
    StatItem("MAT", "1"), StatItem("INNS", "1"), StatItem("NO", "1"),
    StatItem("RUNS", "1"), StatItem("HS", "1*"), StatItem("AVG", "-"),
    StatItem("SR", "100"), StatItem("30s", "0"), StatItem("50s", "0"),
    StatItem("100s", "0"), StatItem("4s", "0"), StatItem("6s", "0"),
    StatItem("DUCKS", "0"), StatItem("WON", "1"), StatItem("LOSS", "0")
)

private val bowlingStats = listOf(
    StatItem("MAT", "1"), StatItem("INNS", "1"), StatItem("OVERS", "1"),
    StatItem("MAIDENS", "0"), StatItem("RUNS", "16"), StatItem("WKTS", "0"),
    StatItem("BB", "0/16"), StatItem("3 WKTS", "0"), StatItem("5 WKTS", "0"),
    StatItem("ECO", "16"), StatItem("SR", "0"), StatItem("AVG", "0"),
    StatItem("WD", "0"), StatItem("NB", "0"), StatItem("DOTS", "3"),
    StatItem("4S", "1"), StatItem("6S", "2")
)

private val fieldingStats = listOf(
    StatItem("MAT", "1"), StatItem("CATCHES", "0"), StatItem("C.B", "0"),
    StatItem("R/O", "0"), StatItem("ST", "0"), StatItem("ASST. R/O", "0"),
    StatItem("BYES", "0")
)

private val captaincyStats = listOf(
    StatItem("MAT", "1"), StatItem("TOSS WON", "1"),
    StatItem("WIN %", "100.00%"), StatItem("LOSS %", "0.00%")
)

@Composable
private fun HostTournamentPrompt() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CricketCard)
            .border(1.dp, CricketStroke, RoundedCornerShape(18.dp))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1D5EF7).copy(alpha = 0.2f))
                    .border(1.dp, Color(0xFF1D5EF7), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.jod),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(Color(0xFFBFD8FF))
                )
            }
            Spacer(Modifier.width(16.dp))
            Text(
                text = "Want to host\na tournament?",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .height(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(CricketAccent)
                    .clickable { /* Handle register */ }
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Register",
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun TournamentCard(tournament: CricketTournament) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CricketCard)
            .border(
                1.dp,
                if (tournament.status == "LIVE") CricketAccent else CricketStroke,
                RoundedCornerShape(18.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            TournamentVisualPanel(tournament)

            // Gradient overlay for title readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 300f
                        )
                    )
            )

            TournamentStatusTag(
                label = tournament.status,
                live = tournament.status == "LIVE",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            )

            Text(
                tournament.name,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                TournamentMetaLine(tournament.date, iconRes = R.drawable.calender)
                Spacer(Modifier.height(6.dp))
                TournamentMetaLine(tournament.location, iconRes = R.drawable.loca)
            }
            TournamentArrow(if (tournament.status == "LIVE") CricketAccent else Color.White)
        }
    }
}

@Composable
private fun TournamentVisualPanel(tournament: CricketTournament) {
    val imageRes = when (tournament.visual) {
        TournamentVisual.NEON -> R.drawable.stadium
        TournamentVisual.FIELD -> R.drawable.club_stadium
        TournamentVisual.TROPHY -> R.drawable.trophyfull
        TournamentVisual.SLAM -> R.drawable.cricketblast
    }

    Image(
        painter = painterResource(imageRes),
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
}



@Composable
private fun TournamentStatusTag(label: String, live: Boolean, modifier: Modifier = Modifier) {
    val bgColor = when {
        live -> CricketAccent
        label == "PAST" -> Color.White.copy(alpha = 0.1f)
        else -> CricketStroke.copy(alpha = 0.4f)
    }
    val textColor = when {
        live -> Color.Black
        label == "PAST" -> Color.White.copy(alpha = 0.5f)
        else -> Color.White
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (live) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color.Black)
                )
                Spacer(Modifier.width(6.dp))
            }
            Text(
                label,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TournamentMetaLine(text: String, iconRes: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.6f))
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TournamentArrow(accent: Color) {
    Canvas(Modifier.size(24.dp)) {
        drawLine(
            accent,
            Offset(size.width * 0.36f, size.height * 0.2f),
            Offset(size.width * 0.68f, size.height * 0.5f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            accent,
            Offset(size.width * 0.68f, size.height * 0.5f),
            Offset(size.width * 0.36f, size.height * 0.8f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun CreateTeamPrompt() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(ClubCardBlack)
            .border(1.dp, ClubCardBorder, RoundedCornerShape(18.dp))
            .drawBehind {
                drawLine(
                    color = CricketAccent,
                    start = Offset(4.dp.toPx(), 16.dp.toPx()),
                    end = Offset(4.dp.toPx(), size.height - 16.dp.toPx()),
                    strokeWidth = 6.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
            .padding(start = 24.dp, top = 20.dp, end = 20.dp, bottom = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Want to create a new team?",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Start your own legacy in the community.",
                color = ClubMuted,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        Box(
            modifier = Modifier
                .height(40.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(CricketAccent)
                .clickable { /* Create team action */ }
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Create",
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.W800
            )
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
                    .height(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (index == 0) CricketAccent else Color.White.copy(alpha = 0.1f))
                    .then(
                        if (index != 0) Modifier.border(
                            1.dp,
                            ClubCardBorder,
                            RoundedCornerShape(20.dp)
                        ) else Modifier
                    )
                    .clickable { /* Filter action */ }
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    color = if (index == 0) Color.Black else Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
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
            .height(56.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(ClubCardBlack)
            .border(1.dp, ClubCardBorder, RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.search),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(ClubMuted)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "Quick search",
                color = ClubMuted,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ActiveTeamsHeader(teamCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(CricketAccent)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "ACTIVE TEAMS",
            color = CricketAccent,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun NoJoinedTeamsCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(ClubCardBlack)
            .border(1.dp, ClubCardBorder, RoundedCornerShape(18.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "No teams joined yet",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Teams you join will appear here automatically.",
            color = ClubMuted,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TeamCard(team: CricketTeam) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(ClubCardBlack)
            .border(1.dp, ClubCardBorder, RoundedCornerShape(18.dp))
            .clickable {
                context.startActivity(
                    Intent(
                        context,
                        TeamProfileActivity::class.java
                    ).putExtra(TeamProfileActivity.EXTRA_TEAM_ID, team.id)
                )
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(team.accent),
            contentAlignment = Alignment.Center
        ) {
            Text(
                team.initials,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                team.name,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.loca),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    colorFilter = ColorFilter.tint(ClubMuted)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    team.location,
                    color = ClubMuted,
                    fontSize = 14.sp
                )
            }
        }
        Text(
            "⋮",
            color = Color.White.copy(alpha = 0.3f),
            fontSize = 20.sp
        )
    }
}

@Composable
private fun StatsMatrix() {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            StatTile("Matches", "42", "+8 this month", CricketAccent, Modifier.weight(1f))
            StatTile("Win Rate", "68%", "last 12 games", CricketBlue, Modifier.weight(1f))
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            StatTile("Runs", "5.8K", "team total", Color(0xFFFFCA64), Modifier.weight(1f))
            StatTile("Wickets", "214", "all squads", Color(0xFFFF8FB0), Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    note: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
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
        Text(
            value,
            color = Color.White,
            fontSize = 25.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.ExtraBold
        )
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
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF07101A),
                        Color(0xFF0B2038),
                        Color(0xFF172608)
                    )
                )
            )
            .border(1.dp, CricketStroke, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(
                "AI Momentum",
                color = CricketAccent,
                fontSize = 8.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "Syndicate form is climbing",
                color = Color.White,
                fontSize = 17.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(top = 7.dp)
            )
            Text(
                "Bowling economy improved by 11% across the last five matches.",
                color = CricketMuted,
                fontSize = 10.sp,
                lineHeight = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
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
        Text(
            "Saved scorecards",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold
        )
        listOf(
            "Phoenix XI beat Titans CC",
            "Royals chased 132 in 18.4 overs",
            "Warriors CC lifted Weekend Cup"
        ).forEach {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(CricketAccent))
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
            drawLine(
                tint,
                Offset(size.width * 0.16f, size.height * 0.28f),
                Offset(size.width * 0.84f, size.height * 0.28f),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
            drawLine(
                tint,
                Offset(size.width * 0.16f, size.height * 0.5f),
                Offset(size.width * 0.84f, size.height * 0.5f),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
            drawLine(
                tint,
                Offset(size.width * 0.16f, size.height * 0.72f),
                Offset(size.width * 0.84f, size.height * 0.72f),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }
    }
}


