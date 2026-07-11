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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class TossActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applySportsXtremeWindowStyle()
        setContent {
            TossScreen(
                onBack = { finish() },
                onPlay = { startActivity(Intent(this, StartInnings::class.java)) }
            )
        }
    }
}

private val TossAccent = Color(0xFFC1FF00)
private val TossBg = Color(0xFF020A15)
private val TossCard = Color(0xFF0B1523)
private val TossStroke = Color(0xFF25314A)
private val TossMuted = Color(0xFFAEB9C0)

@Composable
private fun TossScreen(onBack: () -> Unit, onPlay: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TossBg)
            .drawBehind {
                drawCircle(Color(0x101B5BFF), radius = size.width * 0.55f, center = Offset(size.width * 0.5f, size.height * 0.25f))
                drawCircle(Color(0x141C3F14), radius = size.width * 0.52f, center = Offset(size.width * 0.5f, size.height * 0.58f))
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
        ) {
            TossTopBar(onBack)
            Text(
                "WHO WON THE TOSS?",
                color = TossMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 27.dp)
            )
            Row(
                modifier = Modifier.padding(top = 15.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                TossTeamCard("DW", "Dipesh Warrior\n69", true, Modifier.weight(1f))
                TossTeamCard("BH", "Bhu", false, Modifier.weight(1f))
            }
            Text(
                "WINNER OF THE TOSS ELECTED TO?",
                color = TossMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 29.dp)
            )
            Row(
                modifier = Modifier.padding(top = 15.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                TossDecisionCard("Bat", bat = true, selected = false, Modifier.weight(1f))
                TossDecisionCard("Bowl", bat = false, selected = true, Modifier.weight(1f))
            }
            Text(
                "TAP THE COIN TO FLIP",
                color = TossMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                modifier = Modifier
                    .padding(top = 58.dp)
                    .align(Alignment.CenterHorizontally)
            )
            TossCoin(Modifier.padding(top = 30.dp).align(Alignment.CenterHorizontally))
            Spacer(Modifier.weight(1f))
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(TossBg)
                .padding(horizontal = 8.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Need help?", color = TossMuted, fontSize = 12.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.49f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(TossAccent)
                    .clickable(onClick = onPlay),
                contentAlignment = Alignment.Center
            ) {
                Text("Let's play", color = Color(0xFF111604), fontSize = 17.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun TossTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TossBackArrow(Modifier.size(26.dp).clickable(onClick = onBack), Color.White)
        Text(
            "Toss",
            color = Color.White,
            fontSize = 19.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 25.dp).weight(1f)
        )
        CameraIcon(Modifier.size(23.dp), TossMuted)
    }
}

@Composable
private fun TossTeamCard(initials: String, name: String, selected: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .height(135.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(TossCard)
            .border(1.dp, if (selected) TossAccent else TossStroke, RoundedCornerShape(10.dp))
            .drawBehind {
                if (selected) {
                    drawCircle(TossAccent.copy(alpha = 0.12f), radius = size.width * 0.45f, center = Offset(size.width * 0.5f, size.height * 0.48f))
                }
            }
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(55.dp)
                .clip(CircleShape)
                .background(if (initials == "DW") Color(0xFF2BB7B4) else Color(0xFF2E78FF)),
            contentAlignment = Alignment.Center
        ) {
            Text(initials, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
        }
        Text(
            name,
            color = Color.White,
            fontSize = 13.sp,
            lineHeight = 17.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 14.dp)
        )
    }
}

@Composable
private fun TossDecisionCard(label: String, bat: Boolean, selected: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .height(118.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(TossCard)
            .border(1.dp, if (selected) TossAccent else TossStroke, RoundedCornerShape(10.dp))
            .drawBehind {
                if (selected) {
                    drawCircle(TossAccent.copy(alpha = 0.13f), radius = size.width * 0.36f, center = Offset(size.width * 0.5f, size.height * 0.43f))
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFF061A2F))
                .border(1.dp, Color(0xFF273E73), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (bat) {
                BatIcon(Modifier.size(38.dp), TossAccent)
            } else {
                BallIcon(Modifier.size(38.dp), Color(0xFFB52A2A))
            }
        }
        Text(label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 9.dp))
    }
}

@Composable
private fun TossCoin(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(184.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF06172A))
            .drawBehind {
                drawCircle(Color(0x22007FFF), radius = size.width * 0.48f, center = Offset(size.width * 0.5f, size.height * 0.5f))
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(128.dp)) {
            val center = Offset(size.width * 0.5f, size.height * 0.5f)
            drawCircle(Color(0xFF3D4949), radius = size.minDimension * 0.49f, center = center)
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xFFE7ECE5), Color(0xFF78867D), Color(0xFF15201F)),
                    center = center,
                    radius = size.minDimension * 0.48f
                ),
                radius = size.minDimension * 0.43f,
                center = center
            )
            drawCircle(Color(0xFF121C1B), radius = size.minDimension * 0.31f, center = center, style = Stroke(width = 5.dp.toPx()))
            drawCircle(TossAccent.copy(alpha = 0.5f), radius = size.minDimension * 0.34f, center = center, style = Stroke(width = 1.4.dp.toPx()))
            drawLine(Color.White.copy(alpha = 0.45f), Offset(size.width * 0.2f, size.height * 0.38f), Offset(size.width * 0.8f, size.height * 0.22f), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
        }
        Text("HEADS", color = TossAccent, fontSize = 28.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun TossBackArrow(modifier: Modifier, tint: Color) {
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
private fun CameraIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round)
        drawRoundRect(tint, topLeft = Offset(size.width * 0.18f, size.height * 0.3f), size = Size(size.width * 0.64f, size.height * 0.48f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx()), style = stroke)
        drawCircle(tint, radius = size.minDimension * 0.14f, center = Offset(size.width * 0.5f, size.height * 0.54f), style = stroke)
        drawLine(tint, Offset(size.width * 0.34f, size.height * 0.3f), Offset(size.width * 0.4f, size.height * 0.2f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.4f, size.height * 0.2f), Offset(size.width * 0.6f, size.height * 0.2f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.6f, size.height * 0.2f), Offset(size.width * 0.66f, size.height * 0.3f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}

@Composable
private fun BatIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.25f, size.height * 0.75f), Offset(size.width * 0.76f, size.height * 0.25f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(Color.White.copy(alpha = 0.5f), Offset(size.width * 0.34f, size.height * 0.66f), Offset(size.width * 0.82f, size.height * 0.18f), strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
    }
}

@Composable
private fun BallIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        drawCircle(tint, radius = size.minDimension * 0.36f, center = Offset(size.width * 0.5f, size.height * 0.5f))
        drawArc(Color.White.copy(alpha = 0.45f), 120f, 120f, false, topLeft = Offset(size.width * 0.19f, size.height * 0.19f), size = Size(size.width * 0.62f, size.height * 0.62f), style = Stroke(width = 1.5.dp.toPx()))
    }
}
