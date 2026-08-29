package com.example.sportsxtreme.presentation.team

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

class CreateTeamActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = android.graphics.Color.rgb(2, 10, 20)
        window.navigationBarColor = android.graphics.Color.rgb(2, 10, 20)
        setContent {
            CreateTeamScreen(
                onBack = ::finish,
                onContinue = {
                    startActivity(
                        Intent(
                            this,
                            ManagePlayersInsideTeamActivity::class.java
                        )
                    )
                }
            )
        }
    }

    private val TeamBackground = Color(0xFF020A14)
    private val TeamCard = Color(0xFF09131F)
    private val TeamStroke = Color(0xFF263443)
    private val TeamAccent = Color(0xFFC9FF16)
    private val TeamMuted = Color(0xFF9BA8B6)

    @Composable
    private fun CreateTeamScreen(onBack: () -> Unit, onContinue: () -> Unit) {
        var teamName by remember { mutableStateOf("") }
        var city by remember { mutableStateOf("") }
        var mobile by remember { mutableStateOf("") }
        var captain by remember { mutableStateOf("") }
        var addMyself by remember { mutableStateOf(true) }

        Column(Modifier.fillMaxSize().background(TeamBackground).padding(horizontal = 16.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(top = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "‹",
                    color = TeamAccent,
                    fontSize = 38.sp,
                    modifier = Modifier.clickable { onBack() }.padding(end = 10.dp)
                )
                Text("SPORT", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("XTREME", color = TeamAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Column(
                Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "CREATE YOUR ",
                    color = Color.White,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    "TEAM",
                    color = TeamAccent,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.offset(y = (-28).dp).padding(start = 185.dp)
                )
                Text(
                    "Let's build your dream team",
                    color = TeamMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.offset(y = (-20).dp)
                )
                LogoPicker()
                Text(
                    "Team Logo",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(top = 10.dp)
                )
                Text("Upload your team logo", color = TeamMuted, fontSize = 11.sp)
                Spacer(Modifier.height(12.dp))
                TeamTextField("TEAM NAME *", "Enter your team name", teamName, { teamName = it })
                TeamTextField("CITY / TOWN *", "Enter city / town", city, { city = it })
                TeamTextField(
                    "TEAM CAPTAIN / COORDINATOR (OPTIONAL)",
                    "+91  Enter mobile number",
                    mobile,
                    { mobile = it },
                    KeyboardType.Phone
                )
                TeamTextField(
                    "TEAM CAPTAIN NAME (OPTIONAL)",
                    "Enter captain name",
                    captain,
                    { captain = it })
                Row(
                    Modifier.fillMaxWidth().padding(top = 10.dp)
                        .border(1.dp, Color(0xFF536B1C), RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(34.dp)
                            .background(Color(0xFF1C3210), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) { Text("♟", color = TeamAccent, fontSize = 19.sp) }
                    Column(Modifier.padding(start = 10.dp).weight(1f)) {
                        Text(
                            "Add yourself in the team",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text("You will be added as a member", color = TeamMuted, fontSize = 10.sp)
                    }
                    Switch(
                        checked = addMyself,
                        onCheckedChange = { addMyself = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = TeamAccent
                        )
                    )
                }
                Spacer(Modifier.height(12.dp))
            }
            Box(
                Modifier.fillMaxWidth().height(52.dp).padding(bottom = 8.dp)
                    .background(TeamAccent, RoundedCornerShape(8.dp)).clickable { onContinue() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Continue     →",
                    color = Color(0xFF101709),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    @Composable
    private fun LogoPicker() {
        Box(
            Modifier.padding(top = 2.dp).size(122.dp).border(2.dp, Color(0xFF516D19), CircleShape)
                .background(Color(0xFF07111C), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("▱", color = Color.White, fontSize = 38.sp)
                Text("ADD LOGO", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                "+",
                color = Color(0xFF172000),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.TopEnd).background(TeamAccent, CircleShape)
                    .padding(horizontal = 7.dp, vertical = 1.dp)
            )
        }
    }

    @Composable
    private fun TeamTextField(
        label: String,
        placeholder: String,
        value: String,
        onValueChange: (String) -> Unit,
        keyboardType: KeyboardType = KeyboardType.Text
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            label = { Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            placeholder = { Text(placeholder, fontSize = 13.sp) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = TeamCard,
                unfocusedContainerColor = TeamCard,
                focusedBorderColor = TeamAccent,
                unfocusedBorderColor = TeamStroke,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = TeamAccent,
                unfocusedLabelColor = TeamMuted,
                unfocusedPlaceholderColor = TeamMuted
            )
        )
    }
}
