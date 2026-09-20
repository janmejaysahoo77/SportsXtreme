package com.example.sportsxtreme.presentation.clubs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sportsxtreme.R
import com.example.sportsxtreme.presentation.ui.theme.*

@Composable
fun TournamentDashboardScreen(onBack: () -> Unit) {
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedStatus by remember { mutableStateOf("LIVE") }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        containerColor = BlueBackground,
        topBar = {
            TournamentTopBar(onBack = onBack)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* FAB Action */ },
                containerColor = XtremeLime,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.Black, modifier = Modifier.size(28.dp))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp)
        ) {
            // Info Banner
            InfoBanner()

            // Metrics Grid
            MetricsGrid()

            Spacer(modifier = Modifier.height(16.dp))

            // Create Tournament Button
            CreateTournamentButton()

            Spacer(modifier = Modifier.height(20.dp))

            // Search and Filter
            SearchAndFilterRow(searchQuery) { searchQuery = it }

            Spacer(modifier = Modifier.height(20.dp))

            // Category Selection
            CategorySelectionTabs(selectedCategory) { selectedCategory = it }

            Spacer(modifier = Modifier.height(16.dp))

            // Status Tabs
            StatusTabs(selectedStatus) { selectedStatus = it }

            Spacer(modifier = Modifier.height(20.dp))

            // Tournament List
            TournamentList()
        }
    }
}

@Composable
private fun TournamentTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(64.dp)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(BlueCardBackGround)
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))



        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Tournament Dashboard",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "VICTORY CRICKET CLUB",
                color = XtremeLime,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }

        IconButton(onClick = { /* Notifications */ }) {
            Box {
                Image(
                    painter = painterResource(id = R.drawable.bells),
                    contentDescription = "Notifications",
                )

            }
        }

        IconButton(onClick = { /* Menu */ }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun InfoBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(BlueCardBackGround.copy(alpha = 0.5f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            tint = XtremeLime,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Manage your Club and Inter-Club tournaments.",
            color = XtremeMuted,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun MetricsGrid() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetricItem(R.drawable.hots, "3", "LIVE", true, Modifier.weight(1f))
        MetricItem(R.drawable.claender, "2", "UPCOMING", false, Modifier.weight(1f))
        MetricItem(R.drawable.trophies, "12", "COMPLETED", false, Modifier.weight(1f))
        MetricItem(R.drawable.groups2, "48", "PARTICIPATING TEAMS", false, Modifier.weight(1f))
    }
}

@Composable
private fun MetricItem(iconRes: Int, value: String, label: String, isActive: Boolean, modifier: Modifier) {
    Card(
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BlueCardBackGround),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            Text(text = label, color = if (isActive) XtremeLime else XtremeMuted, fontSize = 7.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CreateTournamentButton() {
    Surface(
        onClick = { /* Create */ },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(48.dp),
        color = XtremeLime,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.AddCircle, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = "Create Tournament", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SearchAndFilterRow(query: String, onQueryChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.weight(1f),
            color = BlueCardBackGround,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = XtremeMuted, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { inner ->
                        if (query.isEmpty()) Text("Search tournaments...", color = XtremeMuted, fontSize = 13.sp)
                        inner()
                    }
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Surface(
            modifier = Modifier.size(44.dp),
            color = BlueCardBackGround,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            IconButton(onClick = { /* Filter */ }) {
                Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun CategorySelectionTabs(selected: String, onSelected: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CategoryTab("All", R.drawable.groups2, selected == "All", Modifier.weight(1f)) { onSelected("All") }
        CategoryTab("Club Tournament", R.drawable.victory, selected == "Club Tournament", Modifier.weight(1.5f)) { onSelected("Club Tournament") }
        CategoryTab("Inter-Club Tournament", R.drawable.groups2, selected == "Inter-Club Tournament", Modifier.weight(1.8f)) { onSelected("Inter-Club Tournament") }
    }
}

@Composable
private fun CategoryTab(label: String, iconRes: Int, isActive: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        color = if (isActive) XtremeLime else BlueCardBackGround,
        shape = RoundedCornerShape(14.dp),
        border = if (!isActive) BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                colorFilter = if (isActive) ColorFilter.tint(Color.Black) else ColorFilter.tint(Color.White)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(text = if (label == "All") "All" else label.split(" ")[0], color = if (isActive) Color.Black else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                if (label != "All") Text(text = if (label.contains("Club")) "Only club squads" else "Multiple clubs", color = if (isActive) Color.Black.copy(alpha = 0.7f) else XtremeMuted, fontSize = 7.sp)
            }
        }
    }
}

@Composable
private fun StatusTabs(selected: String, onSelected: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(34.dp)
            .clip(RoundedCornerShape(17.dp))
            .background(Color.Black.copy(alpha = 0.3f))
            .padding(2.dp)
    ) {
        StatusTab("LIVE", R.drawable.hots, selected == "LIVE", Modifier.weight(1f)) { onSelected("LIVE") }
        StatusTab("UPCOMING", R.drawable.claender, selected == "UPCOMING", Modifier.weight(1f)) { onSelected("UPCOMING") }
        StatusTab("COMPLETED", R.drawable.tick, selected == "COMPLETED", Modifier.weight(1f)) { onSelected("COMPLETED") }
    }
}

@Composable
private fun StatusTab(label: String, iconRes: Int, isActive: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(15.dp))
            .background(if (isActive) XtremeLime else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                colorFilter = if (isActive) ColorFilter.tint(Color.Black) else ColorFilter.tint(XtremeMuted)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = label, color = if (isActive) Color.Black else XtremeMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TournamentList() {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TournamentCard(
            title = "Summer Cricket League 2026",
            type = "Club Tournament",
            desc = "Only squads from Victory Cricket Club participate.",
            tags = listOf("Senior Men", "Round Robin"),
            stats = mapOf("Teams" to "12", "Matches" to "18", "Dates" to "12 Jul - 28 Jul"),
            progress = 0.75f,
            progressText = "75%",
            logo = R.drawable.victory
        )
        TournamentCard(
            title = "Odisha Champions Cup",
            type = "Inter-Club Tournament",
            desc = "Teams from multiple clubs participate.",
            tags = listOf("Women's", "Knockout"),
            stats = mapOf("Teams" to "8", "Matches" to "10", "Dates" to "14 Jul - 30 Jul"),
            progress = 0.5f,
            progressText = "50%",
            logo = R.drawable.victory
        )
        TournamentCard(
            title = "Victory U19 League",
            type = "Club Tournament",
            desc = "Only squads from Victory Cricket Club participate.",
            tags = listOf("Under-19", "Round Robin"),
            stats = mapOf("Teams" to "6", "Matches" to "9", "Dates" to "10 Jul - 25 Jul"),
            progress = 0.2f,
            progressText = "20%",
            logo = R.drawable.victory
        )
    }
}

@Composable
private fun TournamentCard(
    title: String,
    type: String,
    desc: String,
    tags: List<String>,
    stats: Map<String, String>,
    progress: Float,
    progressText: String,
    logo: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BlueCardBackGround),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.03f))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = logo),
                        contentDescription = null,
                        modifier = Modifier.size(38.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.victory),
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            colorFilter = ColorFilter.tint(XtremeLime)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = type, color = XtremeLime, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(text = desc, color = XtremeMuted, fontSize = 8.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        tags.forEach { tag ->
                            Surface(
                                color = Color.White.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(text = tag, color = Color.White, fontSize = 8.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                }
                Surface(
                    color = Color(0xFF1E3A1A),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.hots),
                            contentDescription = null,
                            modifier = Modifier.size(10.dp),
                            colorFilter = ColorFilter.tint(XtremeLime)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "LIVE", color = XtremeLime, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Stats row
            Row(modifier = Modifier.fillMaxWidth()) {
                StatSubItem(R.drawable.groups2, stats["Teams"] + " Teams", Modifier.weight(1f))
                StatSubItem(R.drawable.hots, stats["Matches"] + " Matches", Modifier.weight(1f)) // Using hots as per instruction context
                StatSubItem(R.drawable.claender, stats["Dates"]!!, Modifier.weight(1.5f))
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = 0.1f))) {
                    Box(modifier = Modifier.fillMaxWidth(progress).fillMaxHeight().background(XtremeLime))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = progressText, color = XtremeLime, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(12.dp))
                Surface(
                    onClick = { /* View */ },
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, XtremeLime.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "View Tournament", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = XtremeLime, modifier = Modifier.size(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatSubItem(iconRes: Int, text: String, modifier: Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            colorFilter = ColorFilter.tint(XtremeMuted)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, color = XtremeMuted, fontSize = 9.sp)
    }
}
