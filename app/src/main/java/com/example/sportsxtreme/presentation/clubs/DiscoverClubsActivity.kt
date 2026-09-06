package com.example.sportsxtreme.presentation.clubs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.sportsxtreme.R
import com.example.sportsxtreme.presentation.profile.Lime

/**
 * Professional Google UI/UX implementation for Discover Clubs.
 * Enhanced with interactive search, single selectable tags, and refined stats UI.
 * Now uses professional club logos for Warriors, Royal Kings, and Speedster Academy.
 */
class DiscoverClubsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            SportsXtremeTheme {
                DiscoverClubsScreen(onBack = { finish() })
            }
        }
    }
}

// Design System Colors
private val ColorBackground = Color(0xFF050708)
private val ColorSurface = Color(0xFF121619)
private val ColorPrimary = Color(0xFFBEFF18) // Neon Lime
private val ColorTextPrimary = Color.White
private val ColorTextSecondary = Color(0xFF969DA0)
private val ColorDivider = Color(0xFF2F3639)
private val ColorVerifiedBlue = Color(0xFF3698FF)

@Composable
fun SportsXtremeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = ColorBackground,
            surface = ColorSurface,
            primary = ColorPrimary,
            onBackground = ColorTextPrimary,
            onSurface = ColorTextPrimary
        ),
        content = content
    )
}

@Composable
fun DiscoverClubsScreen(onBack: () -> Unit) {
    var selectedFilter by remember { mutableStateOf("Corporate") }
    var searchQuery by remember { mutableStateOf("") }
    var sortBy by remember { mutableStateOf("Distance") }

    Scaffold(
        topBar = { DiscoverTopBar(onBack) },
        floatingActionButton = { NearbyClubsFab() },
        containerColor = ColorBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            SearchField(searchQuery) { searchQuery = it }
            Spacer(modifier = Modifier.height(24.dp))
            FilterSection(selectedFilter) { selectedFilter = it }
            Spacer(modifier = Modifier.height(24.dp))
            LocationBanner()
            Spacer(modifier = Modifier.height(32.dp))
            SectionHeader(sortBy) { sortBy = it }
            Spacer(modifier = Modifier.height(16.dp))

            // Club List Items
            ClubCard(
                name = stringResource(R.string.club_warriors_name),
                logoResId = R.drawable.delhi,
                location = stringResource(R.string.club_warriors_loc),
                distance = "1.2 km",
                description = stringResource(R.string.club_warriors_desc),
                stats = listOf(
                    "2.1K" to R.string.stat_followers,
                    "235" to R.string.stat_members,
                    "12" to R.string.stat_tournaments,
                    "4.9" to R.string.stat_rating
                ),
                tags = listOf(
                    R.string.tag_verified,
                    R.string.tag_tournament_organizer,
                    R.string.tag_academy,
                    R.string.tag_own_ground
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            ClubCard(
                name = stringResource(R.string.club_royal_kings_name),
                logoResId = R.drawable.rcb,
                location = stringResource(R.string.club_royal_kings_loc),
                distance = "2.7 km",
                description = stringResource(R.string.club_royal_kings_desc),
                stats = listOf(
                    "1.8K" to R.string.stat_followers,
                    "168" to R.string.stat_members,
                    "8" to R.string.stat_tournaments,
                    "4.7" to R.string.stat_rating
                ),
                tags = listOf(
                    R.string.tag_verified,
                    R.string.tag_youth_academy,
                    R.string.tag_coaching_available,
                    R.string.tag_cricket_ground
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            ClubCard(
                name = stringResource(R.string.club_speedster_name),
                logoResId = R.drawable.gujarat,
                location = stringResource(R.string.club_speedster_loc),
                distance = "3.4 km",
                description = stringResource(R.string.club_speedster_desc),
                stats = listOf(
                    "1.5K" to R.string.stat_followers,
                    "310" to R.string.stat_members,
                    "15" to R.string.stat_tournaments,
                    "4.8" to R.string.stat_rating
                ),
                tags = listOf(
                    R.string.tag_verified,
                    R.string.tag_academy,
                    R.string.tag_coaching_available,
                    R.string.tag_womens_team
                )
            )
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun DiscoverTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Back",
                tint = ColorTextPrimary,
                modifier = Modifier.size(32.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.discover_clubs_header),
                color = ColorTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(
                text = stringResource(R.string.discover_clubs_subheader),
                color = ColorTextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))
        Icon(
            painter = painterResource(R.drawable.setting),
            contentDescription = "Filter",
            tint = ColorTextPrimary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
    }
}

@Composable
fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(45, 51, 46), shape = RoundedCornerShape(16.dp))
            .height(56.dp)
            .background(Color.DarkGray.copy(alpha = 0.1f))
            .clip(
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Search,
            contentDescription = null,
            tint = ColorTextSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(
                    text = stringResource(R.string.search_clubs_hint),
                    color = ColorTextSecondary,
                    fontSize = 14.sp
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                textStyle = TextStyle(color = ColorTextPrimary, fontSize = 14.sp),
                cursorBrush = SolidColor(ColorPrimary),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun FilterSection(selectedFilter: String, onFilterSelected: (String) -> Unit) {
    val filters = listOf(
        stringResource(R.string.filter_all_clubs),
        stringResource(R.string.filter_corporate),
        stringResource(R.string.filter_verified)
    )
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(filters) { filter ->
            val isSelected = filter == selectedFilter
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .border(1.dp, if(isSelected) Lime else Color(45, 51, 46) , shape = CircleShape)
                    .background(if (isSelected) ColorPrimary else Color.DarkGray.copy(alpha = 0.1f))
                    .clickable { onFilterSelected(filter) }
                    .padding(horizontal = 24.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (filter == stringResource(R.string.filter_all_clubs)) {
                        Icon(
                            Icons.Rounded.LocationOn,
                            contentDescription = null,
                            tint = if (isSelected) Color.Black else ColorTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    } else if (filter == stringResource(R.string.filter_verified)) {
                        Icon(
                            Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = if (isSelected) Color.Black else ColorTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = filter,
                        color = if (isSelected) Color.Black else ColorTextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun LocationBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(45, 51, 46), shape = RoundedCornerShape(16.dp))
            .height(82.dp)
            .background(Color.DarkGray.copy(alpha = 0.1f))
            .clip(
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(ColorPrimary)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.showing_clubs_near),
                color = ColorTextSecondary,
                fontSize = 10.sp
            )
            Text(
                text = stringResource(R.string.location_bhubaneswar),
                color = ColorTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Icon(
            Icons.Rounded.LocationOn,
            contentDescription = null,
            tint = ColorPrimary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = stringResource(R.string.change_location),
            color = ColorPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.End,
            modifier = Modifier.clickable { /* Location Action */ }
        )
    }
}

@Composable
fun SectionHeader(sortBy: String, onSortChange: (String) -> Unit) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Rounded.LocationOn,
            contentDescription = null,
            tint = ColorPrimary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.nearby_clubs_label),
            color = ColorTextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(R.string.sort_by_label),
            color = ColorTextSecondary,
            fontSize = 12.sp
        )
        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { showMenu = true }
            ) {
                Text(
                    text = sortBy,
                    color = ColorPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = if (showMenu) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = ColorPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(ColorSurface)
            ) {
                listOf("Distance", "Popularity", "Rating").forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, color = ColorTextPrimary) },
                        onClick = {
                            onSortChange(option)
                            showMenu = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ClubCard(
    name: String,
    logoResId: Int,
    location: String,
    distance: String,
    description: String,
    stats: List<Pair<String, Int>>,
    tags: List<Int>
) {
    var selectedTag by remember { mutableStateOf<Int?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(45, 51, 46)),
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray.copy(alpha = 0.1f)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                // Club Logo Container with overlapping badge fixed
                Box(modifier = Modifier.size(78.dp)) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .align(Alignment.BottomStart)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(logoResId),
                            contentDescription = name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().padding(8.dp)
                        )
                    }
                    // Verified Badge - Now outside clipped logo box to prevent cutting
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 4.dp, top = 4.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(ColorBackground)
                            .padding(2.dp)
                    ) {
                        Icon(
                            Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = ColorVerifiedBlue,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = name,
                            color = ColorTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.weight(1f),
                            lineHeight = 22.sp
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = ColorPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = distance,
                                    color = ColorPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            IconButton(onClick = {}, modifier = Modifier.size(24.dp)) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = null,
                                    tint = ColorTextSecondary
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.LocationOn,
                            contentDescription = null,
                            tint = ColorPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = location, color = ColorTextSecondary, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = description,
                        color = ColorTextSecondary,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Join Request Button
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = stringResource(R.string.join_request_label),
                            color = Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "↗", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Stats Row in Black Box Container
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                stats.forEach { (value, labelRes) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val icon = when (labelRes) {
                                R.string.stat_followers -> Icons.Rounded.Person
                                R.string.stat_members -> Icons.Rounded.Person
                                R.string.stat_tournaments -> Icons.Rounded.Star
                                R.string.stat_rating -> Icons.Rounded.Star
                                else -> Icons.Rounded.Info
                            }
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = ColorPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = value,
                                color = ColorTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Text(
                            text = stringResource(labelRes),
                            color = ColorTextSecondary,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (stats.last() != (value to labelRes)) {
                        Box(modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(ColorDivider))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Single Selectable Tags in Card Container
            FlowRow(
                mainAxisSpacing = 8.dp,
                crossAxisSpacing = 8.dp
            ) {
                tags.forEach { tagRes ->
                    val isSelected = selectedTag == tagRes
                    Card(
                        modifier = Modifier
                            .clickable {
                                selectedTag = if (isSelected) null else tagRes
                            },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) ColorPrimary else Color.Transparent
                        ),
                        border = if (!isSelected) BorderStroke(1.dp, ColorDivider) else null
                    ) {
                        Text(
                            text = stringResource(tagRes),
                            color = if (isSelected) Color.Black else ColorTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NearbyClubsFab() {
    Surface(
        onClick = {},
        shape = CircleShape,
        color = ColorPrimary,
        modifier = Modifier.size(90.dp),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Rounded.LocationOn,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "NEARBY\nCLUBS",
                color = Color.Black,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                lineHeight = 11.sp
            )
        }
    }
}

@Composable
fun FlowRow(
    mainAxisSpacing: androidx.compose.ui.unit.Dp,
    crossAxisSpacing: androidx.compose.ui.unit.Dp,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(content = content) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints) }
        var rowWidth = 0
        var rowHeight = 0
        var totalHeight = 0
        val rows = mutableListOf<List<androidx.compose.ui.layout.Placeable>>()
        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()

        placeables.forEach { placeable ->
            if (rowWidth + placeable.width + mainAxisSpacing.toPx() > constraints.maxWidth) {
                rows.add(currentRow)
                totalHeight += rowHeight + crossAxisSpacing.toPx().toInt()
                rowWidth = 0
                rowHeight = 0
                currentRow = mutableListOf()
            }
            currentRow.add(placeable)
            rowWidth += (placeable.width + mainAxisSpacing.toPx()).toInt()
            rowHeight = maxOf(rowHeight, placeable.height)
        }
        rows.add(currentRow)
        totalHeight += rowHeight

        layout(constraints.maxWidth, totalHeight) {
            var y = 0
            rows.forEach { row ->
                var x = 0
                var maxHeight = 0
                row.forEach { placeable ->
                    placeable.placeRelative(x, y)
                    x += (placeable.width + mainAxisSpacing.toPx()).toInt()
                    maxHeight = maxOf(maxHeight, placeable.height)
                }
                y += (maxHeight + crossAxisSpacing.toPx()).toInt()
            }
        }
    }
}
