package com.example.sportsxtreme.presentation.clubs

import android.graphics.Color
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

class ClubLandingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = Color.rgb(5, 7, 8)
        window.navigationBarColor = Color.rgb(5, 7, 8)
        setContent { ClubLandingScreen(onBack = ::finish) }
    }
}

private val Lime = ComposeColor(190, 255, 24)
private val ScreenBlack = ComposeColor(5, 7, 8)
private val CardBlack = ComposeColor(15, 17, 17)
private val Muted = ComposeColor(162, 169, 164)

@Composable
private fun ClubLandingScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(ScreenBlack).verticalScroll(rememberScrollState()).padding(horizontal = 12.dp)
    ) {
        Header(onBack)
        Spacer(Modifier.height(10.dp))
        CommunityHero()
        Spacer(Modifier.height(12.dp))
        MyClubsCard()
        Spacer(Modifier.height(12.dp))
        DiscoverCard()
        Spacer(Modifier.height(12.dp))
        CreateClubCard()
        Spacer(Modifier.height(12.dp))
        BenefitsCard()
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun Header(onBack: () -> Unit) = Row(
    modifier = Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically
) {
    Text("‹", color = ComposeColor.White, fontSize = 34.sp, modifier = Modifier.size(36.dp).clickable { onBack() })
    Spacer(Modifier.width(12.dp))
    Text("Clubs", color = ComposeColor.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
    Text("⌕", color = Muted, fontSize = 25.sp)
    Spacer(Modifier.width(18.dp))
    Text("♧", color = Muted, fontSize = 20.sp)
}

@Composable
private fun CommunityHero() = Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardBlack)) {
    Box(modifier = Modifier.fillMaxWidth().height(158.dp).background(Brush.linearGradient(listOf(ComposeColor(28, 57, 52), CardBlack)))) {
        CricketGlow(Modifier.fillMaxSize())
        Column(modifier = Modifier.padding(14.dp)) {
            Tag("COMMUNITY HUB")
            Spacer(Modifier.height(11.dp))
            Text("Build Your Cricket\nCommunity", color = ComposeColor.White, fontSize = 19.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(7.dp))
            Text("Create, discover and manage\ncricket clubs, players, tournaments\nand teams all in one place.", color = Muted, fontSize = 9.sp, lineHeight = 12.sp)
            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(21.dp)) { Stat("10K+", "CLUBS CREATED"); Stat("2M+", "PLAYERS"); Stat("50K+", "MATCHES") }
        }
    }
}

@Composable
private fun MyClubsCard() = ClubCard("◉", "My Clubs", "Access and manage the clubs you create or participate in.", "VIEW CLUB") {
    Tag("1 Active Club")
}

@Composable
private fun DiscoverCard() = ClubCard("◉", "Discover Clubs", "Find and join the most vibrant cricket clubs and experiences near you.", "DISCOVER ϟ") {
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) { Tag("Nearby"); Tag("Verified"); Tag("Trending") }
}

@Composable
private fun CreateClubCard() = ClubCard("+", "Create Club", "Build your legacy from the ground up. Start your own cricket club today.", "CREATE CLUB ϟ")

@Composable
private fun ClubCard(symbol: String, title: String, body: String, button: String, extra: @Composable (() -> Unit)? = null) =
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardBlack)) {
        val context = LocalContext.current
        Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
            Canvas(Modifier.fillMaxSize()) { drawCircle(ComposeColor(0, 128, 194, 41), radius = size.minDimension * .55f, center = Offset(size.width, size.height / 2)) }
            Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                Box(Modifier.size(25.dp).clip(RoundedCornerShape(7.dp)).background(ComposeColor(35, 43, 38)), contentAlignment = Alignment.Center) { Text(symbol, color = Lime, fontWeight = FontWeight.Bold) }
                Spacer(Modifier.height(7.dp))
                Row(verticalAlignment = Alignment.CenterVertically) { Text(title, color = ComposeColor.White, fontSize = 13.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.weight(1f)); extra?.invoke() }
                Spacer(Modifier.height(4.dp))
                Text(body, color = Muted, fontSize = 8.sp, lineHeight = 10.sp, modifier = Modifier.fillMaxWidth(.7f))
                Spacer(Modifier.weight(1f))
                Box(Modifier.fillMaxWidth().height(29.dp).clip(RoundedCornerShape(7.dp)).background(Lime).clickable {
                    when (title) {
                        "My Clubs" -> context.startActivity(Intent(context, MyClubsLandingPageActivity::class.java))
                        "Discover Clubs" -> context.startActivity(Intent(context, DiscoverClubsActivity::class.java))
                        "Create Club" -> context.startActivity(Intent(context, Step_OneCreateClubActivity::class.java))
                    }
                }, contentAlignment = Alignment.Center) { Text(button, color = ComposeColor.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }

@Composable
private fun BenefitsCard() = Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardBlack)) {
    Column(Modifier.padding(13.dp)) {
        Tag("PROFESSIONAL SUITE")
        Spacer(Modifier.height(10.dp))
        Text("Everything You Need\nto Run a Cricket\nClub", color = ComposeColor.White, fontSize = 15.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Benefit("♟", "Team Management", "Manage teams, squads, players and coaches")
        Benefit("▥", "Tournament Hosting", "Organize matches and tournaments")
        Benefit("ϟ", "AI Performance Analytics", "Advanced player insights and stats")
    }
}

@Composable
private fun Benefit(icon: String, title: String, body: String) = Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
    Text(icon, color = Lime, fontSize = 17.sp, modifier = Modifier.width(28.dp))
    Column { Text(title, color = ComposeColor.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold); Text(body, color = Muted, fontSize = 7.sp) }
}

@Composable
private fun Tag(text: String) = Text(text, color = Lime, fontSize = 7.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clip(RoundedCornerShape(5.dp)).background(ComposeColor(44, 60, 20)).padding(horizontal = 7.dp, vertical = 3.dp))

@Composable
private fun Stat(value: String, label: String) = Column { Text(value, color = ComposeColor.White, fontSize = 10.sp, fontWeight = FontWeight.Bold); Text(label, color = Muted, fontSize = 5.sp) }

@Composable
private fun CricketGlow(modifier: Modifier) = Canvas(modifier) {
    drawCircle(ComposeColor(159, 239, 81, 38), radius = size.minDimension * .46f, center = Offset(size.width * .76f, size.height * .72f))
    drawCircle(ComposeColor(177, 255, 31, 64), radius = size.minDimension * .24f, center = Offset(size.width * .76f, size.height * .72f), style = Stroke(2f))
}
