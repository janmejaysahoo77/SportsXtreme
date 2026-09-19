package com.example.sportsxtreme.presentation.clubs

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.sportsxtreme.R
import com.example.sportsxtreme.presentation.ui.theme.*

class OwnedClubPageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = XtremeDarkBlueHex.toInt()
        window.navigationBarColor = XtremeDarkBlueHex.toInt()
        setContent {
            OwnedClubScreen(onBack = { finish() })
        }
    }
}

private val OwnedBg = XtremeBgBlue
private val OwnedCard = XtremeCardBlue
private val Accent = XtremeLime
private val OwnedMuted = XtremeMuted

@Composable
fun OwnedClubScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    Scaffold(
        containerColor = OwnedBg,
        topBar = {
            OwnedClubTopBar(onBack = onBack)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Hero Banner Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.club_stadium),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Bottom Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, OwnedBg),
                                startY = 300f
                            )
                        )
                )
            }

            // Profile Info Overlap Section
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .offset(y = (-40).dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        // Club Logo
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(25, 43, 23))
                                .border(2.dp, OwnedBg, RoundedCornerShape(20.dp))
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(R.drawable.team_logo),
                                contentDescription = "Club Logo",
                                modifier = Modifier.size(64.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.padding(bottom = 4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Warriors Cricket Club",
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Image(
                                    painter = painterResource(R.drawable.verified),
                                    contentDescription = "Verified",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(R.drawable.tower),
                                    contentDescription = "Location",
                                    modifier = Modifier.size(14.dp),
                                    colorFilter = ColorFilter.tint(OwnedMuted)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Cuttack, Odisha",
                                    color = OwnedMuted,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Role Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Accent.copy(alpha = 0.15f))
                            .border(1.dp, Accent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(R.drawable.club_owner),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Club Owner",
                                color = Accent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Stats Overview Section
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .offset(y = (-20).dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "|  Club Overview",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "View Analytics  →",
                        color = Accent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            context.startActivity(Intent(context, ClubAnalyticsActivity::class.java))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Grid
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile(
                        label = "Members",
                        value = "486",
                        iconRes = R.drawable.contact__4_,
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        label = "Squads",
                        value = "5",
                        iconRes = R.drawable.grouping,
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        label = "Matches",
                        value = "145",
                        iconRes = R.drawable.greenball,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile(
                        label = "Tournaments",
                        value = "12",
                        iconRes = R.drawable.trophy1,
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        label = "Wins",
                        value = "98",
                        iconRes = R.drawable.victory,
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        label = "Season",
                        value = "2026",
                        iconRes = R.drawable.calender1,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Club Management Section
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "|  Club Management",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                ManagementItem(
                    title = "Manage Members",
                    subtitle = "Invite players, assign roles & teams",
                    iconRes = R.drawable.add_member,
                    onClick = {
                        context.startActivity(Intent(context, ClubMemberPageActivity::class.java))
                    }
                )
                
                ManagementItem(
                    title = "Match Fixtures",
                    subtitle = "Schedule & manage upcoming matches",
                    iconRes = R.drawable.calender1,
                    onClick = {
                        context.startActivity(Intent(context, MatchesPlayedByClubActivity::class.java))
                    }
                )

                ManagementItem(
                    title = "Club Settings",
                    subtitle = "Profile, permissions & preferences",
                    iconRes = R.drawable.settings,
                    onClick = { /* Settings */ }
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun OwnedClubTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
        Text(
            text = "Warriors Cricket Club",
            color = Color.White,
            fontSize = 19.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = { /* Settings */ },
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
        ) {
            Image(
                painter = painterResource(R.drawable.settings),
                contentDescription = "Settings",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    iconRes: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = OwnedCard),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                color = OwnedMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ManagementItem(
    title: String,
    subtitle: String,
    iconRes: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = OwnedCard),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = OwnedMuted,
                    fontSize = 12.sp
                )
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = OwnedMuted
            )
        }
    }
}
