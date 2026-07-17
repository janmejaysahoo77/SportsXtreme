package com.example.sportsxtreme.presentation.media

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val MediaBg = Color(0xFF010509)
private val MediaPanel = Color(0xFF07101A)
private val MediaCard = Color(0xFF0B1422)
private val MediaAccent = Color(0xFFC1FF00)
private val MediaBlue = Color(0xFF148CFF)
private val MediaMuted = Color(0xFF8F9DA6)
private val MediaStroke = Color(0x3D6A7480)

private data class MediaNavItem(val label: String, val icon: MediaIcon)
private enum class MediaIcon { HOME, REEL, SEARCH, PROFILE, PLUS, BELL, MENU, PLAY }

private val mediaNavItems = listOf(
    MediaNavItem("Home", MediaIcon.HOME),
    MediaNavItem("Reel", MediaIcon.REEL),
    MediaNavItem("Search", MediaIcon.SEARCH),
    MediaNavItem("Profile", MediaIcon.PROFILE)
)

@Composable
fun XtremeMediaScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MediaBg)
            .drawBehind {
                drawCircle(Color(0x24148CFF), radius = size.width * 0.74f, center = Offset(size.width * 0.04f, size.height * 0.1f))
                drawCircle(Color(0x1CC1FF00), radius = size.width * 0.62f, center = Offset(size.width * 1.0f, size.height * 0.34f))
                drawCircle(Color(0x1A4D00FF), radius = size.width * 0.58f, center = Offset(size.width * 0.45f, size.height * 0.95f))
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                0 -> MediaHomeContent()
                1 -> MediaReelContent()
                2 -> MediaSearchContent()
                else -> MediaProfileContent()
            }
        }
        MediaBottomNav(
            selectedTab = selectedTab,
            onSelect = { selectedTab = it },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        MediaFab(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 38.dp)
        )
    }
}

@Composable
private fun MediaTopBar(onBackToHome: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(Brush.horizontalGradient(listOf(Color(0xFF050D14), Color(0xFF06111F), Color(0xFF02070D))))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MediaIconCanvas(MediaIcon.MENU, tint = MediaMuted, modifier = Modifier.size(25.dp).clickable(onClick = onBackToHome))
        Spacer(Modifier.width(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Xtreme", color = MediaBlue, fontSize = 20.sp, lineHeight = 22.sp, fontWeight = FontWeight.ExtraBold, fontStyle = FontStyle.Italic)
            Text("Media", color = Color.White, fontSize = 18.sp, lineHeight = 20.sp, fontWeight = FontWeight.ExtraBold, fontStyle = FontStyle.Italic)
        }
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .height(27.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(Color(0x15148CFF))
                .border(1.dp, MediaBlue.copy(alpha = 0.55f), RoundedCornerShape(15.dp))
                .padding(horizontal = 11.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Creator", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
        }
        Spacer(Modifier.width(13.dp))
        MediaIconCanvas(MediaIcon.BELL, tint = MediaMuted, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun MediaHomeContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 12.dp, top = 14.dp, end = 12.dp, bottom = 98.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        FeatureCard()
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            QuickMetric("12.4K", "Views", MediaBlue, Modifier.weight(1f))
            QuickMetric("86", "Clips", MediaAccent, Modifier.weight(1f))
            QuickMetric("2.1K", "Fans", Color(0xFFFF8FB0), Modifier.weight(1f))
        }
        MediaPostCard("Matchday Pulse", "Fresh cricket moments from your network", MediaAccent)
        MediaPostCard("Creator Arena", "Upload highlights, reactions and local sports stories", MediaBlue)
    }
}

@Composable
private fun MediaReelContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 12.dp, top = 14.dp, end = 12.dp, bottom = 98.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF09182B), Color(0xFF07101A), Color(0xFF142405))))
            .border(1.dp, MediaStroke, RoundedCornerShape(13.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(78.dp)
                    .clip(CircleShape)
                    .background(MediaAccent),
                contentAlignment = Alignment.Center
            ) {
                MediaIconCanvas(MediaIcon.PLAY, tint = Color(0xFF071007), modifier = Modifier.size(34.dp))
            }
            Text("Reel Studio", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(top = 18.dp))
            Text("Swipe-ready highlights will live here.", color = MediaMuted, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
private fun MediaSearchContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 12.dp, top = 14.dp, end = 12.dp, bottom = 98.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(Color(0xFF151A21))
                .border(1.dp, Color(0xFF3B4451), RoundedCornerShape(13.dp))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MediaIconCanvas(MediaIcon.SEARCH, tint = MediaMuted, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text("Search creators, reels, teams", color = Color(0xFFD8DEE7), fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
        }
        MediaPostCard("Trending Now", "Fastest clips around your cricket circle", MediaAccent)
        MediaPostCard("Nearby Creators", "Find editors, scorers and streamers", MediaBlue)
    }
}

@Composable
private fun MediaProfileContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 12.dp, top = 14.dp, end = 12.dp, bottom = 98.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(13.dp))
                .background(MediaCard)
                .border(1.dp, MediaStroke, RoundedCornerShape(13.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(58.dp).clip(CircleShape).background(MediaBlue), contentAlignment = Alignment.Center) {
                Text("XM", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(Modifier.width(13.dp))
            Column {
                Text("Xtreme Creator", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                Text("SportsXtreme Media Profile", color = MediaMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
        MediaPostCard("Creator Toolkit", "Manage uploads, drafts and analytics", MediaAccent)
    }
}

@Composable
private fun FeatureCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(186.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF08264A), Color(0xFF07101A), Color(0xFF152606))))
            .border(1.dp, MediaBlue.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            repeat(10) { index ->
                val y = size.height * (0.12f + index * 0.07f)
                drawLine(Color.White.copy(alpha = 0.06f), Offset(0f, y), Offset(size.width, y + 20.dp.toPx()), strokeWidth = 1.dp.toPx())
            }
            drawCircle(MediaAccent.copy(alpha = 0.18f), radius = size.width * 0.35f, center = Offset(size.width * 0.78f, size.height * 0.25f))
        }
        Column(modifier = Modifier.align(Alignment.BottomStart)) {
            Text("XTREME MEDIA LIVE", color = MediaAccent, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
            Text("Create the next\nsports moment", color = Color.White, fontSize = 25.sp, lineHeight = 28.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(top = 7.dp))
            Text("Reels, stories and matchday highlights in one creator hub.", color = Color(0xFFDDE6EC), fontSize = 10.sp, lineHeight = 13.sp, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
private fun QuickMetric(value: String, label: String, accent: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .height(78.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(MediaCard)
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(11.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        Text(label, color = accent, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun MediaPostCard(title: String, subtitle: String, accent: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(82.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF09111E))
            .border(1.dp, MediaStroke, RoundedCornerShape(12.dp))
            .padding(13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(accent.copy(alpha = 0.16f))
                .border(1.dp, accent.copy(alpha = 0.42f), RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center
        ) {
            MediaIconCanvas(MediaIcon.PLAY, tint = accent, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
            Text(subtitle, color = MediaMuted, fontSize = 10.sp, lineHeight = 13.sp, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun MediaBottomNav(selectedTab: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(74.dp)
            .background(Color(0xFF050A12))
            .border(1.dp, Color(0x2FFFFFFF))
            .padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        mediaNavItems.take(2).forEachIndexed { index, item ->
            MediaNavCell(item, selected = selectedTab == index, onClick = { onSelect(index) }, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.weight(1f))
        mediaNavItems.drop(2).forEachIndexed { localIndex, item ->
            val index = localIndex + 2
            MediaNavCell(item, selected = selectedTab == index, onClick = { onSelect(index) }, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun MediaNavCell(item: MediaNavItem, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(if (selected) MediaAccent else Color.Transparent)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        MediaIconCanvas(item.icon, tint = if (selected) Color(0xFF071007) else MediaMuted, modifier = Modifier.size(22.dp))
        Text(item.label, color = if (selected) Color(0xFF071007) else MediaMuted, fontSize = 8.5.sp, fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun MediaFab(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(62.dp)
            .clip(CircleShape)
            .background(MediaAccent)
            .border(2.dp, Color(0xFFE8FF72), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        MediaIconCanvas(MediaIcon.PLUS, tint = Color(0xFF071007), modifier = Modifier.size(30.dp))
    }
}

@Composable
private fun MediaIconCanvas(icon: MediaIcon, tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        when (icon) {
            MediaIcon.HOME -> {
                val path = Path().apply {
                    moveTo(size.width * 0.18f, size.height * 0.5f)
                    lineTo(size.width * 0.5f, size.height * 0.22f)
                    lineTo(size.width * 0.82f, size.height * 0.5f)
                }
                drawPath(path, tint, style = stroke)
                drawRoundRect(tint, Offset(size.width * 0.3f, size.height * 0.48f), Size(size.width * 0.4f, size.height * 0.34f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx()), style = stroke)
            }
            MediaIcon.REEL -> {
                drawRoundRect(tint, Offset(size.width * 0.18f, size.height * 0.2f), Size(size.width * 0.64f, size.height * 0.6f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx()), style = stroke)
                drawLine(tint, Offset(size.width * 0.3f, size.height * 0.22f), Offset(size.width * 0.42f, size.height * 0.38f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.55f, size.height * 0.22f), Offset(size.width * 0.67f, size.height * 0.38f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawCircle(tint, radius = size.minDimension * 0.055f, center = Offset(size.width * 0.38f, size.height * 0.6f))
                drawCircle(tint, radius = size.minDimension * 0.055f, center = Offset(size.width * 0.62f, size.height * 0.6f))
            }
            MediaIcon.SEARCH -> {
                drawCircle(tint, radius = size.minDimension * 0.22f, center = Offset(size.width * 0.44f, size.height * 0.43f), style = stroke)
                drawLine(tint, Offset(size.width * 0.6f, size.height * 0.6f), Offset(size.width * 0.8f, size.height * 0.8f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            MediaIcon.PROFILE -> {
                drawCircle(tint, radius = size.minDimension * 0.15f, center = Offset(size.width * 0.5f, size.height * 0.34f), style = stroke)
                drawArc(tint, 205f, 130f, false, topLeft = Offset(size.width * 0.25f, size.height * 0.48f), size = Size(size.width * 0.5f, size.height * 0.36f), style = stroke)
            }
            MediaIcon.PLUS -> {
                drawLine(tint, Offset(size.width * 0.5f, size.height * 0.2f), Offset(size.width * 0.5f, size.height * 0.8f), strokeWidth = 2.6.dp.toPx(), cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.2f, size.height * 0.5f), Offset(size.width * 0.8f, size.height * 0.5f), strokeWidth = 2.6.dp.toPx(), cap = StrokeCap.Round)
            }
            MediaIcon.BELL -> {
                val path = Path().apply {
                    moveTo(size.width * 0.28f, size.height * 0.61f)
                    cubicTo(size.width * 0.3f, size.height * 0.34f, size.width * 0.38f, size.height * 0.22f, size.width * 0.5f, size.height * 0.22f)
                    cubicTo(size.width * 0.62f, size.height * 0.22f, size.width * 0.7f, size.height * 0.34f, size.width * 0.72f, size.height * 0.61f)
                    lineTo(size.width * 0.8f, size.height * 0.74f)
                    lineTo(size.width * 0.2f, size.height * 0.74f)
                    close()
                }
                drawPath(path, tint, style = stroke)
                drawLine(tint, Offset(size.width * 0.43f, size.height * 0.84f), Offset(size.width * 0.57f, size.height * 0.84f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            MediaIcon.MENU -> {
                drawLine(tint, Offset(size.width * 0.16f, size.height * 0.3f), Offset(size.width * 0.84f, size.height * 0.3f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.16f, size.height * 0.5f), Offset(size.width * 0.84f, size.height * 0.5f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.16f, size.height * 0.7f), Offset(size.width * 0.84f, size.height * 0.7f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            MediaIcon.PLAY -> {
                val path = Path().apply {
                    moveTo(size.width * 0.36f, size.height * 0.24f)
                    lineTo(size.width * 0.75f, size.height * 0.5f)
                    lineTo(size.width * 0.36f, size.height * 0.76f)
                    close()
                }
                drawPath(path, tint)
            }
        }
    }
}
