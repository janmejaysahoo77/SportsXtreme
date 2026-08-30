package com.example.sportsxtreme.presentation.team

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

class ManagePlayersInsideTeamActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = android.graphics.Color.rgb(2, 10, 20)
        window.navigationBarColor = android.graphics.Color.rgb(2, 10, 20)
        setContent {
            ManagePlayersScreen(
                onBack = ::finish,
                onProfile = { startActivity(Intent(this, TeamProfileActivity::class.java)) },
                onAddPlayer = { startActivity(Intent(this, AddPlayerInATeamActivity::class.java)) }
            )
        }
    }

    private val ManageBackground = Color(0xFF020A14)
    private val ManageCard = Color(0xFF09131F)
    private val ManageStroke = Color(0xFF253443)
    private val ManageAccent = Color(0xFFC9FF16)
    private val ManageMuted = Color(0xFFAAB5C0)

    private data class TeamPlayer(
        val name: String,
        val role: String,
        val tag: String? = null,
        val alert: Boolean = false
    )

    @Composable
private fun ManagePlayersScreen(onBack: () -> Unit, onProfile: () -> Unit, onAddPlayer: () -> Unit) {
        val players = listOf(
            TeamPlayer("Rohan Kumar", "All Rounder", "ADMIN"),
            TeamPlayer("Aarav Singh", "Batsman", "ADMIN • PRO"),
            TeamPlayer("Vikram Yadav", "Batsman", "CAPTAIN"),
            TeamPlayer("Rahul Das", "Wicket Keeper", "PRO"),
            TeamPlayer("Suryansh Patel", "Bowler", alert = true),
            TeamPlayer("Aditya Mehta", "All Rounder", alert = true)
        )
        Column(Modifier.fillMaxSize().background(ManageBackground).padding(horizontal = 12.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(top = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "‹",
                    color = ManageAccent,
                    fontSize = 38.sp,
                    modifier = Modifier.clickable { onBack() }.padding(horizontal = 8.dp)
                )
                Column(Modifier.padding(start = 34.dp)) {
                    Text(
                        "MANAGE PLAYERS",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        "Thunder Warriors  ✎",
                        color = ManageAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            TeamSummary()
            Row(
                Modifier.fillMaxWidth().padding(top = 15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.weight(1f).height(40.dp)
                        .background(ManageCard, RoundedCornerShape(9.dp))
                        .border(1.dp, ManageStroke, RoundedCornerShape(9.dp)),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        "⌕   Search player...",
                        color = ManageMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 13.dp)
                    )
                }
                Box(
                    Modifier.padding(start = 9.dp).size(40.dp)
                        .border(1.dp, ManageStroke, RoundedCornerShape(9.dp)),
                    contentAlignment = Alignment.Center
                ) { Text("☷", color = ManageAccent, fontSize = 21.sp) }
            }
            LazyColumn(
                Modifier.weight(1f).padding(top = 14.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) { items(players) { PlayerRow(it) } }
            Row(
                Modifier.fillMaxWidth().padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionButton(
                    "♙+  Profile",
                    Modifier.weight(1f),
                    filled = false,
                    onClick = onProfile
                )
            ActionButton("♙+  Add Player", Modifier.weight(1.1f), filled = true, onClick = onAddPlayer)
            }
        }
    }

    @Composable
    private fun TeamSummary() {
        Row(
            Modifier.fillMaxWidth().padding(top = 18.dp).height(99.dp)
                .border(1.dp, Color(0xFF7B9634), RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(66.dp).border(2.dp, ManageAccent, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) { Text("ϟ", color = ManageAccent, fontSize = 38.sp, fontWeight = FontWeight.Black) }
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text(
                    "THUNDER",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "WARRIORS",
                    color = ManageAccent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Stat("12", "PLAYERS"); Stat("1", "ADMIN"); Stat("1", "CAPTAIN")
        }
    }

    @Composable
    private fun Stat(number: String, label: String) = Column(
        Modifier.padding(start = 9.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(number, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold); Text(
        label,
        color = ManageMuted,
        fontSize = 7.sp
    )
    }

    @Composable
    private fun PlayerRow(player: TeamPlayer) {
        Row(
            Modifier.fillMaxWidth().height(65.dp).background(ManageCard, RoundedCornerShape(10.dp))
                .border(1.dp, ManageStroke, RoundedCornerShape(10.dp)).padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(46.dp).background(Color(0xFF22410D), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    player.name.first().toString(),
                    color = Color.White,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(Modifier.padding(start = 11.dp).weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        player.name,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    ); player.tag?.let {
                    Text(
                        "  $it",
                        color = ManageAccent,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                }
                Text(player.role, color = ManageMuted, fontSize = 11.sp)
            }
            if (player.alert) Text("•", color = Color(0xFFFF5766), fontSize = 28.sp)
            Text(
                "•••",
                color = ManageMuted,
                fontSize = 15.sp,
                modifier = Modifier.padding(start = 7.dp)
            )
        }
    }

    @Composable
    private fun ActionButton(
        text: String,
        modifier: Modifier,
        filled: Boolean,
        onClick: () -> Unit = {}
    ) {
        Box(
            modifier.height(50.dp).background(
                if (filled) ManageAccent else Color.Transparent,
                RoundedCornerShape(8.dp)
            ).border(
                if (filled) 0.dp else 1.dp,
                if (filled) Color.Transparent else Color(0xFF718A31),
                RoundedCornerShape(8.dp)
            ).clickable { onClick() }, contentAlignment = Alignment.Center
        ) {
            Text(
                text,
                color = if (filled) Color(0xFF111709) else ManageAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}
