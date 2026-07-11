package com.example.sportsxtreme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class AddPlayerByMobileNumberActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applySportsXtremeWindowStyle()
        setContent {
            AddPlayerByMobileNumberScreen(onBack = { finish() }, onDone = { finish() })
        }
    }
}

private val MobileAccent = Color(0xFFC1FF00)
private val MobileBg = Color(0xFF020A15)
private val MobilePanel = Color(0xFF07101A)
private val MobileCard = Color(0xFF0B1523)
private val MobileMuted = Color(0xFFB7C0C7)
private val MobileDim = Color(0xFF485061)

@Composable
private fun AddPlayerByMobileNumberScreen(onBack: () -> Unit, onDone: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MobileBg)
            .drawBehind {
                repeat(22) { index ->
                    val x = size.width * ((index * 37 % 100) / 100f)
                    val y = size.height * (0.14f + ((index * 19 % 72) / 100f))
                    drawCircle(Color(0x24465A6D), radius = 1.1.dp.toPx(), center = Offset(x, y))
                }
                drawCircle(Color(0x152C7500), radius = size.width * 0.52f, center = Offset(size.width * 0.48f, size.height * 0.44f))
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            MobileTopBar(onBack)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(56.dp))
                MobileInputCard()
                AddAnotherButton(Modifier.padding(top = 34.dp))
                Text(
                    "QUEUE UP PLAYER BEFORE FINAL SYNC",
                    color = MobileDim,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(top = 15.dp, start = 3.dp)
                )
                Spacer(Modifier.height(190.dp))
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MobileBg)
                    .padding(horizontal = 26.dp, vertical = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(67.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(MobileAccent)
                        .clickable(onClick = onDone),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "D O N E",
                        color = Color(0xFF111604),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MobileTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(102.dp)
            .background(MobilePanel)
            .padding(start = 27.dp, end = 18.dp, top = 26.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MobileBackArrow(Modifier.size(24.dp).clickable(onClick = onBack), Color.White)
        Text(
            "Add player by mobile number",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.3.sp,
            modifier = Modifier.padding(start = 26.dp).weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MobileInputCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(263.dp)
            .clip(RoundedCornerShape(34.dp))
            .background(MobileCard)
            .border(1.dp, Color(0xFF4D721A), RoundedCornerShape(34.dp))
            .padding(horizontal = 35.dp, vertical = 35.dp)
    ) {
        FieldLabel("MOBILE NUMBER")
        Row(
            modifier = Modifier.padding(top = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("+91", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Text(
                "98765 43210",
                color = MobileDim,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(start = 13.dp)
            )
        }
        DividerLine(Modifier.padding(top = 14.dp))
        FieldLabel("PLAYER NAME", Modifier.padding(top = 42.dp))
        Row(
            modifier = Modifier.padding(top = 17.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayerIcon(Modifier.size(23.dp), MobileAccent)
            Text(
                "e.g. John Doe",
                color = MobileDim,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(start = 17.dp)
            )
        }
        DividerLine(Modifier.padding(top = 15.dp))
    }
}

@Composable
private fun FieldLabel(text: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text,
            color = MobileMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.1.sp
        )
        Text(
            "*",
            color = MobileAccent,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}

@Composable
private fun DividerLine(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFF34404C))
    )
}

@Composable
private fun AddAnotherButton(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp)
            .height(37.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFF4D721A), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "+ ADD ANOTHER PLAYER",
            color = MobileAccent,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun MobileBackArrow(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.25f, size.height * 0.5f), Offset(size.width * 0.78f, size.height * 0.5f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        val path = Path().apply {
            moveTo(size.width * 0.45f, size.height * 0.25f)
            lineTo(size.width * 0.22f, size.height * 0.5f)
            lineTo(size.width * 0.45f, size.height * 0.75f)
        }
        drawPath(path, tint, style = stroke)
    }
}

@Composable
private fun PlayerIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.14f, center = Offset(size.width * 0.5f, size.height * 0.27f), style = stroke)
        drawArc(
            color = tint,
            startAngle = 205f,
            sweepAngle = 130f,
            useCenter = false,
            topLeft = Offset(size.width * 0.23f, size.height * 0.5f),
            size = Size(size.width * 0.54f, size.height * 0.34f),
            style = stroke
        )
        drawLine(tint, Offset(size.width * 0.27f, size.height * 0.84f), Offset(size.width * 0.73f, size.height * 0.84f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}
