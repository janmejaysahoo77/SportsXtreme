package com.example.sportsxtreme.presentation.clubs

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.sportsxtreme.R
import com.example.sportsxtreme.presentation.team.TeamProfileActivity
import com.example.sportsxtreme.presentation.ui.theme.BlueBackground
import com.example.sportsxtreme.presentation.ui.theme.BlueBackgroundColor
import com.example.sportsxtreme.presentation.ui.theme.BlueCardBackGround
import com.example.sportsxtreme.presentation.ui.theme.BlueCardColors
import com.example.sportsxtreme.presentation.ui.theme.XtremeDarkBlueHex
import kotlinx.coroutines.launch

class ClubAnalyticsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = AnalyticsColors.BgDarkHex.toInt()
        window.navigationBarColor = AnalyticsColors.BgDarkHex.toInt()

        val initialTab = intent.getIntExtra("INITIAL_TAB", 0).coerceIn(0, 2)
        setContent {
            ClubAnalyticsScreen(
                initialTab = initialTab,
                onBack = { finish() }
            )
        }
    }
}

// MARK: - Design System & Color Tokens
private object AnalyticsColors {
    const val BgDarkHex = 0xFF01040F

    val Background = BlueBackgroundColor
    val CardBg = BlueCardColors
    val CardBorder = Color(45, 51, 46)
    val InsetBg = Color(1, 4, 15, 255)

    // Primary Neon Lime (Exact match to design)
    val NeonLime = Color(0xFFCCFF00)
    val NeonLimeMuted = Color(0x33CCFF00)
    val NeonLimeSubtle = Color(0x1ACCFF00)

    val TextPrimary = Color(0xFFFFFFFF)
    val TextMuted = Color(0xFF8E9BAE)
    val TextSecondary = Color(0xFFCBD5E1)

    // Status Badge colors
    val LiveBg = Color(0xFF142517)
    val LiveBorder = Color(0xFF26522A)

    val CompletedBg = Color(0xFF1B2330)
    val CompletedBorder = Color(0xFF2D394C)

    // Medal & Trophy Badge tints
    val MedalCircle = Color(0xFF192B1D)
    val MedalBorder = Color(0xFF2B4D31)

    val TrophyCircle = Color(0xFF142339)
    val TrophyBorder = Color(0xFF223A5B)
}

// MARK: - Main Container Screen
@Composable
fun ClubAnalyticsScreen(
    initialTab: Int = 0,
    onBack: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = initialTab) { 3 }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var showInfoDialog by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = BlueBackground,
        topBar = {
            AnalyticsTopBar(
                onBack = onBack,
                onFilterClick = {
                    Toast.makeText(context, "Filter options applied", Toast.LENGTH_SHORT).show()
                },
                onMoreClick = {
                    Toast.makeText(context, "Analytics settings", Toast.LENGTH_SHORT).show()
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(AnalyticsColors.Background)
        ) {
            // Main Tabs: Overview | Teams | Tournaments
            MainAnalyticsTabs(
                selectedTab = pagerState.currentPage,
                onTabSelected = { index ->
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Swipable Pager across the 3 screens
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                when (page) {
                    0 -> OverviewTabContent(
                        onInfoClick = {
                            showInfoDialog = "Club Overview provides cumulative statistics, key insights, and monthly match and tournament participation metrics for the active 2026 season."
                        },
                        onSeniorTeamClick = {
                            val intent = Intent(context, TeamProfileActivity::class.java).apply {
                                putExtra("TEAM_NAME", "Senior Team")
                            }
                            context.startActivity(intent)
                        }
                    )
                    1 -> TeamsTabContent(
                        onTeamClick = { teamName ->
                            val intent = Intent(context, TeamProfileActivity::class.java).apply {
                                putExtra("TEAM_NAME", teamName)
                            }
                            context.startActivity(intent)
                        }
                    )
                    2 -> TournamentsTabContent(
                        onInfoClick = {
                            showInfoDialog = "Tournaments section displays club-hosted internal leagues and external competitive tournaments participated by Victory Cricket Club."
                        },
                        onAddTournament = {
                            val intent = Intent(context, ClubsTournamentPlayedActivity::class.java)
                            context.startActivity(intent)
                        },
                        onViewAllInternal = {
                            val intent = Intent(context, ClubsTournamentPlayedActivity::class.java)
                            context.startActivity(intent)
                        },
                        onTournamentClick = {
                            val intent = Intent(context, ClubsTournamentPlayedActivity::class.java)
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }

    // Informational Dialog
    if (showInfoDialog != null) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = null },
            containerColor = BlueCardBackGround,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "Information",
                    color = AnalyticsColors.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = showInfoDialog ?: "",
                    color = AnalyticsColors.TextMuted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = null }) {
                    Text(
                        text = "OK",
                        color = AnalyticsColors.NeonLime,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}

// MARK: - Top Bar
@Composable
private fun AnalyticsTopBar(
    onBack: () -> Unit,
    onFilterClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = AnalyticsColors.TextPrimary,
                modifier = Modifier.size(22.dp)
            )
        }

        Text(
            text = "Club Analytics",
            color = AnalyticsColors.TextPrimary,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        )

        IconButton(
            onClick = onFilterClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Filter",
                tint = AnalyticsColors.TextPrimary,
                modifier = Modifier.size(22.dp)
            )
        }

        IconButton(
            onClick = onMoreClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More",
                tint = AnalyticsColors.TextPrimary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// MARK: - Main Tabs
@Composable
private fun MainAnalyticsTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf("Overview", "Teams", "Tournaments")

    Box(
        modifier = modifier
            .height(46.dp)
            .clip(RoundedCornerShape(23.dp))
            .background(Color(0xFF131A26))
            .border(BorderStroke(1.dp, AnalyticsColors.CardBorder), RoundedCornerShape(23.dp))
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) AnalyticsColors.NeonLime else Color.Transparent,
                    animationSpec = tween(durationMillis = 250),
                    label = "tabBg"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) Color(0xFF080C14) else AnalyticsColors.TextMuted,
                    animationSpec = tween(durationMillis = 200),
                    label = "tabText"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(19.dp))
                        .background(bgColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onTabSelected(index)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = textColor,
                        fontSize = 12.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// =========================================================================
// MARK: - SCREEN 1: OVERVIEW TAB (Image 2)
// =========================================================================
@Composable
private fun OverviewTabContent(
    onInfoClick: () -> Unit,
    onSeniorTeamClick: () -> Unit
) {
    var selectedYear by remember { mutableStateOf("This Year") }
    var yearDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Section: Club Overview ⓘ
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Club Overview",
                color = AnalyticsColors.TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(6.dp))
            IconButton(
                onClick = onInfoClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Overview Info",
                    tint = AnalyticsColors.TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2-Column Grid (3 rows x 2 columns)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OverviewStatCard(
                label = "TOTAL TEAMS",
                value = "5",
                isAccent = true,
                hasAccentBorder = true,
                modifier = Modifier.weight(1f)
            )
            OverviewStatCard(
                label = "TOTAL MEMBERS",
                value = "82",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OverviewStatCard(
                label = "INTERNAL TOURN.",
                value = "6",
                modifier = Modifier.weight(1f)
            )
            OverviewStatCard(
                label = "EXTERNAL TOURN.",
                value = "3",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OverviewStatCard(
                label = "MATCHES ORGANIZED",
                value = "145",
                modifier = Modifier.weight(1f)
            )
            OverviewStatCard(
                label = "ACTIVE SEASON",
                value = "2026",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Quick Insights
        Text(
            text = "Quick Insights",
            color = AnalyticsColors.TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Insight 1: Best Performing Team
        InsightCard(
            badgeContent = {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(AnalyticsColors.MedalCircle)
                        .border(1.dp, AnalyticsColors.MedalBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🏅", fontSize = 17.sp)
                }
            },
            category = "Best Performing Team",
            titleContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Senior Team",
                        color = AnalyticsColors.TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "75% Win Rate",
                        color = AnalyticsColors.NeonLime,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            onClick = onSeniorTeamClick
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Insight 2: Latest Tournament Champion
        InsightCard(
            badgeContent = {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(AnalyticsColors.TrophyCircle)
                        .border(1.dp, AnalyticsColors.TrophyBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🏆", fontSize = 17.sp)
                }
            },
            category = "Latest Tournament Champion",
            titleContent = {
                Text(
                    text = "Senior Team",
                    color = AnalyticsColors.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            onClick = onSeniorTeamClick
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Insight 3: Top Run Scorer
        InsightCard(
            badgeContent = {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E2638))
                        .border(1.dp, Color(0xFF2D3C52), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.circleuser),
                        contentDescription = "Rahul Das",
                        modifier = Modifier.size(38.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            },
            category = "Top Run Scorer",
            titleContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Rahul Das",
                        color = AnalyticsColors.TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(982 Runs)",
                        color = AnalyticsColors.TextMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            onClick = onSeniorTeamClick
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Monthly Activity
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Monthly Activity",
                color = AnalyticsColors.TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF232838))
                        .clickable { yearDropdownExpanded = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedYear,
                        color = AnalyticsColors.TextPrimary,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Select Year",
                        tint = AnalyticsColors.TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }

                DropdownMenu(
                    expanded = yearDropdownExpanded,
                    onDismissRequest = { yearDropdownExpanded = false }
                ) {
                    listOf("This Year", "2025", "All Time").forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                selectedYear = item
                                yearDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Monthly Dual-Bar Activity Chart
        MonthlyActivityBarChart()

        Spacer(modifier = Modifier.height(36.dp))
    }
}

@Composable
private fun OverviewStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isAccent: Boolean = false,
    hasAccentBorder: Boolean = false
) {
    Card(
        modifier = modifier.height(86.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AnalyticsColors.CardBg),
        border = BorderStroke(
            1.dp,
            if (hasAccentBorder) AnalyticsColors.NeonLime.copy(alpha = 0.5f) else AnalyticsColors.CardBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            Text(
                text = label,
                color = AnalyticsColors.TextMuted,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = value,
                color = if (isAccent) AnalyticsColors.NeonLime else AnalyticsColors.TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 30.sp
            )
        }
    }
}

@Composable
private fun InsightCard(
    badgeContent: @Composable () -> Unit,
    category: String,
    titleContent: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AnalyticsColors.CardBg),
        border = BorderStroke(1.dp, AnalyticsColors.CardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            badgeContent()

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category,
                    color = AnalyticsColors.TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                titleContent()
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = AnalyticsColors.TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun MonthlyActivityBarChart() {
    val months = listOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL")
    // Values as fraction of 120dp max height
    val matchesHeightFracs = listOf(0.38f, 0.65f, 0.38f, 0.72f, 0.88f, 0.66f, 0.96f)
    val tournamentsHeightFracs = listOf(0.18f, 0.28f, 0.16f, 0.22f, 0.42f, 0.18f, 0.28f)
    val maxHeight = 125.dp

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AnalyticsColors.CardBg),
        border = BorderStroke(1.dp, AnalyticsColors.CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 22.dp, bottom = 16.dp, start = 12.dp, end = 12.dp)
        ) {
            // Bars row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(maxHeight + 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                months.forEachIndexed { index, month ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            // Matches Bar (Neon Lime)
                            Box(
                                modifier = Modifier
                                    .width(9.5.dp)
                                    .height(maxHeight * matchesHeightFracs[index])
                                    .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                    .background(AnalyticsColors.NeonLime)
                            )
                            // Tournaments Bar (Dark Slate)
                            Box(
                                modifier = Modifier
                                    .width(9.5.dp)
                                    .height(maxHeight * tournamentsHeightFracs[index])
                                    .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                    .background(Color(0xFF475569))
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = month,
                            color = AnalyticsColors.TextMuted,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Legend Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(AnalyticsColors.NeonLime)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Matches",
                    color = AnalyticsColors.TextSecondary,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.width(24.dp))

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF475569))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Tournaments",
                    color = AnalyticsColors.TextSecondary,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// =========================================================================
// MARK: - SCREEN 2: TEAMS TAB (Image 3)
// =========================================================================
private data class TeamAnalyticsData(
    val name: String,
    val captain: String,
    val players: String,
    val matches: String,
    val wins: String,
    val winRate: String,
    val internalTitles: String,
    val externalTitles: String,
    val isBestTeam: Boolean = false
)

@Composable
private fun TeamsTabContent(
    onTeamClick: (String) -> Unit
) {
    val teams = remember {
        listOf(
            TeamAnalyticsData(
                name = "Senior Team",
                captain = "Rahul Das",
                players = "18",
                matches = "40",
                wins = "30",
                winRate = "75%",
                internalTitles = "2",
                externalTitles = "1",
                isBestTeam = true
            ),
            TeamAnalyticsData(
                name = "Women's Team",
                captain = "Priya Sharma",
                players = "16",
                matches = "22",
                wins = "15",
                winRate = "68%",
                internalTitles = "1",
                externalTitles = "0",
                isBestTeam = false
            ),
            TeamAnalyticsData(
                name = "Under-19 Team",
                captain = "Aarav Nayak",
                players = "17",
                matches = "32",
                wins = "23",
                winRate = "72%",
                internalTitles = "1",
                externalTitles = "2",
                isBestTeam = false
            ),
            TeamAnalyticsData(
                name = "Under-16 Team",
                captain = "Suman Rout",
                players = "15",
                matches = "28",
                wins = "18",
                winRate = "64%",
                internalTitles = "1",
                externalTitles = "0",
                isBestTeam = false
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = "Team Performance",
            color = AnalyticsColors.TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(14.dp))

        teams.forEach { team ->
            TeamPerformanceCard(
                team = team,
                onClick = { onTeamClick(team.name) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(36.dp))
    }
}

@Composable
private fun TeamPerformanceCard(
    team: TeamAnalyticsData,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AnalyticsColors.CardBg),
        border = BorderStroke(
            1.dp,
            if (team.isBestTeam) Color(0xFF264A2F) else AnalyticsColors.CardBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Shield Icon + Name + Best Team badge + Captain + Chevron
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Shield Icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF162335))
                        .border(1.dp, Color(0xFF26374F), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.shield),
                        contentDescription = "Shield",
                        modifier = Modifier.size(22.dp),
                        colorFilter = ColorFilter.tint(AnalyticsColors.NeonLime)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = team.name,
                            color = AnalyticsColors.TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        if (team.isBestTeam) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(AnalyticsColors.NeonLime)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "BEST TEAM",
                                    color = Color(0xFF080C14),
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Captain: ${team.captain}",
                        color = AnalyticsColors.TextMuted,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = AnalyticsColors.TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4 Metrics in a row: PLAYERS, MATCHES, WINS, WIN RATE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TeamMetricBox(label = "PLAYERS", value = team.players, modifier = Modifier.weight(1f))
                TeamMetricBox(label = "MATCHES", value = team.matches, modifier = Modifier.weight(1f))
                TeamMetricBox(label = "WINS", value = team.wins, modifier = Modifier.weight(1f))
                TeamMetricBox(
                    label = "WIN RATE",
                    value = team.winRate,
                    isAccent = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Divider line
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = Color(0xFF1E283A)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom row: Internal Titles | External Titles
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Internal Titles: ",
                        color = AnalyticsColors.TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = team.internalTitles,
                        color = AnalyticsColors.TextPrimary,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "External Titles: ",
                        color = AnalyticsColors.TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = team.externalTitles,
                        color = AnalyticsColors.TextPrimary,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun TeamMetricBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isAccent: Boolean = false
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(AnalyticsColors.InsetBg)
            .padding(horizontal = 8.dp, vertical = 9.dp)
    ) {
        Column {
            Text(
                text = label,
                color = AnalyticsColors.TextMuted,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = if (isAccent) AnalyticsColors.NeonLime else AnalyticsColors.TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

// =========================================================================
// MARK: - SCREEN 3: TOURNAMENTS TAB (Image 1)
// =========================================================================
@Composable
private fun TournamentsTabContent(
    onInfoClick: () -> Unit,
    onAddTournament: () -> Unit,
    onViewAllInternal: () -> Unit,
    onTournamentClick: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf(1) } // 0: All, 1: Internal, 2: External
    val filterOptions = listOf("All", "Internal", "External")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Sub-filter Pills: All | Internal | External
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF131A26))
                .border(BorderStroke(1.dp, AnalyticsColors.CardBorder), RoundedCornerShape(20.dp))
                .padding(3.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                filterOptions.forEachIndexed { index, label ->
                    val isSelected = selectedFilter == index

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(17.dp))
                            .then(
                                if (isSelected) {
                                    Modifier
                                        .background(Color(0xFF101724))
                                        .border(
                                            BorderStroke(1.5.dp, AnalyticsColors.NeonLime),
                                            RoundedCornerShape(17.dp)
                                        )
                                } else Modifier
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                selectedFilter = index
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) AnalyticsColors.NeonLime else AnalyticsColors.TextMuted,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Internal Tournaments Section (visible for All and Internal)
        if (selectedFilter == 0 || selectedFilter == 1) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Internal Tournaments",
                    color = AnalyticsColors.TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onInfoClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Tournament Info",
                        tint = AnalyticsColors.TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // (+) Action button with neon lime circle
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, AnalyticsColors.NeonLime, CircleShape)
                        .clickable(onClick = onAddTournament),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Tournament",
                        tint = AnalyticsColors.NeonLime,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Card 1: Victory Premier League 2026 (LIVE)
            TournamentCard(
                title = "Victory Premier\nLeague 2026",
                subtitle = "Club Internal Tournament",
                isLive = true,
                statusText = "LIVE",
                teams = "4",
                matches = "12",
                champion = "Senior Team",
                runnerUp = "Under-19 Team",
                bannerRes = R.drawable.image__37_,
                onClick = onTournamentClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Card 2: Monsoon Cup 2025 (COMPLETED)
            TournamentCard(
                title = "Monsoon Cup\n2025",
                subtitle = "Club Internal\nTournament",
                isLive = false,
                statusText = "COMPLETED",
                teams = "5",
                matches = "16",
                champion = "Women's Team",
                runnerUp = "Senior Team",
                bannerRes = R.drawable.trophies,
                onClick = onTournamentClick
            )

            Spacer(modifier = Modifier.height(14.dp))

            // "View All Internal Tournaments >" Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF101724))
                    .border(BorderStroke(1.dp, AnalyticsColors.CardBorder), RoundedCornerShape(12.dp))
                    .clickable(onClick = onViewAllInternal),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "View All Internal Tournaments",
                        color = AnalyticsColors.NeonLime,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = AnalyticsColors.NeonLime,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // External Tournaments Section (visible for All and External)
        if (selectedFilter == 0 || selectedFilter == 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "External Tournaments",
                    color = AnalyticsColors.TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "(Participated / Hosted)",
                    color = AnalyticsColors.TextMuted,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // External Tournament Card: Summer Cup 2025
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onTournamentClick),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = AnalyticsColors.CardBg),
                border = BorderStroke(1.dp, AnalyticsColors.CardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E2838)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.trophy1),
                            contentDescription = "Summer Cup",
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Summer Cup 2025",
                            color = AnalyticsColors.TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Hosted by: District C.A",
                            color = AnalyticsColors.TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    // Completed Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(AnalyticsColors.CompletedBg)
                            .border(1.dp, AnalyticsColors.CompletedBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = AnalyticsColors.TextSecondary,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "COMPLETED",
                                color = AnalyticsColors.TextSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = AnalyticsColors.TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Composable
private fun TournamentCard(
    title: String,
    subtitle: String,
    isLive: Boolean,
    statusText: String,
    teams: String,
    matches: String,
    champion: String,
    runnerUp: String,
    bannerRes: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AnalyticsColors.CardBg),
        border = BorderStroke(1.dp, AnalyticsColors.CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Thumbnail + Title/Subtitle + Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF162335)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(bannerRes),
                        contentDescription = null,
                        modifier = Modifier.size(34.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = AnalyticsColors.TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 19.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = subtitle,
                        color = AnalyticsColors.TextMuted,
                        fontSize = 11.5.sp,
                        lineHeight = 15.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Status Pill: LIVE or COMPLETED
                if (isLive) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(AnalyticsColors.LiveBg)
                            .border(1.dp, AnalyticsColors.LiveBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(AnalyticsColors.NeonLime)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = statusText,
                                color = AnalyticsColors.NeonLime,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(AnalyticsColors.CompletedBg)
                            .border(1.dp, AnalyticsColors.CompletedBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = AnalyticsColors.TextSecondary,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = statusText,
                                color = AnalyticsColors.TextSecondary,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2x2 Metric Grid:
            // Row 1: TEAMS | MATCHES
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TournamentMetricBox(
                    label = "TEAMS",
                    value = teams,
                    isAccent = true,
                    modifier = Modifier.weight(1f)
                )
                TournamentMetricBox(
                    label = "MATCHES",
                    value = matches,
                    isAccent = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: CHAMPION | RUNNER UP
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TournamentMetricBox(
                    label = "CHAMPION",
                    value = champion,
                    modifier = Modifier.weight(1f)
                )
                TournamentMetricBox(
                    label = "RUNNER UP",
                    value = runnerUp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TournamentMetricBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isAccent: Boolean = false
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(AnalyticsColors.InsetBg)
            .padding(horizontal = 10.dp, vertical = 9.dp)
    ) {
        Column {
            Text(
                text = label,
                color = AnalyticsColors.TextMuted,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = if (isAccent) AnalyticsColors.NeonLime else AnalyticsColors.TextPrimary,
                fontSize = if (isAccent) 18.sp else 13.5.sp,
                fontWeight = if (isAccent) FontWeight.ExtraBold else FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
