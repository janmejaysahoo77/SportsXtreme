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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class ViewDetailsScreenWhileStartMatch : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applySportsXtremeWindowStyle()
        setContent {
            ViewDetailsStartMatchScreen(
                onBack = { finish() },
                onAddPlayer = { startActivity(Intent(this, AddPlayerActivity::class.java)) },
                onNext = { startActivity(Intent(this, PlayersListFinalActivity::class.java)) }
            )
        }
    }
}

private val DetailsAccent = Color(0xFFC1FF00)
private val DetailsBg = Color(0xFF020A15)
private val DetailsPanel = Color(0xFF07101A)
private val DetailsCard = Color(0xFF0B1523)
private val DetailsStroke = Color(0xFF25314A)
private val DetailsMuted = Color(0xFFADB8BD)

@Composable
private fun ViewDetailsStartMatchScreen(onBack: () -> Unit, onAddPlayer: () -> Unit, onNext: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DetailsBg)
            .drawBehind {
                drawCircle(Color(0x101B5BFF), radius = size.width * 0.62f, center = Offset(size.width * 0.52f, size.height * 0.18f))
                drawCircle(Color(0x161C3F14), radius = size.width * 0.5f, center = Offset(size.width * 0.5f, size.height * 0.34f))
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            DetailsTopBar(onBack)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 7.dp, vertical = 21.dp)
            ) {
                TeamSummaryCard()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 31.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Team Members", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .height(29.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(Color(0xFF3C5316))
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Total 1", color = DetailsAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
                SquadSelectionContent(
                    modifier = Modifier.padding(top = 15.dp),
                    onAddPlayer = onAddPlayer
                )
                Spacer(Modifier.weight(1f))
            }
        }
        BottomActions(
            modifier = Modifier.align(Alignment.BottomCenter),
            onAddPlayer = onAddPlayer,
            onNext = onNext
        )
    }
}

@Composable
private fun DetailsTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp)
            .background(DetailsPanel)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DetailsArrow(Modifier.size(25.dp).clickable(onClick = onBack), Color.White)
        Text(
            "Dinabandhu",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 59.dp),
            maxLines = 1
        )
    }
}

@Composable
private fun TeamSummaryCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(DetailsCard)
            .border(1.dp, DetailsStroke, RoundedCornerShape(10.dp))
            .drawBehind {
                drawLine(
                    brush = Brush.horizontalGradient(listOf(Color.Transparent, DetailsAccent.copy(alpha = 0.7f), Color.Transparent)),
                    start = Offset(size.width * 0.18f, size.height),
                    end = Offset(size.width * 0.9f, size.height),
                    strokeWidth = 1.5.dp.toPx()
                )
            }
            .padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF202838)),
                contentAlignment = Alignment.Center
            ) {
                BatBallIcon(Modifier.size(29.dp), DetailsAccent)
            }
            Column(modifier = Modifier.padding(start = 15.dp).weight(1f)) {
                Text("Dinabandhu", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black, maxLines = 1)
                Text("Established 2024", color = DetailsMuted, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .border(1.dp, Color(0xFF516927), RoundedCornerShape(7.dp))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    EditPencilIcon(Modifier.size(15.dp), DetailsAccent)
                    Text("Edit", color = DetailsAccent, fontSize = 14.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 6.dp))
                }
            }
        }
        Row(
            modifier = Modifier.padding(top = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GroupIcon(Modifier.size(18.dp), Color(0xFF64C8FF))
            Text("1 Player", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 7.dp))
            ShieldIcon(Modifier.padding(start = 64.dp).size(18.dp), DetailsAccent)
            Text("Janaman Gana", color = DetailsMuted, fontSize = 14.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
private fun MemberCard(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(86.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(DetailsCard)
            .border(1.dp, Color(0xFF39521B), RoundedCornerShape(10.dp))
            .padding(horizontal = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFF304E37), Color(0xFF111D20)),
                            radius = 68f
                        )
                    )
                    .border(2.dp, DetailsAccent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                PlayerSilhouette(Modifier.size(38.dp))
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(25.dp)
                    .clip(CircleShape)
                    .background(DetailsAccent),
                contentAlignment = Alignment.Center
            ) {
                ShieldIcon(Modifier.size(13.dp), Color(0xFF111604))
            }
        }
        Column(modifier = Modifier.padding(start = 15.dp).weight(1f)) {
            Text("Janaman Gana", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(modifier = Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .height(16.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF354D11))
                        .padding(horizontal = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("CAPTAIN", color = DetailsAccent, fontSize = 8.sp, fontWeight = FontWeight.Black)
                }
                Text("• Batsman", color = DetailsMuted, fontSize = 13.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 10.dp))
            }
        }
        KebabIcon(Modifier.size(20.dp), Color.White)
    }
}

@Composable
private fun SquadSelectionContent(modifier: Modifier = Modifier, onAddPlayer: () -> Unit) {
    Column(modifier = modifier.fillMaxWidth()) {
        SearchPlayerBox()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "AVAILABLE PLAYERS",
                color = DetailsMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.weight(1f)
            )
            Text(
                "+ ADD PLAYER",
                color = DetailsAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.clickable(onClick = onAddPlayer)
            )
        }
        Text(
            "Choose players participating in this match.",
            color = DetailsMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 18.dp)
        )
        SelectablePlayerCard(
            modifier = Modifier.padding(top = 16.dp),
            name = "Janaman Gana",
            subtitle = "Captain - Batsman",
            selected = true
        )
    }
}

@Composable
private fun SearchPlayerBox() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0B1523))
            .border(1.dp, Color(0xFF24324D), RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SearchIcon(Modifier.size(18.dp), Color(0xFF8EA0BA))
        Text(
            "Quick search",
            color = Color(0xFF9EABC2),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 10.dp)
        )
    }
}

@Composable
private fun SelectablePlayerCard(
    modifier: Modifier = Modifier,
    name: String,
    subtitle: String,
    selected: Boolean
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(DetailsCard)
            .border(
                1.dp,
                if (selected) DetailsAccent else Color(0xFF17243A),
                RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFF304E37), Color(0xFF111D20)),
                            radius = 68f
                        )
                    )
                    .border(1.dp, Color(0xFF314254), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                PlayerSilhouette(Modifier.size(34.dp))
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(if (selected) DetailsAccent else DetailsStroke),
                contentAlignment = Alignment.Center
            ) {
                CheckIcon(Modifier.size(10.dp), Color(0xFF111604))
            }
        }
        Column(modifier = Modifier.padding(start = 13.dp).weight(1f)) {
            Text(name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(modifier = Modifier.padding(top = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(DetailsAccent)
                )
                Text(subtitle, color = DetailsMuted, fontSize = 12.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 7.dp))
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("0", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Black)
            Text("MAT", color = DetailsMuted, fontSize = 9.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun BottomActions(modifier: Modifier = Modifier, onAddPlayer: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(DetailsBg)
            .padding(horizontal = 7.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .height(50.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(Color(0xFF111826))
                .border(1.dp, DetailsStroke, RoundedCornerShape(9.dp))
                .clickable(onClick = onAddPlayer),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AddPersonIcon(Modifier.size(18.dp), Color.White)
            Text("Add Player", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 10.dp))
        }
        Row(
            modifier = Modifier
                .weight(1.2f)
                .height(50.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(DetailsAccent)
                .clickable(onClick = onNext),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("NEXT", color = Color(0xFF111604), fontSize = 15.sp, fontWeight = FontWeight.Bold)
            NextArrowIcon(Modifier.padding(start = 9.dp).size(17.dp), Color(0xFF111604))
        }
    }
}

@Composable
private fun DetailsArrow(modifier: Modifier, tint: Color) {
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
private fun BatBallIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.22f, size.height * 0.28f), Offset(size.width * 0.58f, size.height * 0.74f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawRoundRect(tint, topLeft = Offset(size.width * 0.17f, size.height * 0.17f), size = Size(size.width * 0.18f, size.height * 0.43f), style = stroke)
        drawCircle(tint, radius = size.minDimension * 0.12f, center = Offset(size.width * 0.76f, size.height * 0.28f), style = stroke)
    }
}

@Composable
private fun EditPencilIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.24f, size.height * 0.75f), Offset(size.width * 0.75f, size.height * 0.24f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.19f, size.height * 0.81f), Offset(size.width * 0.35f, size.height * 0.76f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}

@Composable
private fun GroupIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.12f, center = Offset(size.width * 0.38f, size.height * 0.36f), style = stroke)
        drawCircle(tint, radius = size.minDimension * 0.1f, center = Offset(size.width * 0.67f, size.height * 0.4f), style = stroke)
        drawArc(tint, 205f, 130f, false, topLeft = Offset(size.width * 0.18f, size.height * 0.52f), size = Size(size.width * 0.42f, size.height * 0.28f), style = stroke)
        drawArc(tint, 205f, 130f, false, topLeft = Offset(size.width * 0.48f, size.height * 0.55f), size = Size(size.width * 0.32f, size.height * 0.24f), style = stroke)
    }
}

@Composable
private fun ShieldIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round)
        val path = Path().apply {
            moveTo(size.width * 0.5f, size.height * 0.12f)
            lineTo(size.width * 0.78f, size.height * 0.24f)
            lineTo(size.width * 0.72f, size.height * 0.62f)
            lineTo(size.width * 0.5f, size.height * 0.86f)
            lineTo(size.width * 0.28f, size.height * 0.62f)
            lineTo(size.width * 0.22f, size.height * 0.24f)
            close()
        }
        drawPath(path, tint, style = stroke)
    }
}

@Composable
private fun PlayerSilhouette(modifier: Modifier) {
    Canvas(modifier) {
        drawCircle(Color(0xFF1E2A2D), radius = size.minDimension * 0.48f)
        drawCircle(Color(0xFFD0B190), radius = size.minDimension * 0.16f, center = Offset(size.width * 0.5f, size.height * 0.28f))
        drawRoundRect(Color(0xFF1C5635), topLeft = Offset(size.width * 0.28f, size.height * 0.48f), size = Size(size.width * 0.44f, size.height * 0.36f))
    }
}

@Composable
private fun KebabIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        repeat(3) { index ->
            drawCircle(tint, radius = size.minDimension * 0.055f, center = Offset(size.width * 0.5f, size.height * (0.32f + index * 0.18f)))
        }
    }
}

@Composable
private fun SearchIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.28f, center = Offset(size.width * 0.42f, size.height * 0.42f), style = stroke)
        drawLine(tint, Offset(size.width * 0.62f, size.height * 0.62f), Offset(size.width * 0.82f, size.height * 0.82f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}

@Composable
private fun CheckIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round)
        val path = Path().apply {
            moveTo(size.width * 0.2f, size.height * 0.52f)
            lineTo(size.width * 0.42f, size.height * 0.72f)
            lineTo(size.width * 0.82f, size.height * 0.28f)
        }
        drawPath(path, tint, style = stroke)
    }
}

@Composable
private fun NextArrowIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.2f, size.height * 0.5f), Offset(size.width * 0.78f, size.height * 0.5f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        val path = Path().apply {
            moveTo(size.width * 0.56f, size.height * 0.28f)
            lineTo(size.width * 0.78f, size.height * 0.5f)
            lineTo(size.width * 0.56f, size.height * 0.72f)
        }
        drawPath(path, tint, style = stroke)
    }
}

@Composable
private fun PersonIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.13f, center = Offset(size.width * 0.5f, size.height * 0.34f), style = stroke)
        drawArc(tint, 205f, 130f, false, topLeft = Offset(size.width * 0.26f, size.height * 0.52f), size = Size(size.width * 0.48f, size.height * 0.34f), style = stroke)
    }
}

@Composable
private fun AddPersonIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.11f, center = Offset(size.width * 0.36f, size.height * 0.36f), style = stroke)
        drawArc(tint, 205f, 130f, false, topLeft = Offset(size.width * 0.15f, size.height * 0.54f), size = Size(size.width * 0.42f, size.height * 0.28f), style = stroke)
        drawLine(tint, Offset(size.width * 0.68f, size.height * 0.38f), Offset(size.width * 0.68f, size.height * 0.72f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.51f, size.height * 0.55f), Offset(size.width * 0.85f, size.height * 0.55f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}
