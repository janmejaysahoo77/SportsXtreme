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
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.NearMe
import androidx.compose.material.icons.rounded.Search
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sportsxtreme.presentation.ui.theme.*
import androidx.core.view.WindowCompat
import com.example.sportsxtreme.R
import com.example.sportsxtreme.presentation.profile.Lime

/**
 * Professional Google UI/UX implementation for Discover Clubs.
 * Matches the requested image design precisely.
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
private val ColorBackground = XtremeBgBlue
private val ColorSurface = XtremeCardBlue
private val ColorPrimary = XtremeLime // Neon Lime
private val ColorTextPrimary = Color.White
private val ColorTextSecondary = Color(0xFF969DA0)
private val ColorDivider = Color(0xFF272B27)
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
    var selectedFilter by remember { mutableStateOf("All Clubs") }
    var searchQuery by remember { mutableStateOf("") }
    var sortBy by remember { mutableStateOf("Popular") }

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
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            SearchField(searchQuery) { searchQuery = it }
            Spacer(modifier = Modifier.height(16.dp))
            FilterSection(selectedFilter) { selectedFilter = it }
            Spacer(modifier = Modifier.height(24.dp))

            FeaturedClubSection()

            Spacer(modifier = Modifier.height(32.dp))

            AllClubsHeader(sortBy) { sortBy = it }

            Spacer(modifier = Modifier.height(16.dp))

            // Club List Items
            ClubCard(
                name = "Warriors Cricket Club",
                logoResId = R.drawable.delhi,
                location = "CUTTACK, ODISHA",
                description = "A competitive cricket club focused on performance, discipline and...",
                stats = mapOf(
                    "Tournaments" to "12",
                    "Matches" to "42",
                    "Members" to "235",
                    "Rating" to "4.9"
                ),
                tags = listOf("VERIFIED", "ACADEMY", "COACHING AVAILABLE", "WOMAN'S TEAM")
            )
            Spacer(modifier = Modifier.height(16.dp))
            ClubCard(
                name = "Royal Kings Cricket Club",
                logoResId = R.drawable.rcb,
                location = "PURI, ODISHA",
                description = "Uniting talent and passion for cricket. Join us and be a part of the legacy.",
                stats = mapOf(
                    "Tournaments" to "8",
                    "Matches" to "28",
                    "Members" to "162",
                    "Rating" to "4.7"
                ),
                tags = listOf("VERIFIED", "ACADEMY", "COACHING AVAILABLE", "WOMAN'S TEAM")
            )
            Spacer(modifier = Modifier.height(16.dp))
            ClubCard(
                name = "Speedster Cricket Academy",
                logoResId = R.drawable.gujarat,
                location = "BHUBANESWAR, ODISHA",
                description = "Professional coaching for all age groups. Building future champions.",
                stats = mapOf(
                    "Tournaments" to "15",
                    "Matches" to "35",
                    "Members" to "310",
                    "Rating" to "4.8"
                ),
                tags = listOf("VERIFIED", "ACADEMY", "COACHING AVAILABLE", "WOMAN'S TEAM")
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
            .padding(horizontal = 12.dp, vertical = 14.dp),
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
                text = "DISCOVER CLUBS",
                color = ColorTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(
                text = "EXPLORE CRICKET COMMUNITIES",
                color = ColorTextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        IconButton(onClick = { }) {
            Icon(
                Icons.Rounded.Search,
                contentDescription = "Search",
                tint = ColorTextPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
        IconButton(onClick = { }) {
            Icon(
                Icons.Default.FilterList,
                contentDescription = "Filter",
                tint = ColorTextPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(ColorSurface, RoundedCornerShape(12.dp))
            .border(1.dp, ColorDivider, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Rounded.Search,
            contentDescription = null,
            tint = ColorTextSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(
                    text = "Search clubs, locations, academies...",
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
    val filters = listOf("All Clubs", "corporate", "Verified")
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(filters) { filter ->
            val isSelected = filter == selectedFilter
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) ColorPrimary else ColorSurface)
                    .border(
                        1.dp,
                        if (isSelected) ColorPrimary else ColorDivider,
                        shape = CircleShape
                    )
                    .clickable { onFilterSelected(filter) }
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (filter == "Verified") {
                        Icon(
                            Icons.Rounded.Check,
                            contentDescription = null,
                            tint = if (isSelected) Color.Black else ColorTextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = if (filter == "All Clubs") filter else filter.lowercase(),
                        color = if (isSelected) Color.Black else ColorTextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun FeaturedClubSection() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = ColorPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "FEATURED CLUB",
                    color = ColorTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
            Text(
                text = "VIEW ALL >",
                color = ColorPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        FeaturedClubCard()
    }
}

@Composable
fun FeaturedClubCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            , // Increased height for professional spacing
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, ColorDivider),
        colors = CardDefaults.cardColors(containerColor = ColorSurface)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Image Overlay
            Image(
                painter = painterResource(id = R.drawable.stadium),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.15f),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top: Tournament Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        color = Color(0xFF3E3615),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFEAB308).copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFEAB308),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "UPCOMING TOURNAMENT",
                                color = Color(0xFFEAB308),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(16.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo Section with verified badge
                    Box(modifier = Modifier.size(80.dp)) {
                        Surface(
                            modifier = Modifier
                                .size(70.dp)
                                .align(Alignment.Center),
                            shape = CircleShape,
                            color = Color.Black,
                            border = BorderStroke(2.dp, Color.White.copy(alpha = 0.15f))
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.appicon),
                                contentDescription = null,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                        Icon(
                            Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = ColorVerifiedBlue,
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.BottomEnd)
                                .background(ColorSurface, CircleShape)
                                .padding(2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Victory Cricket Club",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = ColorVerifiedBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Icon(
                                Icons.Rounded.LocationOn,
                                null,
                                tint = ColorPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "BHUBANESWAR, ODISHA",
                                color = ColorTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "A passionate cricket club focused on developing talent and promoting excellence in sports...",
                            color = ColorTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(
                    modifier = Modifier.height(16.dp)
                )
                // Bottom: Stats and Call-to-Action
                Column(
                    modifier = Modifier.fillMaxWidth(),

                    ) {
                    // Stat grid
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FeaturedStatItem("2.3K", "FOLLOWERS")
                        FeaturedStatItem("24", "TOURNAMENTS")
                        FeaturedStatItem("486", "MEMBERS")
                        FeaturedStatItem("4.8", "RATING")
                    }
                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                        ) {
                            repeat(4) { i ->
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (i == 0) ColorPrimary else ColorDivider)
                                )
                            }
                        }

                        Button(
                            onClick = { },
                            colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
                        ) {
                            Text(
                                "View Club",
                                color = Color.Black,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.W900
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.FeaturedStatItem(value: String, label: String) {
    Box(
        modifier = Modifier
            .weight(1f)
            .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AllClubsHeader(sortBy: String, onSortChange: (String) -> Unit) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "ALL CLUBS",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "(128)",
            color = ColorTextSecondary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "SORT BY: ",
            color = ColorTextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { showMenu = true }
            ) {
                Text(
                    text = sortBy,
                    color = ColorPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    Icons.Rounded.KeyboardArrowDown,
                    null,
                    tint = ColorPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(ColorSurface)
            ) {
                listOf("Popular", "Distance", "Rating").forEach { option ->
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
    description: String,
    stats: Map<String, String>,
    tags: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, ColorDivider),
        colors = CardDefaults.cardColors(containerColor = ColorSurface),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Image(
                        painter = painterResource(logoResId),
                        contentDescription = name,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = name,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                Icons.Rounded.CheckCircle,
                                null,
                                tint = ColorVerifiedBlue,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        IconButton(onClick = { }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Rounded.MoreHoriz, null, tint = ColorTextSecondary)
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.LocationOn,
                            null,
                            tint = ColorPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = location,
                            color = ColorTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = description,
                        color = ColorTextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.align(Alignment.End),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "Join Request",
                            color = Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.AutoMirrored.Rounded.Send,
                            null,
                            tint = Color.Black,
                            modifier = Modifier
                                .size(18.dp)
                                .rotate(-30f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ClubStatBox(stats["Tournaments"] ?: "0", "TOURNAMENTS")
                ClubStatBox(stats["Members"] ?: "0", "MEMBERS")
                ClubStatBox(stats["Matches"] ?: "0", "MATCHES")
                ClubStatBox(stats["Rating"] ?: "0.0", "RATING", isRating = true)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                tags.take(4).forEach { tag ->
                    Surface(
                        color = Color(0xFF1A1F23),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = tag,
                            color = if (tag == "VERIFIED") ColorVerifiedBlue else ColorTextSecondary,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.ClubStatBox(value: String, label: String, isRating: Boolean = false) {
    Box(
        modifier = Modifier
            .weight(1f)
            .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                if (isRating) {
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(Icons.Rounded.Star, null, tint = Color.White, modifier = Modifier.size(10.dp))
                }
            }
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun NearbyClubsFab() {
    Surface(
        onClick = {},
        color = Color.Transparent,
        modifier = Modifier
            .size(80.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
          Icon(
              Icons.Rounded.LocationOn,
              null,
              tint = Lime,
              modifier = Modifier.size(40.dp)
          )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "NEARBY\nCLUBS",
                color = Lime,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                lineHeight = 10.sp
            )
        }
    }
}
