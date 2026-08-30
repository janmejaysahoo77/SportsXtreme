package com.example.sportsxtreme.presentation.team

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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

class AddPlayerInATeamActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = android.graphics.Color.rgb(2, 10, 20)
        window.navigationBarColor = android.graphics.Color.rgb(2, 10, 20)
        setContent { AddPlayerInATeamScreen(onBack = ::finish) }
    }
}

private val InviteBackground = Color(0xFF020A14)
private val InviteCard = Color(0xFF09131F)
private val InviteStroke = Color(0xFF263443)
private val InviteAccent = Color(0xFFC9FF16)
private val InviteMuted = Color(0xFFAAB5C0)

@Composable
private fun AddPlayerInATeamScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(InviteBackground).padding(horizontal = 16.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("‹", color = InviteAccent, fontSize = 38.sp, modifier = Modifier.clickable { onBack() }.padding(horizontal = 7.dp))
            Column(Modifier.padding(start = 12.dp)) {
                Text("ADD PLAYERS", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Black)
                Row { Text("TO ", color = InviteMuted, fontSize = 14.sp, fontWeight = FontWeight.Bold); Text("THUNDER WARRIORS", color = InviteAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
            }
            Spacer(Modifier.weight(1f)); Text("⌕", color = Color.White, fontSize = 29.sp, modifier = Modifier.border(1.dp, InviteStroke, RoundedCornerShape(8.dp)).padding(horizontal = 7.dp, vertical = 1.dp))
        }
        InviteMethod(
            icon = "▣", title = "Team Link", highlight = "Easiest way to add players.",
            description = "Share this link with captain and let them add\ntheir respective players directly to the team.", large = true
        )
        InviteMethod("▤", "Add via Phone Number", "Best for adding 1 or 2 players quickly.", "", false)
        InviteMethod("♙", "Add from Contacts", "Best if players are already in your", "contacts.", false)
        InviteMethod("▦", "Team QR Code", "Scan and add players directly", "via QR code.", false)
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun InviteMethod(icon: String, title: String, highlight: String, description: String, large: Boolean) {
    Row(
        Modifier.fillMaxWidth().padding(top = if (large) 58.dp else 12.dp).height(if (large) 162.dp else 102.dp).background(InviteCard, RoundedCornerShape(10.dp)).border(1.dp, InviteStroke, RoundedCornerShape(10.dp)).padding(horizontal = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(if (large) 73.dp else 62.dp).background(Color(0xFF122006), CircleShape).border(1.dp, InviteAccent, CircleShape), contentAlignment = Alignment.Center) { Text(icon, color = InviteAccent, fontSize = if (large) 35.sp else 30.sp) }
        Column(Modifier.padding(start = 16.dp).weight(1f)) {
            Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(highlight, color = InviteAccent.takeIf { large } ?: InviteMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            if (description.isNotEmpty()) Text(description, color = InviteMuted, fontSize = 11.sp, lineHeight = 15.sp, modifier = Modifier.padding(top = 5.dp))
            if (large) Row(Modifier.padding(top = 13.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                InviteButton("↗  Share Link", filled = false, modifier = Modifier.width(108.dp))
                InviteButton("◉  WhatsApp", filled = true, modifier = Modifier.width(112.dp))
            }
        }
        Text("›", color = Color.White, fontSize = 34.sp)
    }
}

@Composable
private fun InviteButton(text: String, filled: Boolean, modifier: Modifier) {
    Box(modifier.height(37.dp).background(if (filled) InviteAccent else Color.Transparent, RoundedCornerShape(7.dp)).border(if (filled) 0.dp else 1.dp, if (filled) Color.Transparent else InviteAccent, RoundedCornerShape(7.dp)), contentAlignment = Alignment.Center) { Text(text, color = if (filled) Color(0xFF101709) else InviteAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
}
