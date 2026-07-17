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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

class ProfileActivity : ComponentActivity() {

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        setContent {
            ProfileScreen(onBack = { finish() })
        }
    }
}

@Composable
private fun ProfileScreen(onBack: () -> Unit) {
    val bg = Color(0xFF02070C)
    val panel = Color(0xFF10171D)
    val primary = Color(0xFFC1FF00)
    val muted = Color(0xFF9AA6A9)

    Surface(color = bg, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileTopBar(onBack)
            Spacer(Modifier.height(14.dp))
            Avatar(primary)
            Spacer(Modifier.height(8.dp))
            Text("Siddheshwar Sahoo", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("Odisha, India  -  Joined Jan 2024", color = muted, fontSize = 10.sp)
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = primary, contentColor = Color.Black),
                shape = RoundedCornerShape(50),
                modifier = Modifier.height(30.dp)
            ) {
                Text("GET IT FIXED", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(14.dp))
            StatBanner(panel, primary)
            Spacer(Modifier.height(10.dp))
            ProfileStats(panel)
            SectionTitle("PERSONAL DETAILS", primary)
            DetailCard(panel, "Mobile Number", "+91 7428 5145 4312")
            DetailCard(panel, "Gender", "Male")
            DetailCard(panel, "Email Address", "siddhesh900s7@gmail.com")
            SectionTitle("PLAYING PROFILE", primary)
            DetailCard(panel, "Role", "All-rounder")
            DetailCard(panel, "Batting", "Right Handed")
            DetailCard(panel, "Bowling", "Right Arm Fast")
            Completion(panel, primary)
            ProPlans(panel, primary)
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB8C7FF), contentColor = Color(0xFF071025)),
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth().height(42.dp)
            ) {
                Text("BECOME A PRO", fontSize = 14.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(14.dp))
            SettingsCard(panel)
            Text("Version 1.0.0", color = muted, fontSize = 9.sp, modifier = Modifier.padding(top = 10.dp))
        }
    }
}

@Composable
private fun ProfileTopBar(onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("<", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onBack() }.padding(end = 10.dp))
        Text("Profile", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text("Share", color = Color.White, fontSize = 11.sp)
        Spacer(Modifier.width(16.dp))
        Text("Menu", color = Color.White, fontSize = 11.sp)
    }
    Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF172029)).padding(top = 8.dp))
}

@Composable
private fun Avatar(primary: Color) {
    Box(contentAlignment = Alignment.BottomEnd) {
        Canvas(
            modifier = Modifier
                .size(86.dp)
                .clip(CircleShape)
                .background(Color(0xFF243443))
                .border(2.dp, primary, CircleShape)
        ) {
            drawCircle(Color(0xFFE44942), radius = size.minDimension * 0.22f, center = center.copy(y = size.height * 0.34f))
            drawArc(Color(0xFF0D1116), 185f, 170f, false, style = Stroke(width = 12f, cap = StrokeCap.Round))
            drawCircle(Color(0xFF202A32), radius = size.minDimension * 0.3f, center = center.copy(y = size.height * 0.72f))
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(30))
                .background(primary)
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text("PRO", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun StatBanner(panel: Color, primary: Color) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(panel)
            .border(1.dp, Color(0xFF27323A), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("P", color = Color.Black, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(primary).padding(horizontal = 8.dp, vertical = 4.dp))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text("This year, you won", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("87 MATCHES, 2 TROPHIES", color = Color(0xFFB7C1C4), fontSize = 10.sp)
        }
        Text(">", color = primary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ProfileStats(panel: Color) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SmallStat(panel, "75", "FOLLOWERS", Modifier.weight(1f))
        SmallStat(panel, "23", "FOLLOWING", Modifier.weight(1f))
        SmallStat(panel, "Rs. 22,235", "PRO BALANCE", Modifier.weight(1f))
    }
}

@Composable
private fun SmallStat(panel: Color, value: String, label: String, modifier: Modifier) {
    Column(modifier.clip(RoundedCornerShape(8.dp)).background(panel).padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Color(0xFF8E9A9D), fontSize = 8.sp)
    }
}

@Composable
private fun SectionTitle(title: String, primary: Color) {
    Text(title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 6.dp))
    Box(Modifier.fillMaxWidth().height(1.dp).background(primary))
}

@Composable
private fun DetailCard(panel: Color, label: String, value: String) {
    Column(Modifier.fillMaxWidth().padding(top = 6.dp).clip(RoundedCornerShape(6.dp)).background(panel).padding(12.dp)) {
        Text(label, color = Color(0xFF8E9A9D), fontSize = 9.sp)
        Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun Completion(panel: Color, primary: Color) {
    Column(Modifier.fillMaxWidth().padding(top = 12.dp).clip(RoundedCornerShape(6.dp)).background(panel).padding(12.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Text("Profile Completion", color = Color.White, fontSize = 10.sp, modifier = Modifier.weight(1f))
            Text("68%", color = primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(progress = { 0.68f }, color = primary, trackColor = Color(0xFF303840), modifier = Modifier.fillMaxWidth().height(6.dp).padding(top = 4.dp))
        Text("Complete Profile +", color = primary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun ProPlans(panel: Color, primary: Color) {
    Text("Elevate Your Game", color = primary, fontSize = 18.sp, fontWeight = FontWeight.Black, modifier = Modifier.fillMaxWidth().padding(top = 16.dp))
    Text("Get detailed insights, in-team rankings, and priority support.", color = Color.White, fontSize = 10.sp, modifier = Modifier.fillMaxWidth())
    PlanCard(panel, primary, "PRO", "Rs. 29/mo", true)
    PlanCard(panel, primary, "ELITE", "Rs. 49/mo", false)
    PlanCard(panel, primary, "LEGEND", "Rs. 119/mo", false)
}

@Composable
private fun PlanCard(panel: Color, primary: Color, name: String, price: String, filled: Boolean) {
    Column(Modifier.fillMaxWidth().padding(top = 8.dp).clip(RoundedCornerShape(7.dp)).border(1.dp, Color(0xFF445047), RoundedCornerShape(7.dp)).background(panel).padding(10.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(name, color = primary, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
            Text(price, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
        }
        Text("Match insights  |  Video highlights  |  Player reports", color = Color(0xFFB9C4C7), fontSize = 9.sp, modifier = Modifier.padding(vertical = 8.dp))
        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(containerColor = if (filled) primary else panel, contentColor = if (filled) Color.Black else primary),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth().height(30.dp)
        ) {
            Text("Select Plan", fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SettingsCard(panel: Color) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(panel).padding(12.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Text("Change Language", color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
            Text("English (UK)", color = Color(0xFFB8C4C8), fontSize = 10.sp)
        }
        Spacer(Modifier.height(14.dp))
        Text("Purchase History        >", color = Color.White, fontSize = 12.sp)
        Spacer(Modifier.height(14.dp))
        Text("Logout", color = Color(0xFFFF8D8D), fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
