package com.example.sportsxtreme.presentation.clubs

import android.graphics.Color
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color as UiColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat

class ClubMemberPageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = Color.rgb(3, 9, 18)
        window.navigationBarColor = Color.rgb(3, 9, 18)
        setContent { ClubMembersPage(::finish) }
    }
}

private val MembersBg = UiColor(3, 9, 18)
private val MembersPanel = UiColor(10, 21, 36)
private val MembersLime = UiColor(198, 255, 13)
private val MemberMuted = UiColor(167, 178, 193)

private data class ClubMember(val number: String, val name: String, val role: String, val email: String, val phone: String)

@Composable
private fun ClubMembersPage(onBack: () -> Unit) = Column(Modifier.fillMaxSize().background(MembersBg).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
    val context = LocalContext.current
    Spacer(Modifier.height(17.dp))
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        MemberRoundButton("<", onBack)
        Box(Modifier.size(67.dp).padding(start = 14.dp).clip(RoundedCornerShape(13.dp)).background(UiColor(20, 28, 20)), contentAlignment = Alignment.Center) { Text("VICTORY\nCRICKET\nCLUB", color = UiColor.White, fontSize = 9.sp, lineHeight = 10.sp, fontWeight = FontWeight.Bold) }
        Column(Modifier.weight(1f).padding(start = 11.dp)) { Text("Members", color = UiColor.White, fontSize = 21.sp, fontWeight = FontWeight.Bold); Text("Victory Cricket Club  ✦", color = MemberMuted, fontSize = 11.sp) }
        Text("♧", color = UiColor.White, fontSize = 22.sp)
        Spacer(Modifier.width(16.dp))
        MemberRoundButton("⋮")
    }
    Spacer(Modifier.height(17.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Box(Modifier.height(37.dp).clip(RoundedCornerShape(7.dp)).background(MembersLime).clickable { context.startActivity(Intent(context, AddMemberInClubActivity::class.java)) }.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) { Text("♧  Add Member", color = UiColor.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
    }
    Spacer(Modifier.height(17.dp))
    MemberSection("Admins", listOf(
        ClubMember("01", "Suresh Nayak", "Admin", "suresh.nayak@victorycc.com", "+91 9876543210"),
        ClubMember("02", "Vikas Sharma", "Admin", "vikas.sharma@victorycc.com", "+91 9123456780")
    ))
    MemberSection("Captains", listOf(
        ClubMember("01", "Rahul Kumar", "Captain", "rahul.kumar@victorycc.com", "+91 9876543211"),
        ClubMember("02", "Amit Sharma", "Captain", "amit.sharma@victorycc.com", "+91 9123456781")
    ))
    MemberSection("Players", listOf(
        ClubMember("01", "Vivek Sharma", "Player", "vivek.sharma@victorycc.com", "+91 9876543212"),
        ClubMember("02", "Nilesh Rao", "Player", "nilesh.rao@victorycc.com", "+91 9876543213"),
        ClubMember("03", "Arjun Patil", "Player", "arjun.patil@victorycc.com", "+91 9876543214")
    ))
    Spacer(Modifier.height(24.dp))
}

@Composable
private fun MemberRoundButton(label: String, onClick: (() -> Unit)? = null) = Box(Modifier.size(34.dp).clip(RoundedCornerShape(7.dp)).background(UiColor(11, 23, 38)).clickable { onClick?.invoke() }, contentAlignment = Alignment.Center) { Text(label, color = UiColor.White, fontSize = 22.sp) }

@Composable
private fun MemberSection(title: String, members: List<ClubMember>) {
    Row(Modifier.fillMaxWidth().padding(top = 1.dp, bottom = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("|", color = MembersLime, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("$title  (${members.size})", color = UiColor.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f).padding(start = 8.dp))
        Text("View All    >", color = MembersLime, fontSize = 11.sp)
    }
    members.forEach { MemberCard(it) }
    Spacer(Modifier.height(13.dp))
}

@Composable
private fun MemberCard(member: ClubMember) = Card(Modifier.fillMaxWidth().padding(bottom = 7.dp), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = MembersPanel)) {
    Row(Modifier.height(78.dp).padding(horizontal = 15.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(member.number, color = MembersLime, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(35.dp))
        Box(Modifier.size(55.dp).clip(CircleShape).background(Brush.linearGradient(listOf(UiColor(53, 72, 92), UiColor(17, 28, 39)))), contentAlignment = Alignment.Center) { Text(member.name.take(1), color = UiColor.White, fontSize = 22.sp, fontWeight = FontWeight.Bold) }
        Column(Modifier.weight(1f).padding(start = 13.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Text(member.name, color = UiColor.White, fontSize = 14.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.width(10.dp)); Text(member.role, color = MembersLime, fontSize = 9.sp, modifier = Modifier.clip(RoundedCornerShape(5.dp)).background(UiColor(35, 50, 13)).padding(horizontal = 6.dp, vertical = 3.dp)) }
            Text(member.email, color = MemberMuted, fontSize = 10.sp)
            Text("⌕  ${member.phone}", color = MemberMuted, fontSize = 10.sp)
        }
        Text("⋮", color = UiColor.White, fontSize = 23.sp)
    }
}
