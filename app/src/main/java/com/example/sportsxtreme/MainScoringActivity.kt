package com.example.sportsxtreme

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat

class MainScoringActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        setContent {
            MainScoringScreen(onBack = { finish() })
        }
    }
}

private val ScoringAccent = Color(0xFFC1FF00)
private val ScoringBg = Color(0xFF020A15)
private val ScoringPanel = Color(0xFF07101A)
private val ScoringCard = Color(0xFF0B1523)
private val ScoringStroke = Color(0xFF25314A)
private val ScoringMuted = Color(0xFFAAB6C4)
private val ScoringBlue = Color(0xFF00D2FF)

@Composable
private fun MainScoringScreen(onBack: () -> Unit) {
    var showActions by remember { mutableStateOf(false) }
    var wagonRun by remember { mutableStateOf<Int?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScoringBg)
            .drawBehind {
                drawCircle(Color(0x111B5BFF), radius = size.width * 0.64f, center = Offset(size.width * 0.58f, size.height * 0.18f))
                drawCircle(Color(0x111C3F14), radius = size.width * 0.62f, center = Offset(size.width * 0.24f, size.height * 0.72f))
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScoringTopBar(onBack)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 9.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                ScoreSummaryCard()
                BatterStatusCard("STRIKER", "Abhimanyu Majhi", "4x2 6x1", "22", "(18)", ScoringAccent)
                BatterStatusCard("NON-STRIKER", "Ashwini Gita", "4x1 6x0", "15", "(11)", ScoringStroke)
                BowlerStatusCard()
                CurrentOverCard()
                LastBallChip()
                RunPad(onRunClick = { wagonRun = it })
                Spacer(Modifier.height(if (showActions) 305.dp else if (wagonRun != null) 390.dp else 54.dp))
            }
        }
        MoreActionsBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            expanded = showActions,
            onToggle = { showActions = !showActions }
        )
        if (showActions) {
            AdvancedOptionsSheet(
                modifier = Modifier.align(Alignment.BottomCenter),
                onClose = { showActions = false }
            )
        }
        wagonRun?.let { runs ->
            WagonWheelBottomSheet(
                runs = runs,
                modifier = Modifier.align(Alignment.BottomCenter),
                onClose = { wagonRun = null }
            )
        }
    }
}

@Composable
private fun ScoringTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(66.dp)
            .background(ScoringPanel)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ScoreArrow(Modifier.size(23.dp).clickable(onClick = onBack), Color.White)
        Text(
            "Scoring : Dipesh\nWarrior 69",
            color = Color.White,
            fontSize = 15.sp,
            lineHeight = 17.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 10.dp).weight(1f)
        )
        ScoreHelpIcon(Modifier.size(20.dp), Color.White)
        ScoreGearIcon(Modifier.padding(start = 22.dp, end = 8.dp).size(20.dp), Color.White)
    }
}

@Composable
private fun ScoreSummaryCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(ScoringCard)
            .border(1.dp, Color(0xFF3E5B1B), RoundedCornerShape(10.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("45/2", color = ScoringAccent, fontSize = 34.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .height(22.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF31480F))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("INNINGS 2", color = ScoringAccent, fontSize = 8.sp, fontWeight = FontWeight.Black)
                }
                Text("CRR  6.25", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 9.dp))
            }
        }
        Row(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .height(35.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(Color(0xFF151E2E))
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Target: 98", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
            Text("Need 53 from 15 balls", color = ScoringAccent, fontSize = 10.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun BatterStatusCard(label: String, name: String, sub: String, runs: String, balls: String, accent: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(67.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(ScoringCard)
            .border(if (accent == ScoringStroke) 1.dp else 1.5.dp, accent, RoundedCornerShape(9.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = if (accent == ScoringStroke) ScoringMuted else accent, fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Text(name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 5.dp))
            Text(sub, color = ScoringMuted, fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 3.dp))
        }
        Column(horizontalAlignment = Alignment.End) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(runs, color = if (accent == ScoringStroke) Color.White else accent, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text(" $balls", color = if (accent == ScoringStroke) ScoringMuted else accent, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(bottom = 3.dp))
            }
            ChangeButton()
        }
    }
}

@Composable
private fun BowlerStatusCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(67.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(ScoringCard)
            .border(1.5.dp, ScoringBlue, RoundedCornerShape(9.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("BOWLER", color = ScoringBlue, fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Text("D. Kumar Sahoo", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 5.dp))
            Text("2.3 Overs • Econ 6.00", color = ScoringMuted, fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 3.dp))
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("2/15", color = ScoringBlue, fontSize = 21.sp, fontWeight = FontWeight.Black)
            ChangeButton()
        }
    }
}

@Composable
private fun ChangeButton() {
    Box(
        modifier = Modifier
            .height(24.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF2A3142))
            .padding(horizontal = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("CHANGE", color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun CurrentOverCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(ScoringCard)
            .padding(horizontal = 14.dp, vertical = 13.dp)
    ) {
        Text("CURRENT OVER", color = ScoringMuted, fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.4.sp)
        Row(
            modifier = Modifier.padding(top = 13.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("4", "0", "1", "2", "3", "6").forEachIndexed { index, value ->
                val color = when (index) {
                    0 -> ScoringBlue
                    5 -> ScoringAccent
                    else -> Color(0xFF222A3A)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(31.dp)
                            .clip(CircleShape)
                            .background(color.copy(alpha = if (index == 1 || index == 2 || index == 3 || index == 4) 1f else 0.22f))
                            .border(1.dp, color, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(value, color = if (index == 0) ScoringBlue else if (index == 5) ScoringAccent else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    }
                    Text("B${index + 1}", color = ScoringMuted, fontSize = 5.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 5.dp))
                }
            }
        }
    }
}

@Composable
private fun LastBallChip() {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Row(
            modifier = Modifier
                .height(30.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1B3017))
                .padding(horizontal = 17.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("LAST BALL:", color = ScoringMuted, fontSize = 7.sp, fontWeight = FontWeight.Black)
            Text("  6 Runs", color = ScoringAccent, fontSize = 10.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun RunPad(onRunClick: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        listOf(
            listOf("0", "1", "2"),
            listOf("3", "4\nFOUR", "6\nSIX"),
            listOf("WD", "NB", "BYE"),
            listOf("LB", "OUT", "UNDO")
        ).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { label ->
                    val run = label.substringBefore("\n").toIntOrNull()
                    ScoreKey(
                        label = label,
                        modifier = Modifier.weight(1f),
                        onClick = if (run != null && run in 0..6 && run != 5) {
                            { onRunClick(run) }
                        } else {
                            null
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ScoreKey(label: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    val isFour = label.startsWith("4")
    val isSix = label.startsWith("6")
    val isOut = label == "OUT"
    val border = when {
        isFour -> ScoringBlue
        isSix -> ScoringAccent
        isOut -> Color(0xFFFF7F7F)
        else -> Color.Transparent
    }
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(ScoringCard)
            .border(if (border == Color.Transparent) 0.dp else 1.dp, border, RoundedCornerShape(8.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        val parts = label.split("\n")
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(parts[0], color = when { isFour -> ScoringBlue; isSix -> ScoringAccent; isOut -> Color(0xFFFFB0B0); else -> Color.White }, fontSize = if (parts[0].length == 1) 20.sp else 11.sp, fontWeight = FontWeight.Black)
            if (parts.size > 1) Text(parts[1], color = if (isFour) ScoringBlue else ScoringAccent, fontSize = 6.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun WagonWheelBottomSheet(runs: Int, modifier: Modifier = Modifier, onClose: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(390.dp)
            .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
            .background(Color(0xFFF6F6F6))
            .padding(top = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text("X", color = Color(0xFF1B2024), fontSize = 22.sp, fontWeight = FontWeight.Medium, modifier = Modifier.clickable(onClick = onClose))
            Column(modifier = Modifier.padding(start = 18.dp).weight(1f)) {
                Text("Yy", color = Color(0xFFB5B5B5), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("$runs Runs", color = Color(0xFF008E55), fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
            Text("Skip Match", color = Color(0xFF5E5E5E), fontSize = 12.sp, textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline, modifier = Modifier.padding(end = 18.dp))
            Text("Skip Ball", color = Color(0xFF5E5E5E), fontSize = 12.sp, textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline)
        }
        WagonWheel(modifier = Modifier.size(330.dp))
    }
}

@Composable
private fun WagonWheel(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val radius = size.minDimension * 0.49f
            val center = Offset(size.width / 2f, size.height / 2f)

            drawCircle(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF617E8B), Color(0xFF0A161D), Color(0xFF000000)),
                    startY = center.y - radius,
                    endY = center.y + radius
                ),
                radius = radius,
                center = center
            )
            drawArc(Color(0xFF48B94C), 0f, 360f, true, topLeft = Offset(center.x - radius * 0.77f, center.y - radius * 0.77f), size = Size(radius * 1.54f, radius * 1.54f))
            drawCircle(Color(0x5535C846), radius = radius * 0.53f, center = center)
            drawCircle(Color.Transparent, radius = radius * 0.28f, center = center, style = Stroke(width = 1.dp.toPx()))

            repeat(8) { index ->
                val angle = Math.toRadians((index * 45 - 90).toDouble())
                val end = Offset(
                    (center.x + kotlin.math.cos(angle) * radius).toFloat(),
                    (center.y + kotlin.math.sin(angle) * radius).toFloat()
                )
                drawLine(Color(0xCFE5EEF0), center, end, strokeWidth = 1.dp.toPx())
            }
            drawCircle(Color(0x88DDE9E8), radius = radius * 0.72f, center = center, style = Stroke(width = 1.dp.toPx()))
            drawCircle(Color(0x88DDE9E8), radius = radius * 0.28f, center = center, style = Stroke(width = 1.dp.toPx()))
            drawLine(
                color = Color(0xFFFFD96A),
                start = center,
                end = Offset(center.x, center.y + radius * 0.16f),
                strokeWidth = 8.dp.toPx(),
                cap = StrokeCap.Square
            )
        }
        Text("Off", color = Color(0x3348D860), fontSize = 30.sp, fontWeight = FontWeight.Black, modifier = Modifier.offset(x = (-68).dp, y = 20.dp))
        Text("Leg", color = Color(0x3348D860), fontSize = 28.sp, fontWeight = FontWeight.Black, modifier = Modifier.offset(x = 72.dp, y = 20.dp))
        listOf(
            Triple((-54).dp, (-122).dp, "0"),
            Triple(54.dp, (-122).dp, "0"),
            Triple((-130).dp, (-44).dp, "0"),
            Triple(130.dp, (-44).dp, "0"),
            Triple((-130).dp, 62.dp, "0"),
            Triple(130.dp, 62.dp, "0"),
            Triple((-54).dp, 138.dp, "0"),
            Triple(54.dp, 138.dp, "0")
        ).forEach { (x, y, label) ->
            Text(label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.offset(x = x, y = y))
        }
    }
}

@Composable
private fun MoreActionsBar(modifier: Modifier = Modifier, expanded: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(35.dp)
            .background(Color(0xFF202838))
            .clickable(onClick = onToggle)
            .padding(horizontal = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("MORE ACTIONS", color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp, modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color(0xFF313A4C)),
            contentAlignment = Alignment.Center
        ) {
            Text(if (expanded) "v" else "^", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun AdvancedOptionsSheet(modifier: Modifier = Modifier, onClose: () -> Unit) {
    Column(
        modifier = modifier
            .padding(horizontal = 0.dp)
            .fillMaxWidth()
            .height(276.dp)
            .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
            .background(Color(0xFF091423))
            .border(1.dp, ScoringStroke, RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
            .padding(horizontal = 7.dp, vertical = 11.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(2.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFF596273))
        )
        Text("Advanced Match Options", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 14.dp).fillMaxWidth())
        Text("Rarely used tournament controls.", color = ScoringMuted, fontSize = 7.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 3.dp).fillMaxWidth())
        Column(modifier = Modifier.padding(top = 18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionOption("Change\nTarget", false, Modifier.weight(1f)) { ScoreTargetIcon(Modifier.size(17.dp), ScoringAccent) }
                ActionOption("Penalty Runs", false, Modifier.weight(1f)) { ScorePlusIcon(Modifier.size(17.dp), ScoringAccent) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionOption("D/L Method", false, Modifier.weight(1f)) { ScoreBoltIcon(Modifier.size(17.dp), ScoringAccent) }
                ActionOption("Match Break", false, Modifier.weight(1f)) { ScoreClockIcon(Modifier.size(17.dp), ScoringAccent) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionOption("Change\nScorer", false, Modifier.weight(1f)) { ScorePersonIcon(Modifier.size(17.dp), ScoringAccent) }
                ActionOption("Abandon\nMatch", true, Modifier.weight(1f)) { ScoreNoIcon(Modifier.size(17.dp), Color(0xFFFF5252)) }
            }
        }
        Box(
            modifier = Modifier
                .padding(top = 12.dp)
                .fillMaxWidth()
                .height(38.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF121E2F))
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center
        ) {
            Text("Close", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun ActionOption(label: String, danger: Boolean, modifier: Modifier = Modifier, icon: @Composable () -> Unit) {
    Row(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(if (danger) Color(0xFF241320) else Color(0xFF111B2B))
            .border(1.dp, if (danger) Color(0xFF4D2537) else Color.Transparent, RoundedCornerShape(7.dp))
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(29.dp)
                .clip(CircleShape)
                .background(if (danger) Color(0xFF3A1D2B) else Color(0xFF182539)),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Text(label, color = if (danger) Color(0xFFFF6B6B) else Color.White, fontSize = 7.5.sp, lineHeight = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 10.dp))
    }
}

@Composable
private fun ScoreArrow(modifier: Modifier, tint: Color) {
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
private fun ScoreHelpIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.38f, center = Offset(size.width * 0.5f, size.height * 0.5f), style = stroke)
        drawArc(tint, 210f, 230f, false, topLeft = Offset(size.width * 0.33f, size.height * 0.22f), size = Size(size.width * 0.34f, size.height * 0.34f), style = stroke)
        drawCircle(tint, radius = size.minDimension * 0.035f, center = Offset(size.width * 0.5f, size.height * 0.72f))
    }
}

@Composable
private fun ScoreGearIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.22f, center = Offset(size.width * 0.5f, size.height * 0.5f), style = stroke)
        repeat(8) { index ->
            val angle = Math.toRadians((index * 45).toDouble())
            val start = Offset((size.width * 0.5f + kotlin.math.cos(angle) * size.width * 0.31f).toFloat(), (size.height * 0.5f + kotlin.math.sin(angle) * size.height * 0.31f).toFloat())
            val end = Offset((size.width * 0.5f + kotlin.math.cos(angle) * size.width * 0.4f).toFloat(), (size.height * 0.5f + kotlin.math.sin(angle) * size.height * 0.4f).toFloat())
            drawLine(tint, start, end, strokeWidth = stroke.width, cap = StrokeCap.Round)
        }
    }
}

@Composable
private fun ScoreTargetIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        drawCircle(tint, radius = size.minDimension * 0.35f, center = Offset(size.width * 0.5f, size.height * 0.5f), style = Stroke(width = 1.5.dp.toPx()))
        drawCircle(tint, radius = size.minDimension * 0.16f, center = Offset(size.width * 0.5f, size.height * 0.5f), style = Stroke(width = 1.5.dp.toPx()))
    }
}

@Composable
private fun ScorePlusIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        drawCircle(tint, radius = size.minDimension * 0.34f, center = Offset(size.width * 0.5f, size.height * 0.5f), style = Stroke(width = 1.5.dp.toPx()))
        drawLine(tint, Offset(size.width * 0.5f, size.height * 0.32f), Offset(size.width * 0.5f, size.height * 0.68f), strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.32f, size.height * 0.5f), Offset(size.width * 0.68f, size.height * 0.5f), strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
    }
}

@Composable
private fun ScoreBoltIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val path = Path().apply {
            moveTo(size.width * 0.58f, size.height * 0.08f)
            lineTo(size.width * 0.28f, size.height * 0.54f)
            lineTo(size.width * 0.52f, size.height * 0.54f)
            lineTo(size.width * 0.42f, size.height * 0.92f)
            lineTo(size.width * 0.76f, size.height * 0.43f)
            lineTo(size.width * 0.52f, size.height * 0.43f)
            close()
        }
        drawPath(path, tint)
    }
}

@Composable
private fun ScoreClockIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.35f, center = Offset(size.width * 0.5f, size.height * 0.5f), style = stroke)
        drawLine(tint, Offset(size.width * 0.5f, size.height * 0.5f), Offset(size.width * 0.5f, size.height * 0.3f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.5f, size.height * 0.5f), Offset(size.width * 0.66f, size.height * 0.58f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}

@Composable
private fun ScorePersonIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.12f, center = Offset(size.width * 0.5f, size.height * 0.33f), style = stroke)
        drawArc(tint, 205f, 130f, false, topLeft = Offset(size.width * 0.24f, size.height * 0.52f), size = Size(size.width * 0.52f, size.height * 0.36f), style = stroke)
        drawLine(tint, Offset(size.width * 0.7f, size.height * 0.55f), Offset(size.width * 0.86f, size.height * 0.55f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}

@Composable
private fun ScoreNoIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.35f, center = Offset(size.width * 0.5f, size.height * 0.5f), style = stroke)
        drawLine(tint, Offset(size.width * 0.28f, size.height * 0.28f), Offset(size.width * 0.72f, size.height * 0.72f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}
