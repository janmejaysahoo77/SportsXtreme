package com.example.sportsxtreme.presentation.tournament

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material.icons.filled.Stadium
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.example.sportsxtreme.R
import com.example.sportsxtreme.presentation.match.SelectPlayingTeamsActivity

/** Visual-only match scheduler for league/tournament match creation. */
class ScheduleMatchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        val tournamentId = intent.getStringExtra("tournament_id").orEmpty()
        setContent {
            ScheduleMatchScreen(
                onBack = { finish() },
                onContinue = {
                    startActivity(
                        Intent(this, SelectPlayingTeamsActivity::class.java)
                            .putExtra(SelectPlayingTeamsActivity.EXTRA_TOURNAMENT_SCHEDULE_FLOW, true)
                            .putExtra("tournament_id", tournamentId)
                    )
                }
            )
        }
    }
}

private val Neon = Color(0xFFC6FF00)
private val ScheduleBackground = Color(0xFF020914)
private val SchedulePanel = Color(0xFF071525)
private val ScheduleCard = Color(0xFF09192A)
private val ScheduleSubtle = Color(0xFF91A0AF)

@Composable
private fun ScheduleMatchScreen(onBack: () -> Unit, onContinue: () -> Unit) {
    var teamA by remember { mutableStateOf("Desert Warriors") }
    var teamB by remember { mutableStateOf("Royal Challengers") }
    val scrollState = rememberScrollState()

    Box(
        Modifier.fillMaxSize().background(ScheduleBackground).drawBehind {
            drawCircle(Color(0x25185CE5), size.width * .75f, Offset(size.width * 1.08f, size.height * .06f))
            drawCircle(Color(0x1DD3FF00), size.width * .62f, Offset(-size.width * .18f, size.height * .43f))
        }
    ) {
        Column(Modifier.fillMaxSize()) {
            ScheduleHeader(onBack)
            Column(
                Modifier.weight(1f).verticalScroll(scrollState).padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                LeagueSpotlight()
                SectionLabel("SELECT TEAMS", Icons.Default.Groups)
                TeamMatchup(
                    teamA = teamA,
                    teamB = teamB,
                    onTeamAClick = { teamA = if (teamA == "Desert Warriors") "Sky Strikers" else "Desert Warriors" },
                    onTeamBClick = { teamB = if (teamB == "Royal Challengers") "Coastal Kings" else "Royal Challengers" }
                )
                SectionLabel("MATCH DETAILS", Icons.Default.CalendarMonth)
                DetailSelector(Icons.Default.CalendarMonth, "MATCH DATE", "03 June 2026", "Wednesday • league fixture")
                DetailSelector(Icons.Default.AccessTime, "MATCH TIME", "08:00 PM", "Local match start time")
                DetailSelector(Icons.Default.Stadium, "MATCH VENUE", "Bhubaneswar Cricket Ground", "Main arena • pitch 02")
                SectionLabel("MATCH PREVIEW", Icons.Default.SportsCricket)
                MatchPreview(teamA, teamB)
                Spacer(Modifier.height(86.dp))
            }
        }
        ScheduleButton(
            onClick = onContinue,
            modifier = Modifier.align(Alignment.BottomCenter).padding(14.dp)
        )
    }
}

@Composable
private fun ScheduleHeader(onBack: () -> Unit) = Row(
    Modifier.fillMaxWidth().height(68.dp).padding(horizontal = 14.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    Icon(Icons.Default.ArrowBack, "Back", tint = Color.White, modifier = Modifier.size(24.dp).clickable(onClick = onBack))
    Column(Modifier.padding(start = 16.dp).weight(1f)) {
        Text("SCHEDULE MATCH", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Black, letterSpacing = .5.sp)
        Text("Set the teams and lock in the fixture", color = ScheduleSubtle, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
    Box(Modifier.size(9.dp).clip(CircleShape).background(Neon).shadow(10.dp, CircleShape))
}

@Composable
private fun LeagueSpotlight() = Row(
    Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(SchedulePanel)
        .border(1.dp, Color(0xFF164A78), RoundedCornerShape(18.dp)).padding(15.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    Box(Modifier.size(49.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFF162B3E)), contentAlignment = Alignment.Center) {
        Icon(Icons.Default.EmojiEvents, null, tint = Neon, modifier = Modifier.size(29.dp))
    }
    Column(Modifier.padding(start = 13.dp).weight(1f)) {
        Text("Dubai Premier League", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Text("ACTIVE SEASON 2026", color = Color(0xFF0B1700), fontSize = 9.sp, fontWeight = FontWeight.Black,
            modifier = Modifier.padding(top = 5.dp).clip(RoundedCornerShape(8.dp)).background(Neon).padding(horizontal = 8.dp, vertical = 3.dp))
        Row(Modifier.padding(top = 9.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, null, tint = Neon, modifier = Modifier.size(13.dp))
            Text("Dubai", color = ScheduleSubtle, fontSize = 10.sp, modifier = Modifier.padding(start = 3.dp))
            Icon(Icons.Default.Groups, null, tint = Neon, modifier = Modifier.padding(start = 13.dp).size(13.dp))
            Text("8 Teams", color = ScheduleSubtle, fontSize = 10.sp, modifier = Modifier.padding(start = 3.dp))
        }
    }
    Image(painterResource(R.drawable.tournamentlogo), "League logo", Modifier.size(54.dp).clip(RoundedCornerShape(14.dp)), contentScale = ContentScale.Crop)
}

@Composable
private fun SectionLabel(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) = Row(verticalAlignment = Alignment.CenterVertically) {
    Icon(icon, null, tint = Neon, modifier = Modifier.size(18.dp))
    Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = .7.sp, modifier = Modifier.padding(start = 7.dp))
}

@Composable
private fun TeamMatchup(teamA: String, teamB: String, onTeamAClick: () -> Unit, onTeamBClick: () -> Unit) = Row(
    Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)
) {
    TeamCard("TEAM A", teamA, "15 Players", R.drawable.cricketlogo, onTeamAClick, Modifier.weight(1f))
    Text("VS", color = Neon, fontSize = 18.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 12.dp))
    TeamCard("TEAM B", teamB, "15 Players", R.drawable.footballlogo, onTeamBClick, Modifier.weight(1f))
}

@Composable
private fun TeamCard(label: String, team: String, players: String, logo: Int, onClick: () -> Unit, modifier: Modifier) = Column(
    modifier.clip(RoundedCornerShape(17.dp)).background(ScheduleCard).border(1.5.dp, Color(0xFF75A800), RoundedCornerShape(17.dp))
        .clickable(onClick = onClick).padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally
) {
    Text(label, color = ScheduleSubtle, fontSize = 10.sp, fontWeight = FontWeight.Black)
    Box(Modifier.padding(top = 10.dp).size(58.dp).clip(CircleShape).background(Color(0xFF020B15)).border(1.dp, Color(0xFF138BFF), CircleShape), contentAlignment = Alignment.Center) {
        Image(painterResource(logo), "$team logo", Modifier.size(46.dp).clip(CircleShape), contentScale = ContentScale.Crop)
    }
    Text(team, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 9.dp))
    Text(players, color = ScheduleSubtle, fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp))
    Row(Modifier.fillMaxWidth().padding(top = 10.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF102237)).padding(horizontal = 8.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("Change team", color = Color(0xFFD3DFE8), fontSize = 9.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = Neon, modifier = Modifier.size(15.dp))
    }
}

@Composable
private fun DetailSelector(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, caption: String) = Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
    Text(label, color = ScheduleSubtle, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = .6.sp)
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(ScheduleCard).border(1.dp, Color(0xFF153554), RoundedCornerShape(14.dp)).clickable { }.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(37.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF1A2B17)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = Neon, modifier = Modifier.size(20.dp)) }
        Column(Modifier.padding(start = 12.dp).weight(1f)) {
            Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(caption, color = ScheduleSubtle, fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp))
        }
        Icon(Icons.Default.ChevronRight, "Edit $label", tint = Color(0xFFB0C1CD), modifier = Modifier.size(21.dp))
    }
}

@Composable
private fun MatchPreview(teamA: String, teamB: String) = Column(
    Modifier.fillMaxWidth().clip(RoundedCornerShape(17.dp)).background(Brush.linearGradient(listOf(Color(0xFF0B1E32), Color(0xFF07101C))))
        .border(1.dp, Color(0xFF16415B), RoundedCornerShape(17.dp)).padding(15.dp)
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Image(painterResource(R.drawable.cricketlogo), null, Modifier.size(34.dp).clip(CircleShape), contentScale = ContentScale.Crop)
        Text(teamA, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 7.dp).weight(1f), maxLines = 1)
        Text("VS", color = Neon, fontSize = 12.sp, fontWeight = FontWeight.Black)
        Text(teamB, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.padding(end = 7.dp).weight(1f), maxLines = 1)
        Image(painterResource(R.drawable.footballlogo), null, Modifier.size(34.dp).clip(CircleShape), contentScale = ContentScale.Crop)
    }
    Row(Modifier.padding(top = 14.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        PreviewMeta("03 JUN 2026", Icons.Default.CalendarMonth)
        PreviewMeta("08:00 PM", Icons.Default.AccessTime)
        PreviewMeta("BHUBANESWAR", Icons.Default.LocationOn)
    }
    Row(Modifier.padding(top = 13.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Tag("LEAGUE STAGE", Neon, Modifier.weight(1f))
        Tag("SCHEDULED", Color(0xFF54D45E), Modifier.weight(1f))
    }
}

@Composable
private fun PreviewMeta(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) = Row(verticalAlignment = Alignment.CenterVertically) {
    Icon(icon, null, tint = ScheduleSubtle, modifier = Modifier.size(12.dp)); Text(text, color = ScheduleSubtle, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 3.dp))
}

@Composable
private fun Tag(text: String, color: Color, modifier: Modifier) = Text(text, color = color, fontSize = 9.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, modifier = modifier.clip(RoundedCornerShape(8.dp)).background(color.copy(alpha = .13f)).padding(vertical = 7.dp))

@Composable
private fun ScheduleButton(onClick: () -> Unit, modifier: Modifier) = Row(
    modifier.fillMaxWidth().height(58.dp).shadow(24.dp, RoundedCornerShape(15.dp)).clip(RoundedCornerShape(15.dp)).background(Neon).clickable(onClick = onClick),
    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center
) {
    Icon(Icons.Default.CalendarMonth, null, tint = Color(0xFF102000), modifier = Modifier.size(21.dp))
    Text("CONTINUE TO TEAM SETUP", color = Color(0xFF102000), fontSize = 14.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 10.dp))
    Icon(Icons.Default.ArrowForward, null, tint = Color(0xFF102000), modifier = Modifier.padding(start = 10.dp).size(19.dp))
}
