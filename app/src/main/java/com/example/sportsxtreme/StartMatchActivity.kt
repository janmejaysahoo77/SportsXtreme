package com.example.sportsxtreme

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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

class StartMatchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        setContent {
            StartMatchScreen(
                onBack = { finish() },
                onContinue = {
                    startActivity(Intent(this, LeagueTournamentFlowActivity::class.java))
                }
            )
        }
    }
}

private val MatchAccent = Color(0xFFC1FF00)
private val MatchBg = Color(0xFF010509)
private val MatchPanel = Color(0xFF07101A)
private val MatchCard = Color(0xFF0B1320)
private val MatchStroke = Color(0xFF1F2A3C)
private val MatchMuted = Color(0xFF8E9C9A)

@Composable
private fun StartMatchScreen(onBack: () -> Unit, onContinue: () -> Unit) {
    var selectedType by remember { mutableIntStateOf(0) }
    var selectedTournament by remember { mutableIntStateOf(0) }
    var selectedStage by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MatchBg)
            .drawBehind {
                repeat(11) { i ->
                    val y = size.height * (0.09f + i * 0.055f)
                    drawLine(
                        color = Color(0x0E9BB2BA),
                        start = Offset(0f, y),
                        end = Offset(size.width, y + size.width * 0.06f),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
    ) {
        StartMatchTopBar(onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 8.dp, end = 8.dp, top = 17.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            HeaderCopy()
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                MatchTypeCard("Tournament Match", "Part of a tournament or league competition.", MatchIcon.BAT, selectedType == 0) { selectedType = 0 }
                MatchTypeCard("Series Match", "Create a multi-match series between teams.", MatchIcon.BAT, selectedType == 1) { selectedType = 1 }
                MatchTypeCard("Friendly Match", "Casual, Practice & Quick Setup", MatchIcon.HANDSHAKE, selectedType == 2) { selectedType = 2 }
            }
            SectionTitle("SELECT TOURNAMENT", "SEE ALL")
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                TournamentCard(
                    title = "Dubai Premier League",
                    subtitle = "ACTIVE SEASON 2024",
                    imageRes = R.drawable.ground,
                    selected = selectedTournament == 0
                ) { selectedTournament = 0 }
                TournamentCard(
                    title = "KPL Knockout",
                    subtitle = "REGISTRATION OPEN",
                    imageRes = null,
                    selected = selectedTournament == 1
                ) { selectedTournament = 1 }
            }
            SectionTitle("SELECT TOURNAMENT STAGE")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StageCard(
                    title = "ROUND ROBIN\n(LEAGUE MATCHES)",
                    subtitle = "Every team plays against every other team.",
                    icon = MatchIcon.REFRESH,
                    selected = selectedStage == 0,
                    modifier = Modifier.weight(1f)
                ) { selectedStage = 0 }
                StageCard(
                    title = "KNOCKOUT",
                    subtitle = "Lose once and you're eliminated.",
                    icon = MatchIcon.TROPHY,
                    selected = selectedStage == 1,
                    modifier = Modifier.weight(1f)
                ) { selectedStage = 1 }
            }
            InfoNotice("Add tournament stages to enable rankings and points tables for this season.")
            Spacer(Modifier.height(1.dp))
            ContinueButton(onContinue)
        }
    }
}

@Composable
private fun StartMatchTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp)
            .background(MatchPanel)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MatchArrow(Modifier.size(23.dp).clickable(onClick = onBack), right = false, tint = MatchAccent)
        Text(
            "Start a Match",
            color = MatchAccent,
            fontSize = 17.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 17.dp).weight(1f),
            maxLines = 1
        )
        HelpIcon(Modifier.size(20.dp))
    }
}

@Composable
private fun HeaderCopy() {
    Column {
        Text("Choose Match Type", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
        Text(
            "Select how you want to organize and score this match.",
            color = MatchMuted,
            fontSize = 10.sp,
            lineHeight = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
private fun MatchTypeCard(title: String, subtitle: String, icon: MatchIcon, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(65.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Color(0xFF14200B) else MatchCard)
            .border(if (selected) 1.6.dp else 1.dp, if (selected) MatchAccent else MatchStroke, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconTile(icon, selected)
        Column(modifier = Modifier.padding(start = 13.dp).weight(1f)) {
            Text(title, color = if (selected) MatchAccent else Color(0xFFD5DEDA), fontSize = 12.sp, fontWeight = FontWeight.Black, maxLines = 1)
            Text(subtitle, color = MatchMuted, fontSize = 8.5.sp, lineHeight = 10.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 5.dp))
        }
        SelectionDot(selected)
    }
}

@Composable
private fun SectionTitle(title: String, action: String? = null) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = Color(0xFFBBC7C4), fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
        action?.let {
            Text(it, color = MatchAccent, fontSize = 8.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun TournamentCard(title: String, subtitle: String, imageRes: Int?, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Color(0xFF14200B) else MatchCard)
            .border(if (selected) 1.6.dp else 1.dp, if (selected) MatchAccent else MatchStroke, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(41.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF1A2432)),
            contentAlignment = Alignment.Center
        ) {
            if (imageRes != null) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                MatchVectorIcon(MatchIcon.TROPHY, Modifier.size(18.dp), Color(0xFF697584))
            }
        }
        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
            Text(title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, maxLines = 1)
            Text(subtitle, color = if (selected) MatchAccent else Color(0xFFB5C0BD), fontSize = 6.5.sp, fontWeight = FontWeight.Black, maxLines = 1)
        }
        SelectionDot(selected)
    }
}

@Composable
private fun StageCard(title: String, subtitle: String, icon: MatchIcon, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .height(126.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Color(0xFF111C10) else MatchCard)
            .border(if (selected) 1.6.dp else 1.dp, if (selected) MatchAccent else MatchStroke, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 13.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MatchVectorIcon(icon, Modifier.size(22.dp), if (selected) MatchAccent else Color(0xFF536173))
        Text(
            title,
            color = Color.White,
            fontSize = 7.3.sp,
            lineHeight = 9.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 13.dp)
        )
        Text(
            subtitle,
            color = Color(0xFFB8C4C0),
            fontSize = 6.4.sp,
            lineHeight = 8.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp).weight(1f)
        )
        SelectionDot(selected)
    }
}

@Composable
private fun InfoNotice(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MatchCard)
            .border(1.dp, MatchStroke, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        InfoCircle(Modifier.size(16.dp))
        Text(text, color = Color(0xFFAAB8B4), fontSize = 9.sp, lineHeight = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 9.dp))
    }
}

@Composable
private fun ContinueButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(51.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(MatchAccent)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("CONTINUE", color = Color(0xFF111604), fontSize = 10.sp, fontWeight = FontWeight.Black)
        MatchArrow(Modifier.padding(start = 12.dp).size(14.dp), right = true, tint = Color(0xFF111604))
    }
}

@Composable
private fun IconTile(icon: MatchIcon, active: Boolean) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (active) Color(0xFF1D2B17) else Color(0xFF172030))
            .border(1.dp, Color(0xFF253448), RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        MatchVectorIcon(icon, Modifier.size(20.dp), if (active) MatchAccent else Color(0xFF687586))
    }
}

@Composable
private fun SelectionDot(selected: Boolean) {
    Canvas(Modifier.size(17.dp)) {
        val stroke = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(if (selected) MatchAccent else Color.Transparent, radius = size.minDimension * 0.42f)
        drawCircle(if (selected) MatchAccent else Color(0xFF2F3B51), radius = size.minDimension * 0.42f, style = stroke)
        if (selected) {
            val path = Path().apply {
                moveTo(size.width * 0.31f, size.height * 0.5f)
                lineTo(size.width * 0.45f, size.height * 0.64f)
                lineTo(size.width * 0.71f, size.height * 0.36f)
            }
            drawPath(path, Color(0xFF111604), style = stroke)
        }
    }
}

@Composable
private fun HelpIcon(modifier: Modifier) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(Color(0xFFA7B5B1), radius = size.minDimension * 0.38f, style = stroke)
        drawLine(Color(0xFFA7B5B1), Offset(size.width * 0.5f, size.height * 0.62f), Offset(size.width * 0.5f, size.height * 0.63f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawArc(Color(0xFFA7B5B1), 205f, 235f, false, style = stroke)
    }
}

@Composable
private fun InfoCircle(modifier: Modifier) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(Color(0xFF5FA8FF), radius = size.minDimension * 0.42f, style = stroke)
        drawLine(Color(0xFF5FA8FF), Offset(size.width * 0.5f, size.height * 0.46f), Offset(size.width * 0.5f, size.height * 0.72f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawCircle(Color(0xFF5FA8FF), radius = size.minDimension * 0.04f, center = Offset(size.width * 0.5f, size.height * 0.3f))
    }
}

@Composable
private fun MatchArrow(modifier: Modifier, right: Boolean, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        val path = Path().apply {
            if (right) {
                moveTo(size.width * 0.36f, size.height * 0.26f)
                lineTo(size.width * 0.64f, size.height * 0.5f)
                lineTo(size.width * 0.36f, size.height * 0.74f)
            } else {
                moveTo(size.width * 0.64f, size.height * 0.26f)
                lineTo(size.width * 0.36f, size.height * 0.5f)
                lineTo(size.width * 0.64f, size.height * 0.74f)
            }
        }
        drawPath(path, tint, style = stroke)
    }
}

@Composable
private fun MatchVectorIcon(icon: MatchIcon, modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round)
        when (icon) {
            MatchIcon.BAT -> {
                drawLine(tint, Offset(size.width * 0.26f, size.height * 0.76f), Offset(size.width * 0.7f, size.height * 0.32f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawOval(tint, topLeft = Offset(size.width * 0.56f, size.height * 0.14f), size = androidx.compose.ui.geometry.Size(size.width * 0.28f, size.height * 0.31f), style = stroke)
                drawCircle(tint, radius = size.minDimension * 0.08f, center = Offset(size.width * 0.25f, size.height * 0.76f), style = stroke)
            }
            MatchIcon.HANDSHAKE -> {
                drawLine(tint, Offset(size.width * 0.18f, size.height * 0.45f), Offset(size.width * 0.43f, size.height * 0.62f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.82f, size.height * 0.45f), Offset(size.width * 0.57f, size.height * 0.62f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.38f, size.height * 0.5f), Offset(size.width * 0.62f, size.height * 0.5f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.43f, size.height * 0.62f), Offset(size.width * 0.5f, size.height * 0.7f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.57f, size.height * 0.62f), Offset(size.width * 0.5f, size.height * 0.7f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            MatchIcon.TROPHY -> {
                drawRoundRect(tint, topLeft = Offset(size.width * 0.28f, size.height * 0.18f), size = androidx.compose.ui.geometry.Size(size.width * 0.44f, size.height * 0.34f), style = stroke)
                drawLine(tint, Offset(size.width * 0.5f, size.height * 0.52f), Offset(size.width * 0.5f, size.height * 0.74f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.36f, size.height * 0.74f), Offset(size.width * 0.64f, size.height * 0.74f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            MatchIcon.REFRESH -> {
                drawArc(tint, 35f, 245f, false, style = stroke)
                drawArc(tint, 215f, 245f, false, style = stroke)
                val top = Path().apply {
                    moveTo(size.width * 0.66f, size.height * 0.2f)
                    lineTo(size.width * 0.8f, size.height * 0.2f)
                    lineTo(size.width * 0.78f, size.height * 0.35f)
                }
                val bottom = Path().apply {
                    moveTo(size.width * 0.34f, size.height * 0.8f)
                    lineTo(size.width * 0.2f, size.height * 0.8f)
                    lineTo(size.width * 0.22f, size.height * 0.65f)
                }
                drawPath(top, tint, style = stroke)
                drawPath(bottom, tint, style = stroke)
            }
        }
    }
}

private enum class MatchIcon {
    BAT,
    HANDSHAKE,
    TROPHY,
    REFRESH
}
