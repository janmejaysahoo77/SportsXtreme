package com.example.sportsxtreme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LeaderboardAccent = Color(0xFFC1FF00)
private val LeaderboardBg = Color(0xFF010509)
private val LeaderboardMuted = Color(0xFF93A09D)
private val LeaderboardStroke = Color(0x3D6A7480)

private data class RankedPlayer(
    val rank: Int,
    val name: String,
    val country: String,
    val points: Int,
    val accent: Color
)

private val playerTabs = listOf("Batting", "Bowling", "All Rounder", "Teams")
private val sportFilters = listOf("Tennis", "Leather", "Box Cricket")

private val leaderboardPlayers = listOf(
    RankedPlayer(1, "Joe Root", "England", 880, LeaderboardAccent),
    RankedPlayer(2, "Harry Brook", "England", 857, Color(0xFF35CFFF)),
    RankedPlayer(3, "Travis Head", "Australia", 853, Color(0xFF35CFFF)),
    RankedPlayer(4, "Steven Smith", "Australia", 831, Color(0xFFD8A15A)),
    RankedPlayer(5, "Kane Williamson", "New Zealand", 822, Color(0xFF67E5FF)),
    RankedPlayer(6, "Kamindu Mendis", "Sri Lanka", 781, Color(0xFFD8A15A)),
    RankedPlayer(7, "Temba Bavuma", "South Africa", 775, Color(0xFF35CFFF))
)

@Composable
fun LeaderboardScreen(onMenuClick: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LeaderboardBg)
            .drawBehind {
                drawCircle(
                    color = Color(0x24203416),
                    radius = size.width * 0.72f,
                    center = Offset(size.width * 0.98f, size.height * 0.22f)
                )
                drawCircle(
                    color = Color(0x1200D2FF),
                    radius = size.width * 0.54f,
                    center = Offset(size.width * 0.02f, size.height * 0.62f)
                )
            }
    ) {
        LeaderboardTopStrip(onMenuClick = onMenuClick)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 10.dp, top = 14.dp, end = 10.dp, bottom = 92.dp)
        ) {
            item { LeaderboardTitleBlock() }
            item { PlayerTypeTabs() }
            item { SportFilterRow() }
            item { RankingTableHeader() }
            items(leaderboardPlayers) { player ->
                RankingRow(player)
            }
        }
    }
}

@Composable
private fun LeaderboardTopStrip(onMenuClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Brush.horizontalGradient(listOf(Color(0xFF050D11), Color(0xFF030A16))))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeaderMenuButton(onMenuClick)
        Spacer(Modifier.width(16.dp))
        Image(
            painter = painterResource(R.drawable.appicon),
            contentDescription = "SportsXtreme",
            modifier = Modifier
                .width(58.dp)
                .height(32.dp)
                .clip(RoundedCornerShape(5.dp))
        )
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .width(76.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(LeaderboardAccent),
            contentAlignment = Alignment.Center
        ) {
            Text("PRO @ 69", color = Color(0xFF081007), fontSize = 8.5.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
        }
        Spacer(Modifier.weight(1f))
        HeaderBell()
    }
}

@Composable
private fun LeaderboardTitleBlock() {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 10.dp)) {
        Text("Leaderboard", color = Color.White, fontSize = 20.sp, lineHeight = 23.sp, fontWeight = FontWeight.ExtraBold)
        Text(
            "Real-Time performance ranking",
            color = LeaderboardMuted,
            fontSize = 9.5.sp,
            lineHeight = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 10.dp)
        )
    }
}

@Composable
private fun PlayerTypeTabs() {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(23.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        playerTabs.forEachIndexed { index, label ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    label,
                    color = if (index == 0) LeaderboardAccent else Color(0xFFC5D1CE),
                    fontSize = 11.sp,
                    lineHeight = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    Modifier
                        .width(if (index == 0) 48.dp else 0.dp)
                        .height(2.dp)
                        .background(LeaderboardAccent)
                )
            }
        }
    }
}

@Composable
private fun SportFilterRow() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 18.dp).horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        sportFilters.forEachIndexed { index, label ->
            Box(
                modifier = Modifier
                    .height(33.dp)
                    .clip(RoundedCornerShape(16.5.dp))
                    .background(if (index == 0) LeaderboardAccent else Color(0xFF171D27))
                    .border(1.dp, if (index == 0) Color(0xFFE2FF67) else Color(0xFF3A4657), RoundedCornerShape(16.5.dp))
                    .padding(horizontal = 17.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    color = if (index == 0) Color(0xFF101604) else Color(0xFFD7DFE5),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun RankingTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(31.dp)
            .clip(RoundedCornerShape(topStart = 9.dp, topEnd = 9.dp))
            .background(Color(0xFF111723))
            .border(1.dp, LeaderboardStroke, RoundedCornerShape(topStart = 9.dp, topEnd = 9.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("RANK", color = LeaderboardMuted, fontSize = 8.5.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.width(58.dp))
        Text("PLAYER", color = LeaderboardMuted, fontSize = 8.5.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
        Text("POINTS", color = LeaderboardMuted, fontSize = 8.5.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun RankingRow(player: RankedPlayer) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Color(0xFF0B1018))
            .border(0.6.dp, LeaderboardStroke)
            .padding(start = 16.dp, end = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.width(42.dp)) {
            Text(
                player.rank.toString(),
                color = if (player.rank == 1) LeaderboardAccent else Color.White,
                fontSize = 21.sp,
                lineHeight = 23.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text("--", color = LeaderboardMuted, fontSize = 8.sp, lineHeight = 9.sp, fontWeight = FontWeight.Bold)
        }
        PlayerAvatar(player)
        Spacer(Modifier.width(13.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                player.name,
                color = Color.White,
                fontSize = 16.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(player.country, color = Color(0xFF9EAAA7), fontSize = 9.5.sp, lineHeight = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
        Text(
            player.points.toString(),
            color = Color(0xFFE6EDF5),
            fontSize = 22.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun PlayerAvatar(player: RankedPlayer) {
    Box(
        modifier = Modifier
            .size(47.dp)
            .clip(CircleShape)
            .background(Color(0xFF0F1720))
            .border(1.3.dp, player.accent, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(player.accent.copy(alpha = 0.16f), radius = size.minDimension * 0.48f, center = center)
            drawCircle(Color(0xFF172A31), radius = size.minDimension * 0.31f, center = Offset(size.width * 0.5f, size.height * 0.38f))
            drawCircle(Color(0xFF415E63), radius = size.minDimension * 0.15f, center = Offset(size.width * 0.5f, size.height * 0.31f))
            drawArc(
                color = Color(0xFF425C62),
                startAngle = 205f,
                sweepAngle = 130f,
                useCenter = false,
                topLeft = Offset(size.width * 0.25f, size.height * 0.44f),
                size = Size(size.width * 0.5f, size.height * 0.38f),
                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Text(
            player.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun HeaderMenuButton(onMenuClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(7.dp))
            .clickable(onClick = onMenuClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(18.dp)) {
            val tint = Color(0xFF8E9E99)
            val stroke = 1.8.dp.toPx()
            drawLine(tint, Offset(size.width * 0.16f, size.height * 0.28f), Offset(size.width * 0.84f, size.height * 0.28f), strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(tint, Offset(size.width * 0.16f, size.height * 0.5f), Offset(size.width * 0.84f, size.height * 0.5f), strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(tint, Offset(size.width * 0.16f, size.height * 0.72f), Offset(size.width * 0.84f, size.height * 0.72f), strokeWidth = stroke, cap = StrokeCap.Round)
        }
    }
}

@Composable
private fun HeaderBell() {
    Canvas(Modifier.size(21.dp)) {
        val tint = Color(0xFF8E9E99)
        val stroke = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round)
        val bell = Path().apply {
            moveTo(size.width * 0.28f, size.height * 0.61f)
            cubicTo(size.width * 0.3f, size.height * 0.34f, size.width * 0.38f, size.height * 0.22f, size.width * 0.5f, size.height * 0.22f)
            cubicTo(size.width * 0.62f, size.height * 0.22f, size.width * 0.7f, size.height * 0.34f, size.width * 0.72f, size.height * 0.61f)
            lineTo(size.width * 0.8f, size.height * 0.74f)
            lineTo(size.width * 0.2f, size.height * 0.74f)
            close()
        }
        drawPath(bell, tint, style = stroke)
        drawLine(tint, Offset(size.width * 0.43f, size.height * 0.84f), Offset(size.width * 0.57f, size.height * 0.84f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}
