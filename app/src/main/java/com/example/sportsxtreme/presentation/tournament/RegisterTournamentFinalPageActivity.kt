package com.example.sportsxtreme.presentation.tournament

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.example.sportsxtreme.R

class RegisterTournamentFinalPageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        setContent { RegisterTournamentFinalPage(onBack = { finish() }) }
    }

    companion object { const val EXTRA_TOURNAMENT_ID = "tournament_id" }
}

private val FinalBg = Color(0xFF0A1020)
private val FinalPanel = Color(0xFF151E33)
private val FinalAccent = Color(0xFFC1FF00)
private val FinalMuted = Color(0xFF9AA6BA)

@Composable
private fun RegisterTournamentFinalPage(onBack: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(1) }
    val tabs = listOf("About", "Teams", "Matches", "Points Table")
    Column(Modifier.fillMaxSize().background(FinalBg)) {
        FinalTopBar(onBack)
        FinalTournamentHeader()
        Row(Modifier.fillMaxWidth().height(48.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            tabs.forEachIndexed { index, label ->
                Column(
                    modifier = Modifier
                        .height(48.dp)
                        .clickable { selectedTab = index }
                        .padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(label, color = if (selectedTab == index) FinalAccent else FinalMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Box(Modifier.height(2.dp).width(42.dp).background(if (selectedTab == index) FinalAccent else Color.Transparent))
                }
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF1C2740)))
        if (selectedTab == 1) TeamsTab() else Box(Modifier.fillMaxSize())
    }
}

@Composable
private fun FinalTopBar(onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(58.dp).padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) {
        BackGlyph(Modifier.size(25.dp).clickable(onClick = onBack))
        Spacer(Modifier.weight(1f))
        Text("💬", fontSize = 20.sp)
        Spacer(Modifier.width(17.dp))
        Text("⚙", color = Color.White, fontSize = 22.sp)
        Spacer(Modifier.width(17.dp))
        Text("⋮", color = Color.White, fontSize = 28.sp)
    }
}

@Composable
private fun FinalTournamentHeader() {
    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(54.dp).clip(CircleShape).background(Color(0xFFEDF5DD)).border(2.dp, FinalAccent, CircleShape), contentAlignment = Alignment.Center) {
            Text("🏏", fontSize = 23.sp)
        }
        Column(Modifier.padding(start = 13.dp).weight(1f)) {
            Text("Tournament", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Black)
            Text("05 JUN, 2026 — 13 JUN, 2026  ·  19 VIEWS", color = FinalMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) { Box(Modifier.size(12.dp).clip(CircleShape).background(Color(0xFF252D42))) }
        }
    }
}

@Composable
private fun TeamsTab() {
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 15.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.fillMaxWidth().height(42.dp).clip(RoundedCornerShape(11.dp)).background(FinalPanel), contentAlignment = Alignment.CenterStart) {
            Text("⌕  Quick search", color = FinalMuted, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 14.dp))
        }
        Spacer(Modifier.height(15.dp))
        Box(Modifier.size(108.dp).clip(CircleShape).background(Color(0xFF1B2B19)), contentAlignment = Alignment.Center) { TeamGlyph(Modifier.size(57.dp)) }
        Spacer(Modifier.height(27.dp))
        Text("Invite Captains to Add Teams", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
        Spacer(Modifier.height(11.dp))
        Text("Save time! Share this link with\ncaptains, and they'll add their teams\nand players.", color = FinalMuted, fontSize = 13.sp, lineHeight = 19.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(34.dp))
        FinalActionButton("SHARE WITH CAPTAINS", filled = true)
        Text("OR", color = Color(0xFF65718A), fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(vertical = 20.dp))
        FinalActionButton("ADD MANUALLY", filled = false)
    }
}

@Composable
private fun FinalActionButton(label: String, filled: Boolean) {
    Box(Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(11.dp)).background(if (filled) FinalAccent else Color.Transparent).border(if (filled) 0.dp else 1.dp, FinalAccent.copy(alpha = .6f), RoundedCornerShape(11.dp)), contentAlignment = Alignment.Center) {
        Text(label, color = Color(0xFF101604).takeIf { filled } ?: FinalAccent, fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
    }
}

@Composable
private fun TeamGlyph(modifier: Modifier) = Canvas(modifier) {
    val stroke = Stroke(2.7.dp.toPx(), cap = StrokeCap.Round)
    val color = FinalAccent
    drawCircle(color, radius = size.width * .16f, center = androidx.compose.ui.geometry.Offset(size.width * .36f, size.height * .33f), style = stroke)
    drawCircle(color, radius = size.width * .16f, center = androidx.compose.ui.geometry.Offset(size.width * .67f, size.height * .33f), style = stroke)
    drawRoundRect(color, topLeft = androidx.compose.ui.geometry.Offset(size.width * .16f, size.height * .55f), size = androidx.compose.ui.geometry.Size(size.width * .68f, size.height * .3f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx()), style = stroke)
}

@Composable
private fun BackGlyph(modifier: Modifier) = Canvas(modifier) {
    val stroke = Stroke(2.dp.toPx(), cap = StrokeCap.Round)
    val c = Color.White
    drawLine(c, androidx.compose.ui.geometry.Offset(size.width * .75f, size.height * .5f), androidx.compose.ui.geometry.Offset(size.width * .2f, size.height * .5f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    drawLine(c, androidx.compose.ui.geometry.Offset(size.width * .2f, size.height * .5f), androidx.compose.ui.geometry.Offset(size.width * .48f, size.height * .22f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    drawLine(c, androidx.compose.ui.geometry.Offset(size.width * .2f, size.height * .5f), androidx.compose.ui.geometry.Offset(size.width * .48f, size.height * .78f), strokeWidth = stroke.width, cap = StrokeCap.Round)
}
