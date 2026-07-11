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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class StartInnings : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applySportsXtremeWindowStyle()
        setContent {
            StartInningsScreen(
                onBack = { finish() },
                onSelectPlayer = { startActivity(Intent(this, PlayersListFinalActivity::class.java)) },
                onStartScoring = { finish() }
            )
        }
    }
}

private val InningsAccent = Color(0xFFC1FF00)
private val InningsBg = Color(0xFF020A15)
private val InningsPanel = Color(0xFF07101A)
private val InningsCard = Color(0xFF0B1523)
private val InningsStroke = Color(0xFF25314A)
private val InningsMuted = Color(0xFFAEB9C0)

@Composable
private fun StartInningsScreen(onBack: () -> Unit, onSelectPlayer: () -> Unit, onStartScoring: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(InningsBg)
            .drawBehind {
                drawCircle(Color(0x101B5BFF), radius = size.width * 0.56f, center = Offset(size.width * 0.52f, size.height * 0.22f))
                drawCircle(Color(0x141C3F14), radius = size.width * 0.5f, center = Offset(size.width * 0.48f, size.height * 0.52f))
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            InningsTopBar(onBack)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 22.dp)
            ) {
                SectionTitle("BATTING - DIPESH WARRIOR 69")
                Row(
                    modifier = Modifier.padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    PlayerSelectCard(
                        label = "SELECT STRIKER",
                        selected = false,
                        onClick = onSelectPlayer,
                        icon = { CricketBatterIcon(Modifier.size(78.dp), InningsAccent) },
                        modifier = Modifier.weight(1f)
                    )
                    PlayerSelectCard(
                        label = "SELECT NON-STRIKER",
                        selected = false,
                        onClick = onSelectPlayer,
                        icon = { CricProIcon(Modifier.size(82.dp), Color(0xFF7FE8FF)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                SectionTitle(
                    text = "BOWLING - BHU",
                    modifier = Modifier.padding(top = 31.dp)
                )
                PlayerSelectCard(
                    label = "SELECT BOWLER",
                    selected = true,
                    onClick = onSelectPlayer,
                    icon = { BowlerIconTile(Modifier.size(93.dp)) },
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(0.48f)
                )
            }
            Spacer(Modifier.weight(1f))
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 14.dp, bottom = 92.dp)
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFF1A2231))
                .border(1.dp, Color(0xFF4A5260), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            InningsCameraIcon(Modifier.size(26.dp), Color.White)
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(InningsPanel)
                .padding(horizontal = 12.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "MATCH RULES",
                color = InningsMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.63f)
                    .height(51.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(InningsAccent)
                    .clickable(onClick = onStartScoring),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "START SCORING",
                    color = Color(0xFF111604),
                    fontSize = 15.sp,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun InningsTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .background(Color(0xFF050B16))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        InningsBackArrow(Modifier.size(25.dp).clickable(onClick = onBack), Color.White)
        Text(
            "START INNINGS",
            color = Color.White,
            fontSize = 22.sp,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 26.dp).weight(1f),
            maxLines = 1
        )
        HelpIcon(Modifier.size(24.dp), InningsMuted)
    }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 16.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(InningsAccent)
        )
        Text(
            text,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 9.dp)
        )
    }
}

@Composable
private fun PlayerSelectCard(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(196.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(InningsCard)
            .border(1.dp, if (selected) InningsAccent else InningsStroke, RoundedCornerShape(7.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(7.dp))
                .background(Color(0xFF06172A))
                .drawBehind {
                    drawCircle(InningsAccent.copy(alpha = 0.12f), radius = size.width * 0.5f, center = Offset(size.width * 0.5f, size.height * 0.5f))
                },
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Text(
            label,
            color = if (selected) InningsAccent else Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.padding(top = 14.dp)
        )
    }
}

@Composable
private fun BowlerIconTile(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(2.dp))
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF071A31)),
            contentAlignment = Alignment.Center
        ) {
            CricketBatterIcon(Modifier.size(48.dp), InningsAccent)
        }
    }
}

@Composable
private fun CricketBatterIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.09f, center = Offset(size.width * 0.47f, size.height * 0.17f), style = stroke)
        drawLine(tint, Offset(size.width * 0.45f, size.height * 0.27f), Offset(size.width * 0.36f, size.height * 0.52f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.42f, size.height * 0.34f), Offset(size.width * 0.63f, size.height * 0.46f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.36f, size.height * 0.52f), Offset(size.width * 0.24f, size.height * 0.78f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.37f, size.height * 0.53f), Offset(size.width * 0.58f, size.height * 0.78f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.62f, size.height * 0.44f), Offset(size.width * 0.78f, size.height * 0.18f), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
    }
}

@Composable
private fun CricProIcon(modifier: Modifier, tint: Color) {
    Column(
        modifier = modifier
            .background(Color(0xFF071A31)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Canvas(Modifier.size(56.dp)) {
            val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
            drawCircle(tint, radius = size.minDimension * 0.08f, center = Offset(size.width * 0.58f, size.height * 0.14f), style = stroke)
            drawLine(tint, Offset(size.width * 0.55f, size.height * 0.24f), Offset(size.width * 0.47f, size.height * 0.52f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            drawLine(tint, Offset(size.width * 0.48f, size.height * 0.34f), Offset(size.width * 0.25f, size.height * 0.47f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            drawLine(tint, Offset(size.width * 0.55f, size.height * 0.3f), Offset(size.width * 0.78f, size.height * 0.4f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            drawLine(tint, Offset(size.width * 0.47f, size.height * 0.52f), Offset(size.width * 0.3f, size.height * 0.78f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            drawLine(tint, Offset(size.width * 0.48f, size.height * 0.52f), Offset(size.width * 0.66f, size.height * 0.78f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        }
        Text("CRIC-PRO", color = tint, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun InningsBackArrow(modifier: Modifier, tint: Color) {
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
private fun HelpIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.38f, center = Offset(size.width * 0.5f, size.height * 0.5f), style = stroke)
        drawCircle(tint, radius = size.minDimension * 0.035f, center = Offset(size.width * 0.5f, size.height * 0.72f))
        drawLine(tint, Offset(size.width * 0.5f, size.height * 0.56f), Offset(size.width * 0.5f, size.height * 0.42f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}

@Composable
private fun InningsCameraIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        drawRoundRect(tint, topLeft = Offset(size.width * 0.18f, size.height * 0.32f), size = Size(size.width * 0.64f, size.height * 0.46f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx()), style = stroke)
        drawCircle(tint, radius = size.minDimension * 0.14f, center = Offset(size.width * 0.5f, size.height * 0.55f), style = stroke)
        drawLine(tint, Offset(size.width * 0.34f, size.height * 0.32f), Offset(size.width * 0.42f, size.height * 0.22f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.42f, size.height * 0.22f), Offset(size.width * 0.6f, size.height * 0.22f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.6f, size.height * 0.22f), Offset(size.width * 0.66f, size.height * 0.32f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}
