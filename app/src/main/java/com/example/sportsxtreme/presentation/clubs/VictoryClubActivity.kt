package com.example.sportsxtreme.presentation.clubs

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
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

class VictoryClubActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = XtremeDarkBlueHex.toInt()
        window.navigationBarColor = XtremeDarkBlueHex.toInt()
        setContent {
            VictoryClubScreen(onBack = { finish() })
        }
    }
}

private val BgColor = XtremeBgBlue
private val CardColor = XtremeCardBlue
private val AccentColor = XtremeLime
private val MutedColor = XtremeMuted
private val BorderColor = XtremeCardBorder

@Composable
fun VictoryClubScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxSize().background(
        BlueBackground
    )) {



        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                VictoryClubTopBar(onBack = onBack)
            }) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 40.dp)
            ) {
                // Header Section
                VictoryClubHeader()

                Spacer(modifier = Modifier.height(32.dp))

                // Club Overview Section
                VictoryClubSectionTitle(
                    title = "Club Overview",
                    actionText = "View All Stats",
                    onActionClick = {
                        context.startActivity(Intent(context, ClubAnalyticsActivity::class.java))
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stat Grid (3x2)
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        VictoryStatTile("Members", "486", R.drawable.contact__4_, Modifier.weight(1f))
                        VictoryStatTile("Squads", "5", R.drawable.image__41_, Modifier.weight(1f))
                        VictoryStatTile("Tournaments", "6", R.drawable.image__42_, Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        VictoryStatTile("Venues", "3", R.drawable.building, Modifier.weight(1f))
                        VictoryStatTile("Matches", "145", R.drawable.greenball, Modifier.weight(1f))
                        VictoryStatTile("Season", "2026", R.drawable.calender1, Modifier.weight(1f))
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Club Management Section
                VictoryClubSectionTitle(title = "Club Management")

                Spacer(modifier = Modifier.height(16.dp))

                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    VictoryManagementItem(
                        title = "Members",
                        subtitle = "Manage club members, invitations and roles.",
                        iconRes = R.drawable.contact__4_,
                        onClick = {
                            context.startActivity(Intent(context, ClubMemberPageActivity::class.java))
                        }
                    )
                    VictoryManagementItem(
                        title = "Squads",
                        subtitle = "Manage Senior, Women's, Under-19 and Under-16 squads.",
                        iconRes = R.drawable.image__41_,
                        onClick = {
                            context.startActivity(Intent(context, SquadInClubActivity::class.java))
                        }
                    )
                    VictoryManagementItem(
                        title = "Tournaments",
                        subtitle = "Create and manage internal and external tournaments.",
                        iconRes = R.drawable.image__42_,
                        onClick = {
                            context.startActivity(Intent(context, ClubsTournamentPlayedActivity::class.java))
                        }
                    )
                    VictoryManagementItem(
                        title = "Matches",
                        subtitle = "Schedule, score and manage fixtures.",
                        iconRes = R.drawable.calender1,
                        onClick = {
                            context.startActivity(Intent(context, MatchesPlayedByClubActivity::class.java))
                        }
                    )
                    VictoryManagementItem(
                        title = "Analytics",
                        subtitle = "View club performance, squad insights and tournament analytics.",
                        iconRes = R.drawable.bar,
                        onClick = {
                            context.startActivity(Intent(context, ClubAnalyticsActivity::class.java))
                        }
                    )
                    VictoryManagementItem(
                        title = "More",
                        subtitle = "Announcements, club profile, settings and other tools.",
                        iconRes = R.drawable.setting1,
                        onClick = { /* More */ }
                    )
                }
            }
        }
    }
}

@Composable
private fun VictoryClubTopBar(onBack: () -> Unit) {
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
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            onClick = { /* Notifications */ },
            modifier = Modifier.size(40.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.image__38_),
                contentDescription = "Notifications",
                modifier = Modifier.size(35.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = { /* More */ },
            modifier = Modifier.size(40.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.setting1),
                contentDescription = "More",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun VictoryClubHeader() {
    val colorStops = listOf(
        BlueBackground.copy(alpha = 0.98f),
        BlueBackground.copy(alpha = 0.6f),
        BlueBackground.copy(alpha = 0.98f)
    )
    Box {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.stadium),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Black overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(colorStops)
                    )
            )
        }




        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {

                Image(
                    painter = painterResource(R.drawable.image__37_),
                    contentDescription = "Club Logo",
                    modifier = Modifier.size(70.dp)
                )


            Spacer(modifier = Modifier.width(16.dp))

            Column() {
                Row() {
                    Text(
                        text = "Victory Cricket Club",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Image(
                        painter = painterResource(R.drawable.image__39_),
                        contentDescription = "Verified",
                        modifier = Modifier.size(35.dp)
                    )

                }
                // Club Owner Badge
                Box(
                    modifier = Modifier.size(100.dp)
                ) {

                    Image(
                        painter = painterResource(R.drawable.image__40_),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                    )


                }
            }


        }
    }
}

@Composable
private fun VictoryClubSectionTitle(
    title: String,
    actionText: String? = null,
    onActionClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(AccentColor)
        )
        Text(
            text = title,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        )
        if (actionText != null) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .clickable(onClick = onActionClick)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.image__41_), // Line chart icon
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = AccentColor
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = actionText,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun VictoryStatTile(
    label: String,
    value: String,
    iconRes: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glassAnimation")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset"
    )

    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = BorderStroke(
            1.dp,
            Color.White.copy(alpha = 0.12f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = BlueCardBackGround.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(18.dp)
                )
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.05f),
                            Color.White.copy(alpha = 0.28f), // Shimmer highlight sweep
                            Color.White.copy(alpha = 0.05f),
                            Color.Transparent,
                        ),
                        start = Offset(animatedOffset * 600f - 300f, 0f),
                        end = Offset(animatedOffset * 600f, 600f)
                    ),
                    shape = RoundedCornerShape(18.dp)
                )
        ) {

            // Bottom accent
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 12.dp, bottom = 8.dp)
                    .width(24.dp)
                    .height(2.dp)
                    .background(AccentColor)
            )

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
                    text = label,
                    color = MutedColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = value,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun VictoryManagementItem(
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
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = BlueCardBackGround),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Neon line on the left
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(2.dp)
                    .fillMaxHeight()
                    .padding(vertical = 12.dp)
                    .background(AccentColor)
            )

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
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        color = MutedColor,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
                
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}
