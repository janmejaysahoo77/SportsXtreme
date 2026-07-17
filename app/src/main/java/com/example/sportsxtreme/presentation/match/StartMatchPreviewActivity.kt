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
import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.geometry.CornerRadius
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat

class StartMatchPreviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        setContent {
            StartMatchPreviewScreen(
                onBack = { finish() },
                onContinue = { startActivity(Intent(this, TossActivity::class.java)) }
            )
        }
    }
}

private val PreviewAccent = Color(0xFFC1FF00)
private val NeonYellow = Color(0xFFFFD600)
private val PreviewBg = Color(0xFF020A15)
private val PreviewPanel = Color(0xFF07101A)
private val PreviewCard = Color(0xFF0B1523)
private val PreviewStroke = Color(0xFF25314A)
private val PreviewMuted = Color(0xFFA9B5C4)
private val PreviewBlue = Color(0xFF70B9FF)

@Composable
private fun StartMatchPreviewScreen(onBack: () -> Unit, onContinue: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PreviewBg)
            .drawBehind {
                drawCircle(Color(0x101B5BFF), radius = size.width * 0.68f, center = Offset(size.width * 0.72f, size.height * 0.18f))
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            PreviewTopBar(onBack)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                MatchHeroCard()
                MatchInformation()
                MatchVenue()
                BallTypeSection()
                AdvancedSettingsCard()
                ScheduleMatchSection()
                Spacer(Modifier.height(100.dp))
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Transparent, PreviewBg, PreviewBg)))
                .padding(horizontal = 14.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .pureNeonGlow(PreviewAccent.copy(alpha = 0.8f), 24.dp, 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PreviewAccent)
                    .clickable(onClick = onContinue),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("CONTINUE TO TOSS", color = Color(0xFF111604), fontSize = 15.sp, fontWeight = FontWeight.Black)
                Text("  ->", color = Color(0xFF111604), fontSize = 16.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun PreviewTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(PreviewPanel)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PreviewArrow(Modifier.size(28.dp).clickable(onClick = onBack), Color.White)
        Text(
            "Start A Match",
            color = Color.White,
            fontSize = 22.sp,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 16.dp).weight(1f),
            maxLines = 1
        )
        PreviewInfoIcon(Modifier.size(22.dp), Color.White)
    }
}

@Composable
private fun MatchHeroCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .pureNeonGlow(NeonYellow.copy(alpha = 0.75f), 26.dp, 18.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF142238), PreviewCard),
                    radius = 440f,
                    center = Offset(180f, 80f)
                )
            )
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NeonYellow.copy(alpha = 0.22f),
                            NeonYellow.copy(alpha = 0.09f),
                            Color.Transparent
                        ),
                        center = Offset(size.width / 2f, size.height * 0.65f),
                        radius = size.width * 0.6f
                    )
                )
            }
            .padding(horizontal = 28.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .height(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF101B2A))
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("SUMMER CHAMPIONSHIP 2026", color = Color(0xFFE7D264), fontSize = 9.sp, fontWeight = FontWeight.Black)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeroTeam("DW", "Dipesh\nWarrior 69", "12 PLAYERS", Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .pureNeonGlow(PreviewAccent.copy(alpha = 1f), 32.dp, 0.dp, isCircle = true)
                    .clip(CircleShape)
                    .background(PreviewAccent),
                contentAlignment = Alignment.Center
            ) {
                Text("VS", color = Color(0xFF111604), fontSize = 16.sp, fontWeight = FontWeight.Black)
            }
            HeroTeam("BH", "Bhu", "12 PLAYERS", Modifier.weight(1f))
        }
    }
}

@Composable
private fun HeroTeam(initials: String, name: String, players: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color(0xFF111D35), Color(0xFF070D18)), radius = 100f)),
            contentAlignment = Alignment.Center
        ) {
            Text(initials, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black)
        }
        Text(
            name,
            color = Color.White,
            fontSize = 15.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 10.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Row(modifier = Modifier.padding(top = 5.dp), verticalAlignment = Alignment.CenterVertically) {
            PreviewPeopleIcon(Modifier.size(14.dp), PreviewMuted)
            Text(players, color = PreviewMuted, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 4.dp))
        }
    }
}

@Composable
private fun MatchInformation() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader("Match Information", "Configure match format and playing conditions.")
        InfoGrid()
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String? = null) {
    Column {
        Text(title, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Black)
        if (subtitle != null) {
            Text(subtitle, color = PreviewMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 5.dp))
        }
    }
}

@Composable
private fun InfoGrid() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .pureNeonGlow(NeonYellow.copy(alpha = 0.65f), 18.dp, 14.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(PreviewCard)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MatchInfoTile("Limited Overs", "FORMAT", selected = true, modifier = Modifier.weight(1f)) {
                PreviewBallIcon(Modifier.size(20.dp), PreviewAccent)
            }
            MatchInfoTile("Test Match", null, selected = false, modifier = Modifier.weight(1f)) {
                PreviewStumpsIcon(Modifier.size(19.dp), PreviewMuted)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MatchInfoTile("20", "TOTAL OVERS", selected = false, modifier = Modifier.weight(1f))
            MatchInfoTile("4", "OVERS/BOWLER", selected = false, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun MatchInfoTile(title: String, label: String?, selected: Boolean, modifier: Modifier = Modifier, icon: @Composable (() -> Unit)? = null) {
    Column(
        modifier = modifier
            .height(88.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Color(0xFF172513) else Color(0xFF111B2B))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            icon()
            Spacer(Modifier.height(8.dp))
        } else if (label != null) {
            Text(label, color = PreviewMuted, fontSize = 9.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(6.dp))
        }
        Text(title, color = Color.White, fontSize = if (title.length <= 3) 24.sp else 12.sp, fontWeight = FontWeight.Black, maxLines = 1)
    }
}

@Composable
private fun MatchVenue() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PreviewPinIcon(Modifier.size(16.dp), Color(0xFFFF5F9E))
            Text(" Match Venue", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .pureNeonGlow(NeonYellow.copy(alpha = 0.65f), 18.dp, 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(PreviewCard)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            VenueField("CITY / TOWN", "Bhubaneswar") { PreviewPinIcon(Modifier.size(18.dp), PreviewAccent) }
            VenueField("GROUND NAME", "IIT Bhubaneswar") { PreviewGroundIcon(Modifier.size(18.dp), PreviewAccent) }
        }
    }
}

@Composable
private fun VenueField(label: String, value: String, icon: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(66.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF111B2B))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Column(modifier = Modifier.padding(start = 14.dp)) {
            Text(label, color = PreviewMuted, fontSize = 10.sp, fontWeight = FontWeight.Black)
            Text(value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black, maxLines = 1)
        }
    }
}

@Composable
private fun BallTypeSection() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Ball Type", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pureNeonGlow(NeonYellow.copy(alpha = 0.65f), 18.dp, 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(PreviewCard)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BallTypeCard("TENNIS", R.drawable.tennisball, selected = true, modifier = Modifier.weight(1f))
            BallTypeCard("LEATHER", R.drawable.leatherball, selected = false, modifier = Modifier.weight(1f))
            BallTypeCard("OTHER", R.drawable.otherball, selected = false, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun BallTypeCard(title: String, imageRes: Int, selected: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .height(92.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Color(0xFF172513) else Color(0xFF111B2B)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .paint(painterResource(id = imageRes), contentScale = ContentScale.Fit)
        )
        Text(title, color = if (selected) PreviewAccent else PreviewMuted, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 6.dp))
    }
}

@Composable
private fun AdvancedSettingsCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .pureNeonGlow(NeonYellow.copy(alpha = 0.65f), 18.dp, 14.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(PreviewCard)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PreviewGearIcon(Modifier.size(26.dp), PreviewAccent)
        Column(modifier = Modifier.padding(start = 16.dp).weight(1f)) {
            Text("Advanced Settings", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Black)
            Text("Pitch, Powerplays, Officials & Streaming", color = PreviewMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 5.dp))
        }
        Text("v", color = PreviewMuted, fontSize = 20.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun ScheduleMatchSection() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PreviewCalendarIcon(Modifier.size(16.dp), PreviewAccent)
            Text(" Schedule Match", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
        }
        Text("For matches not starting immediately", color = PreviewMuted, fontSize = 10.sp, fontWeight = FontWeight.Medium)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .pureNeonGlow(NeonYellow.copy(alpha = 0.65f), 18.dp, 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(PreviewCard)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ScheduleField("MATCH DATE", "03 June 2026") { PreviewCalendarIcon(Modifier.size(20.dp), PreviewAccent) }
            ScheduleField("MATCH TIME", "08:00 PM") { PreviewClockIcon(Modifier.size(20.dp), PreviewAccent) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF102019)),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PreviewCheckCircle(Modifier.size(18.dp), PreviewAccent)
                Text(" CONFIRM SCHEDULE", color = PreviewAccent, fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun ScheduleField(label: String, value: String, icon: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF111B2B))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Column(modifier = Modifier.padding(start = 14.dp).weight(1f)) {
            Text(label, color = PreviewMuted, fontSize = 9.sp, fontWeight = FontWeight.Black)
            Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)
        }
        Text(">", color = PreviewMuted, fontSize = 18.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun PreviewArrow(modifier: Modifier, tint: Color) {
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
private fun PreviewInfoIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.38f, style = stroke)
        drawLine(tint, Offset(size.width * 0.5f, size.height * 0.45f), Offset(size.width * 0.5f, size.height * 0.68f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.035f, center = Offset(size.width * 0.5f, size.height * 0.3f))
    }
}

@Composable
private fun PreviewPeopleIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.13f, center = Offset(size.width * 0.4f, size.height * 0.36f), style = stroke)
        drawCircle(tint, radius = size.minDimension * 0.1f, center = Offset(size.width * 0.68f, size.height * 0.39f), style = stroke)
        drawArc(tint, 205f, 130f, false, topLeft = Offset(size.width * 0.2f, size.height * 0.54f), size = Size(size.width * 0.42f, size.height * 0.3f), style = stroke)
    }
}

@Composable
private fun PreviewBallIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        drawCircle(tint, radius = size.minDimension * 0.34f, center = Offset(size.width * 0.5f, size.height * 0.5f))
    }
}

@Composable
private fun PreviewStumpsIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        drawRoundRect(tint, topLeft = Offset(size.width * 0.24f, size.height * 0.18f), size = Size(size.width * 0.12f, size.height * 0.62f), cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx()), style = stroke)
        drawRoundRect(tint, topLeft = Offset(size.width * 0.44f, size.height * 0.18f), size = Size(size.width * 0.12f, size.height * 0.62f), cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx()), style = stroke)
        drawRoundRect(tint, topLeft = Offset(size.width * 0.64f, size.height * 0.18f), size = Size(size.width * 0.12f, size.height * 0.62f), cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx()), style = stroke)
    }
}

@Composable
private fun PreviewPinIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.12f, center = Offset(size.width * 0.5f, size.height * 0.42f), style = stroke)
        val path = Path().apply {
            moveTo(size.width * 0.5f, size.height * 0.88f)
            cubicTo(size.width * 0.18f, size.height * 0.55f, size.width * 0.26f, size.height * 0.15f, size.width * 0.5f, size.height * 0.15f)
            cubicTo(size.width * 0.74f, size.height * 0.15f, size.width * 0.82f, size.height * 0.55f, size.width * 0.5f, size.height * 0.88f)
        }
        drawPath(path, tint, style = stroke)
    }
}

@Composable
private fun PreviewGroundIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round)
        drawRoundRect(tint, topLeft = Offset(size.width * 0.15f, size.height * 0.28f), size = Size(size.width * 0.7f, size.height * 0.52f), cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()), style = stroke)
        drawLine(tint, Offset(size.width * 0.15f, size.height * 0.45f), Offset(size.width * 0.85f, size.height * 0.45f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.07f, center = Offset(size.width * 0.5f, size.height * 0.58f), style = stroke)
    }
}

@Composable
private fun PreviewCricketBall(modifier: Modifier, color: Color) {
    Canvas(modifier) {
        drawCircle(color, radius = size.minDimension * 0.42f, center = Offset(size.width * 0.5f, size.height * 0.5f))
        drawArc(Color.White.copy(alpha = 0.55f), 72f, 215f, false, topLeft = Offset(size.width * 0.28f, size.height * 0.18f), size = Size(size.width * 0.38f, size.height * 0.64f), style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round))
    }
}

@Composable
private fun PreviewGearIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.23f, center = Offset(size.width * 0.5f, size.height * 0.5f), style = stroke)
        repeat(8) { index ->
            val angle = Math.toRadians((index * 45).toDouble())
            val start = Offset((size.width * 0.5f + kotlin.math.cos(angle) * size.width * 0.31f).toFloat(), (size.height * 0.5f + kotlin.math.sin(angle) * size.height * 0.31f).toFloat())
            val end = Offset((size.width * 0.5f + kotlin.math.cos(angle) * size.width * 0.4f).toFloat(), (size.height * 0.5f + kotlin.math.sin(angle) * size.height * 0.4f).toFloat())
            drawLine(tint, start, end, strokeWidth = stroke.width, cap = StrokeCap.Round)
        }
    }
}

@Composable
private fun PreviewCalendarIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        drawRoundRect(tint, topLeft = Offset(size.width * 0.18f, size.height * 0.22f), size = Size(size.width * 0.64f, size.height * 0.58f), cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()), style = stroke)
        drawLine(tint, Offset(size.width * 0.18f, size.height * 0.4f), Offset(size.width * 0.82f, size.height * 0.4f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.36f, size.height * 0.16f), Offset(size.width * 0.36f, size.height * 0.28f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.64f, size.height * 0.16f), Offset(size.width * 0.64f, size.height * 0.28f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}

@Composable
private fun PreviewClockIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.35f, center = Offset(size.width * 0.5f, size.height * 0.5f), style = stroke)
        drawLine(tint, Offset(size.width * 0.5f, size.height * 0.5f), Offset(size.width * 0.5f, size.height * 0.32f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.5f, size.height * 0.5f), Offset(size.width * 0.66f, size.height * 0.58f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}

@Composable
private fun PreviewCheckCircle(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.34f, center = Offset(size.width * 0.5f, size.height * 0.5f), style = stroke)
        val path = Path().apply {
            moveTo(size.width * 0.32f, size.height * 0.52f)
            lineTo(size.width * 0.45f, size.height * 0.64f)
            lineTo(size.width * 0.7f, size.height * 0.38f)
        }
        drawPath(path, tint, style = stroke)
    }
}

fun Modifier.pureNeonGlow(color: Color, radius: androidx.compose.ui.unit.Dp = 16.dp, cornerRadius: androidx.compose.ui.unit.Dp = 12.dp, isCircle: Boolean = false) = this.drawBehind {
    if (color == Color.Transparent) return@drawBehind
    val paint = Paint().apply {
        this.color = color
        this.asFrameworkPaint().apply {
            maskFilter = android.graphics.BlurMaskFilter(radius.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
        }
    }
    drawIntoCanvas { canvas ->
        if (isCircle) {
            canvas.drawCircle(center, size.width / 2f, paint)
        } else {
            canvas.drawRoundRect(0f, 0f, size.width, size.height, cornerRadius.toPx(), cornerRadius.toPx(), paint)
        }
    }
}
