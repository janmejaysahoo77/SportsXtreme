package com.example.sportsxtreme.presentation.match

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
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
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
private val TeamAMuted = Color(0xFFAAB3B8)

@Composable
private fun SelectTeamAScreen(teamSlot: String, onBack: () -> Unit, onDone: () -> Unit, onViewDetails: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var swipeAmount by remember { mutableStateOf(0f) }
    var previousTab by remember { mutableIntStateOf(0) }

    fun switchTab(tab: Int) {
        if (tab != selectedTab) {
            previousTab = selectedTab
            selectedTab = tab
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TeamABg)
            .drawBehind {
                drawRect(Brush.verticalGradient(listOf(Color(0xFF020712), Color(0xFF081323), Color(0xFF030712))))
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SelectTeamATopBar(title = if (selectedTab == 0) "Select team $teamSlot" else "Add Team", onBack = onBack)
            TeamATabs(selectedTab = selectedTab, onSelectTab = { switchTab(it) })
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = { swipeAmount = 0f },
                            onHorizontalDrag = { _, dragAmount -> swipeAmount += dragAmount },
                            onDragEnd = {
                                if (swipeAmount < -70f && selectedTab == 0) switchTab(1)
                                if (swipeAmount > 70f && selectedTab == 1) switchTab(0)
                                swipeAmount = 0f
                            },
                            onDragCancel = { swipeAmount = 0f }
                        )
                    }
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = if (selectedTab == 0) 16.dp else 14.dp),
                verticalArrangement = Arrangement.spacedBy(if (selectedTab == 0) 14.dp else 13.dp)
            ) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        val forward = targetState > previousTab
                        val enter = slideInHorizontally(animationSpec = tween(260)) { width -> if (forward) width else -width } + fadeIn(tween(180))
                        val exit = slideOutHorizontally(animationSpec = tween(260)) { width -> if (forward) -width else width } + fadeOut(tween(140))
                        (enter togetherWith exit).using(SizeTransform(clip = false))
                    },
                    label = "team-tab-content"
                ) { tab ->
                    Column(verticalArrangement = Arrangement.spacedBy(if (tab == 0) 14.dp else 13.dp)) {
                        if (tab == 0) {
                            TournamentTeamsContent(
                                onAddTeams = { switchTab(1) },
                                onViewDetails = onViewDetails
                            )
                        } else {
                            AddTeamContent()
                        }
                    }
                }
                Spacer(Modifier.height(92.dp))
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color(0xE6070D18))
                .padding(horizontal = 12.dp, vertical = 11.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(20.dp, RoundedCornerShape(12.dp), clip = false)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.horizontalGradient(listOf(TeamAAccent, Color(0xFF9BFF00))))
                    .clickable(onClick = onDone),
                contentAlignment = Alignment.Center
            ) {
                Text(if (selectedTab == 0) "DONE" else "ADD TEAM", color = Color(0xFF111604), fontSize = 15.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun SelectTeamATopBar(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(Color(0xE6090F1C))
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TeamAArrow(Modifier.size(25.dp).clickable(onClick = onBack), Color.White)
        Text(
            title,
            color = Color.White,
            fontSize = 22.sp,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 20.dp).weight(1f),
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
            .height(50.dp)
            .background(Color(0xE6090F1C))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    "Tournament teams",
                    color = if (selectedTab == 0) TeamAAccent else TeamAMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.clickable { onSelectTab(0) }.padding(vertical = 10.dp)
                )
            }
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    "Add Teams",
                    color = if (selectedTab == 1) TeamAAccent else TeamAMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.clickable { onSelectTab(1) }.padding(vertical = 10.dp)
                )
            }
        }
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .padding(horizontal = 14.dp)
        ) {
            val tabWidth = maxWidth / 2
            val underlineWidth = if (selectedTab == 0) 115.dp else 68.dp
            val targetStart = if (selectedTab == 0) {
                (tabWidth - underlineWidth) / 2
            } else {
                tabWidth + (tabWidth - underlineWidth) / 2
            }
            val animatedStart by animateDpAsState(
                targetValue = targetStart,
                animationSpec = tween(240),
                label = "team-tab-underline-offset"
            )
            Box(
                modifier = Modifier
                    .offset(x = animatedStart)
                    .width(underlineWidth)
                    .height(3.dp)
                    .background(TeamAAccent)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFF172033))
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
            .height(58.dp)
            .shadow(14.dp, RoundedCornerShape(14.dp), clip = false)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF111A29), Color(0xFF0C1726))))
            .padding(horizontal = 17.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TeamASearchIcon(Modifier.size(22.dp), TeamAMuted)
        Text("Quick search teams", color = TeamAMuted, fontSize = 16.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 13.dp))
    }
}

@Composable
private fun LeagueHeader(onAddTeams: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            "League Matches",
            color = Color.White,
            fontSize = 22.sp,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Black,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .height(30.dp)
                .shadow(10.dp, RoundedCornerShape(8.dp), clip = false)
                .clip(RoundedCornerShape(8.dp))
                .background(Brush.horizontalGradient(listOf(TeamAAccent, Color(0xFF98FF00))))
                .clickable(onClick = onAddTeams)
                .padding(horizontal = 17.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("ADD TEAMS", color = Color(0xFF111604), fontSize = 10.5.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun AddTeamContent() {
    Column {
        Text("Create Team", color = Color(0xFFBBE5ED), fontSize = 36.sp, fontWeight = FontWeight.Black, maxLines = 1)
        Text(
            "Add a new team to this tournament by\nentering the details below.",
            color = Color(0xFFC8D5D8),
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 5.dp)
        )
    }
    TeamLogoUploadCentered()
    TeamFormCard()
    JoinPlayerRow()
    AddTeamInfoCard()
}

@Composable
private fun TeamLogoUploadCentered() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .drawDottedCircle(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                TeamACameraIcon(Modifier.size(24.dp), TeamAAccent)
                Text(
                    "Upload team logo\n(optional)",
                    color = TeamAAccent,
                    fontSize = 7.5.sp,
                    lineHeight = 9.sp,
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
            .shadow(16.dp, RoundedCornerShape(14.dp), clip = false)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF111C2D), Color(0xFF0B1322))))
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        TeamInputField("Team Name *", "Enter team name")
        TeamInputField("City / Town *", "Enter city or town")
        TeamInputField("Captain Mobile Number *", "Enter captain mobile number")
        TeamInputField("Captain Name", "Enter captain name")
    }
}

@Composable
private fun TeamInputField(label: String, placeholder: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, color = TeamAAccent, fontSize = 10.sp, fontWeight = FontWeight.Black)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1C2636))
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(placeholder, color = Color(0xFFC5CBD0), fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFF172719))
                .drawBehind {
                    val stroke = Stroke(width = 1.5.dp.toPx())
                    drawRoundRect(TeamAAccent, style = stroke, cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx()))
                }
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
            .height(72.dp)
            .shadow(14.dp, RoundedCornerShape(12.dp), clip = false)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF101827))
            .padding(horizontal = 17.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TeamAInfoIcon(Modifier.size(18.dp), Color(0xFF73C1FF))
        Text(
            "Added teams will appear in\nTournament Teams",
            color = Color(0xFFDCE7EB),
            fontSize = 10.sp,
            lineHeight = 13.sp,
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
            .height(88.dp)
            .shadow(if (selected) 18.dp else 12.dp, RoundedCornerShape(14.dp), clip = false)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (selected) Brush.horizontalGradient(listOf(Color(0xFF16270E), Color(0xFF101B2A)))
                else Brush.horizontalGradient(listOf(Color(0xFF111B2A), Color(0xFF0D1624)))
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(color, color.copy(alpha = 0.55f)))),
                contentAlignment = Alignment.Center
            ) {
                Text(initials, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
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
            Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, color = if (selected) TeamAAccent else TeamAMuted, fontSize = 11.sp, fontWeight = FontWeight.Black, maxLines = 1)
        }
        Text(
            "Add Playing Team",
            color = Color(0xFFC9D0FF),
            fontSize = 11.5.sp,
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
