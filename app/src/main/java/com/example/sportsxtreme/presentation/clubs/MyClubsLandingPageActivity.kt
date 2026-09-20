package com.example.sportsxtreme.presentation.clubs

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch

class MyClubsLandingPageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContent {
            MyClubsScreen(onBack = { finish() })
        }
    }
}

private val ScreenBlack = BlueBackground
private val DarkBlueCard = BlueCardBackGround
private val CardBlack = BlueCardBackGround
private val Lime = XtremeLime
private val Muted = XtremeMuted
private val CardBorder = BlueCardBackGround

@Composable
private fun MyClubsScreen(onBack: () -> Unit) {
    val pagerState = rememberPagerState { 2 }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = ScreenBlack,
        topBar = {
            MyClubsTopBar(onBack = onBack)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Manage the clubs you own and have joined.",
                color = Muted,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Custom Tab Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(10, 21, 35))
                    .padding(4.dp)
            ) {
                TabItem(
                    title = "Owned",
                    isSelected = pagerState.currentPage == 0,
                    modifier = Modifier.weight(1f),
                    onClick = { scope.launch { pagerState.animateScrollToPage(0) } }
                )
                TabItem(
                    title = "Joined",
                    isSelected = pagerState.currentPage == 1,
                    modifier = Modifier.weight(1f),
                    onClick = { scope.launch { pagerState.animateScrollToPage(1) } }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Top
            ) { page ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (page == 0) {
                        MyClubsOwnedClubsContent()
                    } else {
                        MyClubsJoinedClubsContent()
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun MyClubsTopBar(onBack: () -> Unit) {
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
            text = "My Clubs",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = { /* Search */ },
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun TabItem(title: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) Lime else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) Color.Black else Muted,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MyClubsOwnedClubsContent() {
    val context = LocalContext.current
    SectionHeader(title = "Owned Clubs", showCreate = true)
    Spacer(modifier = Modifier.height(20.dp))
    ClubItemCard(
        name = "Warriors Cricket Club",
        location = "Cuttack, Odisha",
        role = "Founder/Owner",
        bannerRes = R.drawable.stadium,
        isBranded = false,
        onClick = {
            context.startActivity(Intent(context, OwnedClubPageActivity::class.java))
        }
    )
    Spacer(modifier = Modifier.height(20.dp))
    ClubItemCard(
        name = "Royal Kings Cricket Club",
        location = "Puri, Odisha",
        role = "President",
        bannerRes = R.drawable.club_stadium,
        isBranded = false,
        onClick = {
            context.startActivity(Intent(context, VictoryClubActivity::class.java))
        }
    )
}

@Composable
private fun MyClubsJoinedClubsContent() {
    val context = LocalContext.current
    SectionHeader(title = "Joined Clubs", showCreate = false)
    Spacer(modifier = Modifier.height(20.dp))
    ClubItemCard(
        name = "Warriors Cricket Club",
        location = "Cuttack, Odisha",
        role = "Player",
        bannerRes = R.drawable.cricket_choosesports,
        isBranded = true,
        showBadge = true,
        onClick = {
            context.startActivity(Intent(context, OwnedClubPageActivity::class.java))
        }
    )
    Spacer(modifier = Modifier.height(20.dp))
    ClubItemCard(
        name = "Royal Kings Cricket Club",
        location = "Puri, Odisha",
        role = "Coach",
        bannerRes = R.drawable.club_stadium,
        isBranded = false,
        showBadge = true,
        onClick = {
            context.startActivity(Intent(context, VictoryClubActivity::class.java))
        }
    )
}

@Composable
private fun SectionHeader(title: String, showCreate: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

            Image(
                painter = painterResource(R.drawable.bar),
                contentDescription = null,
                modifier = Modifier.size(45.dp),
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
        if (showCreate) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Lime)
                    .clickable { /* Create */ }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "+ Create Club",
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun ClubItemCard(
    name: String,
    location: String,
    role: String,
    bannerRes: Int,
    isBranded: Boolean,
    showBadge: Boolean = false,
    onClick: () -> Unit = {}
) {
    val colorStops = listOf(
        BlueCardBackGround.copy(alpha = 0.5f),
        BlueCardBackGround.copy(alpha = 0.2f),
        BlueCardBackGround.copy(alpha = 0.5f)
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = DarkBlueCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ){
                    Image(
                        painter = painterResource(bannerRes),
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
                
                // Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                startY = 100f
                            )
                        )
                )

                if (isBranded) {
                    Text(
                        text = "SportsXtreme",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
            ) {
                Column {
                    // Overlapping Logo
                    Box(
                        modifier = Modifier
                            .offset(y = (-28).dp)
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(25, 43, 23))
                            .border(2.dp, CardBlack, CircleShape)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.team_logo),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Column(modifier = Modifier.offset(y = (-20).dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = name,
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (showBadge) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Image(
                                    painter = painterResource(R.drawable.baseline_check_circle_24),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    colorFilter = ColorFilter.tint(Lime)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(R.drawable.mapp),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = location,
                                color = Muted,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(R.drawable.contact__3_),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = role,
                                color = Lime,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // View Club Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Lime)
                                .clickable(onClick = onClick)
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "View Club",
                                color = Color.Black,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(20.dp)
                                    .align(Alignment.CenterEnd),
                                tint = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}
