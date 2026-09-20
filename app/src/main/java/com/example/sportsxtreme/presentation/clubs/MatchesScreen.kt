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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sportsxtreme.R
import com.example.sportsxtreme.presentation.ui.theme.*

@Composable
fun MatchesScreen(onBack: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Live", "Upcoming", "Completed")

    Scaffold(
        containerColor = BlueBackground,
        topBar = {
            MatchesTopBar(onBack = onBack)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Selection
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(44.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(2.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selectedTab == index) BlueCardBackGround else Color.Transparent)
                            .clickable { selectedTab = index },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = title,
                                color = if (selectedTab == index) XtremeLime else Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                            )
                            if (selectedTab == index) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(XtremeLime)
                                )
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (selectedTab) {
                    0 -> LiveMatches()
                    1 -> UpcomingMatches()
                    2 -> CompletedMatches()
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun MatchesTopBar(onBack: () -> Unit) {
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

        // Victory Logo
        Image(
            painter = painterResource(id = R.drawable.victory),
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Matches",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Victory Cricket Club",
                    color = XtremeMuted,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Image(
                    painter = painterResource(id = R.drawable.verified),
                    contentDescription = "Verified",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        IconButton(onClick = { /* Notifications */ }) {
            Image(
                painter = painterResource(id = R.drawable.bells),
                contentDescription = "Notifications",
            )
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
private fun LiveMatches() {
    MatchCard(
        teamA = "Senior Team",
        teamB = "Titans CC",
        scoreA = "128/4",
        oversA = "(16.2 Overs)",
        scoreB = "142/6",
        oversB = "(20 Overs)",
        status = "Titans CC need 15 runs in 22 balls",
        location = "Kalinga Cricket Ground, Bhubaneswar",
        isLive = true,
        watching = "2.1k Watching"
    )
    MatchCard(
        teamA = "Women's Team",
        teamB = "Queens XI",
        scoreA = "86/2",
        oversA = "(10 Overs)",
        scoreB = "-",
        oversB = "(Yet to Bat)",
        status = "Women's Team elected to bat",
        location = "Barabati Stadium, Cuttack",
        isLive = true,
        watching = "1.3k Watching"
    )
    MatchCard(
        teamA = "U19 Team",
        teamB = "Rising Stars",
        scoreA = "98/3",
        oversA = "(14.1 Overs)",
        scoreB = "-",
        oversB = "(Yet to Bat)",
        status = "U19 Team elected to bat",
        location = "Green Field Stadium, Bhubaneswar",
        isLive = true,
        watching = "856 Watching"
    )
    MatchCard(
        teamA = "Senior Team",
        teamB = "Royal Warriors",
        scoreA = "67/1",
        oversA = "(8.0 Overs)",
        scoreB = "-",
        oversB = "(Yet to Bat)",
        status = "Senior Team elected to bat",
        location = "Kalinga Cricket Ground, Bhubaneswar",
        isLive = true,
        watching = "642 Watching"
    )
}

@Composable
private fun UpcomingMatches() {
    // Similar placeholders for Upcoming
    Text("No upcoming matches found", color = XtremeMuted, modifier = Modifier.padding(16.dp))
}

@Composable
private fun CompletedMatches() {
    // Similar placeholders for Completed
    Text("No completed matches found", color = XtremeMuted, modifier = Modifier.padding(16.dp))
}

@Composable
private fun MatchCard(
    teamA: String,
    teamB: String,
    scoreA: String,
    oversA: String,
    scoreB: String,
    oversB: String,
    status: String,
    location: String,
    isLive: Boolean,
    watching: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BlueCardBackGround),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Live and Watching Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (isLive) {
                    Surface(
                        color = Color(0xFF3D1A1A),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "LIVE",
                            color = Color(0xFFFF5252),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                
                Surface(
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = XtremeLime,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = watching, color = Color.White, fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "$teamA vs $teamB",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Teams and Score Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Team A
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(80.dp)) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.03f))
                            .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.shield),
                            contentDescription = null,
                            modifier = Modifier.size(35.dp),
                            colorFilter = ColorFilter.tint(Color(0xFF2196F3))
                        )
                        Text(
                            text = teamA.split(" ").map { it.take(1) }.joinToString(""),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = teamA.uppercase(), color = XtremeMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }

                // Score A
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = scoreA, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    Text(text = oversA, color = XtremeMuted, fontSize = 10.sp)
                }

                // VS / LIVE Tag
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "LIVE", color = Color(0xFFFF5252), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(text = "vs", color = XtremeMuted, fontSize = 12.sp)
                }

                // Score B
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = scoreB, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    Text(text = oversB, color = XtremeMuted, fontSize = 10.sp)
                }

                // Team B
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(80.dp)) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.03f))
                            .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.shields),
                            contentDescription = null,
                            modifier = Modifier.size(35.dp),
                            colorFilter = ColorFilter.tint(Color(0xFFFFB300))
                        )
                        Text(
                            text = teamB.split(" ").map { it.take(1) }.joinToString(""),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = teamB.uppercase(), color = XtremeMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 1.dp)
            
            Spacer(modifier = Modifier.height(8.dp))

            // Status and Location Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = status,
                    color = XtremeMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.weight(1f)
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.pointer),
                        contentDescription = null,
                        tint = XtremeMuted,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = location.split(",").first(), color = XtremeMuted, fontSize = 10.sp)
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = XtremeMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
