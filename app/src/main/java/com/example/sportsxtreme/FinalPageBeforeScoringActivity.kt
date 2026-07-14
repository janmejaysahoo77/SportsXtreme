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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat

class FinalPageBeforeScoringActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        setContent {
            FinalPageBeforeScoringScreen(
                onBack = { finish() },
                onOpenSelection = { startActivity(Intent(this, StartMatchPreviewActivity::class.java)) },
                onStartScoring = { startActivity(Intent(this, MainScoringActivity::class.java)) }
            )
        }
    }
}

private val InningsAccent = Color(0xFFC1FF00)
private val InningsBg = Color(0xFF020A15)
private val InningsPanel = Color(0xFF07101A)
private val InningsCard = Color(0xFF0B1523)
private val InningsStroke = Color(0xFF25314A)
private val InningsMuted = Color(0xFFAAB6C4)

@Composable
private fun FinalPageBeforeScoringScreen(onBack: () -> Unit, onOpenSelection: () -> Unit, onStartScoring: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(InningsBg)
            .drawBehind {
                drawCircle(Color(0x101B5BFF), radius = size.width * 0.62f, center = Offset(size.width * 0.62f, size.height * 0.2f))
                drawCircle(Color(0x151C3F14), radius = size.width * 0.6f, center = Offset(size.width * 0.3f, size.height * 0.48f))
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            InningsTopBar(onBack)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 4.dp, vertical = 22.dp)
            ) {
                InningsSectionTitle("BATTING - DIPESH WARRIOR 69")
                Row(
                    modifier = Modifier.padding(top = 15.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PlayerPickCard("SELECT STRIKER", selected = false, modifier = Modifier.weight(1f), onClick = onOpenSelection) {
                        BatterIcon(Modifier.size(86.dp), Color(0xFFE4F5C7))
                    }
                    PlayerPickCard("SELECT NON-STRIKER", selected = false, modifier = Modifier.weight(1f), onClick = onOpenSelection) {
                        NonStrikerIcon(Modifier.size(86.dp), Color(0xFF8FE5FF))
                    }
                }
                InningsSectionTitle("BOWLING - BHU", Modifier.padding(top = 33.dp))
                Row(modifier = Modifier.padding(top = 15.dp)) {
                    PlayerPickCard("SELECT BOWLER", selected = true, modifier = Modifier.width(166.dp), onClick = onOpenSelection) {
                        BowlerIcon(Modifier.size(92.dp), InningsAccent)
                    }
                    Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(150.dp))
            }
        }
        CameraBubble(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 91.dp)
        )
        BottomStartBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            onStartScoring = onStartScoring
        )
    }
}

@Composable
private fun InningsTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(66.dp)
            .background(InningsPanel)
            .padding(horizontal = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        InningsArrow(Modifier.size(24.dp).clickable(onClick = onBack), Color.White)
        Text(
            "START INNINGS",
            color = Color.White,
            fontSize = 20.sp,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 24.dp).weight(1f)
        )
        HelpIcon(Modifier.padding(end = 15.dp).size(24.dp), InningsMuted)
    }
}

@Composable
private fun InningsSectionTitle(text: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(InningsAccent)
        )
        Text(text, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 1.6.sp, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
private fun PlayerPickCard(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit, icon: @Composable () -> Unit) {
    Column(
        modifier = modifier
            .height(194.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(InningsCard)
            .border(if (selected) 1.5.dp else 1.dp, if (selected) InningsAccent else InningsStroke, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(132.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(Color(0xFF061A26))
                .drawBehind {
                    drawCircle(InningsAccent.copy(alpha = 0.18f), radius = size.width * 0.58f, center = Offset(size.width * 0.5f, size.height * 0.5f))
                },
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Text(
            label,
            color = if (selected) InningsAccent else Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
private fun CameraBubble(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(Color(0xFF252B3A))
            .border(1.dp, Color(0xFF596273), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        InningCameraIcon(Modifier.size(26.dp), Color.White)
    }
}

@Composable
private fun BottomStartBar(modifier: Modifier = Modifier, onStartScoring: () -> Unit) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(82.dp)
            .background(InningsPanel)
            .border(1.dp, Color(0xFF111E33))
            .padding(horizontal = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("MATCH RULES", color = InningsMuted, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp, modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .width(194.dp)
                .height(50.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(InningsAccent)
                .clickable(onClick = onStartScoring),
            contentAlignment = Alignment.Center
        ) {
            Text("START SCORING", color = Color(0xFF111604), fontSize = 15.sp, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun InningsArrow(modifier: Modifier, tint: Color) {
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
        drawCircle(tint, radius = size.minDimension * 0.4f, center = Offset(size.width * 0.5f, size.height * 0.5f), style = stroke)
        drawArc(tint, 210f, 230f, false, topLeft = Offset(size.width * 0.32f, size.height * 0.22f), size = Size(size.width * 0.34f, size.height * 0.34f), style = stroke)
        drawCircle(tint, radius = size.minDimension * 0.035f, center = Offset(size.width * 0.5f, size.height * 0.72f))
    }
}

@Composable
private fun InningCameraIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        drawRoundRect(tint, topLeft = Offset(size.width * 0.16f, size.height * 0.3f), size = Size(size.width * 0.68f, size.height * 0.5f), cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()), style = stroke)
        drawLine(tint, Offset(size.width * 0.36f, size.height * 0.3f), Offset(size.width * 0.43f, size.height * 0.18f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.43f, size.height * 0.18f), Offset(size.width * 0.6f, size.height * 0.18f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.6f, size.height * 0.18f), Offset(size.width * 0.67f, size.height * 0.3f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.12f, center = Offset(size.width * 0.5f, size.height * 0.55f), style = stroke)
    }
}

@Composable
private fun BatterIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        drawRect(Color(0xFF071627), topLeft = Offset.Zero, size = size)
        drawCircle(tint, radius = size.minDimension * 0.43f, center = Offset(size.width * 0.5f, size.height * 0.5f), style = stroke)
        drawCircle(tint, radius = size.minDimension * 0.06f, center = Offset(size.width * 0.5f, size.height * 0.28f), style = stroke)
        drawLine(tint, Offset(size.width * 0.48f, size.height * 0.36f), Offset(size.width * 0.4f, size.height * 0.58f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.4f, size.height * 0.58f), Offset(size.width * 0.29f, size.height * 0.75f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.41f, size.height * 0.58f), Offset(size.width * 0.62f, size.height * 0.72f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.48f, size.height * 0.42f), Offset(size.width * 0.67f, size.height * 0.53f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.62f, size.height * 0.28f), Offset(size.width * 0.8f, size.height * 0.48f), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
    }
}

@Composable
private fun NonStrikerIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        drawRect(Color(0xFF071627), topLeft = Offset.Zero, size = size)
        drawCircle(tint, radius = size.minDimension * 0.05f, center = Offset(size.width * 0.56f, size.height * 0.28f), style = stroke)
        drawLine(tint, Offset(size.width * 0.53f, size.height * 0.35f), Offset(size.width * 0.43f, size.height * 0.58f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.43f, size.height * 0.58f), Offset(size.width * 0.28f, size.height * 0.72f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.44f, size.height * 0.58f), Offset(size.width * 0.64f, size.height * 0.72f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.51f, size.height * 0.43f), Offset(size.width * 0.73f, size.height * 0.52f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.72f, size.height * 0.25f), Offset(size.width * 0.82f, size.height * 0.52f), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.025f, center = Offset(size.width * 0.34f, size.height * 0.75f))
        drawCircle(tint, radius = size.minDimension * 0.025f, center = Offset(size.width * 0.66f, size.height * 0.75f))
    }
}

@Composable
private fun BowlerIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        drawRect(Color(0xFFF6FFF2), topLeft = Offset(size.width * 0.1f, size.height * 0.1f), size = Size(size.width * 0.8f, size.height * 0.8f))
        drawRoundRect(Color(0xFF071627), topLeft = Offset(size.width * 0.22f, size.height * 0.22f), size = Size(size.width * 0.56f, size.height * 0.56f), cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()))
        drawCircle(tint, radius = size.minDimension * 0.05f, center = Offset(size.width * 0.58f, size.height * 0.3f), style = stroke)
        drawLine(tint, Offset(size.width * 0.55f, size.height * 0.38f), Offset(size.width * 0.43f, size.height * 0.6f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.43f, size.height * 0.6f), Offset(size.width * 0.31f, size.height * 0.75f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.43f, size.height * 0.6f), Offset(size.width * 0.62f, size.height * 0.73f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.55f, size.height * 0.42f), Offset(size.width * 0.73f, size.height * 0.26f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}
