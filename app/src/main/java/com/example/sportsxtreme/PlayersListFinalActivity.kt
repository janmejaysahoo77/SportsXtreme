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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class PlayersListFinalActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applySportsXtremeWindowStyle()
        setContent {
            PlayersListFinalScreen(
                onBack = { finish() },
                onDone = { startActivity(Intent(this, MatchDetailsJustBeforeMatchActivity::class.java)) }
            )
        }
    }
}

private val FinalAccent = Color(0xFFC1FF00)
private val FinalBg = Color(0xFF020A15)
private val FinalPanel = Color(0xFF07101A)
private val FinalCard = Color(0xFF0B1523)
private val FinalStroke = Color(0xFF25314A)
private val FinalMuted = Color(0xFFADB8BD)

private data class FinalPlayer(
    val name: String,
    val subtitle: String,
    val selectedRole: String? = null
)

private val ChosenPlayers = listOf(
    FinalPlayer("Janaman Gana", "Chosen player - Batsman", "C")
)

private val ExtraPlayers = listOf(
    FinalPlayer("Anshu Gita", "All rounder", "VC"),
    FinalPlayer("Ashwini Gita", "Right hand batter"),
    FinalPlayer("Abhimanyu Majhi", "Fast bowler", "WC"),
    FinalPlayer("Raj Kumar", "Wicket keeper"),
    FinalPlayer("Sahil Dev", "Spin bowler")
)

@Composable
private fun PlayersListFinalScreen(onBack: () -> Unit, onDone: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FinalBg)
            .drawBehind {
                drawCircle(Color(0x101B5BFF), radius = size.width * 0.58f, center = Offset(size.width * 0.55f, size.height * 0.18f))
                drawCircle(Color(0x141C3F14), radius = size.width * 0.5f, center = Offset(size.width * 0.55f, size.height * 0.48f))
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            FinalTopBar(onBack)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 10.dp, vertical = 18.dp)
            ) {
                SectionHeader(title = "CHOSEN PLAYERS", count = ChosenPlayers.size)
                ChosenPlayers.forEach { player ->
                    FinalPlayerRow(
                        modifier = Modifier.padding(top = 12.dp),
                        player = player,
                        highlighted = true
                    )
                }

                SectionHeader(
                    title = "EXTRA PLAYERS",
                    count = ExtraPlayers.size,
                    modifier = Modifier.padding(top = 26.dp)
                )
                ExtraPlayers.forEach { player ->
                    FinalPlayerRow(
                        modifier = Modifier.padding(top = 10.dp),
                        player = player,
                        highlighted = false
                    )
                }
                Spacer(Modifier.height(92.dp))
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(FinalBg)
                .padding(horizontal = 10.dp, vertical = 14.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(FinalAccent)
                    .clickable(onClick = onDone),
                contentAlignment = Alignment.Center
            ) {
                Text("DONE", color = Color(0xFF111604), fontSize = 15.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun FinalTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp)
            .background(FinalPanel)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FinalBackArrow(Modifier.size(25.dp).clickable(onClick = onBack), Color.White)
        Column(modifier = Modifier.padding(start = 14.dp).weight(1f)) {
            Text("PLAYERS LIST", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black, maxLines = 1)
            Text("Dinabandhu final squad", color = FinalMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
        Box(
            modifier = Modifier
                .height(30.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(Color(0xFF3C5316))
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Total 6", color = FinalAccent, fontSize = 12.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = FinalMuted, fontSize = 12.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
        Text("$count PLAYERS", color = FinalAccent, fontSize = 12.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun FinalPlayerRow(modifier: Modifier = Modifier, player: FinalPlayer, highlighted: Boolean) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(76.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(FinalCard)
            .border(1.dp, if (highlighted) FinalAccent else Color(0xFF17243A), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlayerAvatar(highlighted = highlighted)
        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
            Text(player.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(modifier = Modifier.padding(top = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (highlighted) FinalAccent else Color(0xFF64C8FF))
                )
                Text(player.subtitle, color = FinalMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 7.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        RoleButtons(selectedRole = player.selectedRole)
    }
}

@Composable
private fun PlayerAvatar(highlighted: Boolean) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFF304E37), Color(0xFF111D20)),
                    radius = 64f
                )
            )
            .border(1.dp, if (highlighted) FinalAccent else Color(0xFF314254), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(33.dp)) {
            drawCircle(Color(0xFF1E2A2D), radius = size.minDimension * 0.48f)
            drawCircle(Color(0xFFD0B190), radius = size.minDimension * 0.16f, center = Offset(size.width * 0.5f, size.height * 0.28f))
            drawRoundRect(Color(0xFF1C5635), topLeft = Offset(size.width * 0.28f, size.height * 0.48f), size = androidx.compose.ui.geometry.Size(size.width * 0.44f, size.height * 0.36f))
        }
    }
}

@Composable
private fun RoleButtons(selectedRole: String?) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        RoleButton(label = "C", selected = selectedRole == "C")
        RoleButton(label = "VC", selected = selectedRole == "VC")
        RoleButton(label = "WC", selected = selectedRole == "WC")
    }
}

@Composable
private fun RoleButton(label: String, selected: Boolean) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(if (selected) FinalAccent else Color(0xFF111826))
            .border(1.dp, if (selected) FinalAccent else FinalStroke, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (selected) Color(0xFF111604) else Color.White,
            fontSize = if (label.length == 1) 10.sp else 8.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1
        )
    }
}

@Composable
private fun FinalBackArrow(modifier: Modifier, tint: Color) {
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
