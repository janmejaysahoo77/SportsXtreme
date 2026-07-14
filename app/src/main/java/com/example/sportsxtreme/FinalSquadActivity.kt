package com.example.sportsxtreme

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat

class FinalSquadActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        setContent {
            FinalSquadScreen(
                onBack = { finish() },
                onNext = { startActivity(Intent(this, StartMatchPreviewActivity::class.java)) }
            )
        }
    }
}

private val SquadAccent = Color(0xFFC1FF00)
private val SquadBg = Color(0xFF020A15)
private val SquadPanel = Color(0xFF07101A)
private val SquadCard = Color(0xFF0B1523)
private val SquadStroke = Color(0xFF25314A)
private val SquadMuted = Color(0xFFAAB6C4)

@Composable
private fun FinalSquadScreen(onBack: () -> Unit, onNext: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SquadBg)
            .drawBehind {
                drawCircle(Color(0x101B5BFF), radius = size.width * 0.65f, center = Offset(size.width * 0.52f, size.height * 0.2f))
                drawCircle(Color(0x151C3F14), radius = size.width * 0.55f, center = Offset(size.width * 0.55f, size.height * 0.42f))
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            FinalSquadTopBar(onBack)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 20.dp)
            ) {
                FinalSquadHeader()
                SquadPlayerCard(
                    name = "ANSHU Gita",
                    role = "Batsman",
                    tag = "C",
                    selected = true,
                    modifier = Modifier.padding(top = 22.dp)
                )
                SquadPlayerCard(
                    name = "Abhimanyu Majhi",
                    role = "All Rounder",
                    tag = "VC",
                    selected = true,
                    modifier = Modifier.padding(top = 12.dp)
                )
                SquadPlayerCard(
                    name = "Raj Kumar",
                    role = "Wicket Keeper",
                    tag = "WK",
                    selected = false,
                    modifier = Modifier.padding(top = 12.dp)
                )
                Spacer(Modifier.height(110.dp))
            }
        }
        CameraFloatingButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 21.dp, bottom = 87.dp)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(SquadBg)
                .padding(horizontal = 12.dp, vertical = 13.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(SquadAccent)
                    .clickable(onClick = onNext),
                contentAlignment = Alignment.Center
            ) {
                Text("Next", color = Color(0xFF111604), fontSize = 15.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun FinalSquadTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp)
            .background(SquadPanel)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SquadArrow(Modifier.size(25.dp).clickable(onClick = onBack), Color.White)
        Text(
            "Final Squad",
            color = Color.White,
            fontSize = 21.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 48.dp),
            maxLines = 1
        )
    }
}

@Composable
private fun FinalSquadHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Your Squad",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.weight(1f),
            maxLines = 1
        )
        SquadRoleButton("C", selected = true)
        Spacer(Modifier.width(8.dp))
        SquadRoleButton("VC", selected = false)
        Spacer(Modifier.width(8.dp))
        SquadRoleButton("WK", selected = false)
    }
}

@Composable
private fun SquadRoleButton(text: String, selected: Boolean) {
    Box(
        modifier = Modifier
            .size(39.dp)
            .clip(CircleShape)
            .background(if (selected) SquadAccent else SquadCard)
            .border(1.dp, if (selected) SquadAccent else SquadStroke, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (selected) Color(0xFF111604) else Color.White,
            fontSize = if (text.length == 1) 13.sp else 11.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun SquadPlayerCard(name: String, role: String, tag: String, selected: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(76.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(SquadCard)
            .border(if (selected) 1.5.dp else 1.dp, if (selected) SquadAccent else SquadStroke, RoundedCornerShape(10.dp))
            .padding(horizontal = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color(0xFF315A43), Color(0xFF101820)), radius = 62f))
                .border(1.dp, Color(0xFF3A465D), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            SquadPersonIcon(Modifier.size(31.dp))
        }
        Column(modifier = Modifier.padding(start = 13.dp).weight(1f)) {
            Text(name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(modifier = Modifier.padding(top = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(SquadAccent)
                )
                Text(role, color = SquadMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 5.dp))
            }
        }
        Box(
            modifier = Modifier
                .height(27.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (selected) Color(0xFF344C12) else Color(0xFF111826))
                .border(1.dp, if (selected) Color(0xFF58751E) else SquadStroke, RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(tag, color = if (selected) SquadAccent else SquadMuted, fontSize = 10.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun CameraFloatingButton(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(SquadCard)
            .border(1.5.dp, SquadAccent, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        SquadCameraIcon(Modifier.size(25.dp), SquadAccent)
    }
}

@Composable
private fun SquadArrow(modifier: Modifier, tint: Color) {
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
private fun SquadCameraIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.9.dp.toPx(), cap = StrokeCap.Round)
        drawRoundRect(
            color = tint,
            topLeft = Offset(size.width * 0.15f, size.height * 0.3f),
            size = Size(size.width * 0.7f, size.height * 0.5f),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
            style = stroke
        )
        drawLine(tint, Offset(size.width * 0.34f, size.height * 0.3f), Offset(size.width * 0.42f, size.height * 0.19f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.42f, size.height * 0.19f), Offset(size.width * 0.59f, size.height * 0.19f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.59f, size.height * 0.19f), Offset(size.width * 0.66f, size.height * 0.3f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.13f, center = Offset(size.width * 0.5f, size.height * 0.55f), style = stroke)
    }
}

@Composable
private fun SquadPersonIcon(modifier: Modifier) {
    Canvas(modifier) {
        drawCircle(Color(0xFFD0B190), radius = size.minDimension * 0.17f, center = Offset(size.width * 0.5f, size.height * 0.28f))
        drawRoundRect(
            Color(0xFF253D58),
            topLeft = Offset(size.width * 0.24f, size.height * 0.49f),
            size = Size(size.width * 0.52f, size.height * 0.34f),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )
    }
}
