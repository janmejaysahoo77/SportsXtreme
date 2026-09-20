package com.example.sportsxtreme.presentation.clubs

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.sportsxtreme.R
import com.example.sportsxtreme.presentation.ui.theme.*

class AddTeamInsideSquadActivity : ComponentActivity() {
    companion object { const val EXTRA_SQUAD_NAME = "extra_squad_name" }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val squadName = intent.getStringExtra(EXTRA_SQUAD_NAME) ?: "Senior Men"
            SquadTeamsScreen(squadName = squadName, onBack = { finish() })
        }
    }
}

@Composable
fun SquadTeamsScreen(squadName: String, onBack: () -> Unit) {
    val context = LocalContext.current
    Scaffold(
        containerColor = BlueBackground,
        topBar = {
            SquadTeamsTopBar(title = squadName, onBack = onBack)
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

            // Squad Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = BlueCardBackGround),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                    // Left Glow
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .fillMaxHeight()
                            .background(brush = Brush.verticalGradient(listOf(XtremeLime, Color.Transparent)))
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, Color.White.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.groups2),
                                contentDescription = null,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "$squadName Squad",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "5", color = XtremeLime, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = " Teams  •  ", color = XtremeMuted, fontSize = 12.sp)
                                Text(text = "90", color = XtremeLime, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = " Players", color = XtremeMuted, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Teams Section
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.width(3.dp).height(16.dp).clip(RoundedCornerShape(1.dp)).background(XtremeLime))
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = "Teams", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            val teams = listOf("A", "B", "C", "D", "E")
            val captains = listOf("Rahul", "Amit", "Suresh", "Vikash", "Nilesh")
            
            teams.forEachIndexed { index, letter ->
                TeamItemCard(
                    name = "Senior Team $letter",
                    captain = captains[index],
                    onClick = {
                        context.startActivity(Intent(context, PlayerInsideSquadOfAClub::class.java).apply {
                            putExtra(PlayerInsideSquadOfAClub.EXTRA_TEAM_NAME, "Senior Team $letter")
                        })
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))
            CreateNewTeamDashedButton()
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SquadTeamsTopBar(title: String, onBack: () -> Unit) {
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
            text = title,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp).weight(1f)
        )

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
private fun TeamItemCard(
    name: String,
    captain: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().height(80.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BlueCardBackGround),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
             // Left Glow
             Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(brush = Brush.verticalGradient(listOf(XtremeLime, Color.Transparent)))
            )

            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.03f))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.groups2),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Row {
                        Text(text = "18", color = XtremeLime, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(text = " Players  •  Captain $captain", color = XtremeMuted, fontSize = 11.sp)
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun CreateNewTeamDashedButton() {
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
                text = "Create New Team",
                color = XtremeLime,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
