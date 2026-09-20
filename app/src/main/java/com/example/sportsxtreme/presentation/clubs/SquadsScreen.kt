package com.example.sportsxtreme.presentation.clubs

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.sportsxtreme.R
import com.example.sportsxtreme.presentation.ui.theme.*

@Composable
fun SquadsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    Scaffold(
        containerColor = BlueBackground,
        topBar = {
            SquadsTopBar(onBack = onBack)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Info Header - Styled like @MembersScreen HeaderSection
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp) // Matched with MembersScreen logo size
                        .clip(RoundedCornerShape(12.dp))
                        .background(BlueCardBackGround)
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.groups2),
                        contentDescription = null,
                        modifier = Modifier.size(35.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = "Squads",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Squads are categories of teams\nin your club.",
                        color = XtremeMuted,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Squad Categories
            SquadCategoryCard(
                title = "Senior Men",
                teamsCount = "5 Teams",
                iconRes = R.drawable.trophy12,
                onClick = {
                    context.startActivity(Intent(context, AddTeamInsideSquadActivity::class.java).apply {
                        putExtra(AddTeamInsideSquadActivity.EXTRA_SQUAD_NAME, "Senior Men")
                    })
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            SquadCategoryCard(
                title = "Women's",
                teamsCount = "3 Teams",
                iconRes = R.drawable.girl,
                onClick = { /* Navigate */ }
            )

            Spacer(modifier = Modifier.height(12.dp))
            
            SquadCategoryCard(
                title = "Under-19 Boys",
                teamsCount = "4 Teams",
                iconRes = R.drawable.user,
                onClick = { /* Navigate */ }
            )

            Spacer(modifier = Modifier.height(12.dp))
            
            SquadCategoryCard(
                title = "Under-16 Boys",
                teamsCount = "2 Teams",
                iconRes = R.drawable.user,
                onClick = { /* Navigate */ }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Create New Squad Button (Dashed)
            CreateNewSquadDashedButton()
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SquadsTopBar(onBack: () -> Unit) {
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
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Text(
            text = "Squads",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp).weight(1f)
        )

        IconButton(
            onClick = { /* Add Squad */ },
            modifier = Modifier
                .border(1.5.dp, XtremeLime, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = XtremeLime,
            )
        }
    }
}

@Composable
private fun SquadCategoryCard(
    title: String,
    teamsCount: String,
    iconRes: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BlueCardBackGround),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Left Glow Line - Signature @MembersScreen / @VictoryClub style
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(XtremeLime, Color.Transparent)
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.03f))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.padding(4.dp),

                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = teamsCount,
                        color = XtremeMuted,
                        fontSize = 11.sp
                    )
                }
                
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun CreateNewSquadDashedButton() {
    val stroke = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable { /* Action */ },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRoundRect(
                color = XtremeLime.copy(alpha = 0.5f),
                style = stroke,
                cornerRadius = CornerRadius(10.dp.toPx())
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = XtremeLime,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Create New Squad",
                color = XtremeLime,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
