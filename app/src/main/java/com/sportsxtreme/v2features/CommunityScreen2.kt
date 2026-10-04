package com.sportsxtreme.v2features

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sportsxtreme.R

// Color Palette
private val GreenAccent = Color(0xFFC1FF00)
private val CyanAccent = Color(0xFF00D2FF)
private val ScreenBg = Color(0xFF010509)
private val MutedText = Color(0xFF9AA7A3)
private val StrokeBorder = Color(0x334E5B64)

private data class CommunityCategory(
    val title: String,
    val subtitle: String,
    @get:DrawableRes val icon: Int,
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

private val categories = listOf(
    CommunityCategory("Scorers", "OFFICIAL", R.drawable.scorerss, GreenAccent),
    CommunityCategory("Umpires", "COURT ROLE", R.drawable.umpires, Color(0xFF3AD7FF)),
    CommunityCategory("Voices", "LIVE", R.drawable.commentatorss, Color(0xFFFF8EAE)),
    CommunityCategory("Streams", "BROADCASTERS", R.drawable.stremers, GreenAccent),
    CommunityCategory("Organisers", "EVENTS", R.drawable.organiserss, Color(0xFF4DE9FF)),
    CommunityCategory("Academies", "TRAINING", R.drawable.academy, Color(0xFFFFA3B7)),
    CommunityCategory("Grounds", "VENUES", R.drawable.ground, GreenAccent),
    CommunityCategory("Box", "NETS", R.drawable.boxnnets, Color(0xFF71D8FF))
)

private val filters = listOf("Odisha", "Opponent", "Team to Join", "Player", "Clubs")

private val opportunitiesList = listOf(
    Opportunity(
        name = "Rahul Sharma",
        role = "PRO BOWLER",
        description = "Experienced right-arm fast bowler available for weekend tournaments & league matches.",
        meta = "Match fee",
        price = "₹2,500",
        action = "Contact",
        age = "2h ago",
        accent = GreenAccent,
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
fun CommunityScreen2(
    onMenuClick: () -> Unit = {}
) {
    var selectedFilterIndex by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
            .drawBehind {
                drawCircle(
                    color = Color(0x29223416),
                    radius = size.width * 0.75f,
                    center = Offset(size.width * 0.95f, size.height * 0.25f)
                )
                drawCircle(
                    color = Color(0x1400D2FF),
                    radius = size.width * 0.55f,
                    center = Offset(size.width * 0.05f, size.height * 0.65f)
                )
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Top Header Bar
            HeaderBar(onMenuClick = onMenuClick)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 2. Category Grid (2 rows x 4 items)
                item { CategoryGrid() }

                // 3. Filter Options Row
                item {
                    FilterRow(
                        selectedIndex = selectedFilterIndex,
                        onSelectFilter = { selectedFilterIndex = it }
                    )
                }

                // 4. New Opportunities Title
                item {
                    Text(
                        text = "New Opportunities",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }

                // 5. Opportunities Items
                items(opportunitiesList) { opportunity ->
                    OpportunityCard(opportunity = opportunity)
                }

                // 6. Premium Banner
                item { PremiumSignalCard() }

                // 34.dp Bottom Padding Spacer
                item {
                    Spacer(modifier = Modifier.height(34.dp))
                }
            }
        }
    }
}

@Composable
private fun HeaderBar(onMenuClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Hamburger Menu
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onMenuClick),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(20.dp)) {
                val stroke = 2.dp.toPx()
                val color = Color.White
                drawLine(color, Offset(0f, size.height * 0.2f), Offset(size.width, size.height * 0.2f), stroke, StrokeCap.Round)
                drawLine(color, Offset(0f, size.height * 0.5f), Offset(size.width, size.height * 0.5f), stroke, StrokeCap.Round)
                drawLine(color, Offset(0f, size.height * 0.8f), Offset(size.width, size.height * 0.8f), stroke, StrokeCap.Round)
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // SportsXtreme Logo with Brand Drawable
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.splash_brand_tight_logo),
                contentDescription = "Brand Logo",
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Sports",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Xtreme",
                color = CyanAccent,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Search Icon
        Canvas(
            modifier = Modifier
                .size(20.dp)
                .clickable { }
        ) {
            val stroke = 2.dp.toPx()
            drawCircle(Color.White, radius = size.width * 0.35f, center = Offset(size.width * 0.4f, size.height * 0.4f), style = Stroke(stroke))
            drawLine(Color.White, Offset(size.width * 0.65f, size.height * 0.65f), Offset(size.width * 0.95f, size.height * 0.95f), stroke, StrokeCap.Round)
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Notification Icon (@drawable/ghanti)
        Image(
            painter = painterResource(id = R.drawable.ghanti),
            contentDescription = "Notifications",
            modifier = Modifier
                .size(22.dp)
                .clickable { }
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Message Icon
        Canvas(
            modifier = Modifier
                .size(20.dp)
                .clickable { }
        ) {
            val stroke = 1.8.dp.toPx()
            val rectPath = Path().apply {
                moveTo(size.width * 0.1f, size.height * 0.2f)
                lineTo(size.width * 0.9f, size.height * 0.2f)
                lineTo(size.width * 0.9f, size.height * 0.7f)
                lineTo(size.width * 0.4f, size.height * 0.7f)
                lineTo(size.width * 0.2f, size.height * 0.9f)
                lineTo(size.width * 0.2f, size.height * 0.7f)
                lineTo(size.width * 0.1f, size.height * 0.7f)
                close()
            }
            drawPath(rectPath, Color.White, style = Stroke(stroke, cap = StrokeCap.Round))
            drawLine(Color.White, Offset(size.width * 0.3f, size.height * 0.4f), Offset(size.width * 0.7f, size.height * 0.4f), stroke, StrokeCap.Round)
            drawLine(Color.White, Offset(size.width * 0.3f, size.height * 0.55f), Offset(size.width * 0.55f, size.height * 0.55f), stroke, StrokeCap.Round)
        }
    }
}

@Composable
private fun CategoryGrid() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        categories.chunked(4).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { category ->
                    CategoryTile(category = category, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CategoryTile(category: CommunityCategory, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .height(132.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF222830), Color(0xFF171D22), Color(0xFF11161B))
                )
            )
            .border(1.dp, Color(0x4A657079), RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(category.glow.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = category.icon),
                contentDescription = category.title,
                modifier = Modifier.size(42.dp)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = category.title,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = category.subtitle,
                color = category.glow,
                fontSize = 8.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FilterRow(
    selectedIndex: Int,
    onSelectFilter: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterSlidersButton()

        filters.forEachIndexed { index, label ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .height(30.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) GreenAccent else Color(0xFF1B2126))
                    .border(1.dp, if (isSelected) Color(0xFFDFFF6B) else StrokeBorder, CircleShape)
                    .clickable { onSelectFilter(index) }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (isSelected) Color(0xFF101604) else Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun FilterSlidersButton() {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF21272D))
            .border(1.dp, StrokeBorder, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(16.dp)) {
            val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
            val tint = Color.White.copy(alpha = 0.9f)
            drawLine(tint, Offset(size.width * 0.15f, size.height * 0.25f), Offset(size.width * 0.85f, size.height * 0.25f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            drawLine(tint, Offset(size.width * 0.15f, size.height * 0.5f), Offset(size.width * 0.85f, size.height * 0.5f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            drawLine(tint, Offset(size.width * 0.15f, size.height * 0.75f), Offset(size.width * 0.85f, size.height * 0.75f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            drawCircle(GreenAccent, radius = 1.8.dp.toPx(), center = Offset(size.width * 0.35f, size.height * 0.25f))
            drawCircle(GreenAccent, radius = 1.8.dp.toPx(), center = Offset(size.width * 0.65f, size.height * 0.5f))
            drawCircle(GreenAccent, radius = 1.8.dp.toPx(), center = Offset(size.width * 0.45f, size.height * 0.75f))
        }
    }
}

@Composable
private fun OpportunityCard(opportunity: Opportunity) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF191E24), Color(0xFF14191E), Color(0xFF19221B))
                )
            )
            .border(1.dp, StrokeBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Text(
            text = opportunity.age,
            color = MutedText,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.TopEnd)
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            BlankAvatar(accent = opportunity.accent)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = opportunity.name,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = opportunity.role,
                    color = opportunity.accent,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )

                if (opportunity.description.isNotBlank()) {
                    Text(
                        text = opportunity.description,
                        color = Color(0xFFE0EAE6),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(top = 10.dp, end = 4.dp)
                    )
                }

                opportunity.location?.let { loc ->
                    MetaLine(text = loc, modifier = Modifier.padding(top = 10.dp))
                }

                opportunity.league?.let { lg ->
                    MetaLine(text = lg, modifier = Modifier.padding(top = 4.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (opportunity.price != null) {
                        Text(
                            text = opportunity.price,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        if (opportunity.meta.isNotBlank()) {
                            Text(
                                text = " /${opportunity.meta}",
                                color = MutedText,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        opportunity.bubble?.let { SmallBubble(text = it) }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    ActionPill(label = opportunity.action)
                }
            }
        }
    }
}

@Composable
private fun BlankAvatar(accent: Color) {
    Canvas(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color(0xFF0E1419))
    ) {
        drawCircle(color = accent.copy(alpha = 0.18f), radius = size.minDimension * 0.47f, center = center)
        drawCircle(color = accent, radius = size.minDimension * 0.46f, center = center, style = Stroke(width = 1.5.dp.toPx()))
        drawCircle(color = Color.White.copy(alpha = 0.15f), radius = size.minDimension * 0.22f, center = Offset(size.width * 0.5f, size.height * 0.42f), style = Stroke(width = 1.2.dp.toPx()))
        drawArc(
            color = Color.White.copy(alpha = 0.15f),
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
        Canvas(modifier = Modifier.size(10.dp)) {
            val tint = MutedText
            val path = Path().apply {
                moveTo(size.width * 0.5f, size.height * 0.92f)
                cubicTo(size.width * 0.18f, size.height * 0.56f, size.width * 0.24f, size.height * 0.16f, size.width * 0.5f, size.height * 0.16f)
                cubicTo(size.width * 0.76f, size.height * 0.16f, size.width * 0.82f, size.height * 0.56f, size.width * 0.5f, size.height * 0.92f)
            }
            drawPath(path, tint, style = Stroke(width = 1.3.dp.toPx(), cap = StrokeCap.Round))
            drawCircle(tint, radius = 1.2.dp.toPx(), center = Offset(size.width * 0.5f, size.height * 0.43f))
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = Color(0xFFC8D2CE),
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SmallBubble(text: String) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(Color(0xFF11161B))
            .border(1.dp, StrokeBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ActionPill(label: String) {
    Box(
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(1.4.dp, GreenAccent, RoundedCornerShape(10.dp))
            .background(Color(0x201B2404))
            .clickable { }
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = GreenAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PremiumSignalCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF101419), Color(0xFF111A12), Color(0xFF263815))
                )
            )
            .border(1.dp, Color(0x263A462F), RoundedCornerShape(12.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            repeat(16) { i ->
                val x = size.width * (i / 15f)
                drawLine(
                    color = Color.White.copy(alpha = 0.02f),
                    start = Offset(x, 0f),
                    end = Offset(x + size.width * 0.18f, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }
    }
}
