package com.sportsxtreme.v2features

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sportsxtreme.R
import com.example.sportsxtreme.presentation.profile.Lime

// Color Palette matching pixel-perfect screenshots
private val GreenAccent = Color(0xFFC1FF00)
private val CyanAccent = Color(0xFF00D2FF)
private val GoldAccent = Color(0xFFFFB800)
private val ScreenBg = Color(0xFF010509)
private val CardBg = Color(0xFF080F14)
private val MutedText = Color(0xFF93A09D)
private val TableBorder = Color(0x1F6A7480)
private val PillBorder = Color(0xFFC1FF00)
private val TrendUpColor = Color(0xFF28D158)
private val TrendDownColor = Color(0xFFFF3B30)

enum class LeaderboardTab {
    PLAYER, TEAMS, CLUBS
}

enum class TrendType {
    UP, DOWN, SAME
}

data class Trend(
    val type: TrendType,
    val amount: Int = 0
)

data class PlayerRank(
    val rank: Int,
    val trend: Trend,
    val initials: String,
    val name: String,
    val country: String,
    val teamCode: String,
    val points: Int,
    val avatarBorderColor: Color
)

data class ClubRank(
    val rank: Int,
    val trend: Trend,
    val code: String,
    val fullName: String,
    val points: Int
)

// Default Data for Players (Image 1 & Image 2)
private val playerLeaderboardData = listOf(
    PlayerRank(1, Trend(TrendType.UP, 2), "VK", "Virat Kohli", "India", "RCB", 9120, GreenAccent),
    PlayerRank(2, Trend(TrendType.DOWN, 1), "RG", "Rohit Sharma", "India", "MI", 8950, CyanAccent),
    PlayerRank(3, Trend(TrendType.SAME), "SS", "Suryakumar Yadav", "India", "MI", 8786, CyanAccent),
    PlayerRank(4, Trend(TrendType.UP, 3), "SK", "Shubman Gill", "India", "GT", 8643, CyanAccent),
    PlayerRank(5, Trend(TrendType.DOWN, 1), "RR", "Riyan Parag", "India", "RR", 8421, CyanAccent),
    PlayerRank(6, Trend(TrendType.SAME), "PV", "Pat Cummins", "Australia", "SRH", 8215, GoldAccent),
    PlayerRank(7, Trend(TrendType.UP, 4), "MS", "Marcus Stoinis", "Australia", "LSG", 7980, CyanAccent),
    PlayerRank(8, Trend(TrendType.DOWN, 2), "HD", "Heinrich Klaasen", "South Africa", "SRH", 7763, GoldAccent),
    PlayerRank(9, Trend(TrendType.SAME), "SA", "Sanju Samson", "India", "RR", 7541, CyanAccent),
    PlayerRank(10, Trend(TrendType.UP, 1), "DJ", "David Miller", "South Africa", "GT", 7310, CyanAccent)
)

// Default Data for Clubs & Teams (Image 3)
private val clubLeaderboardData = listOf(
    ClubRank(1, Trend(TrendType.UP, 2), "RCB", "Royal Challengers Bengaluru", 9120),
    ClubRank(2, Trend(TrendType.DOWN, 1), "MI", "Mumbai Indians", 8950),
    ClubRank(3, Trend(TrendType.SAME), "GT", "Gujarat Titans", 8786),
    ClubRank(4, Trend(TrendType.UP, 1), "RR", "Rajasthan Royals", 8643),
    ClubRank(5, Trend(TrendType.DOWN, 1), "SRH", "Sunrisers Hyderabad", 8421),
    ClubRank(6, Trend(TrendType.SAME), "LSG", "Lucknow Super Giants", 8215),
    ClubRank(7, Trend(TrendType.UP, 2), "CSK", "Chennai Super Kings", 7980),
    ClubRank(8, Trend(TrendType.DOWN, 1), "KKR", "Kolkata Knight Riders", 7763),
    ClubRank(9, Trend(TrendType.SAME), "PBKS", "Punjab Kings", 7541),
    ClubRank(10, Trend(TrendType.UP, 1), "DC", "Delhi Capitals", 7310)
)

@Composable
fun LeaderBoardScreen2(
    onMenuClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(LeaderboardTab.PLAYER) }
    
    // Filter states
    var selectedSkill by remember { mutableStateOf("Batting") }
    var selectedType by remember { mutableStateOf("Leather") }
    var selectedFormat by remember { mutableStateOf("Limited Over") }

    // Dropdown visibility states
    var isSkillDropdownOpen by remember { mutableStateOf(false) }
    var isTypeDropdownOpen by remember { mutableStateOf(false) }
    var isFormatDropdownOpen by remember { mutableStateOf(false) }

    val skills = listOf("Batting", "Bowling", "All rounder")
    val types = listOf("Leather", "Tennis", "Others")
    val formats = listOf("Limited Over", "T20", "T10", "Test Cricket", "Box / Tuf")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
            .drawBehind {
                drawCircle(
                    color = Color(0x1F203416),
                    radius = size.width * 0.8f,
                    center = Offset(size.width * 0.95f, size.height * 0.2f)
                )
                drawCircle(
                    color = Color(0x1200D2FF),
                    radius = size.width * 0.6f,
                    center = Offset(size.width * 0.05f, size.height * 0.65f)
                )
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Top Header Bar
            HeaderBar(onMenuClick = onMenuClick)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 114.dp)
            ) {
                // 2. Title & Subtitle
                item {
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        Text(
                            text = "Leaderboard",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Real-Time performance ranking",
                            color = MutedText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // 3. Navigation Tabs (Player / Teams / Clubs)
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    TabsRow(
                        selectedTab = selectedTab,
                        onTabSelected = { tab ->
                            selectedTab = tab
                            // Close dropdowns when changing tab
                            isSkillDropdownOpen = false
                            isTypeDropdownOpen = false
                            isFormatDropdownOpen = false
                        }
                    )
                }

                // 4. Filters Row & Dropdown Menus (Only for Player & Teams tabs)
                if (selectedTab == LeaderboardTab.PLAYER || selectedTab == LeaderboardTab.TEAMS) {
                    item {
                        Spacer(modifier = Modifier.height(14.dp))
                        FilterSection(
                            selectedSkill = selectedSkill,
                            selectedType = selectedType,
                            selectedFormat = selectedFormat,
                            isSkillDropdownOpen = isSkillDropdownOpen,
                            isTypeDropdownOpen = isTypeDropdownOpen,
                            isFormatDropdownOpen = isFormatDropdownOpen,
                            skills = skills,
                            types = types,
                            formats = formats,
                            onToggleSkill = {
                                isSkillDropdownOpen = !isSkillDropdownOpen
                                isTypeDropdownOpen = false
                                isFormatDropdownOpen = false
                            },
                            onToggleType = {
                                isTypeDropdownOpen = !isTypeDropdownOpen
                                isSkillDropdownOpen = false
                                isFormatDropdownOpen = false
                            },
                            onToggleFormat = {
                                isFormatDropdownOpen = !isFormatDropdownOpen
                                isSkillDropdownOpen = false
                                isTypeDropdownOpen = false
                            },
                            onSelectSkill = {
                                selectedSkill = it
                                isSkillDropdownOpen = false
                            },
                            onSelectType = {
                                selectedType = it
                                isTypeDropdownOpen = false
                            },
                            onSelectFormat = {
                                selectedFormat = it
                                isFormatDropdownOpen = false
                            }
                        )
                    }
                }

                // 5. Table Header
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    TableHeader(tab = selectedTab)
                }

                // 6. Ranking Rows
                when (selectedTab) {
                    LeaderboardTab.PLAYER -> {
                        items(playerLeaderboardData) { player ->
                            PlayerRankingRow(player = player)
                        }
                    }
                    LeaderboardTab.TEAMS, LeaderboardTab.CLUBS -> {
                        items(clubLeaderboardData) { club ->
                            ClubRankingRow(club = club)
                        }
                    }
                }

                // 34.dp bottom padding item
                item {
                    Spacer(modifier = Modifier.height(34.dp))
                }
            }
        }
    }
}

// ==========================================
// COMPOSABLE COMPONENTS
// ==========================================

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

        // SportsXtreme Logo with @drawable/splash_brand_tight_logo
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.splash_brand_tight_logo),
                contentDescription = "SportsXtreme Brand Logo",
                modifier = Modifier
                    .size(35.dp)
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

        // Notification Icon using @drawable/ghanti
        Image(
            painter = painterResource(id = R.drawable.ghanti),
            contentDescription = "Notifications",
            modifier = Modifier
                .size(30.dp)
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
private fun TabsRow(
    selectedTab: LeaderboardTab,
    onTabSelected: (LeaderboardTab) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LeaderboardTab.entries.forEach { tab ->
            val isSelected = selectedTab == tab
            val tabName = when (tab) {
                LeaderboardTab.PLAYER -> "Player"
                LeaderboardTab.TEAMS -> "Teams"
                LeaderboardTab.CLUBS -> "Clubs"
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabSelected(tab) }
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = tabName,
                    color = if (isSelected) GreenAccent else MutedText,
                    fontSize = 15.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(2.5.dp)
                            .background(GreenAccent, shape = RoundedCornerShape(2.dp))
                    )
                } else {
                    Spacer(modifier = Modifier.height(2.5.dp))
                }
            }
        }
    }
}

@Composable
private fun FilterSection(
    selectedSkill: String,
    selectedType: String,
    selectedFormat: String,
    isSkillDropdownOpen: Boolean,
    isTypeDropdownOpen: Boolean,
    isFormatDropdownOpen: Boolean,
    skills: List<String>,
    types: List<String>,
    formats: List<String>,
    onToggleSkill: () -> Unit,
    onToggleType: () -> Unit,
    onToggleFormat: () -> Unit,
    onSelectSkill: (String) -> Unit,
    onSelectType: (String) -> Unit,
    onSelectFormat: (String) -> Unit
) {
    Column {
        // Pills Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Pill 1: Skill (Batting / Bowling / All rounder)
            FilterPill(
                iconType = FilterIconType.PERSON,
                label = selectedSkill,
                isOpen = isSkillDropdownOpen,
                onClick = onToggleSkill
            )

            // Pill 2: Type (Leather / Tennis / Others)
            FilterPill(
                iconType = FilterIconType.BALL,
                label = selectedType,
                isOpen = isTypeDropdownOpen,
                onClick = onToggleType
            )

            // Pill 3: Format (Limited Over / T20 / T10 / Test Cricket / Box / Tuf)
            FilterPill(
                iconType = FilterIconType.TROPHY,
                label = selectedFormat,
                isOpen = isFormatDropdownOpen,
                onClick = onToggleFormat
            )
        }

        // Expanded Dropdown Overlay (Image 2)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Skill Dropdown Box
            if (isSkillDropdownOpen) {
                DropdownMenuCard(
                    items = skills.map { skill ->
                        DropdownItemData(
                            label = skill,
                            iconType = when (skill) {
                                "Batting" -> FilterIconType.PERSON
                                "Bowling" -> FilterIconType.BALL
                                else -> FilterIconType.STAR
                            },
                            isSelected = skill == selectedSkill
                        )
                    },
                    onItemClick = { onSelectSkill(it.label) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Type Dropdown Box
            if (isTypeDropdownOpen) {
                DropdownMenuCard(
                    items = types.map { type ->
                        DropdownItemData(
                            label = type,
                            iconType = when (type) {
                                "Leather", "Tennis" -> FilterIconType.BALL
                                else -> FilterIconType.MORE
                            },
                            isSelected = type == selectedType
                        )
                    },
                    onItemClick = { onSelectType(it.label) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Format Dropdown Box
            if (isFormatDropdownOpen) {
                DropdownMenuCard(
                    items = formats.map { format ->
                        DropdownItemData(
                            label = format,
                            iconType = FilterIconType.TROPHY,
                            isSelected = format == selectedFormat,
                        )
                    },
                    onItemClick = { onSelectFormat(it.label) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

enum class FilterIconType {
    PERSON, BALL, TROPHY, STAR, MORE
}

@Composable
private fun FilterPill(
    iconType: FilterIconType,
    label: String,
    isOpen: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(Color(0xFF091217))
            .border(1.2.dp, PillBorder, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Left Icon
            FilterIcon(iconType = iconType, tint = GreenAccent, modifier = Modifier.size(16.dp))

            Spacer(modifier = Modifier.width(8.dp))

            // Label
            Text(
                text = label,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Down Chevron Arrow
            Canvas(modifier = Modifier.size(10.dp)) {
                val stroke = 1.8.dp.toPx()
                val path = Path().apply {
                    if (isOpen) {
                        moveTo(0f, size.height * 0.7f)
                        lineTo(size.width * 0.5f, size.height * 0.2f)
                        lineTo(size.width, size.height * 0.7f)
                    } else {
                        moveTo(0f, size.height * 0.3f)
                        lineTo(size.width * 0.5f, size.height * 0.8f)
                        lineTo(size.width, size.height * 0.3f)
                    }
                }
                drawPath(path, Color.White, style = Stroke(stroke, cap = StrokeCap.Round))
            }
        }
    }
}

private data class DropdownItemData(
    val label: String,
    val iconType: FilterIconType,
    val isSelected: Boolean
)

@Composable
private fun DropdownMenuCard(
    items: List<DropdownItemData>,
    onItemClick: (DropdownItemData) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF09141B))
            .border(1.dp, PillBorder, RoundedCornerShape(12.dp))
            .padding(vertical = 6.dp)
    ) {
        items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick(item) }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterIcon(
                    iconType = item.iconType,
                    tint = if (item.isSelected) GreenAccent else MutedText,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = item.label,
                    color = if (item.isSelected) Color.White else MutedText,
                    fontSize = 13.sp,
                    fontWeight = if (item.isSelected) FontWeight.Bold else FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                if (item.isSelected) {
                    // Green Checkmark
                    Canvas(modifier = Modifier.size(14.dp)) {
                        val path = Path().apply {
                            moveTo(size.width * 0.15f, size.height * 0.5f)
                            lineTo(size.width * 0.45f, size.height * 0.8f)
                            lineTo(size.width * 0.9f, size.height * 0.2f)
                        }
                        drawPath(path, GreenAccent, style = Stroke(2.2.dp.toPx(), cap = StrokeCap.Round))
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterIcon(
    iconType: FilterIconType,
    tint: Color,
    modifier: Modifier = Modifier
) {
    when (iconType) {
        FilterIconType.BALL -> {
            Image(
                painter = painterResource(id = R.drawable.ball),
                contentDescription = "Ball Icon",
                modifier = modifier
            )
        }
        FilterIconType.TROPHY -> {
            Image(
                painter = painterResource(id = R.drawable.white_trophy),
                contentDescription = "Limited Over Icon",
                modifier = modifier,
                colorFilter = ColorFilter.tint(
                    Lime
                )
            )
        }
        FilterIconType.PERSON -> {
            Canvas(modifier = modifier) {
                drawCircle(tint, radius = size.width * 0.25f, center = Offset(size.width * 0.5f, size.height * 0.3f), style = Stroke(1.5.dp.toPx()))
                val arcPath = Path().apply {
                    moveTo(size.width * 0.15f, size.height * 0.85f)
                    cubicTo(size.width * 0.2f, size.height * 0.55f, size.width * 0.8f, size.height * 0.55f, size.width * 0.85f, size.height * 0.85f)
                }
                drawPath(arcPath, tint, style = Stroke(1.5.dp.toPx(), cap = StrokeCap.Round))
            }
        }
        FilterIconType.STAR -> {
            Canvas(modifier = modifier) {
                val star = Path().apply {
                    moveTo(size.width * 0.5f, 0f)
                    lineTo(size.width * 0.65f, size.height * 0.35f)
                    lineTo(size.width, size.height * 0.35f)
                    lineTo(size.width * 0.72f, size.height * 0.6f)
                    lineTo(size.width * 0.82f, size.height)
                    lineTo(size.width * 0.5f, size.height * 0.75f)
                    lineTo(size.width * 0.18f, size.height)
                    lineTo(size.width * 0.18f, size.height)
                    lineTo(size.width * 0.28f, size.height * 0.6f)
                    lineTo(0f, size.height * 0.35f)
                    lineTo(size.width * 0.35f, size.height * 0.35f)
                    close()
                }
                drawPath(star, tint, style = Stroke(1.2.dp.toPx()))
            }
        }
        FilterIconType.MORE -> {
            Canvas(modifier = modifier) {
                drawCircle(tint, radius = 2.dp.toPx(), center = Offset(size.width * 0.2f, size.height * 0.5f))
                drawCircle(tint, radius = 2.dp.toPx(), center = Offset(size.width * 0.5f, size.height * 0.5f))
                drawCircle(tint, radius = 2.dp.toPx(), center = Offset(size.width * 0.8f, size.height * 0.5f))
            }
        }
    }
}

@Composable
private fun TableHeader(tab: LeaderboardTab) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .background(Color(0xFF070D12))
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "#",
            color = MutedText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(48.dp)
        )

        val col2Text = when (tab) {
            LeaderboardTab.PLAYER -> "PLAYER"
            LeaderboardTab.TEAMS -> "TEAM"
            LeaderboardTab.CLUBS -> "CLUB"
        }

        Text(
            text = col2Text,
            color = MutedText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        if (tab == LeaderboardTab.PLAYER) {
            Text(
                text = "TEAM",
                color = MutedText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(70.dp)
            )
        }

        Text(
            text = "POINTS",
            color = MutedText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.width(60.dp)
        )
    }
}

@Composable
private fun PlayerRankingRow(player: PlayerRank) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(CardBg)
            .border(0.5.dp, TableBorder)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank & Trend
        Column(
            modifier = Modifier.width(48.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = player.rank.toString(),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            TrendBadge(trend = player.trend)
        }

        // Circular Initials Avatar
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFF0F1B22))
                .border(1.5.dp, player.avatarBorderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = player.initials,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Player Name & Country
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = player.name,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = player.country,
                color = MutedText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }

        // Team Logo & Code
        Row(
            modifier = Modifier.width(70.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TeamLogoView(teamCode = player.teamCode, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = player.teamCode,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Points
        Text(
            text = player.points.toString(),
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.End,
            modifier = Modifier.width(60.dp)
        )
    }
}

@Composable
private fun ClubRankingRow(club: ClubRank) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .background(CardBg)
            .border(0.5.dp, TableBorder)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank & Trend
        Column(
            modifier = Modifier.width(48.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = club.rank.toString(),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            TrendBadge(trend = club.trend)
        }

        // Club Logo
        TeamLogoView(teamCode = club.code, modifier = Modifier.size(38.dp))

        Spacer(modifier = Modifier.width(12.dp))

        // Club Title & Subtitle
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = club.code,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = club.fullName,
                color = MutedText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Points
        Text(
            text = club.points.toString(),
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.End,
            modifier = Modifier.width(60.dp)
        )
    }
}

@Composable
private fun TrendBadge(trend: Trend) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        when (trend.type) {
            TrendType.UP -> {
                Canvas(modifier = Modifier.size(8.dp)) {
                    val path = Path().apply {
                        moveTo(size.width * 0.5f, 0f)
                        lineTo(size.width, size.height)
                        lineTo(0f, size.height)
                        close()
                    }
                    drawPath(path, TrendUpColor)
                }
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = trend.amount.toString(),
                    color = TrendUpColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            TrendType.DOWN -> {
                Canvas(modifier = Modifier.size(8.dp)) {
                    val path = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(size.width, 0f)
                        lineTo(size.width * 0.5f, size.height)
                        close()
                    }
                    drawPath(path, TrendDownColor)
                }
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = trend.amount.toString(),
                    color = TrendDownColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            TrendType.SAME -> {
                Text(
                    text = "-",
                    color = MutedText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun TeamLogoView(teamCode: String, modifier: Modifier = Modifier) {
    when (teamCode.uppercase()) {
        "RCB" -> {
            Image(
                painter = painterResource(id = R.drawable.rcb),
                contentDescription = "RCB Logo",
                modifier = modifier
            )
        }
        "GT" -> {
            Image(
                painter = painterResource(id = R.drawable.gujarat),
                contentDescription = "GT Logo",
                modifier = modifier
            )
        }
        "DC" -> {
            Image(
                painter = painterResource(id = R.drawable.delhi),
                contentDescription = "DC Logo",
                modifier = modifier
            )
        }
        else -> {
            // Render custom vector emblems for other IPL teams (MI, RR, SRH, LSG, CSK, KKR, PBKS)
            val colors = when (teamCode.uppercase()) {
                "MI" -> listOf(Color(0xFF004BA0), Color(0xFFFFD700))
                "RR" -> listOf(Color(0xFFEA1A85), Color(0xFF002B66))
                "SRH" -> listOf(Color(0xFFF26522), Color(0xFF111111))
                "LSG" -> listOf(Color(0xFF00A8E8), Color(0xFFE31E24))
                "CSK" -> listOf(Color(0xFFFCCA06), Color(0xFF00529B))
                "KKR" -> listOf(Color(0xFF3A225D), Color(0xFFF2C94C))
                "PBKS" -> listOf(Color(0xFFED1B24), Color(0xFFA6A6A6))
                else -> listOf(Color(0xFF1E303A), GreenAccent)
            }

            Box(
                modifier = modifier
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = colors
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = teamCode.take(3),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
