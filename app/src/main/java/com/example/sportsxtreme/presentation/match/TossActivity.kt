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
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
private val TossPanel = Color(0xFF07111D)
private val TossCard = Color(0xFF0A1725)
private val TossStroke = Color(0xFF2B3950)
private val TossMuted = Color(0xFFAAB6C4)
private val TossGold = Color(0xFFFFD36A)

@Composable
private fun TossScreen(onBack: () -> Unit, onPlay: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TossBg)
            .drawBehind {
                drawRect(
                    Brush.verticalGradient(
                        listOf(Color(0xFF071625), Color(0xFF020A15), Color(0xFF03050B))
                    )
                )
                drawCircle(Color(0x1EFFD36A), radius = size.width * 0.52f, center = Offset(size.width * 0.82f, size.height * 0.18f))
                drawCircle(Color(0x2214D8A6), radius = size.width * 0.55f, center = Offset(size.width * 0.08f, size.height * 0.42f))
                drawCircle(Color(0x161B5BFF), radius = size.width * 0.62f, center = Offset(size.width * 0.78f, size.height * 0.78f))
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TossTopBar(onBack)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 22.dp)
            ) {
                TossSectionTitle("WHO WON THE TOSS?")
                Row(
                    modifier = Modifier.padding(top = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    TossTeamCard("DW", "Dipesh Warrior\n69", selected = true, color = Color(0xFF009B86), modifier = Modifier.weight(1f))
                    TossTeamCard("BH", "Bhu", selected = false, color = Color(0xFF2476F2), modifier = Modifier.weight(1f))
                }

                TossSectionTitle("WINNER OF THE TOSS ELECTED TO?", Modifier.padding(top = 32.dp))
                Row(
                    modifier = Modifier.padding(top = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    TossChoiceCard(
                        title = "Bat",
                        selected = false,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .paint(
                                    painterResource(id = R.drawable.proimage_onboarding4),
                                    contentScale = ContentScale.Crop
                                )
                        )
                    }
                    TossChoiceCard(
                        title = "Bowl",
                        selected = true,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .paint(
                                    painterResource(id = R.drawable.otherball),
                                    contentScale = ContentScale.Fit
                                )
                        )
                    }
                }

                Text(
                    "TAP THE COIN TO FLIP",
                    color = Color(0xFFE6F2FF),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier
                        .padding(top = 52.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Box(
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TossCoin(Modifier.size(200.dp))
                }
                Spacer(Modifier.height(120.dp))
            }
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF07111D), Color(0xFF0E1C2B), Color(0xFF07111D))
                    )
                )
                .border(1.dp, Color(0xFF28344A))
                .padding(horizontal = 14.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Need help?", color = TossMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .width(148.dp)
                    .height(52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.horizontalGradient(listOf(TossAccent, TossGold)))
                    .clickable(onClick = onPlay),
                contentAlignment = Alignment.Center
            ) {
                Text("Let's play", color = Color(0xFF111604), fontSize = 16.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun TossTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF06111D), Color(0xFF0B1A28), Color(0xFF07101A))
                )
            )
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TossArrow(Modifier.size(26.dp).clickable(onClick = onBack), Color.White)
        Text(
            "Toss",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 16.dp).weight(1f)
        )
        TossCameraIcon(Modifier.size(24.dp), TossMuted)
    }
}

@Composable
private fun TossSectionTitle(text: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Brush.verticalGradient(listOf(TossGold, TossAccent)))
        )
        Text(
            text,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.8.sp,
            modifier = Modifier.padding(start = 9.dp)
        )
    }
}

@Composable
private fun TossTeamCard(initials: String, name: String, selected: Boolean, color: Color, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 1.045f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 420f),
        label = "team-card-pop"
    )

    Column(
        modifier = modifier
            .height(140.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(if (selected || pressed) 16.dp else 7.dp, RoundedCornerShape(12.dp), clip = false)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    if (selected) listOf(Color(0xFF183021), Color(0xFF0D1B16), Color(0xFF06110D))
                    else listOf(Color(0xFF13283A), TossCard, Color(0xFF050D17))
                )
            )
            .border(
                if (selected) 2.dp else 1.dp,
                if (selected) TossAccent else TossStroke,
                RoundedCornerShape(12.dp)
            )
            .drawBehind {
                drawCircle(
                    (if (selected) TossAccent else color).copy(alpha = if (selected) 0.22f else 0.12f),
                    radius = size.width * 0.44f,
                    center = Offset(size.width * 0.5f, size.height * 0.5f)
                )
            }
            .clickable(interactionSource = interactionSource, indication = null, onClick = {}),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(color.copy(alpha = 0.92f), color.copy(alpha = 0.62f), Color(0xFF07101A))))
                .border(1.dp, Color.White.copy(alpha = 0.20f), CircleShape),
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
            modifier = Modifier.padding(top = 14.dp)
        )
    }
}

@Composable
private fun TossChoiceCard(title: String, selected: Boolean, modifier: Modifier = Modifier, icon: @Composable () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 1.045f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 420f),
        label = "choice-card-pop"
    )

    Column(
        modifier = modifier
            .height(128.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(if (selected || pressed) 15.dp else 7.dp, RoundedCornerShape(12.dp), clip = false)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF13283A), TossCard, Color(0xFF050D17))
                )
            )
            .border(
                if (selected) 2.dp else 1.dp,
                if (selected) TossAccent else TossStroke,
                RoundedCornerShape(12.dp)
            )
            .drawBehind {
                drawCircle(
                    TossAccent.copy(alpha = if (selected) 0.22f else 0.08f),
                    radius = size.width * 0.44f,
                    center = Offset(size.width * 0.5f, size.height * 0.44f)
                )
            }
            .clickable(interactionSource = interactionSource, indication = null, onClick = {}),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        icon()
        Text(
            title,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(top = 10.dp)
        )
    }
}

@Composable
private fun TossCoin(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "coin-glow")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.86f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(animation = tween(1300), repeatMode = RepeatMode.Reverse),
        label = "coin-pulse"
    )
    val shimmer by infiniteTransition.animateFloat(
        initialValue = -18f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(animation = tween(1900), repeatMode = RepeatMode.Reverse),
        label = "coin-shimmer"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = pulse
                scaleY = pulse
            }
            .shadow(24.dp, CircleShape, clip = false),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                Brush.radialGradient(
                    listOf(TossAccent.copy(alpha = 0.32f), Color.Transparent),
                    radius = size.minDimension * 0.62f
                ),
                radius = size.minDimension * 0.62f,
                center = Offset(size.width * 0.5f, size.height * 0.5f)
            )
            drawCircle(
                Brush.radialGradient(
                    listOf(Color(0xFFFFF2B5), TossGold, Color(0xFF5E6A65), Color(0xFF2C3838)),
                    radius = size.minDimension * 0.5f
                ),
                radius = size.minDimension * 0.49f,
                center = Offset(size.width * 0.5f, size.height * 0.5f)
            )
            drawCircle(
                Brush.radialGradient(
                    listOf(Color(0xFF243631), Color(0xFF0A1512), Color(0xFF050E0D)),
                    radius = size.minDimension * 0.42f
                ),
                radius = size.minDimension * 0.44f,
                center = Offset(size.width * 0.5f, size.height * 0.5f)
            )
            val lineColor = Color(0xFF3E514E).copy(alpha = 0.42f)
            for (i in 1..10) {
                val y = size.height * 0.08f + i * (size.height * 0.84f / 11f)
                drawLine(lineColor, Offset(size.width * 0.08f, y), Offset(size.width * 0.92f, y), strokeWidth = 1.dp.toPx())
            }
            for (i in 1..10) {
                val x = size.width * 0.08f + i * (size.width * 0.84f / 11f)
                drawLine(lineColor, Offset(x, size.height * 0.08f), Offset(x, size.height * 0.92f), strokeWidth = 1.dp.toPx())
            }
            drawArc(
                TossAccent,
                195f, 155f, false,
                topLeft = Offset(size.width * 0.08f, size.height * 0.08f),
                size = Size(size.width * 0.84f, size.height * 0.84f),
                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                Color.White.copy(alpha = 0.45f),
                shimmer, 54f, false,
                topLeft = Offset(size.width * 0.14f, size.height * 0.14f),
                size = Size(size.width * 0.72f, size.height * 0.72f),
                style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
            )
            drawCircle(
                Color(0xFF50605C),
                radius = size.minDimension * 0.32f,
                center = Offset(size.width * 0.5f, size.height * 0.5f),
                style = Stroke(width = 1.dp.toPx())
            )
        }
        Text(
            "HEADS",
            color = TossAccent,
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 3.sp
        )
    }
}

@Composable
private fun TossArrow(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
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
