package com.example.sportsxtreme.presentation.clubs

import android.graphics.Color
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

class MyClubsLandingPageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = Color.rgb(6, 16, 30)
        window.navigationBarColor = Color.rgb(6, 16, 30)
        setContent { MyClubsPage(::finish) }
    }
}

private val PageBg = UiColor(6, 16, 30)
private val Panel = UiColor(17, 27, 43)
private val Green = UiColor(190, 255, 24)
private val Secondary = UiColor(151, 162, 174)

@Composable
private fun MyClubsPage(onBack: () -> Unit) = Column(
    Modifier.fillMaxSize().background(PageBg).verticalScroll(rememberScrollState()).padding(horizontal = 9.dp)
) {
    Row(Modifier.fillMaxWidth().height(52.dp), verticalAlignment = Alignment.CenterVertically) {
        RoundIcon("<", onBack)
        Text("My Clubs", color = UiColor.White, fontSize = 17.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
        RoundIcon("o")
    }
    Text("Manage the clubs you own and have\njoined.", color = Secondary, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(14.dp))
    Row(Modifier.fillMaxWidth().height(38.dp).clip(RoundedCornerShape(22.dp)).background(UiColor(10, 21, 35)).padding(3.dp)) {
        Box(Modifier.weight(1f).fillMaxSize().clip(RoundedCornerShape(18.dp)).background(Green), contentAlignment = Alignment.Center) { Text("Owned", color = UiColor.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
        Box(Modifier.weight(1f).fillMaxSize(), contentAlignment = Alignment.Center) { Text("Joined", color = Secondary, fontSize = 12.sp) }
    }
    Spacer(Modifier.height(22.dp))
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(27.dp).clip(RoundedCornerShape(6.dp)).background(UiColor(34, 59, 35)), contentAlignment = Alignment.Center) { Text("*", color = Green, fontSize = 18.sp) }
        Text("Owned Clubs", color = UiColor.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f).padding(start = 8.dp))
        Box(Modifier.height(28.dp).clip(RoundedCornerShape(15.dp)).background(Green).padding(horizontal = 12.dp), contentAlignment = Alignment.Center) { Text("+  Create Club", color = UiColor.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
    }
    Spacer(Modifier.height(18.dp))
    ClubItem("Warriors Cricket Club", "Cuttack, Odisha", "Founder/Owner", false)
    Spacer(Modifier.height(17.dp))
    ClubItem("Royal Kings Cricket Club", "Puri, Odisha", "President", true)
    Spacer(Modifier.height(48.dp))
}

@Composable
private fun RoundIcon(label: String, onClick: (() -> Unit)? = null) = Box(
    Modifier.size(29.dp).clip(CircleShape).background(UiColor(37, 49, 66)).clickable { onClick?.invoke() }, contentAlignment = Alignment.Center
) { Text(label, color = UiColor.White, fontSize = 18.sp) }

@Composable
private fun ClubItem(name: String, place: String, role: String, stadium: Boolean) = Card(
    shape = RoundedCornerShape(15.dp), colors = CardDefaults.cardColors(containerColor = Panel)
) {
    Column {
        Box(Modifier.fillMaxWidth().height(99.dp).background(if (stadium) Brush.linearGradient(listOf(UiColor(5, 67, 104), UiColor(10, 27, 50))) else Brush.verticalGradient(listOf(UiColor(211, 211, 211), UiColor(76, 88, 104)))))
        Box(Modifier.offset(x = 14.dp, y = (-26).dp).size(45.dp).clip(CircleShape).background(UiColor(219, 222, 214)), contentAlignment = Alignment.Center) { Text("img", color = UiColor.Black, fontSize = 8.sp) }
        Column(Modifier.padding(horizontal = 16.dp).offset(y = (-17).dp).padding(bottom = 1.dp)) {
            Text(name, color = UiColor.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text("o  $place", color = Secondary, fontSize = 10.sp)
            Spacer(Modifier.height(5.dp))
            Text("*  $role", color = Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(17.dp))
            Box(Modifier.fillMaxWidth().height(40.dp).clip(RoundedCornerShape(8.dp)).background(Green), contentAlignment = Alignment.CenterStart) {
                Text("View Club", color = UiColor.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 17.dp))
                Text(">", color = UiColor.Black, fontSize = 25.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 17.dp))
            }
        }
    }
}
