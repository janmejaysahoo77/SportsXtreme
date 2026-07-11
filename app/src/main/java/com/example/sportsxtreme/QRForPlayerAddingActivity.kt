package com.example.sportsxtreme

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class QRForPlayerAddingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applySportsXtremeWindowStyle()
        setContent {
            QRForPlayerAddingScreen(onClose = { finish() })
        }
    }
}

private val QrAddBg = Color(0xFF020A15)
private val QrAddAccent = Color(0xFFC1FF00)
private val QrAddTextMuted = Color(0xFFAAAAC6)
private val QrAddPanel = Color(0xFF0A1322)

@Composable
private fun QRForPlayerAddingScreen(onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(QrAddBg)
            .drawBehind {
                drawCircle(Color(0x162F7E00), radius = size.width * 0.72f, center = Offset(size.width * 0.5f, size.height * 0.72f))
                drawCircle(Color(0x0D2C90FF), radius = size.width * 0.42f, center = Offset(size.width * 0.52f, size.height * 0.43f))
            }
            .padding(horizontal = 13.dp)
    ) {
        QRTopActions(onClose)
        TeamIdentity(Modifier.padding(top = 22.dp))
        QRCodeFrame(Modifier.padding(top = 42.dp))
        InstructionText(Modifier.padding(top = 39.dp))
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
                .height(54.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(QrAddAccent)
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Text("Share this code", color = Color(0xFF111604), fontSize = 17.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun QRTopActions(onClose: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(top = 27.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CloseIcon(Modifier.size(35.dp).clickable(onClick = onClose).padding(8.dp), Color.White)
        Spacer(Modifier.weight(1f))
        DownloadIcon(Modifier.size(35.dp).padding(8.dp), Color.White)
        ShareNetworkIcon(Modifier.padding(start = 13.dp).size(35.dp).padding(8.dp), Color.White)
    }
}

@Composable
private fun TeamIdentity(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(99.dp)
                .clip(CircleShape)
                .background(Color.Black)
                .border(4.dp, Color(0xFF202839), CircleShape)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.finalicon),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(13.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
        Text("Bhauah", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 21.dp))
        Text("BHUBANESWAR", color = Color(0xFF7F789B), fontSize = 13.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.5.sp, modifier = Modifier.padding(top = 5.dp))
    }
}

@Composable
private fun QRCodeFrame(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(272.dp)
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(QrAddPanel)
            .border(2.dp, Color(0xFF222A40), RoundedCornerShape(22.dp))
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(5.dp))
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFF234352), Color(0xFF0B1820), Color(0xFF061017)),
                        radius = 430f
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(158.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF223943))
                    .border(3.dp, Color(0xFF0E1924), RoundedCornerShape(12.dp))
                    .padding(11.dp),
                contentAlignment = Alignment.Center
            ) {
                DecorativeQrPattern()
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black)
                        .border(2.dp, QrAddAccent, RoundedCornerShape(10.dp))
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.finalicon),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

@Composable
private fun DecorativeQrPattern() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cells = 27
        val cell = size.minDimension / cells
        for (row in 0 until cells) {
            for (col in 0 until cells) {
                val draw = ((row * 11 + col * 7 + row * col) % 5 == 0) || ((row + col) % 9 == 0)
                if (draw) {
                    val shade = if ((row + col) % 2 == 0) Color(0xFF9BB3B7) else Color(0xFF557077)
                    drawRect(shade, topLeft = Offset(col * cell, row * cell), size = Size(cell * 0.86f, cell * 0.86f))
                }
            }
        }
        drawFinder(1f, 1f, cell)
        drawFinder(20f, 1f, cell)
        drawFinder(1f, 20f, cell)
        drawRect(Color(0x332C3A42), topLeft = Offset.Zero, size = size)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFinder(col: Float, row: Float, cell: Float) {
    val topLeft = Offset(col * cell, row * cell)
    val block = Size(cell * 5, cell * 5)
    drawRect(Color(0xFF8FA8AA), topLeft = topLeft, size = block)
    drawRect(Color(0xFF12262E), topLeft = Offset(topLeft.x + cell, topLeft.y + cell), size = Size(cell * 3, cell * 3))
    drawRect(Color(0xFF8FA8AA), topLeft = Offset(topLeft.x + cell * 2, topLeft.y + cell * 2), size = Size(cell, cell))
}

@Composable
private fun InstructionText(modifier: Modifier = Modifier) {
    Text(
        buildAnnotatedString {
            withStyle(SpanStyle(color = QrAddTextMuted)) {
                append("Ask players to scan this QR code to add\n")
                append("themselves directly to ")
            }
            withStyle(SpanStyle(color = QrAddAccent, fontWeight = FontWeight.Black)) {
                append("Bhauah team.")
            }
        },
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Medium,
        modifier = modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun CloseIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.2f, size.height * 0.2f), Offset(size.width * 0.8f, size.height * 0.8f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.8f, size.height * 0.2f), Offset(size.width * 0.2f, size.height * 0.8f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}

@Composable
private fun DownloadIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.5f, size.height * 0.15f), Offset(size.width * 0.5f, size.height * 0.62f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        val path = Path().apply {
            moveTo(size.width * 0.32f, size.height * 0.44f)
            lineTo(size.width * 0.5f, size.height * 0.64f)
            lineTo(size.width * 0.68f, size.height * 0.44f)
        }
        drawPath(path, tint, style = stroke)
        drawArc(tint, 20f, 140f, false, topLeft = Offset(size.width * 0.18f, size.height * 0.48f), size = Size(size.width * 0.64f, size.height * 0.36f), style = stroke)
    }
}

@Composable
private fun ShareNetworkIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        val left = Offset(size.width * 0.28f, size.height * 0.53f)
        val top = Offset(size.width * 0.72f, size.height * 0.29f)
        val bottom = Offset(size.width * 0.72f, size.height * 0.74f)
        drawLine(tint, left, top, strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, left, bottom, strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.11f, center = left, style = stroke)
        drawCircle(tint, radius = size.minDimension * 0.11f, center = top, style = stroke)
        drawCircle(tint, radius = size.minDimension * 0.11f, center = bottom, style = stroke)
    }
}
