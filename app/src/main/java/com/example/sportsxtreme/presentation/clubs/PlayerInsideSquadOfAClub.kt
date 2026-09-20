package com.example.sportsxtreme.presentation.clubs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.sportsxtreme.R
import com.example.sportsxtreme.presentation.ui.theme.*

class PlayerInsideSquadOfAClub : ComponentActivity() {
    companion object {
        const val EXTRA_TEAM_NAME = "extra_team_name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val teamName = intent.getStringExtra(EXTRA_TEAM_NAME) ?: "Senior Team A"
            TeamPlayersScreen(teamName = teamName, onBack = { finish() })
        }
    }
}

private data class PlayerDetail(
    val id: String,
    val jersey: String,
    val name: String,
    val role: String,
    val badge: String? = null
)

@Composable
fun TeamPlayersScreen(teamName: String, onBack: () -> Unit) {
    val players = listOf(
        PlayerDetail("01", "07", "Rahul Kumar", "Right Hand Batsman", "Captain"),
        PlayerDetail("02", "18", "Amit Sharma", "Right Arm Fast Bowler", "Vice Captain"),
        PlayerDetail("03", "45", "Suresh Nayak", "Wicket Keeper"),
        PlayerDetail("04", "22", "Vivek Sharma", "Right Hand Batsman"),
        PlayerDetail("05", "31", "Nilesh Rao", "Right Arm Off Break"),
        PlayerDetail("06", "11", "Arjun Patil", "Left Hand Batsman"),
        PlayerDetail("07", "09", "Karan Singh", "Right Arm Fast Bowler"),
        PlayerDetail("08", "27", "Vivek Sharma", "Right Arm Medium Fast"),
        PlayerDetail("09", "63", "Rahul Das", "All Rounder"),
        PlayerDetail("10", "04", "Amit Kumar", "Right Hand Batsman")
    )

    Scaffold(
        containerColor = BlueBackground,
        topBar = {
            TeamPlayersTopBar(title = teamName, onBack = onBack)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Team Header Card
            TeamHeaderCard(teamName)

            Spacer(modifier = Modifier.height(20.dp))

            // Players Section Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(XtremeLime)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Players",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    onClick = { /* Add Player Action */ },
                    color = XtremeLime,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add Player",
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Player List
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                players.forEach { player ->
                    DetailedPlayerCard(player)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TeamPlayersTopBar(title: String, onBack: () -> Unit) {
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

        Text(
            text = title,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f)
        )

        IconButton(onClick = { /* Menu Action */ }) {
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
private fun TeamHeaderCard(teamName: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BlueCardBackGround),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(90.dp)) {
            // Background equipment overlay (faint stadium/pattern)
            Image(
                painter = painterResource(id = R.drawable.stadium),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.08f),
                contentScale = ContentScale.Crop
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Team Text Logo (e.g., STA)
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.03f))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = teamName.split(" ").mapNotNull { it.firstOrNull() }.joinToString("").take(3).uppercase()
                    Text(
                        text = initials,
                        color = XtremeLime,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = teamName,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Senior Men Squad",
                        color = XtremeMuted,
                        fontSize = 11.sp
                    )
                }

                // Active Status Badge
                Surface(
                    color = Color(0xFF1E3A1A),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(XtremeLime)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Active",
                            color = XtremeLime,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailedPlayerCard(player: PlayerDetail) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BlueCardBackGround),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Index number
            Text(
                text = player.id,
                color = XtremeMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(28.dp)
            )

            // Profile Image with online dot
            Box(modifier = Modifier.size(44.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.whiteuser),
                    contentDescription = player.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(1.dp, XtremeCardBorder, CircleShape),
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.tint(XtremeMuted)
                )
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(XtremeLime)
                        .border(1.5.dp, BlueCardBackGround, CircleShape)
                        .align(Alignment.BottomEnd)
                        .offset(x = (-1).dp, y = (-1).dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Jersey Number
            Text(
                text = player.jersey,
                color = XtremeLime,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(28.dp)
            )

            // Name and Role
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = player.name,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (player.badge != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        PlayerRoleBadge(player.badge)
                    }
                }
                Text(
                    text = player.role,
                    color = XtremeMuted,
                    fontSize = 10.sp
                )
            }

            // Navigation Arrow
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun PlayerRoleBadge(label: String) {
    Surface(
        color = Color.Transparent,
        border = BorderStroke(1.dp, XtremeLime.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = label,
            color = XtremeLime,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
