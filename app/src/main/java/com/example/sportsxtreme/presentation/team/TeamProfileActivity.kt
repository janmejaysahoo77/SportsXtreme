package com.example.sportsxtreme.presentation.team

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

class TeamProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = android.graphics.Color.rgb(2, 10, 20)
        window.navigationBarColor = android.graphics.Color.rgb(2, 10, 20)
        setContent { TeamProfileScreen(::finish) }
    }
}

private val ProfileBackground = Color(0xFF020A14)
private val ProfileCard = Color(0xFF09131F)
private val ProfileStroke = Color(0xFF294055)
private val ProfileAccent = Color(0xFFC9FF16)
private val ProfileMuted = Color(0xFFC1CBD6)

@Composable
private fun TeamProfileScreen(onBack: () -> Unit) {
    var selectedTab by remember { mutableStateOf("Profile") }
    Column(Modifier.fillMaxSize().background(ProfileBackground)) {
        TopBar(onBack)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            TeamHero()
            ProfileTabs(selectedTab) { selectedTab = it }
            when (selectedTab) {
                "Profile" -> ProfileTabContent()
                "Matches" -> MatchesTabContent()
                "Stats" -> StatsTabContent()
                "Leaderboard" -> LeaderboardTabContent()
                "Members" -> MembersTabContent()
                "Photos" -> PhotosTabContent()
            }
            Spacer(Modifier.height(100.dp))
        }
    }
}

@Composable
private fun TopBar(onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(70.dp).padding(horizontal = 15.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("‹", color = Color.White, fontSize = 36.sp, modifier = Modifier.clickable { onBack() }.padding(end = 18.dp))
        Column(Modifier.weight(1f)) {
            Row { Text("Sports", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black); Text("Xtreme", color = ProfileAccent, fontSize = 18.sp, fontWeight = FontWeight.Black) }
            Text("PLAY  •  SCORE  •  BELONG", color = ProfileMuted, fontSize = 7.sp, letterSpacing = 1.sp)
        }
        Text("⌯", color = Color.White, fontSize = 24.sp, modifier = Modifier.padding(end = 16.dp))
        Text("⋮", color = Color.White, fontSize = 27.sp)
    }
}

@Composable
private fun TeamHero() {
    Box(Modifier.fillMaxWidth().height(184.dp).drawBehind {
        drawRect(Color(0xFF071629))
        repeat(5) { i -> drawCircle(Color(0x263A6490), radius = size.width * .15f, center = Offset(size.width * (i / 4f), size.height * .22f)) }
        drawCircle(Color(0x552E5C3A), radius = size.width * .7f, center = Offset(size.width * .65f, size.height * 1.2f))
    }) {
        Column(Modifier.align(Alignment.BottomStart).padding(start = 22.dp, bottom = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(70.dp).background(Color(0xFF914E42), CircleShape).border(2.dp, Color(0xFFC6EEB4), CircleShape), contentAlignment = Alignment.Center) { Text("AD", color = Color.White, fontSize = 27.sp) }
                Column(Modifier.padding(start = 14.dp)) {
                    Text("Addd", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("⌾  Bhubaneswar  •  ✎", color = Color.White, fontSize = 11.sp)
                    Text("28 Followers", color = ProfileMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 7.dp))
                }
            }
            Text("A passionate team with big dreams, building\na stronger cricketing community.", color = Color.White, fontSize = 11.sp, lineHeight = 14.sp, modifier = Modifier.padding(start = 84.dp, top = 8.dp))
        }
    }
}

@Composable
private fun ProfileTabs(selectedTab: String, onSelect: (String) -> Unit) {
    val tabs = listOf("Profile", "Matches", "Stats", "Leaderboard", "Members", "Photos")
    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp).height(35.dp).background(Color(0xFF0A1B2B), RoundedCornerShape(17.dp)).horizontalScroll(rememberScrollState()), verticalAlignment = Alignment.CenterVertically) {
        tabs.forEach { tab ->
            Text(tab, color = if (tab == selectedTab) ProfileAccent else Color.White, fontSize = 10.sp, fontWeight = if (tab == selectedTab) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.height(35.dp).clickable { onSelect(tab) }.padding(horizontal = 13.dp, vertical = 10.dp))
        }
    }
}

@Composable
private fun ProfileTabContent() {
    Column(Modifier.padding(horizontal = 13.dp, vertical = 4.dp)) {
        AboutCard()
        AchievementsCard()
        Spacer(Modifier.height(30.dp))
    }
}

@Composable
private fun AboutCard() {
    Column(Modifier.fillMaxWidth().background(ProfileCard, RoundedCornerShape(14.dp)).border(1.dp, ProfileAccent, RoundedCornerShape(14.dp)).padding(15.dp)) {
        Text("About", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Text("Addd is a competitive cricket team based in Bhubaneswar. We play\nwith passion, discipline and unity, aiming to create a strong cricket\ncommunity and compete at higher levels.", color = ProfileMuted, fontSize = 11.sp, lineHeight = 15.sp, modifier = Modifier.padding(top = 14.dp))
        DetailsBox()
    }
}

@Composable
private fun DetailsBox() {
    val rows = listOf("▣" to "Founded|17 Aug 2026", "▤" to "Home Ground|Kalinga Stadium", "♟" to "Captain|Rahul Sharma", "⬟" to "Coach / Manager|Amit Patnaik", "⚑" to "Team Motto|Discipline. Unity. Victory.")
    Column(Modifier.fillMaxWidth().padding(top = 15.dp).background(Color(0xFF0A192B), RoundedCornerShape(12.dp)).border(1.dp, ProfileStroke, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp)) {
        rows.forEachIndexed { index, pair ->
            val values = pair.second.split("|")
            Row(Modifier.fillMaxWidth().height(40.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(pair.first, color = Color(0xFFD8E6FF), fontSize = 18.sp, modifier = Modifier.width(34.dp))
                Text(values[0], color = ProfileMuted, fontSize = 10.sp, modifier = Modifier.weight(1f))
                Text(values[1], color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1.25f))
            }
            if (index < rows.lastIndex) Spacer(Modifier.fillMaxWidth().height(1.dp).background(ProfileStroke))
        }
    }
}

@Composable
private fun AchievementsCard() {
    Column(Modifier.fillMaxWidth().padding(top = 16.dp).background(ProfileCard, RoundedCornerShape(14.dp)).border(1.dp, ProfileAccent, RoundedCornerShape(14.dp)).padding(15.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) { Text("♕", color = ProfileAccent, fontSize = 23.sp); Text("Achievements", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 10.dp)) }
        Achievement("2 Tournament Wins")
        Achievement("3 Runners-up")
    }
}

@Composable
private fun Achievement(text: String) {
    Row(Modifier.padding(top = 13.dp), verticalAlignment = Alignment.CenterVertically) { Text("●", color = Color(0xFF88FF2E), fontSize = 16.sp); Text(text, color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(start = 12.dp)) }
}

@Composable
private fun MatchesTabContent() {
    Column(Modifier.padding(horizontal = 12.dp)) {
        MatchSectionHeader("Recent Matches", "Latest match results and performance.")
        RecentMatch("Bhubaneswar Premier League", "Addd", "156/4", "Royal Strikers", "149/8", "Won by 7 runs", true)
        RecentMatch("Odisha Super League", "Addd", "128/7", "Eastern Royals", "162/6", "Lost by 34 runs", false)
        RecentMatch("City Cricket Cup", "Addd", "201/5", "Northern Knights", "198/9", "Won by 3 runs", true)
        MatchSectionHeader("Upcoming Matches", "Stay tuned for our next challenges.")
        UpcomingMatch("City Cricket Cup", "Lion Hearts", "18 Aug 2026", "10:00 AM")
        UpcomingMatch("Bhubaneswar Premier League", "Falcon XI", "24 Aug 2026", "02:30 PM")
        UpcomingMatch("Odisha Super League", "Rising Stars", "1 Sep 2026", "04:00 PM")
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun MatchSectionHeader(title: String, subtitle: String) {
    Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold); Text(subtitle, color = ProfileMuted, fontSize = 10.sp) }
        Text("View All  →", color = ProfileAccent, fontSize = 10.sp)
    }
}

@Composable
private fun RecentMatch(league: String, home: String, homeScore: String, away: String, awayScore: String, result: String, won: Boolean) {
    Column(Modifier.fillMaxWidth().padding(top = 9.dp).background(ProfileCard, RoundedCornerShape(10.dp)).border(1.dp, ProfileStroke, RoundedCornerShape(10.dp)).padding(10.dp)) {
        Row { Text("♛  $league", color = ProfileMuted, fontSize = 8.sp, modifier = Modifier.weight(1f)); Text("12 Aug 2026  •  Kalinga Stadium", color = ProfileMuted, fontSize = 8.sp) }
        Row(Modifier.fillMaxWidth().padding(top = 7.dp), verticalAlignment = Alignment.CenterVertically) {
            TeamBadge(home); Column(Modifier.weight(1f).padding(start = 8.dp)) { Text(home, color = Color.White, fontSize = 11.sp); Text(homeScore, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
            Text("vs", color = ProfileMuted, fontSize = 10.sp)
            Column(Modifier.weight(1f).padding(start = 8.dp)) { Text(awayScore, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold); Text(away, color = Color.White, fontSize = 11.sp) }
        }
        Text(result, color = Color.White, fontSize = 10.sp, modifier = Modifier.fillMaxWidth().padding(top = 7.dp).background(if (won) Color(0xFF075E38) else Color(0xFF78242B), RoundedCornerShape(4.dp)).padding(vertical = 3.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
private fun UpcomingMatch(league: String, opponent: String, date: String, time: String) {
    Row(Modifier.fillMaxWidth().padding(top = 8.dp).height(65.dp).background(ProfileCard, RoundedCornerShape(9.dp)).border(1.dp, ProfileStroke, RoundedCornerShape(9.dp)).padding(9.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text("♛  $league", color = ProfileMuted, fontSize = 8.sp); Row(verticalAlignment = Alignment.CenterVertically) { TeamBadge("Addd"); Text("Addd     vs     $opponent", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(start = 7.dp)) } }
        Column(horizontalAlignment = Alignment.End) { Text(date, color = ProfileMuted, fontSize = 8.sp); Text(time, color = Color.White, fontSize = 10.sp); Text("Upcoming", color = Color.White, fontSize = 8.sp, modifier = Modifier.background(Color(0xFF0752A9), RoundedCornerShape(7.dp)).padding(horizontal = 8.dp, vertical = 3.dp)) }
    }
}

@Composable private fun TeamBadge(name: String) = Box(Modifier.size(26.dp).background(Color(0xFF925443), CircleShape).border(1.dp, Color.White, CircleShape), contentAlignment = Alignment.Center) { Text(name.take(1), color = Color.White, fontSize = 11.sp) }

@Composable
private fun StatsTabContent() {
    Column(Modifier.padding(horizontal = 12.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("Team Statistics", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold); Text("Complete overview of our performance", color = ProfileMuted, fontSize = 11.sp) }; Text("All Tournaments ⌄", color = Color.White, fontSize = 9.sp, modifier = Modifier.border(1.dp, ProfileStroke, RoundedCornerShape(10.dp)).padding(10.dp)) }
        StatGrid()
        StatTitle("Toss Statistics", "How we perform at the toss")
        SplitStatCard("▣", "Toss Won", "14", "58.3%", "▤", "Toss Lost", "10", "41.7%")
        StatTitle("Innings Choice", "What we do after winning the toss")
        SplitStatCard("▰", "Bat First", "8", "57.1%", "◉", "Field First", "6", "42.9%")
        Row(Modifier.fillMaxWidth().padding(top = 13.dp).background(Color(0xFF0A2830), RoundedCornerShape(10.dp)).border(1.dp, Color(0xFF315D38), RoundedCornerShape(10.dp)).padding(13.dp), verticalAlignment = Alignment.CenterVertically) { Text("💡", fontSize = 20.sp); Column(Modifier.padding(start = 10.dp).weight(1f)) { Text("Key Insight", color = ProfileAccent, fontSize = 10.sp); Text("We have a winning percentage of 66.7% and prefer to bat first\nafter winning the toss.", color = ProfileMuted, fontSize = 10.sp) }; Text("→", color = Color.White) }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun StatGrid() {
    val stats = listOf("♟|Matches|24", "▣|Upcoming|6", "♕|Won|16", "✕|Lost|6", "⌁|Tie|0", "═|Drawn|1", "⊗|NR|1", "↗|Win %|66.7%")
    Column(Modifier.fillMaxWidth().padding(top = 13.dp).background(ProfileCard, RoundedCornerShape(10.dp)).border(1.dp, ProfileStroke, RoundedCornerShape(10.dp)).padding(8.dp)) { stats.chunked(4).forEach { row -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { row.forEach { item -> val parts = item.split("|"); Column(Modifier.weight(1f).height(61.dp).background(Color(0xFF0B1D30), RoundedCornerShape(7.dp)).padding(8.dp)) { Text(parts[0], color = ProfileAccent, fontSize = 17.sp); Text(parts[1], color = ProfileMuted, fontSize = 9.sp); Text(parts[2], color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) } } }; Spacer(Modifier.height(8.dp)) } }
}

@Composable private fun StatTitle(title: String, subtitle: String) = Column(Modifier.padding(top = 14.dp)) { Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold); Text(subtitle, color = ProfileMuted, fontSize = 11.sp) }
@Composable
private fun SplitStatCard(icon1: String, label1: String, value1: String, percent1: String, icon2: String, label2: String, value2: String, percent2: String) {
    Row(Modifier.fillMaxWidth().padding(top = 8.dp).height(89.dp).background(ProfileCard, RoundedCornerShape(10.dp)).border(1.dp, ProfileStroke, RoundedCornerShape(10.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) { SplitStat(icon1, label1, value1, percent1, Modifier.weight(1f)); Spacer(Modifier.width(1.dp).fillMaxHeight().background(ProfileStroke)); SplitStat(icon2, label2, value2, percent2, Modifier.weight(1f)) }
}
@Composable private fun SplitStat(icon: String, label: String, value: String, percent: String, modifier: Modifier) = Row(modifier.padding(horizontal = 5.dp), verticalAlignment = Alignment.CenterVertically) { Text(icon, color = ProfileAccent, fontSize = 24.sp); Column(Modifier.padding(start = 10.dp).weight(1f)) { Text(label, color = ProfileMuted, fontSize = 10.sp); Text(value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold); Text("out of 24", color = ProfileMuted, fontSize = 9.sp) }; Box(Modifier.size(48.dp).border(6.dp, ProfileAccent, CircleShape), contentAlignment = Alignment.Center) { Text(percent, color = Color.White, fontSize = 9.sp) } }

@Composable
private fun LeaderboardTabContent() {
    val players = listOf("Rahul Sharma|All-rounder|642|53.5|142.1|18", "Amit Verma|Batsman|521|43.4|138.6|18", "Siddharth Das|Batsman|398|36.2|132.4|16", "Karan Patel|Wicket Keeper|312|28.4|129.1|15", "Rohit Sahu|All-rounder|287|26.1|118.4|14", "Manish Nayak|Batsman|246|22.3|121.6|12", "Aditya Rout|All-rounder|198|19.8|118.4|12", "Vikram Singh|Batsman|176|18.2|111.1|11")
    Column(Modifier.padding(horizontal = 12.dp)) {
        Text("Team Leaderboard", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp)); Text("Top performers from our team", color = ProfileMuted, fontSize = 11.sp)
        Row(Modifier.fillMaxWidth().padding(top = 10.dp).height(33.dp).background(Color(0xFF0A1B2B), RoundedCornerShape(8.dp))) { listOf("✎  Bat", "◉  Bowl", "♙  Field", "♟  P'ship").forEachIndexed { i, tab -> Text(tab, color = if (i == 0) Color(0xFF111709) else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f).fillMaxHeight().background(if (i == 0) ProfileAccent else Color.Transparent, RoundedCornerShape(8.dp)).padding(vertical = 10.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center) } }
        Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp)) { listOf("#", "Player", "Runs", "Avg", "SR", "Matches").forEach { Text(it, color = ProfileMuted, fontSize = 8.sp, modifier = Modifier.weight(if (it == "Player") 2.2f else 1f)) } }
        players.forEachIndexed { index, player -> LeaderboardRow(index + 1, player) }
        Row(Modifier.fillMaxWidth().padding(top = 13.dp).background(Color(0xFF0A2830), RoundedCornerShape(10.dp)).border(1.dp, Color(0xFF315D38), RoundedCornerShape(10.dp)).padding(13.dp), verticalAlignment = Alignment.CenterVertically) { Text("▥", color = ProfileAccent, fontSize = 22.sp); Column(Modifier.padding(start = 10.dp).weight(1f)) { Text("Top Performer", color = ProfileAccent, fontSize = 10.sp); Text("Rahul Sharma leads with 642 runs\nat an average of 53.5", color = ProfileMuted, fontSize = 10.sp) }; Text("→", color = Color.White) }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun LeaderboardRow(rank: Int, record: String) {
    val p = record.split("|")
    Row(Modifier.fillMaxWidth().padding(top = 4.dp).height(40.dp).background(if (rank == 1) Color(0xFF12291E) else ProfileCard, RoundedCornerShape(7.dp)).border(if (rank == 1) 1.dp else 0.dp, if (rank == 1) ProfileAccent else Color.Transparent, RoundedCornerShape(7.dp)).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) { Text(if (rank == 1) "♛" else "$rank", color = if (rank == 1) ProfileAccent else Color.White, fontSize = 10.sp, modifier = Modifier.width(24.dp)); Box(Modifier.size(27.dp).background(Color(0xFF925443), CircleShape), contentAlignment = Alignment.Center) { Text(p[0].take(1), color = Color.White, fontSize = 11.sp) }; Column(Modifier.weight(2.2f).padding(start = 6.dp)) { Row { Text(p[0], color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold); if (rank == 1) Text("  C", color = Color(0xFF111709), fontSize = 8.sp, modifier = Modifier.background(ProfileAccent, RoundedCornerShape(2.dp))) }; Text(p[1], color = ProfileMuted, fontSize = 8.sp) }; Text(p[2], color = Color.White, fontSize = 9.sp, modifier = Modifier.weight(1f)); Text(p[3], color = Color.White, fontSize = 9.sp, modifier = Modifier.weight(1f)); Text(p[4], color = Color.White, fontSize = 9.sp, modifier = Modifier.weight(1f)); Text(p[5], color = Color.White, fontSize = 9.sp, modifier = Modifier.weight(1f)) }
}

@Composable
private fun MembersTabContent() {
    val members = listOf(
        "#18|Rahul Sharma|All-rounder|Captain|Right Hand|Right Arm Off",
        "#07|Amit Verma|Batsman|Vice Captain|Right Hand|Right Arm Medium",
        "#03|Rohit Sahu|All-rounder||Left Hand|Right Arm Medium",
        "#11|Siddharth Das|Batsman||Right Hand|Right Arm Off",
        "#21|Karan Patel|Wicket Keeper||Right Hand|Right Arm Medium",
        "#08|Manish Nayak|Batsman||Left Hand|Right Arm Off",
        "#10|Aditya Rout|All-rounder||Right Hand|Right Arm Medium",
        "#14|Vikram Singh|Batsman||Right Hand|Right Arm Medium",
        "#25|Prakash Lenka|Bowler||Right Hand|Left Arm Fast",
        "#17|Subham Mohanty|Bowler||Right Hand|Right Arm Fast",
        "#27|Debasish Panda|Batsman||Right Hand|Right Arm Fast",
        "#30|Chinmay Behera|All-rounder||Left Hand|Right Arm Medium"
    )
    Column(Modifier.padding(horizontal = 12.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text("Team Members", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold); Text("Meet the warriors behind our journey", color = ProfileMuted, fontSize = 10.sp) }
            Text("12 Players", color = ProfileMuted, fontSize = 10.sp)
        }
        members.forEachIndexed { index, member -> MemberRow(index, member) }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun MemberRow(index: Int, record: String) {
    val member = record.split("|")
    val captain = member[3]
    Row(
        Modifier.fillMaxWidth().padding(top = 7.dp).height(40.dp).background(if (index == 0) Color(0xFF12291E) else ProfileCard, RoundedCornerShape(7.dp)).border(if (index == 0) 1.dp else 0.dp, if (index == 0) ProfileAccent else Color.Transparent, RoundedCornerShape(7.dp)).padding(horizontal = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(member[0], color = Color.White, fontSize = 9.sp, modifier = Modifier.width(32.dp))
        Box(Modifier.size(31.dp).background(Color(0xFF925443), CircleShape).border(1.dp, Color.White, CircleShape), contentAlignment = Alignment.Center) { Text(member[1].take(1), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
        Column(Modifier.width(140.dp).padding(start = 7.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(member[1], color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                if (captain.isNotEmpty()) Text("  $captain", color = if (captain == "Captain") Color(0xFF122300) else Color(0xFF071421), fontSize = 6.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 2.dp).background(if (captain == "Captain") ProfileAccent else Color(0xFF9EB6DE), RoundedCornerShape(3.dp)).padding(horizontal = 4.dp, vertical = 2.dp))
            }
            Text(member[2], color = ProfileMuted, fontSize = 8.sp)
        }
        Column(Modifier.weight(1f)) { Text("▰  Batting", color = Color.White, fontSize = 7.sp); Text(member[4], color = ProfileMuted, fontSize = 7.sp) }
        Column(Modifier.weight(1f)) { Text("◉  Bowling", color = Color.White, fontSize = 7.sp); Text(member[5], color = ProfileMuted, fontSize = 7.sp) }
        Text("›", color = Color.White, fontSize = 20.sp)
    }
}

@Composable
private fun PhotosTabContent() {
    Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Team Photos", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("Moments that make us stronger", color = ProfileMuted, fontSize = 10.sp)
            }
            Text("+  Add Photos", color = ProfileAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.border(1.dp, ProfileAccent, RoundedCornerShape(10.dp)).padding(horizontal = 12.dp, vertical = 8.dp))
        }
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PhotoCategory("All", true)
            PhotoCategory("♛  Matches", false)
            PhotoCategory("⚑  Practice", false)
            PhotoCategory("♟  Team", false)
            PhotoCategory("▣  Events", false)
            PhotoCategory("▧  Behind the Scenes", false)
        }
        Spacer(Modifier.height(420.dp))
    }
}

@Composable
private fun PhotoCategory(label: String, selected: Boolean) {
    Text(label, color = if (selected) Color(0xFF121709) else Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.background(if (selected) ProfileAccent else Color(0xFF0A1B2B), RoundedCornerShape(9.dp)).border(if (selected) 0.dp else 1.dp, if (selected) Color.Transparent else ProfileStroke, RoundedCornerShape(9.dp)).padding(horizontal = 12.dp, vertical = 8.dp))
}
