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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat

class TossActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        setContent {
            TossScreen(
                onBack = { finish() },
                onPlay = { startActivity(Intent(this, FinalPageBeforeScoringActivity::class.java)) }
            )
        }
    }
}

private val TossAccent = Color(0xFFC1FF00)
private val TossBg = Color(0xFF020A15)
private val TossPanel = Color(0xFF07101A)
private val TossCard = Color(0xFF0B1523)
private val TossStroke = Color(0xFF25314A)
private val TossMuted = Color(0xFFAAB6C4)

@Composable
private fun TossScreen(onBack: () -> Unit, onPlay: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TossBg)
            .drawBehind {
                drawCircle(Color(0x141B5BFF), radius = size.width * 0.6f, center = Offset(size.width * 0.55f, size.height * 0.45f))
                drawCircle(Color(0x171C3F14), radius = size.width * 0.58f, center = Offset(size.width * 0.22f, size.height * 0.24f))
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TossTopBar(onBack)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 18.dp)
            ) {
                TossSectionTitle("WHO WON THE TOSS?")
                Row(
                    modifier = Modifier.padding(top = 13.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    TossTeamCard("DW", "Dipesh Warrior\n69", selected = true, color = Color(0xFF009B86), modifier = Modifier.weight(1f))
                    TossTeamCard("BH", "Bhu", selected = false, color = Color(0xFF2476F2), modifier = Modifier.weight(1f))
                }
                TossSectionTitle("WINNER OF THE TOSS ELECTED TO?", Modifier.padding(top = 29.dp))
                Row(
                    modifier = Modifier.padding(top = 13.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    TossChoiceCard("Bat", selected = false, modifier = Modifier.weight(1f)) {
                        TossBatIcon(Modifier.size(52.dp))
                    }
                    TossChoiceCard("Bowl", selected = true, modifier = Modifier.weight(1f)) {
                        TossBallIcon(Modifier.size(52.dp))
                    }
                }
                Text(
                    "TAP THE COIN TO FLIP",
                    color = TossMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .padding(top = 56.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Box(
                    modifier = Modifier
                        .padding(top = 26.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TossCoin(Modifier.size(188.dp))
                }
                Spacer(Modifier.height(118.dp))
            }
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(TossBg)
                .border(1.dp, Color(0xFF28344A))
                .padding(horizontal = 8.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Need help?", color = TossMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .width(137.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(TossAccent)
                    .clickable(onClick = onPlay),
                contentAlignment = Alignment.Center
            ) {
                Text("Let's play", color = Color(0xFF111604), fontSize = 15.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun TossTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(TossPanel)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TossArrow(Modifier.size(24.dp).clickable(onClick = onBack), Color.White)
        Text("Toss", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 16.dp).weight(1f))
        TossCameraIcon(Modifier.padding(end = 15.dp).size(22.dp), TossMuted)
    }
}

@Composable
private fun TossSectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, color = TossMuted, fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp, modifier = modifier)
}

@Composable
private fun TossTeamCard(initials: String, name: String, selected: Boolean, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .height(135.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Color(0xFF111D17) else TossCard)
            .border(if (selected) 1.5.dp else 1.dp, if (selected) TossAccent else TossStroke, RoundedCornerShape(10.dp))
            .drawBehind {
                if (selected) drawCircle(TossAccent.copy(alpha = 0.22f), radius = size.width * 0.42f, center = Offset(size.width * 0.5f, size.height * 0.5f))
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(initials, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Black)
        }
        Text(name, color = Color.White, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 13.dp))
    }
}

@Composable
private fun TossChoiceCard(title: String, selected: Boolean, modifier: Modifier = Modifier, icon: @Composable () -> Unit) {
    Column(
        modifier = modifier
            .height(118.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(TossCard)
            .border(if (selected) 1.5.dp else 1.dp, if (selected) TossAccent else TossStroke, RoundedCornerShape(10.dp))
            .drawBehind {
                if (selected) drawCircle(TossAccent.copy(alpha = 0.22f), radius = size.width * 0.42f, center = Offset(size.width * 0.5f, size.height * 0.44f))
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        icon()
        Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun TossCoin(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(2.dp))
            .background(Color(0xFF031323)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(134.dp)) {
            drawCircle(Color(0xFFB9C6C1), radius = size.minDimension * 0.49f, center = Offset(size.width * 0.5f, size.height * 0.5f))
            drawCircle(Color(0xFF131B1E), radius = size.minDimension * 0.42f, center = Offset(size.width * 0.5f, size.height * 0.5f))
            drawCircle(
                Brush.radialGradient(listOf(Color(0xFFE9F2E8), Color(0xFF616D68), Color(0xFF1B2427)), radius = size.minDimension * 0.42f),
                radius = size.minDimension * 0.36f,
                center = Offset(size.width * 0.5f, size.height * 0.5f)
            )
            drawArc(TossAccent, 15f, 145f, false, topLeft = Offset(size.width * 0.11f, size.height * 0.18f), size = Size(size.width * 0.78f, size.height * 0.62f), style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        }
        Text("HEADS", color = TossAccent, fontSize = 24.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun TossArrow(modifier: Modifier, tint: Color) {
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
private fun TossCameraIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round)
        drawRoundRect(tint, topLeft = Offset(size.width * 0.16f, size.height * 0.3f), size = Size(size.width * 0.68f, size.height * 0.5f), cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()), style = stroke)
        drawLine(tint, Offset(size.width * 0.36f, size.height * 0.3f), Offset(size.width * 0.43f, size.height * 0.18f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.43f, size.height * 0.18f), Offset(size.width * 0.6f, size.height * 0.18f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.6f, size.height * 0.18f), Offset(size.width * 0.67f, size.height * 0.3f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.12f, center = Offset(size.width * 0.5f, size.height * 0.55f), style = stroke)
    }
}

@Composable
private fun TossBatIcon(modifier: Modifier) {
    Canvas(modifier) {
        drawCircle(Brush.radialGradient(listOf(Color(0xFF182A45), Color(0xFF06101B)), radius = size.minDimension * 0.5f), radius = size.minDimension * 0.48f)
        drawLine(Color(0xFFB6FF2B), Offset(size.width * 0.25f, size.height * 0.7f), Offset(size.width * 0.72f, size.height * 0.28f), strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
        drawLine(Color(0xFF0F2014), Offset(size.width * 0.3f, size.height * 0.75f), Offset(size.width * 0.76f, size.height * 0.32f), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
    }
}

@Composable
private fun TossBallIcon(modifier: Modifier) {
    Canvas(modifier) {
        drawCircle(Brush.radialGradient(listOf(Color(0xFF132541), Color(0xFF06101B)), radius = size.minDimension * 0.5f), radius = size.minDimension * 0.48f)
        drawCircle(Color(0xFF8D1F23), radius = size.minDimension * 0.28f, center = Offset(size.width * 0.5f, size.height * 0.5f))
        drawArc(TossAccent, 70f, 210f, false, topLeft = Offset(size.width * 0.34f, size.height * 0.22f), size = Size(size.width * 0.31f, size.height * 0.56f), style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round))
    }
}
