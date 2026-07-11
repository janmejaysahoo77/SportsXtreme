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

class AddPlayerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applySportsXtremeWindowStyle()
        setContent {
            AddPlayerScreen(
                onBack = { finish() },
                onContinue = { },
                onAddByMobileNumber = {
                    startActivity(Intent(this, AddPlayerByMobileNumberActivity::class.java))
                },
                onScanQrCode = {
                    startActivity(Intent(this, QRForPlayerAddingActivity::class.java))
                }
            )
        }
    }
}

private val AddPlayerAccent = Color(0xFFC1FF00)
private val AddPlayerBg = Color(0xFF020A15)
private val AddPlayerPanel = Color(0xFF07101A)
private val AddPlayerCard = Color(0xFF0A1422)
private val AddPlayerStroke = Color(0xFF26354B)
private val AddPlayerMuted = Color(0xFFB7C2C1)

@Composable
private fun AddPlayerScreen(
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onAddByMobileNumber: () -> Unit,
    onScanQrCode: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AddPlayerBg)
            .drawBehind {
                repeat(14) { index ->
                    val y = size.height * (0.08f + index * 0.055f)
                    drawLine(
                        color = Color(0x0B9BB2BA),
                        start = Offset(0f, y),
                        end = Offset(size.width, y + size.width * 0.05f),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                drawCircle(Color(0x0F1B5BFF), radius = size.width * 0.58f, center = Offset(size.width * 0.55f, size.height * 0.28f))
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AddPlayerTopBar(onBack)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 26.dp)
            ) {
                Text(
                    "Choose how you want to add players.",
                    color = AddPlayerMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
                InvitePlayerCard(Modifier.padding(top = 28.dp))
                AddMethodCard(
                    modifier = Modifier.padding(top = 14.dp),
                    title = "Add by Mobile Number",
                    subtitle = "Quickly add a player using their phone\nnumber.",
                    icon = { PhoneIcon(Modifier.size(27.dp), Color(0xFF111604)) },
                    onClick = onAddByMobileNumber
                )
                AddMethodCard(
                    modifier = Modifier.padding(top = 14.dp),
                    title = "Scan QR Code",
                    subtitle = "Scan a player's QR code to add them\ninstantly.",
                    icon = { QrIcon(Modifier.size(27.dp), Color(0xFF111604)) },
                    onClick = onScanQrCode
                )
                Spacer(Modifier.height(116.dp))
                AddPlayerInfoCard()
                Spacer(Modifier.height(120.dp))
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AddPlayerBg)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(45.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AddPlayerAccent)
                        .clickable(onClick = onContinue),
                    contentAlignment = Alignment.Center
                ) {
                    Text("CONTINUE", color = Color(0xFF111604), fontSize = 13.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
private fun AddPlayerTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(53.dp)
            .background(AddPlayerPanel)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AddPlayerBackArrow(Modifier.size(25.dp).clickable(onClick = onBack), AddPlayerAccent)
        Text(
            "Add Players to\nDinabandhu",
            color = Color.White,
            fontSize = 17.sp,
            lineHeight = 21.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 12.dp).weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        HelpIcon(Modifier.size(21.dp), AddPlayerMuted)
    }
}

@Composable
private fun InvitePlayerCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(122.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(AddPlayerCard)
            .border(1.dp, Color(0xFF3E531D), RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 13.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AddPlayerIconBubble { ShareIcon(Modifier.size(27.dp), Color(0xFF111604)) }
            Column(modifier = Modifier.padding(start = 14.dp).weight(1f)) {
                Text(
                    "Invite Player",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1
                )
                Text(
                    "Send an invite link to join SportsXtreme.",
                    color = AddPlayerMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
            ChevronIcon(Modifier.size(20.dp), AddPlayerMuted)
        }
        Row(
            modifier = Modifier.padding(top = 17.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ShareButton("Share", Modifier.weight(1f), filled = false)
            ShareButton("WhatsApp", Modifier.weight(1f), filled = true)
        }
    }
}

@Composable
private fun AddMethodCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(78.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(AddPlayerCard)
            .border(1.dp, Color(0xFF17243A), RoundedCornerShape(9.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AddPlayerIconBubble(icon)
        Column(modifier = Modifier.padding(start = 14.dp).weight(1f)) {
            Text(
                title,
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(subtitle, color = AddPlayerMuted, fontSize = 10.sp, lineHeight = 12.sp, fontWeight = FontWeight.Bold)
        }
        ChevronIcon(Modifier.size(20.dp), AddPlayerMuted)
    }
}

@Composable
private fun AddPlayerIconBubble(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(AddPlayerAccent),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun ShareButton(text: String, modifier: Modifier, filled: Boolean) {
    Row(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(if (filled) Color(0xFF0D5A3C) else Color.Transparent)
            .border(1.dp, if (filled) Color(0xFF14784F) else Color(0xFF566F2B), RoundedCornerShape(5.dp)),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (text == "WhatsApp") {
            MessageIcon(Modifier.size(14.dp), AddPlayerAccent)
        } else {
            SmallShareIcon(Modifier.size(14.dp), AddPlayerAccent)
        }
        Text(text, color = AddPlayerAccent, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
private fun AddPlayerInfoCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF061A28))
            .border(1.dp, Color(0xFF0A4D71), RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        InfoIcon(Modifier.size(19.dp), Color(0xFF16B8FF))
        Text(
            "Players can be added via invite link, mobile\nnumber, or QR code.",
            color = Color(0xFFD2E1E8),
            fontSize = 11.sp,
            lineHeight = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

@Composable
private fun AddPlayerBackArrow(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        val path = Path().apply {
            moveTo(size.width * 0.65f, size.height * 0.24f)
            lineTo(size.width * 0.35f, size.height * 0.5f)
            lineTo(size.width * 0.65f, size.height * 0.76f)
        }
        drawPath(path, tint, style = stroke)
    }
}

@Composable
private fun HelpIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.38f, style = stroke)
        drawArc(tint, 210f, 220f, false, style = stroke)
        drawCircle(tint, radius = size.minDimension * 0.04f, center = Offset(size.width * 0.5f, size.height * 0.68f))
    }
}

@Composable
private fun ShareIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        val a = Offset(size.width * 0.3f, size.height * 0.52f)
        val b = Offset(size.width * 0.68f, size.height * 0.3f)
        val c = Offset(size.width * 0.68f, size.height * 0.7f)
        drawLine(tint, a, b, strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, a, c, strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.11f, center = a, style = stroke)
        drawCircle(tint, radius = size.minDimension * 0.11f, center = b, style = stroke)
        drawCircle(tint, radius = size.minDimension * 0.11f, center = c, style = stroke)
    }
}

@Composable
private fun PhoneIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 2.1.dp.toPx(), cap = StrokeCap.Round)
        val path = Path().apply {
            moveTo(size.width * 0.3f, size.height * 0.18f)
            cubicTo(size.width * 0.16f, size.height * 0.24f, size.width * 0.2f, size.height * 0.62f, size.width * 0.48f, size.height * 0.78f)
            cubicTo(size.width * 0.62f, size.height * 0.86f, size.width * 0.78f, size.height * 0.78f, size.width * 0.82f, size.height * 0.66f)
        }
        drawPath(path, tint, style = stroke)
        drawCircle(tint, radius = size.minDimension * 0.07f, center = Offset(size.width * 0.32f, size.height * 0.22f), style = stroke)
        drawCircle(tint, radius = size.minDimension * 0.07f, center = Offset(size.width * 0.74f, size.height * 0.67f), style = stroke)
    }
}

@Composable
private fun QrIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        fun square(x: Float, y: Float) {
            drawRect(tint, topLeft = Offset(size.width * x, size.height * y), size = Size(size.width * 0.18f, size.height * 0.18f), style = stroke)
        }
        square(0.16f, 0.16f)
        square(0.62f, 0.16f)
        square(0.16f, 0.62f)
        drawLine(tint, Offset(size.width * 0.5f, size.height * 0.52f), Offset(size.width * 0.78f, size.height * 0.52f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.52f, size.height * 0.62f), Offset(size.width * 0.52f, size.height * 0.82f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.04f, center = Offset(size.width * 0.72f, size.height * 0.72f))
    }
}

@Composable
private fun ChevronIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        val path = Path().apply {
            moveTo(size.width * 0.38f, size.height * 0.28f)
            lineTo(size.width * 0.62f, size.height * 0.5f)
            lineTo(size.width * 0.38f, size.height * 0.72f)
        }
        drawPath(path, tint, style = stroke)
    }
}

@Composable
private fun SmallShareIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.5f, size.height * 0.2f), Offset(size.width * 0.5f, size.height * 0.72f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.32f, size.height * 0.38f), Offset(size.width * 0.5f, size.height * 0.2f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.68f, size.height * 0.38f), Offset(size.width * 0.5f, size.height * 0.2f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawRoundRect(tint, topLeft = Offset(size.width * 0.24f, size.height * 0.54f), size = Size(size.width * 0.52f, size.height * 0.3f), style = stroke)
    }
}

@Composable
private fun MessageIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
        drawRoundRect(tint, topLeft = Offset(size.width * 0.15f, size.height * 0.2f), size = Size(size.width * 0.7f, size.height * 0.5f), style = stroke)
        drawLine(tint, Offset(size.width * 0.32f, size.height * 0.7f), Offset(size.width * 0.22f, size.height * 0.84f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.32f, size.height * 0.7f), Offset(size.width * 0.48f, size.height * 0.7f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}

@Composable
private fun InfoIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.42f, style = stroke)
        drawLine(tint, Offset(size.width * 0.5f, size.height * 0.45f), Offset(size.width * 0.5f, size.height * 0.72f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.035f, center = Offset(size.width * 0.5f, size.height * 0.29f))
    }
}
