package com.example.sportsxtreme

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat

class SelectTeamAorBActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        val teamSlot = intent.getStringExtra(EXTRA_TEAM_SLOT)?.takeIf { it == "B" } ?: "A"
        setContent {
            SelectTeamAScreen(
                teamSlot = teamSlot,
                onBack = { finish() },
                onDone = { finish() },
                onViewDetails = { startActivity(Intent(this, ViewDetailsScreenWhileStartMatch::class.java)) }
            )
        }
    }

    companion object {
        const val EXTRA_TEAM_SLOT = "team_slot"
    }
}

private val TeamAAccent = Color(0xFFC1FF00)
private val TeamABg = Color(0xFF070D18)
private val TeamAPanel = Color(0xFF090F1C)
private val TeamACard = Color(0xFF151B28)
private val TeamAStroke = Color(0xFF2D3548)
private val TeamAMuted = Color(0xFFAAB3B8)

@Composable
private fun SelectTeamAScreen(teamSlot: String, onBack: () -> Unit, onDone: () -> Unit, onViewDetails: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TeamABg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SelectTeamATopBar(title = if (selectedTab == 0) "Select team $teamSlot" else "Add Team", onBack = onBack)
            TeamATabs(selectedTab = selectedTab, onSelectTab = { selectedTab = it })
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = if (selectedTab == 0) 23.dp else 17.dp),
                verticalArrangement = Arrangement.spacedBy(if (selectedTab == 0) 18.dp else 12.dp)
            ) {
                if (selectedTab == 0) {
                    TournamentTeamsContent(
                        onAddTeams = { selectedTab = 1 },
                        onViewDetails = onViewDetails
                    )
                } else {
                    AddTeamContent()
                }
                Spacer(Modifier.height(92.dp))
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(TeamABg)
                .padding(horizontal = 14.dp, vertical = 11.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(51.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(TeamAAccent)
                    .clickable(onClick = onDone),
                contentAlignment = Alignment.Center
            ) {
                Text(if (selectedTab == 0) "DONE" else "Add Team", color = Color(0xFF111604), fontSize = 14.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun SelectTeamATopBar(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(TeamAPanel)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TeamAArrow(Modifier.size(25.dp).clickable(onClick = onBack), Color.White)
        Text(
            title,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 26.dp).weight(1f),
            maxLines = 1
        )
        TeamAQrIcon(Modifier.size(25.dp), Color.White)
        TeamAInfoIcon(Modifier.padding(start = 24.dp).size(24.dp), Color.White)
    }
}

@Composable
private fun TeamATabs(selectedTab: Int, onSelectTab: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(TeamAPanel)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Tournament teams",
                color = if (selectedTab == 0) Color.White else TeamAMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.clickable { onSelectTab(0) }
            )
            Text(
                "Add Teams",
                color = if (selectedTab == 1) TeamAAccent else TeamAMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .padding(start = 34.dp)
                    .clickable { onSelectTab(1) }
            )
        }
        Box(
            modifier = Modifier
                .padding(start = if (selectedTab == 0) 14.dp else 119.dp)
                .width(if (selectedTab == 0) 107.dp else 72.dp)
                .height(3.dp)
                .background(TeamAAccent)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(TeamAStroke)
        )
    }
}

@Composable
private fun TournamentTeamsContent(onAddTeams: () -> Unit, onViewDetails: () -> Unit) {
    SearchBox()
    LeagueHeader(onAddTeams)
    TeamRow(
        initials = "BH",
        title = "Bhu",
        subtitle = "Ready for Draft",
        selected = true,
        color = Color(0xFF1E73FF),
        onViewDetails = onViewDetails
    )
    TeamRow(
        initials = "DW",
        title = "Dipesh Warrior 69",
        subtitle = "Pending Entry",
        selected = false,
        color = Color(0xFF007A70),
        onViewDetails = onViewDetails
    )
}

@Composable
private fun SearchBox() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(53.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(TeamACard)
            .border(1.dp, TeamAStroke, RoundedCornerShape(9.dp))
            .padding(horizontal = 17.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TeamASearchIcon(Modifier.size(22.dp), TeamAMuted)
        Text("Quick search", color = TeamAMuted, fontSize = 16.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 13.dp))
    }
}

@Composable
private fun LeagueHeader(onAddTeams: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            "League Matches",
            color = Color.White,
            fontSize = 20.sp,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Black,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .height(22.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(TeamAAccent)
                .clickable(onClick = onAddTeams)
                .padding(horizontal = 17.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("ADD TEAMS", color = Color(0xFF111604), fontSize = 10.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun AddTeamContent() {
    Column {
        Text("Create Team", color = Color.White, fontSize = 31.sp, fontWeight = FontWeight.Black, maxLines = 1)
        Text(
            "Add a new team to this tournament by\nentering the details below.",
            color = Color(0xFFC8D5D8),
            fontSize = 9.sp,
            lineHeight = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 5.dp)
        )
    }
    TeamLogoUpload()
    TeamFormCard()
    JoinPlayerRow()
    AddTeamInfoCard()
}

@Composable
private fun TeamLogoUpload() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .drawDottedCircle(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TeamACameraIcon(Modifier.size(19.dp), TeamAAccent)
                Text(
                    "Upload Team Logo\n(Optional)",
                    color = TeamAAccent,
                    fontSize = 6.5.sp,
                    lineHeight = 8.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

private fun Modifier.drawDottedCircle(): Modifier = this.then(
    Modifier.drawBehind {
        val strokeWidth = 1.4.dp.toPx()
        val radius = size.minDimension * 0.47f
        val circumference = 2f * Math.PI.toFloat() * radius
        val segments = 44
        val gapAngle = 360f / segments
        val sweep = gapAngle * 0.42f
        repeat(segments) { index ->
            drawArc(
                color = TeamAAccent,
                startAngle = index * gapAngle,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(size.width * 0.5f - radius, size.height * 0.5f - radius),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
)

@Composable
private fun TeamFormCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF101827))
            .border(1.dp, Color(0xFF50612A), RoundedCornerShape(8.dp))
            .padding(horizontal = 13.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TeamInputField("Team Name *", "Enter team name")
        TeamInputField("City / Town *", "Enter city or town")
        TeamInputField("Captain Mobile Number *", "Enter captain mobile number")
        TeamInputField("Captain Name", "Enter captain name")
    }
}

@Composable
private fun TeamInputField(label: String, placeholder: String) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(label, color = TeamAAccent, fontSize = 8.sp, fontWeight = FontWeight.Black)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(35.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF313847))
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(placeholder, color = Color(0xFFC5CBD0), fontSize = 8.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun JoinPlayerRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(17.dp)
                .border(1.5.dp, TeamAAccent, RoundedCornerShape(1.dp))
        )
        Column(modifier = Modifier.padding(start = 9.dp)) {
            Text("Join this team as a player", color = TeamAAccent, fontSize = 9.sp, fontWeight = FontWeight.Black)
            Text("Your profile will be added to this team.", color = Color(0xFFC2CED1), fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AddTeamInfoCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(61.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF101827))
            .border(1.dp, Color(0xFF50612A), RoundedCornerShape(6.dp))
            .padding(horizontal = 17.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TeamAInfoIcon(Modifier.size(16.dp), Color(0xFF73C1FF))
        Text(
            "Added teams will appear in\nTournament Teams",
            color = Color(0xFFDCE7EB),
            fontSize = 8.5.sp,
            lineHeight = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 13.dp)
        )
    }
}

@Composable
private fun TeamRow(initials: String, title: String, subtitle: String, selected: Boolean, color: Color, onViewDetails: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(74.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(TeamACard)
            .border(if (selected) 2.dp else 1.dp, if (selected) TeamAAccent else TeamAStroke, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Text(initials, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(17.dp)
                        .clip(CircleShape)
                        .background(TeamAAccent),
                    contentAlignment = Alignment.Center
                ) {
                    TeamACheck(Modifier.size(11.dp), Color(0xFF111604))
                }
            }
        }
        Column(modifier = Modifier.padding(start = 13.dp).weight(1f)) {
            Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, color = if (selected) TeamAAccent else TeamAMuted, fontSize = 10.sp, fontWeight = FontWeight.Black, maxLines = 1)
        }
        Text(
            "View Details",
            color = Color(0xFFC9D0FF),
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.clickable(onClick = onViewDetails)
        )
    }
}

@Composable
private fun TeamAArrow(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        val path = Path().apply {
            moveTo(size.width * 0.66f, size.height * 0.24f)
            lineTo(size.width * 0.34f, size.height * 0.5f)
            lineTo(size.width * 0.66f, size.height * 0.76f)
        }
        drawPath(path, tint, style = stroke)
    }
}

@Composable
private fun TeamAInfoIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.38f, style = stroke)
        drawLine(tint, Offset(size.width * 0.5f, size.height * 0.45f), Offset(size.width * 0.5f, size.height * 0.69f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.035f, center = Offset(size.width * 0.5f, size.height * 0.3f))
    }
}

@Composable
private fun TeamAQrIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        val box = size.minDimension * 0.24f
        listOf(
            Offset(size.width * 0.13f, size.height * 0.13f),
            Offset(size.width * 0.63f, size.height * 0.13f),
            Offset(size.width * 0.13f, size.height * 0.63f)
        ).forEach { topLeft ->
            drawRect(tint, topLeft = topLeft, size = Size(box, box), style = stroke)
            drawCircle(tint, radius = size.minDimension * 0.035f, center = Offset(topLeft.x + box * 0.5f, topLeft.y + box * 0.5f))
        }
        drawLine(tint, Offset(size.width * 0.62f, size.height * 0.62f), Offset(size.width * 0.86f, size.height * 0.62f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.62f, size.height * 0.62f), Offset(size.width * 0.62f, size.height * 0.86f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.76f, size.height * 0.74f), Offset(size.width * 0.86f, size.height * 0.86f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}

@Composable
private fun TeamASearchIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.28f, center = Offset(size.width * 0.42f, size.height * 0.42f), style = stroke)
        drawLine(tint, Offset(size.width * 0.62f, size.height * 0.62f), Offset(size.width * 0.84f, size.height * 0.84f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}

@Composable
private fun TeamACameraIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
        drawRoundRect(
            color = tint,
            topLeft = Offset(size.width * 0.18f, size.height * 0.28f),
            size = Size(size.width * 0.64f, size.height * 0.48f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx()),
            style = stroke
        )
        drawLine(tint, Offset(size.width * 0.36f, size.height * 0.28f), Offset(size.width * 0.43f, size.height * 0.17f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.43f, size.height * 0.17f), Offset(size.width * 0.6f, size.height * 0.17f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.6f, size.height * 0.17f), Offset(size.width * 0.67f, size.height * 0.28f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.13f, center = Offset(size.width * 0.5f, size.height * 0.52f), style = stroke)
    }
}

@Composable
private fun TeamACheck(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
        val path = Path().apply {
            moveTo(size.width * 0.24f, size.height * 0.51f)
            lineTo(size.width * 0.43f, size.height * 0.68f)
            lineTo(size.width * 0.78f, size.height * 0.31f)
        }
        drawPath(path, tint, style = stroke)
    }
}
