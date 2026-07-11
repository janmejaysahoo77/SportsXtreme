package com.example.sportsxtreme

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MyProfileViewOfSportsXtreme : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applySportsXtremeWindowStyle()
        setContent {
            ProfileScreen(onBack = { finish() })
        }
    }
}

private val ProfileBg = Color(0xFF05090F)
private val ProfilePanel = Color(0xFF121820)
private val ProfilePanelSoft = Color(0xFF171F28)
private val ProfileAccent = Color(0xFFC1FF00)
private val ProfileTextMuted = Color(0xFFA7B2B6)

@Composable
private fun ProfileScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ProfileBg)
    ) {
        ProfileTopBar(onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileHero()
            ProfileUpgradeButton()
            Spacer(modifier = Modifier.height(14.dp))
            ProfileStats()
            SectionTitle("Personal Details")
            DetailCard("Mobile Number", "+91 98766 43210")
            DetailCard("Gender", "Male")
            DetailCard("Email Address", "siddharth.sahoo@gmail.com")
            SectionTitle("Playing Profile")
            DetailCard("Role", "All-rounder")
            DetailCard("Batting", "Right Handed")
            DetailCard("Bowling", "Right Arm Fast")
            ProfileCompletion()
            UpgradePlans()
            BottomActions()
        }
    }
}

@Composable
private fun ProfileTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF070D15))
            .padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "<",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .size(30.dp)
                .clickable { onBack() },
            textAlign = TextAlign.Center
        )
        Text(
            text = "Profile",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Text(text = "SH", color = Color.White, fontSize = 10.sp, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
        Text(text = "N", color = Color.White, fontSize = 12.sp, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
    }
}

@Composable
private fun ProfileHero() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(86.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color(0xFF355071), Color(0xFF111821))))
                .border(2.dp, ProfileAccent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(58.dp)) {
                val bat = Path().apply {
                    moveTo(size.width * 0.42f, size.height * 0.18f)
                    lineTo(size.width * 0.72f, size.height * 0.48f)
                    lineTo(size.width * 0.54f, size.height * 0.66f)
                    lineTo(size.width * 0.25f, size.height * 0.34f)
                    close()
                }
                drawPath(bat, color = Color(0xFFFF6B32))
                drawLine(
                    color = Color.White,
                    start = Offset(size.width * 0.55f, size.height * 0.64f),
                    end = Offset(size.width * 0.72f, size.height * 0.82f),
                    strokeWidth = 5f
                )
            }
            Text(
                text = "PRO",
                color = Color.Black,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ProfileAccent)
                    .padding(horizontal = 9.dp, vertical = 2.dp)
            )
        }
        Text("Siddheshwar Sahoo", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("Odisha, India  |  Joined Jan 2024", color = ProfileTextMuted, fontSize = 11.sp)
    }
}

@Composable
private fun ProfileUpgradeButton() {
    Text(
        text = "Get Pro Access",
        color = Color.Black,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .padding(top = 10.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(ProfileAccent)
            .padding(horizontal = 34.dp, vertical = 9.dp)
    )
}

@Composable
private fun ProfileStats() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(ProfilePanel)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatBlock("75", "Followers")
        StatBlock("323", "Following")
        StatBlock("ID_294", "Player ID")
        StatBlock("***", "Rating")
    }
}

@Composable
private fun StatBlock(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(label.uppercase(), color = ProfileTextMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        color = ProfileTextMuted,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp, bottom = 6.dp)
    )
}

@Composable
private fun DetailCard(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(ProfilePanel)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(label, color = ProfileTextMuted, fontSize = 9.sp)
        Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ProfileCompletion() {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Profile Completion", color = ProfileTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("58%", color = ProfileAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF303842))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.58f)
                    .height(6.dp)
                    .background(ProfileAccent)
            )
        }
        Text("Complete Profile +", color = ProfileAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 7.dp))
    }
}

@Composable
private fun UpgradePlans() {
    Text(
        text = "Elevate Your Game",
        color = ProfileAccent,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 24.dp)
    )
    Text("Get detailed analytics, ad-free streaming, and priority support.", color = ProfileTextMuted, fontSize = 11.sp, textAlign = TextAlign.Center)
    PlanCard("PRO", "Rs 19/mo", listOf("Zero Ad Interruption", "HD Streaming"), featured = false)
    PlanCard("ELITE", "Rs 49/mo", listOf("Match Highlights", "Live MC ID", "Player Insights"), featured = true)
    PlanCard("LEGEND", "Rs 99/mo", listOf("Verified VIP Badge", "Massive Prize Discount", "Personal MC Coach"), featured = false)
    Text(
        text = "BECOME A PRO",
        color = Color(0xFF061126),
        fontSize = 13.sp,
        fontWeight = FontWeight.Black,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFFC6D8FF))
            .padding(vertical = 12.dp)
    )
}

@Composable
private fun PlanCard(tier: String, price: String, perks: List<String>, featured: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (featured) Color(0xFF1C2530) else ProfilePanelSoft)
            .border(1.dp, if (featured) ProfileAccent else Color(0xFF53606D), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(tier, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text(price, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
        }
        perks.forEach {
            Text("- $it", color = ProfileTextMuted, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
        }
        Text(
            text = "Select Plan",
            color = if (featured) Color.Black else ProfileAccent,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(if (featured) ProfileAccent else Color.Transparent)
                .border(1.dp, ProfileAccent, RoundedCornerShape(4.dp))
                .padding(vertical = 5.dp)
        )
    }
}

@Composable
private fun BottomActions() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 22.dp, bottom = 20.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(ProfilePanel)
    ) {
        BottomRow("Change language", "English (US)")
        BottomRow("Purchase History", ">")
        BottomRow("Logout", "", isDanger = true)
    }
    Text("Version 1.0\n(c) 2026 SportsXtreme Interactive", color = Color(0xFF68747A), fontSize = 9.sp, textAlign = TextAlign.Center)
}

@Composable
private fun BottomRow(label: String, value: String, isDanger: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = if (isDanger) Color(0xFFFF7A7A) else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        if (value.isNotBlank()) {
            Text(value, color = ProfileTextMuted, fontSize = 11.sp)
        }
    }
}
