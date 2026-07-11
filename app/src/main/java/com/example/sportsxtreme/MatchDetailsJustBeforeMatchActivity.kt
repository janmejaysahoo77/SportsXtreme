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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MatchDetailsJustBeforeMatchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applySportsXtremeWindowStyle()
        setContent {
            MatchDetailsJustBeforeMatchScreen(
                onBack = { finish() },
                onContinue = { startActivity(Intent(this, TossActivity::class.java)) }
            )
        }
    }
}

private val MatchAccent = Color(0xFFC1FF00)
private val MatchBg = Color(0xFF020A15)
private val MatchPanel = Color(0xFF07101A)
private val MatchCard = Color(0xFF0B1523)
private val MatchStroke = Color(0xFF25314A)
private val MatchMuted = Color(0xFFAEB9C0)

@Composable
private fun MatchDetailsJustBeforeMatchScreen(onBack: () -> Unit, onContinue: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MatchBg)
            .drawBehind {
                drawCircle(Color(0x101B5BFF), radius = size.width * 0.6f, center = Offset(size.width * 0.5f, size.height * 0.2f))
                drawCircle(Color(0x151C3F14), radius = size.width * 0.55f, center = Offset(size.width * 0.5f, size.height * 0.48f))
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            MatchTopBar(onBack)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            ) {
                MatchSummaryCard()
                MatchInformation()
                MatchVenue()
                BallTypeSection()
                AdvancedSettingsCard(Modifier.padding(top = 16.dp))
                ScheduleMatchSection()
                Spacer(Modifier.height(90.dp))
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(MatchBg)
                .padding(horizontal = 8.dp, vertical = 11.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MatchAccent)
                    .clickable(onClick = onContinue),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("CONTINUE TO TOSS", color = Color(0xFF111604), fontSize = 13.sp, fontWeight = FontWeight.Black)
                MatchRightArrow(Modifier.padding(start = 9.dp).size(16.dp), Color(0xFF111604))
            }
        }
    }
}

@Composable
private fun MatchTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(MatchPanel)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MatchBackArrow(Modifier.size(23.dp).clickable(onClick = onBack), Color.White)
        Text(
            "Start A Match",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 13.dp).weight(1f),
            maxLines = 1
        )
        HelpCircle(Modifier.size(20.dp), MatchMuted)
    }
}

@Composable
private fun MatchSummaryCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(155.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MatchCard)
            .border(1.dp, Color(0xFF3E531D), RoundedCornerShape(12.dp))
            .drawBehind {
                drawCircle(Color(0x221B5BFF), radius = size.width * 0.28f, center = Offset(size.width * 0.23f, size.height * 0.52f))
                drawCircle(Color(0x221B5BFF), radius = size.width * 0.28f, center = Offset(size.width * 0.78f, size.height * 0.52f))
                drawCircle(Color(0x331C3F14), radius = size.width * 0.38f, center = Offset(size.width * 0.5f, size.height * 0.55f))
            }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .height(18.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(Color(0xFF162414))
                .border(1.dp, Color(0xFF3E531D), RoundedCornerShape(9.dp))
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("SUMMER CHAMPIONSHIP 2026", color = MatchAccent, fontSize = 8.sp, fontWeight = FontWeight.Black)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TeamBadge("DW", "Dinabandhu\nWarrior 69", "15 PLAYERS", Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(MatchAccent),
                contentAlignment = Alignment.Center
            ) {
                Text("VS", color = Color(0xFF111604), fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
            TeamBadge("BH", "Bihu", "15 PLAYERS", Modifier.weight(1f))
        }
    }
}

@Composable
private fun TeamBadge(initials: String, name: String, players: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFF111826))
                .border(1.dp, Color(0xFF4053A8), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(initials, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
        }
        Text(name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, lineHeight = 13.sp, modifier = Modifier.padding(top = 8.dp))
        Text(players, color = MatchMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 3.dp))
    }
}

@Composable
private fun MatchInformation() {
    Column(modifier = Modifier.padding(top = 20.dp)) {
        Text("Match Information", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Black)
        Text("Configure match format and playing conditions.", color = MatchMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
        BorderedSection(Modifier.padding(top = 12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                MatchTypeCard("Limited Overs", "POPULAR", true, Modifier.weight(1f))
                MatchTypeCard("Test Match", "", false, Modifier.weight(1f))
            }
            Row(modifier = Modifier.padding(top = 9.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                StatInput("TOTAL OVERS", "20", Modifier.weight(1f))
                StatInput("OVERS/BOWLER", "4", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MatchTypeCard(title: String, tag: String, selected: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .height(62.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF111826))
            .border(1.dp, if (selected) MatchAccent else MatchStroke, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SmallIconDot(selected)
            Spacer(Modifier.weight(1f))
            if (tag.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .background(MatchAccent)
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(tag, color = Color(0xFF111604), fontSize = 6.sp, fontWeight = FontWeight.Black)
                }
            }
        }
        Text(title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 9.dp))
    }
}

@Composable
private fun StatInput(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF111826))
            .border(1.dp, MatchStroke, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(label, color = MatchMuted, fontSize = 7.sp, fontWeight = FontWeight.Black)
        Text(value, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun MatchVenue() {
    Column(modifier = Modifier.padding(top = 20.dp)) {
        Text("Match Venue", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Black)
        BorderedSection(Modifier.padding(top = 12.dp)) {
            VenueField("CITY / TOWN", "Bhubaneswar")
            VenueField("GROUND NAME", "IIT Bhubaneswar", Modifier.padding(top = 9.dp))
        }
    }
}

@Composable
private fun VenueField(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF111826))
            .border(1.dp, MatchStroke, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PinIcon(Modifier.size(16.dp), MatchAccent)
        Column(modifier = Modifier.padding(start = 10.dp).weight(1f)) {
            Text(label, color = MatchMuted, fontSize = 7.sp, fontWeight = FontWeight.Black)
            Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun BallTypeSection() {
    Column(modifier = Modifier.padding(top = 20.dp)) {
        Text("Ball Type", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Black)
        BorderedSection(Modifier.padding(top = 12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BallTypeCard("TENNIS", Color(0xFFBFFF00), true, Modifier.weight(1f))
                BallTypeCard("LEATHER", Color(0xFF551818), false, Modifier.weight(1f))
                BallTypeCard("OTHER", Color(0xFFB85B1E), false, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun BallTypeCard(label: String, ballColor: Color, selected: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .height(72.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF111826))
            .border(1.dp, if (selected) MatchAccent else MatchStroke, RoundedCornerShape(8.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Canvas(Modifier.size(27.dp)) {
            drawCircle(ballColor, radius = size.minDimension * 0.42f, center = Offset(size.width * 0.5f, size.height * 0.5f))
            drawArc(Color.White.copy(alpha = 0.35f), 120f, 120f, false, topLeft = Offset(size.width * 0.18f, size.height * 0.18f), size = Size(size.width * 0.64f, size.height * 0.64f), style = Stroke(width = 1.2.dp.toPx()))
        }
        Text(label, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 7.dp))
    }
}

@Composable
private fun AdvancedSettingsCard(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MatchCard)
            .border(1.dp, Color(0xFF3E531D), RoundedCornerShape(10.dp))
            .padding(horizontal = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GearIcon(Modifier.size(18.dp), MatchAccent)
        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
            Text("Advanced Settings", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)
            Text("DRS, Powerplay, Officials & Streaming", color = MatchMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        }
        DownArrow(Modifier.size(16.dp), MatchMuted)
    }
}

@Composable
private fun ScheduleMatchSection() {
    Column(modifier = Modifier.padding(top = 20.dp)) {
        Text("Schedule Match", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Black)
        Text("For matches not starting immediately", color = MatchMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
        BorderedSection(Modifier.padding(top = 12.dp)) {
            ScheduleField("MATCH DATE", "03 June 2026")
            ScheduleField("MATCH TIME", "08:00 PM", Modifier.padding(top = 9.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .height(37.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF111826)),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SmallIconDot(true)
                Text("CONFIRM SCHEDULE", color = MatchAccent, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
private fun ScheduleField(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF111826))
            .border(1.dp, MatchStroke, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CalendarIcon(Modifier.size(18.dp), MatchAccent)
        Column(modifier = Modifier.padding(start = 11.dp).weight(1f)) {
            Text(label, color = MatchMuted, fontSize = 7.sp, fontWeight = FontWeight.Black)
            Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
        }
        MatchRightArrow(Modifier.size(14.dp), MatchMuted)
    }
}

@Composable
private fun BorderedSection(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF07101A))
            .border(1.dp, Color(0xFF3E531D), RoundedCornerShape(12.dp))
            .padding(10.dp),
        content = content
    )
}

@Composable
private fun SmallIconDot(selected: Boolean) {
    Box(
        modifier = Modifier
            .size(14.dp)
            .clip(CircleShape)
            .background(if (selected) MatchAccent else Color(0xFF26354B)),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Box(Modifier.size(5.dp).clip(CircleShape).background(Color(0xFF111604)))
        }
    }
}

@Composable
private fun MatchBackArrow(modifier: Modifier, tint: Color) {
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
private fun MatchRightArrow(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.2f, size.height * 0.5f), Offset(size.width * 0.78f, size.height * 0.5f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        val path = Path().apply {
            moveTo(size.width * 0.58f, size.height * 0.3f)
            lineTo(size.width * 0.78f, size.height * 0.5f)
            lineTo(size.width * 0.58f, size.height * 0.7f)
        }
        drawPath(path, tint, style = stroke)
    }
}

@Composable
private fun HelpCircle(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        drawCircle(tint, radius = size.minDimension * 0.35f, center = Offset(size.width * 0.5f, size.height * 0.5f), style = Stroke(width = 1.5.dp.toPx()))
        drawCircle(tint, radius = size.minDimension * 0.035f, center = Offset(size.width * 0.5f, size.height * 0.72f))
        drawLine(tint, Offset(size.width * 0.5f, size.height * 0.56f), Offset(size.width * 0.5f, size.height * 0.42f), strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
    }
}

@Composable
private fun PinIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.28f, center = Offset(size.width * 0.5f, size.height * 0.38f), style = stroke)
        drawCircle(tint, radius = size.minDimension * 0.08f, center = Offset(size.width * 0.5f, size.height * 0.38f))
        drawLine(tint, Offset(size.width * 0.5f, size.height * 0.66f), Offset(size.width * 0.5f, size.height * 0.88f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}

@Composable
private fun GearIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.22f, center = Offset(size.width * 0.5f, size.height * 0.5f), style = stroke)
        drawCircle(tint, radius = size.minDimension * 0.07f, center = Offset(size.width * 0.5f, size.height * 0.5f))
        repeat(6) { index ->
            val angle = Math.toRadians((index * 60).toDouble())
            val start = Offset((size.width * 0.5f + kotlin.math.cos(angle).toFloat() * size.width * 0.31f), (size.height * 0.5f + kotlin.math.sin(angle).toFloat() * size.height * 0.31f))
            val end = Offset((size.width * 0.5f + kotlin.math.cos(angle).toFloat() * size.width * 0.43f), (size.height * 0.5f + kotlin.math.sin(angle).toFloat() * size.height * 0.43f))
            drawLine(tint, start, end, strokeWidth = stroke.width, cap = StrokeCap.Round)
        }
    }
}

@Composable
private fun DownArrow(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.28f, size.height * 0.4f), Offset(size.width * 0.5f, size.height * 0.62f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.72f, size.height * 0.4f), Offset(size.width * 0.5f, size.height * 0.62f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}

@Composable
private fun CalendarIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        drawRoundRect(tint, topLeft = Offset(size.width * 0.2f, size.height * 0.25f), size = Size(size.width * 0.6f, size.height * 0.55f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx()), style = stroke)
        drawLine(tint, Offset(size.width * 0.2f, size.height * 0.42f), Offset(size.width * 0.8f, size.height * 0.42f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.35f, size.height * 0.18f), Offset(size.width * 0.35f, size.height * 0.32f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.65f, size.height * 0.18f), Offset(size.width * 0.65f, size.height * 0.32f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}
