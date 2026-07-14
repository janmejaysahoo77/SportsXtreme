package com.example.sportsxtreme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import kotlin.math.PI
import kotlin.math.sin

class TournamentRequirementsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        setContent {
            TournamentRequirementsScreen(onBack = { finish() })
        }
    }
}

private val ReqAccent = Color(0xFFC1FF00)
private val ReqBg = Color(0xFF010509)
private val ReqPanel = Color(0xFF0B111C)
private val ReqField = Color(0xFF111828)
private val ReqStroke = Color(0xFF2E3950)
private val ReqMuted = Color(0xFF8E9C9A)
private val ReqCyan = Color(0xFF4DE9FF)
private val ReqGold = Color(0xFFFFB84D)

@Composable
private fun TournamentRequirementsScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReqBg)
            .drawBehind {
                drawCircle(Color(0x332C4D11), radius = size.width * 0.78f, center = Offset(size.width, size.height * 0.16f))
                drawCircle(Color(0x1A00D2FF), radius = size.width * 0.58f, center = Offset(0f, size.height * 0.78f))
            }
    ) {
        RequirementsTopBar(onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            RequirementHeroCard()
            RequirementStatsRow()
            TournamentDetailsSection()
            WinningPrizeSection()
            FormatSection()
            NotesSection()
            SmartFeaturesSection()
            TermsPanel()
            ContinueButton()
        }
    }
}

@Composable
private fun RequirementsTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF07101A), Color(0xFF0D1C22), Color(0xFF07101A))
                )
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ReqArrowIcon(modifier = Modifier.size(30.dp).clickable(onClick = onBack), right = false)
        Text(
            "Team Requirements",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(Color(0x1FC1FF00))
                .border(1.dp, Color(0x55C1FF00), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("2", color = ReqAccent, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun RequirementHeroCard() {
    val pulse = rememberReqPremiumPulse()
    val glow = reqPremiumGlowAlpha(pulse)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(178.dp)
            .shadow(16.dp, RoundedCornerShape(20.dp), clip = false)
            .drawBehind {
                drawCircle(ReqAccent.copy(alpha = glow * 0.32f), radius = size.width * 0.36f, center = Offset(size.width * 0.12f, size.height * 1.02f))
                drawCircle(ReqCyan.copy(alpha = glow * 0.2f), radius = size.width * 0.27f, center = Offset(size.width * 0.94f, size.height * 0.08f))
            }
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF17260D), Color(0xFF0B1A22), Color(0xFF111523))
                )
            )
            .border(1.2.dp, ReqAccent.copy(alpha = 0.42f + glow * 0.32f), RoundedCornerShape(20.dp))
    ) {
        Image(
            painter = painterResource(id = R.drawable.prizeimage_onboarding4),
            contentDescription = "Tournament prize",
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .height(178.dp)
                .width(190.dp),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xF007100B), Color(0xD007100B), Color(0x3307100B))
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 18.dp, end = 120.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Finalize setup", color = ReqAccent, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
            Text("Rules, prizes and entries", color = Color.White, fontSize = 25.sp, lineHeight = 28.sp, fontWeight = FontWeight.Black)
            Text(
                "Set team limits, entry fee, prize pool and public discovery options.",
                color = Color(0xFFC5D0CC),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
        StepBadge(modifier = Modifier.align(Alignment.TopEnd).padding(14.dp))
    }
}

@Composable
private fun StepBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xE60D1420))
            .border(1.dp, Color(0x664DE9FF), RoundedCornerShape(18.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("STEP 2 OF 2", color = ReqCyan, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun RequirementStatsRow() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatCard("12 Jun", "Date", ReqAccent, Modifier.weight(1f))
        StatCard("T20", "Match Form", ReqCyan, Modifier.weight(1f))
        StatCard("Tennis", "Ball Type", ReqGold, Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(value: String, label: String, accent: Color, modifier: Modifier) {
    val pulse = rememberReqPremiumPulse(2600f)
    val glow = reqPremiumGlowAlpha(pulse)
    Column(
        modifier = modifier
            .height(82.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp), clip = false)
            .drawBehind {
                drawCircle(accent.copy(alpha = glow * 0.22f), radius = size.width * 0.5f, center = Offset(size.width * 0.5f, size.height * 0.05f))
            }
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF101827), Color(0xFF0B111C))))
            .border(1.dp, accent.copy(alpha = 0.42f + glow * 0.3f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = accent, fontSize = 21.sp, fontWeight = FontWeight.Black, maxLines = 1)
        Text(label, color = Color(0xFFBFCBC7), fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun TournamentDetailsSection() {
    ReqSection("Tournament Details", "ENTRY") {
        ReqInput("Tournament Location", "Bhubaneswar, Odisha")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ReqInput("Entry Fee", "Rs 999", Modifier.weight(1f))
            ReqInput("Number of Teams", "10", Modifier.weight(1f))
        }
        ReqInput("Match Duration", "How many hours is one match?")
        InfoPanel("Only the organizer can edit team information after the tournament is created.")
    }
}

@Composable
private fun WinningPrizeSection() {
    ReqSection("Winning Prize", "REWARDS", iconRes = R.drawable.tournamentlogo) {
        ChipRowReq(listOf("Cash", "Trophy", "Both"), selectedIndex = 0)
        ReqInput("Prize Pool", "Rs 50,000")
        ReqInput("Runner-up Prize", "Rs 10,000")
    }
}

@Composable
private fun FormatSection() {
    ReqSection("Tournament Format", "FIXTURES") {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SelectBox("League", selected = false, modifier = Modifier.weight(1f))
            SelectBox("Knockout", selected = true, modifier = Modifier.weight(1f))
        }
        InfoPanel("Knockout teams are eliminated after one loss. League format keeps all teams in the race longer.")
    }
}

@Composable
private fun NotesSection() {
    ReqSection("Tournament Notes", "RULES") {
        ReqInput("Notes", "Add rules, prize breakdown, reporting time, dress code, or important instructions", minHeight = 96.dp)
    }
}

@Composable
private fun SmartFeaturesSection() {
    ReqSection("Smart Features", "PUBLIC FEED", iconRes = R.drawable.organiserss) {
        ToggleLine("Invite all the players of my previous tournaments", "Notify past players and teams instantly.", true)
        ToggleLine("Do you need officials? Umpire, scorer, streamer", "Post to the officials feed and find match staff faster.", false)
    }
}

@Composable
private fun TermsPanel() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(7.dp, RoundedCornerShape(16.dp), clip = false)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF101A14), Color(0xFF0B111C))))
            .border(1.dp, Color(0x994F6B1F), RoundedCornerShape(16.dp))
            .padding(15.dp),
        verticalAlignment = Alignment.Top
    ) {
        SmallBadge(active = true)
        Text(
            "I agree to all SportsXtreme terms and conditions for creating and managing this tournament.",
            color = Color.White,
            fontSize = 13.sp,
            lineHeight = 17.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

@Composable
private fun ContinueButton() {
    val pulse = rememberReqPremiumPulse(2200f)
    val shineX = 0.1f + pulse * 1.15f
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .shadow(12.dp, RoundedCornerShape(18.dp), clip = false)
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.horizontalGradient(listOf(ReqAccent, Color(0xFFDFFF6C), ReqGold)))
            .drawBehind {
                val startX = size.width * (shineX - 0.24f)
                val endX = size.width * shineX
                drawLine(
                    color = Color.White.copy(alpha = 0.32f),
                    start = Offset(startX, 0f),
                    end = Offset(endX, size.height),
                    strokeWidth = 26.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        Text("Continue", color = Color(0xFF111604), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun ReqSection(
    title: String,
    endBadge: String? = null,
    iconRes: Int? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val pulse = rememberReqPremiumPulse(3200f)
    val glow = reqPremiumGlowAlpha(pulse)
    Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionIcon(iconRes)
            Text(
                title,
                color = Color.White,
                fontSize = 17.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(start = 8.dp).weight(1f)
            )
            endBadge?.let {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF17240B))
                        .border(1.dp, Color(0x705C7E22), RoundedCornerShape(14.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(it, color = ReqAccent, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(17.dp), clip = false)
                .drawBehind {
                    drawCircle(ReqCyan.copy(alpha = glow * 0.08f), radius = size.width * 0.42f, center = Offset(size.width * 0.9f, 0f))
                }
                .clip(RoundedCornerShape(17.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF101827), ReqPanel)))
                .border(1.dp, Color(0xFF31405C), RoundedCornerShape(17.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp),
            content = content
        )
    }
}

@Composable
private fun ReqInput(label: String, placeholder: String, modifier: Modifier = Modifier, minHeight: Dp = 52.dp) {
    var value by remember { mutableStateOf("") }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(label, color = Color(0xFF99A9A5), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
        BasicTextField(
            value = value,
            onValueChange = { value = it },
            textStyle = TextStyle(color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
            singleLine = minHeight <= 56.dp,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(minHeight)
                        .clip(RoundedCornerShape(13.dp))
                        .background(Brush.verticalGradient(listOf(Color(0xFF172033), ReqField)))
                        .border(1.dp, Color(0xFF42526F), RoundedCornerShape(13.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = if (minHeight > 56.dp) Alignment.TopStart else Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(placeholder, color = Color(0xFF768784), fontSize = 14.sp, lineHeight = 18.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
private fun ChipRowReq(labels: List<String>, selectedIndex: Int) {
    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        labels.forEachIndexed { index, label ->
            Box(
                modifier = Modifier
                    .height(38.dp)
                    .clip(RoundedCornerShape(19.dp))
                    .background(if (index == selectedIndex) ReqAccent else Color(0xFF1A2231))
                    .border(1.dp, if (index == selectedIndex) Color(0xFFDFFF6C) else Color(0xFF34405A), RoundedCornerShape(19.dp))
                    .padding(horizontal = 17.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(label, color = if (index == selectedIndex) Color(0xFF111604) else Color(0xFFBBC7C4), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun SelectBox(label: String, selected: Boolean, modifier: Modifier) {
    Box(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) ReqAccent else Color(0xFF1A2231))
            .border(1.dp, if (selected) Color(0xFFDFFF6C) else Color(0xFF34405A), RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (selected) Color(0xFF111604) else Color.White, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun InfoPanel(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF162010))
            .border(1.dp, Color(0x805C7E22), RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SmallBadge(active = true)
        Text(text, color = Color(0xFFD8E1DA), fontSize = 12.sp, lineHeight = 16.sp, modifier = Modifier.padding(start = 10.dp))
    }
}

@Composable
private fun ToggleLine(text: String, helper: String, active: Boolean) {
    Row(verticalAlignment = Alignment.Top) {
        SmallBadge(active = active)
        Column(modifier = Modifier.padding(start = 10.dp)) {
            Text(text, color = Color.White, fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.ExtraBold)
            Text(helper, color = ReqMuted, fontSize = 12.sp, lineHeight = 16.sp, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun SmallBadge(active: Boolean) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(if (active) ReqAccent else Color.Transparent)
            .border(1.dp, if (active) ReqAccent else Color(0xFF3B455B), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (active) CheckMark(Modifier.size(15.dp), Color(0xFF111604))
    }
}

@Composable
private fun SectionIcon(iconRes: Int?) {
    val pulse = rememberReqPremiumPulse(2400f)
    val glow = reqPremiumGlowAlpha(pulse)
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(Brush.radialGradient(listOf(Color(0x334DE9FF), Color(0xFF101827))))
            .border(1.dp, ReqCyan.copy(alpha = 0.34f + glow * 0.32f), RoundedCornerShape(11.dp))
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        if (iconRes == null) {
            SectionGlyph()
        } else {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun SectionGlyph() {
    Canvas(Modifier.fillMaxSize()) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        drawLine(ReqAccent, Offset(size.width * 0.2f, size.height * 0.28f), Offset(size.width * 0.8f, size.height * 0.28f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(ReqCyan, Offset(size.width * 0.2f, size.height * 0.5f), Offset(size.width * 0.7f, size.height * 0.5f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(ReqGold, Offset(size.width * 0.2f, size.height * 0.72f), Offset(size.width * 0.58f, size.height * 0.72f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}

@Composable
private fun CheckMark(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.18f, size.height * 0.54f), Offset(size.width * 0.42f, size.height * 0.76f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.42f, size.height * 0.76f), Offset(size.width * 0.84f, size.height * 0.24f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}

@Composable
private fun ReqArrowIcon(modifier: Modifier, right: Boolean) {
    Canvas(modifier) {
        val tint = Color(0xFFC7D2CF)
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        val path = Path().apply {
            if (right) {
                moveTo(size.width * 0.35f, size.height * 0.25f)
                lineTo(size.width * 0.62f, size.height * 0.5f)
                lineTo(size.width * 0.35f, size.height * 0.75f)
            } else {
                moveTo(size.width * 0.65f, size.height * 0.25f)
                lineTo(size.width * 0.38f, size.height * 0.5f)
                lineTo(size.width * 0.65f, size.height * 0.75f)
            }
        }
        drawPath(path, tint, style = stroke)
    }
}

@Composable
private fun rememberReqPremiumPulse(durationMillis: Float = 2800f): Float {
    var phase by remember { mutableStateOf(0f) }
    LaunchedEffect(durationMillis) {
        val start = withFrameNanos { it }
        while (true) {
            withFrameNanos { now ->
                val elapsed = (now - start) / 1_000_000f
                phase = (elapsed % durationMillis) / durationMillis
            }
        }
    }
    return phase
}

private fun reqPremiumGlowAlpha(phase: Float): Float {
    return (0.45f + 0.35f * sin((phase * 2f * PI).toFloat())).coerceIn(0.12f, 0.82f)
}
