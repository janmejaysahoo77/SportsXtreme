package com.example.sportsxtreme.presentation.home

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
import androidx.annotation.DrawableRes
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

private val CommunityAccent = Color(0xFFC1FF00)
private val CommunityBg = Color(0xFF010509)
private val CommunityPanel = Color(0xFF171C22)
private val CommunityMuted = Color(0xFF9AA7A3)
private val CommunityStroke = Color(0x334E5B64)

private data class CommunityCategory(
    val title: String,
    val subtitle: String,
    @DrawableRes val icon: Int,
    val glow: Color
)

private data class Opportunity(
    val name: String,
    val role: String,
    val description: String,
    val meta: String,
    val price: String?,
    val action: String,
    val age: String,
    val accent: Color,
    val location: String? = null,
    val league: String? = null,
    val bubble: String? = null
)

private val communityCategories = listOf(
    CommunityCategory("Scorers", "OFFICIAL", R.drawable.scorerss, Color(0xFFC1FF00)),
    CommunityCategory("Umpires", "COURT ROLE", R.drawable.umpires, Color(0xFF3AD7FF)),
    CommunityCategory("Voices", "LIVE", R.drawable.commentatorss, Color(0xFFFF8EAE)),
    CommunityCategory("Streams", "BROADCASTERS", R.drawable.stremers, Color(0xFFC1FF00)),
    CommunityCategory("Organisers", "EVENTS", R.drawable.organiserss, Color(0xFF4DE9FF)),
    CommunityCategory("Academies", "TRAINING", R.drawable.academy, Color(0xFFFFA3B7)),
    CommunityCategory("Grounds", "VENUES", R.drawable.ground, Color(0xFFC1FF00)),
    CommunityCategory("Box", "NETS", R.drawable.boxnnets, Color(0xFF71D8FF))
)

private val communityFilters = listOf("Odisha", "Opponent", "Team to Join", "Player")

private val opportunities = listOf(
    Opportunity(
        name = "Rahul Sharma",
        role = "PRO BOWLER",
        description = "",
        meta = "Match fee",
        price = "₹2,500",
        action = "Contact",
        age = "2h ago",
        accent = CommunityAccent,
        location = "Kalinga Stadium, Bhubaneswar",
        league = "T20 Corporate League"
    ),
    Opportunity(
        name = "Anjali Mohanty",
        role = "LEAGUE ORGANIZER",
        description = "Looking for a professional umpire for our weekend championship finals. Must be BCCI Level-1 certified.",
        meta = "",
        price = null,
        action = "Apply Now",
        age = "5h ago",
        accent = Color(0xFF69E9FF),
        bubble = "+12"
    ),
    Opportunity(
        name = "Vikram Singh",
        role = "TEAM CAPTAIN",
        description = "Need two reliable players for Sunday night box cricket. Fast fielders preferred.",
        meta = "Tonight",
        price = null,
        action = "Join",
        age = "8h ago",
        accent = Color(0xFFFF99B1),
        location = "KIIT Sports Complex"
    )
)

@Composable
fun CommunityScreen(onMenuClick: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CommunityBg)
            .drawBehind {
                drawCircle(
                    color = Color(0x29223416),
                    radius = size.width * 0.72f,
                    center = Offset(size.width * 0.94f, size.height * 0.28f)
                )
                drawCircle(
                    color = Color(0x1400D2FF),
                    radius = size.width * 0.54f,
                    center = Offset(size.width * 0.02f, size.height * 0.2f)
                )
            }
    ) {
        CommunityTopStrip(onMenuClick = onMenuClick)
        Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 10.dp, top = 14.dp, end = 10.dp, bottom = 92.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            item { CategoryGrid() }
            item { FilterRow() }
            item {
                Text(
                    text = "New Opportunities",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)
                )
            }
            items(opportunities) { opportunity ->
                OpportunityCard(opportunity)
            }
            item { PremiumSignalCard() }
        }

        FloatingAddButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 18.dp, bottom = 20.dp)
        )
        }
    }
}

@Composable
private fun CommunityTopStrip(onMenuClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF050D11), Color(0xFF030A16))
                )
            )
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
        Spacer(Modifier.width(20.dp))
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
                .background(CommunityAccent),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "PRO @ 69",
                color = Color(0xFF081007),
                fontSize = 8.5.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
        }
        Spacer(Modifier.weight(1f))
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
}

@Composable
private fun CategoryGrid() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        communityCategories.chunked(4).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                rowItems.forEach { category ->
                    CategoryTile(category, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CategoryTile(category: CommunityCategory, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .height(142.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF242A31), Color(0xFF171D21), Color(0xFF11171B))
                )
            )
            .border(1.dp, Color(0x5A657079), RoundedCornerShape(10.dp))
            .padding(top = 18.dp, bottom = 14.dp, start = 3.dp, end = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .size(66.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(category.glow.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(category.icon),
                contentDescription = category.title,
                modifier = Modifier.size(48.dp)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = category.title,
                color = Color.White,
                fontSize = 11.sp,
                lineHeight = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = category.subtitle,
                color = category.glow,
                fontSize = 7.8.sp,
                lineHeight = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FilterRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterSlidersIcon()
        communityFilters.forEachIndexed { index, label ->
            Box(
                modifier = Modifier
                    .height(27.dp)
                    .clip(RoundedCornerShape(13.5.dp))
                    .background(if (index == 0) CommunityAccent else Color(0xFF202428))
                    .border(
                        1.dp,
                        if (index == 0) Color(0xFFDFFF6B) else Color(0x334E5B64),
                        RoundedCornerShape(13.5.dp)
                    )
                    .padding(horizontal = 15.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (index == 0) Color(0xFF1D2503) else Color.White,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun FilterSlidersIcon() {
    Box(
        modifier = Modifier
            .size(27.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF23282D))
            .border(1.dp, CommunityStroke, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(16.dp)) {
            val stroke = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round)
            val tint = Color.White.copy(alpha = 0.86f)
            drawLine(tint, Offset(size.width * 0.14f, size.height * 0.25f), Offset(size.width * 0.86f, size.height * 0.25f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            drawLine(tint, Offset(size.width * 0.14f, size.height * 0.5f), Offset(size.width * 0.86f, size.height * 0.5f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            drawLine(tint, Offset(size.width * 0.14f, size.height * 0.75f), Offset(size.width * 0.86f, size.height * 0.75f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            drawCircle(CommunityAccent, radius = 1.7.dp.toPx(), center = Offset(size.width * 0.34f, size.height * 0.25f))
            drawCircle(CommunityAccent, radius = 1.7.dp.toPx(), center = Offset(size.width * 0.64f, size.height * 0.5f))
            drawCircle(CommunityAccent, radius = 1.7.dp.toPx(), center = Offset(size.width * 0.45f, size.height * 0.75f))
        }
    }
}

@Composable
private fun OpportunityCard(opportunity: Opportunity) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF191D22), Color(0xFF14191E), Color(0xFF1B2119)),
                    start = Offset.Zero,
                    end = Offset.Infinite
                )
            )
            .border(1.dp, Color(0x354E5B64), RoundedCornerShape(11.dp))
            .padding(13.dp)
    ) {
        Text(
            text = opportunity.age,
            color = CommunityMuted,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.TopEnd)
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            BlankAvatar(accent = opportunity.accent)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = opportunity.name,
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = opportunity.role,
                    color = opportunity.accent,
                    fontSize = 8.sp,
                    lineHeight = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
                if (opportunity.description.isNotBlank()) {
                    Text(
                        text = opportunity.description,
                        color = Color(0xFFE4ECE9),
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(top = 13.dp, end = 4.dp)
                    )
                }
                opportunity.location?.let {
                    MetaLine(text = it, modifier = Modifier.padding(top = 12.dp))
                }
                opportunity.league?.let {
                    MetaLine(text = it, modifier = Modifier.padding(top = 4.dp))
                }
                Spacer(Modifier.height(18.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (opportunity.price != null) {
                        Text(
                            text = opportunity.price,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        if (opportunity.meta.isNotBlank()) {
                            Text(
                                text = " /${opportunity.meta}",
                                color = CommunityMuted,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        opportunity.bubble?.let { SmallBubble(it) }
                    }
                    Spacer(Modifier.weight(1f))
                    ActionPill(opportunity.action)
                }
            }
        }
    }
}

@Composable
private fun BlankAvatar(accent: Color) {
    Canvas(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Color(0xFF10161B))
    ) {
        drawCircle(
            color = accent.copy(alpha = 0.18f),
            radius = size.minDimension * 0.47f,
            center = center
        )
        drawCircle(
            color = accent,
            radius = size.minDimension * 0.46f,
            center = center,
            style = Stroke(width = 1.4.dp.toPx())
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.12f),
            radius = size.minDimension * 0.22f,
            center = Offset(size.width * 0.5f, size.height * 0.42f),
            style = Stroke(width = 1.2.dp.toPx())
        )
        drawArc(
            color = Color.White.copy(alpha = 0.12f),
            startAngle = 205f,
            sweepAngle = 130f,
            useCenter = false,
            topLeft = Offset(size.width * 0.25f, size.height * 0.52f),
            size = Size(size.width * 0.5f, size.height * 0.42f),
            style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun MetaLine(text: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.size(10.dp)) {
            val tint = CommunityMuted
            val path = Path().apply {
                moveTo(size.width * 0.5f, size.height * 0.92f)
                cubicTo(size.width * 0.18f, size.height * 0.56f, size.width * 0.24f, size.height * 0.16f, size.width * 0.5f, size.height * 0.16f)
                cubicTo(size.width * 0.76f, size.height * 0.16f, size.width * 0.82f, size.height * 0.56f, size.width * 0.5f, size.height * 0.92f)
            }
            drawPath(path, tint, style = Stroke(width = 1.3.dp.toPx(), cap = StrokeCap.Round))
            drawCircle(tint, radius = 1.2.dp.toPx(), center = Offset(size.width * 0.5f, size.height * 0.43f))
        }
        Spacer(Modifier.width(5.dp))
        Text(
            text = text,
            color = Color(0xFFC8D2CE),
            fontSize = 10.sp,
            lineHeight = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SmallBubble(text: String) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(Color(0xFF11161B))
            .border(1.dp, CommunityStroke, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ActionPill(label: String) {
    Box(
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(1.4.dp, CommunityAccent, RoundedCornerShape(10.dp))
            .background(Color(0x201B2404))
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = CommunityAccent, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun PremiumSignalCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(118.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF101419), Color(0xFF111A12), Color(0xFF263815))
                )
            )
            .border(1.dp, Color(0x263A462F), RoundedCornerShape(12.dp))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            repeat(16) { i ->
                val x = size.width * (i / 15f)
                drawLine(
                    color = Color.White.copy(alpha = 0.018f),
                    start = Offset(x, 0f),
                    end = Offset(x + size.width * 0.18f, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }
    }
}

@Composable
private fun FloatingAddButton(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(CommunityAccent)
            .border(2.dp, Color(0xFFE6FF6E), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(22.dp)) {
            drawLine(
                color = Color(0xFF101604),
                start = Offset(size.width * 0.5f, size.height * 0.18f),
                end = Offset(size.width * 0.5f, size.height * 0.82f),
                strokeWidth = 2.6.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFF101604),
                start = Offset(size.width * 0.18f, size.height * 0.5f),
                end = Offset(size.width * 0.82f, size.height * 0.5f),
                strokeWidth = 2.6.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}
