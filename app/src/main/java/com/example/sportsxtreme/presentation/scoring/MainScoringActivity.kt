package com.example.sportsxtreme.presentation.scoring

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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
    var showAdvanced by remember { mutableStateOf(false) }
    var showWagonWheel by remember { mutableStateOf(false) }
    var wagonRun by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScoringBg)
            .drawBehind {
                drawCircle(Color(0x111B5BFF), radius = size.width * 0.64f, center = Offset(size.width * 0.58f, size.height * 0.18f))
                drawCircle(Color(0x111C3F14), radius = size.width * 0.62f, center = Offset(size.width * 0.24f, size.height * 0.72f))
            }
    ) {
        // ── Main content – single screen, no scroll ──
        Column(modifier = Modifier.fillMaxSize()) {
            ScoringTopBar(onBack)
            Spacer(Modifier.weight(0.5f))
            // Score summary
            ScoreSummaryCard(modifier = Modifier.padding(horizontal = 10.dp))
            Spacer(Modifier.weight(1f))
            // Batters + Bowler in a vertical list
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BatterMini("STRIKER", "Abhimanyu Majhi", "22", "(18)", ScoringAccent, Modifier)
                BatterMini("NON-STR", "Ashwini Gita", "15", "(11)", ScoringStroke, Modifier)
                BowlerMini(Modifier)
            }
            Spacer(Modifier.weight(2f))
            // Current over
            CurrentOverCard(modifier = Modifier.padding(horizontal = 10.dp))
            Spacer(Modifier.height(8.dp))
            // Run pad
            RunPad(
                modifier = Modifier.padding(horizontal = 10.dp).padding(bottom = 4.dp),
                onRunClick = { run ->
                    wagonRun = run
                    showWagonWheel = true
                }
            )
            // More actions bar
            MoreActionsBar(
                expanded = showAdvanced,
                onToggle = { showAdvanced = !showAdvanced }
            )
        }

        // ── Scrim + Advanced bottom sheet ──
        if (showAdvanced) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x88000000))
                    .clickable { showAdvanced = false }
            )
            AdvancedOptionsSheet(
                modifier = Modifier.align(Alignment.BottomCenter),
                onClose = { showAdvanced = false }
            )
        }

        // ── Wagon Wheel bottom sheet ──
        if (showWagonWheel) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x88000000))
                    .clickable { showWagonWheel = false }
            )
            WagonWheelBottomSheet(
                runs = wagonRun,
                modifier = Modifier.align(Alignment.BottomCenter),
                onClose = { showWagonWheel = false }
            )
        }
    }
}

// ── Top Bar ──────────────────────────────────────────────
@Composable
private fun ScoringTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(ScoringPanel)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ScoreArrow(Modifier.size(22.dp).clickable(onClick = onBack), Color.White)
        Column(modifier = Modifier.padding(start = 10.dp).weight(1f)) {
            Text("Scoring", color = ScoringMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text("Dipesh Warrior 69", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black, maxLines = 1)
        }
        ScoreHelpIcon(Modifier.size(20.dp), Color.White)
        ScoreGearIcon(Modifier.padding(start = 20.dp).size(20.dp), Color.White)
    }
}

// ── Score Summary ─────────────────────────────────────────
@Composable
private fun ScoreSummaryCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(ScoringCard)
            .border(1.dp, Color(0xFF3E5B1B), RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("45/2", color = ScoringAccent, fontSize = 32.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .height(20.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF31480F))
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("INNINGS 2", color = ScoringAccent, fontSize = 8.sp, fontWeight = FontWeight.Black)
                }
                Text("CRR  6.25", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 6.dp))
            }
        }
        Row(
            modifier = Modifier
                .padding(top = 6.dp)
                .fillMaxWidth()
                .height(30.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF151E2E))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Target: 98", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
            Text("Need 53 from 15 balls", color = ScoringAccent, fontSize = 10.sp, fontWeight = FontWeight.Black)
        }
    }
}

// ── Compact Batter / Bowler cards ────────────────────────
@Composable
private fun BatterMini(label: String, name: String, runs: String, balls: String, accent: Color, modifier: Modifier) {
    val isStriker = accent != ScoringStroke
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(78.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(ScoringCard)
            .border(if (isStriker) 1.5.dp else 1.dp, if (isStriker) accent else ScoringStroke, RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = if (isStriker) accent else ScoringMuted, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 0.8.sp)
            Text(name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 2.dp))
            Text(if (isStriker) "4s: 2  6s: 1" else "4s: 1  6s: 0", color = ScoringMuted, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 2.dp))
        }
        Column(horizontalAlignment = Alignment.End) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(runs, color = if (isStriker) accent else Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                Text(" $balls", color = if (isStriker) accent.copy(alpha = 0.7f) else ScoringMuted, fontSize = 12.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(bottom = 4.dp))
            }
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF1E2A3A))
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("CHANGE", color = ScoringMuted, fontSize = 7.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun BowlerMini(modifier: Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(78.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(ScoringCard)
            .border(1.5.dp, ScoringBlue, RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("BOWLER", color = ScoringBlue, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 0.8.sp)
            Text("D. Kumar Sahoo", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 2.dp))
            Text("2.3 Ov  Econ 6.0", color = ScoringMuted, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 2.dp))
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("2/15", color = ScoringBlue, fontSize = 28.sp, fontWeight = FontWeight.Black)
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF1E2A3A))
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("CHANGE", color = ScoringMuted, fontSize = 7.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

// ── Current Over ──────────────────────────────────────────
@Composable
private fun CurrentOverCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(ScoringCard)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("CURRENT OVER", color = ScoringMuted, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.4.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("LAST BALL: ", color = ScoringMuted, fontSize = 7.sp, fontWeight = FontWeight.Black)
                Text("6 Runs", color = ScoringAccent, fontSize = 9.sp, fontWeight = FontWeight.Black)
            }
        }
        Row(
            modifier = Modifier.padding(top = 10.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("4" to ScoringBlue, "0" to null, "1" to null, "2" to null, "3" to null, "6" to ScoringAccent).forEachIndexed { index, (value, col) ->
                val color = col ?: Color(0xFF222A3A)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (col != null) color.copy(alpha = 0.2f) else Color(0xFF161E2C))
                            .border(1.dp, color, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(value, color = if (col != null) color else Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                    }
                    Text("B${index + 1}", color = ScoringMuted, fontSize = 7.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

// ── Run Pad ───────────────────────────────────────────────
@Composable
private fun RunPad(modifier: Modifier = Modifier, onRunClick: (Int) -> Unit) {
    val rows = listOf(
        listOf("0", "1", "2"),
        listOf("3", "4\nFOUR", "6\nSIX"),
        listOf("WD", "NB", "BYE"),
        listOf("LB", "OUT", "UNDO")
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        rows.forEach { rowLabels ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowLabels.forEach { label ->
                    val run = label.substringBefore("\n").toIntOrNull()
                    ScoreKey(
                        label = label,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        onClick = if (run != null && run in 0..6 && run != 5) {
                            { onRunClick(run) }
                        } else null
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
    val borderCol = when {
        isFour -> ScoringBlue
        isSix -> ScoringAccent
        isOut -> Color(0xFFFF7F7F)
        else -> Color.Transparent
    }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.94f else 1f, label = "scale")

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isFour -> Color(0xFF0D1E2E)
                    isSix -> Color(0xFF162010)
                    isOut -> Color(0xFF1F1015)
                    else -> ScoringCard
                }
            )
            .border(if (borderCol == Color.Transparent) 0.dp else 1.dp, borderCol, RoundedCornerShape(8.dp))
            .then(if (onClick != null) Modifier.clickable(interactionSource = interactionSource, indication = null, onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        val parts = label.split("\n")
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                parts[0],
                color = when {
                    isFour -> ScoringBlue
                    isSix -> ScoringAccent
                    isOut -> Color(0xFFFFB0B0)
                    else -> Color.White
                },
                fontSize = if (parts[0].length == 1) 18.sp else 11.sp,
                fontWeight = FontWeight.Black
            )
            if (parts.size > 1) Text(parts[1], color = if (isFour) ScoringBlue else ScoringAccent, fontSize = 7.sp, fontWeight = FontWeight.Black)
        }
    }
}

// ── More Actions Bar ──────────────────────────────────────
@Composable
private fun MoreActionsBar(expanded: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp)
            .background(Color(0xFF131D2D))
            .border(1.dp, ScoringStroke)
            .clickable(onClick = onToggle)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(Modifier.size(14.dp)) {
            val s = Stroke(1.5.dp.toPx(), cap = StrokeCap.Round)
            drawLine(Color(0xFFC1FF00), Offset(0f, size.height * 0.3f), Offset(size.width, size.height * 0.3f), s.width)
            drawLine(Color(0xFFC1FF00), Offset(size.width * 0.25f, size.height * 0.65f), Offset(size.width, size.height * 0.65f), s.width)
            drawLine(Color(0xFFC1FF00), Offset(size.width * 0.5f, size.height), Offset(size.width, size.height), s.width)
        }
        Text(
            "MORE ACTIONS",
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(start = 10.dp).weight(1f)
        )
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color(0xFF273040)),
            contentAlignment = Alignment.Center
        ) {
            Text(if (expanded) "∨" else "∧", color = ScoringAccent, fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
    }
}

// ── Advanced Options Sheet ────────────────────────────────
@Composable
private fun AdvancedOptionsSheet(modifier: Modifier = Modifier, onClose: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(Color(0xFF0C1828))
            .border(1.dp, ScoringStroke, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(horizontal = 14.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Drag handle
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFF3F5068))
        )
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Advanced Match Options", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black)
                Text("Rarely used tournament controls.", color = ScoringMuted, fontSize = 9.sp, modifier = Modifier.padding(top = 2.dp))
            }
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E2D42))
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Text("✕", color = ScoringMuted, fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
        }
        Spacer(Modifier.height(16.dp))
        // Options grid
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionOption("Change\nTarget", false, Modifier.weight(1f)) { ScoreTargetIcon(Modifier.size(18.dp), ScoringAccent) }
                ActionOption("Penalty Runs", false, Modifier.weight(1f)) { ScorePlusIcon(Modifier.size(18.dp), ScoringAccent) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionOption("D/L Method", false, Modifier.weight(1f)) { ScoreBoltIcon(Modifier.size(18.dp), ScoringAccent) }
                ActionOption("Match Break", false, Modifier.weight(1f)) { ScoreClockIcon(Modifier.size(18.dp), ScoringAccent) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionOption("Change\nScorer", false, Modifier.weight(1f)) { ScorePersonIcon(Modifier.size(18.dp), ScoringAccent) }
                ActionOption("Abandon\nMatch", true, Modifier.weight(1f)) { ScoreNoIcon(Modifier.size(18.dp), Color(0xFFFF5252)) }
            }
        }
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF172236))
                .border(1.dp, ScoringStroke, RoundedCornerShape(10.dp))
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center
        ) {
            Text("Close", color = ScoringMuted, fontSize = 13.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun ActionOption(label: String, danger: Boolean, modifier: Modifier = Modifier, icon: @Composable () -> Unit) {
    Row(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(if (danger) Color(0xFF241320) else Color(0xFF111B2B))
            .border(1.dp, if (danger) Color(0xFF5A2035) else ScoringStroke, RoundedCornerShape(9.dp))
            .clickable { }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (danger) Color(0xFF3E1E2D) else Color(0xFF182539)),
            contentAlignment = Alignment.Center
        ) { icon() }
        Text(
            label,
            color = if (danger) Color(0xFFFF6B6B) else Color.White,
            fontSize = 10.sp,
            lineHeight = 13.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 10.dp)
        )
    }
}

// ── Wagon Wheel Sheet ─────────────────────────────────────
@Composable
private fun WagonWheelBottomSheet(runs: Int, modifier: Modifier = Modifier, onClose: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(380.dp)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(Color(0xFFF2F4F6))
            .padding(top = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFFB0B8C2))
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("✕", color = Color(0xFF1B2024), fontSize = 20.sp, fontWeight = FontWeight.Medium, modifier = Modifier.clickable(onClick = onClose))
            Column(modifier = Modifier.padding(start = 14.dp).weight(1f)) {
                Text("Select shot direction", color = Color(0xFF888F99), fontSize = 11.sp)
                Text("$runs Run${if (runs != 1) "s" else ""}", color = Color(0xFF008E55), fontSize = 14.sp, fontWeight = FontWeight.Black)
            }
            Text("Skip Ball", color = Color(0xFF5E5E5E), fontSize = 12.sp, textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline)
        }
        WagonWheel(modifier = Modifier.size(300.dp).padding(bottom = 8.dp))
    }
}

@Composable
private fun WagonWheel(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val radius = size.minDimension * 0.49f
            val center = Offset(size.width / 2f, size.height / 2f)
            drawCircle(Brush.verticalGradient(listOf(Color(0xFF617E8B), Color(0xFF0A161D), Color(0xFF000000)), startY = center.y - radius, endY = center.y + radius), radius = radius, center = center)
            drawArc(Color(0xFF48B94C), 0f, 360f, true, topLeft = Offset(center.x - radius * 0.77f, center.y - radius * 0.77f), size = Size(radius * 1.54f, radius * 1.54f))
            drawCircle(Color(0x5535C846), radius = radius * 0.53f, center = center)
            repeat(8) { i ->
                val angle = Math.toRadians((i * 45 - 90).toDouble())
                drawLine(Color(0xCFE5EEF0), center, Offset((center.x + kotlin.math.cos(angle) * radius).toFloat(), (center.y + kotlin.math.sin(angle) * radius).toFloat()), strokeWidth = 1.dp.toPx())
            }
            drawCircle(Color(0x88DDE9E8), radius = radius * 0.72f, center = center, style = Stroke(width = 1.dp.toPx()))
            drawCircle(Color(0x88DDE9E8), radius = radius * 0.28f, center = center, style = Stroke(width = 1.dp.toPx()))
            drawLine(Color(0xFFFFD96A), center, Offset(center.x, center.y + radius * 0.16f), strokeWidth = 8.dp.toPx(), cap = StrokeCap.Square)
        }
        Text("Off", color = Color(0x3348D860), fontSize = 26.sp, fontWeight = FontWeight.Black, modifier = Modifier.offset(x = (-58).dp, y = 18.dp))
        Text("Leg", color = Color(0x3348D860), fontSize = 26.sp, fontWeight = FontWeight.Black, modifier = Modifier.offset(x = 62.dp, y = 18.dp))
    }
}

// ── Canvas Icons ──────────────────────────────────────────
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
