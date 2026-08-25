package com.example.sportsxtreme.presentation.clubs

import android.graphics.Color
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color as UiColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

class Step_OneCreateClubActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = Color.rgb(5, 7, 8)
        window.navigationBarColor = Color.rgb(5, 7, 8)
        setContent {
            CreateClubStepOne(
                onBack = ::finish,
                onContinue = { startActivity(Intent(this, Step_TwoCreateClubActivity::class.java)) }
            )
        }
    }
}

private val CreateBg = UiColor(5, 7, 8)
private val CreatePanel = UiColor(16, 21, 28)
private val CreateLime = UiColor(190, 255, 24)
private val CreateMuted = UiColor(154, 164, 173)

@Composable
private fun CreateClubStepOne(onBack: () -> Unit, onContinue: () -> Unit) {
    var clubName by remember { mutableStateOf("") }
    var about by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().background(CreateBg)) {
        Row(Modifier.fillMaxWidth().height(47.dp).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("‹", color = UiColor.White, fontSize = 31.sp, modifier = Modifier.width(32.dp).clickable { onBack() })
            Text("Create Club", color = UiColor.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("Save Draft", color = CreateMuted, fontSize = 7.sp)
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 12.dp)) {
            Text("STEP 1 OF 4", color = CreateMuted, fontSize = 7.sp, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(7.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) { repeat(4) { index -> Box(Modifier.weight(1f).height(3.dp).clip(CircleShape).background(if (index == 0) CreateLime else CreatePanel)) } }
            Spacer(Modifier.height(14.dp))
            Box(Modifier.fillMaxWidth().height(145.dp).clip(RoundedCornerShape(11.dp)).background(UiColor(12, 34, 50))) {
                Column(Modifier.padding(15.dp).align(Alignment.CenterStart)) {
                    Text("Let's Build Your", color = UiColor.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Cricket Club", color = CreateLime, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Launch your legacy today. Set up the\nessentials for your perfect cricket\norganization.", color = CreateMuted, fontSize = 8.sp, lineHeight = 11.sp)
                }
            }
            Spacer(Modifier.height(15.dp))
            Box(Modifier.fillMaxWidth().height(75.dp).clip(RoundedCornerShape(8.dp)).background(CreatePanel), contentAlignment = Alignment.Center) { Text("▣\nUpload Cover Photo", color = CreateMuted, fontSize = 9.sp, lineHeight = 17.sp) }
            Spacer(Modifier.height(13.dp))
            FormLabel("Club Name")
            ClubTextField(clubName, { clubName = it }, "e.g. London Mavericks CC")
            Spacer(Modifier.height(13.dp))
            FormLabel("Club Type")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { TypeOption("◉\nCricket Club", true); TypeOption("♙\nAcademy", false); TypeOption("▣\nCorporate", false) }
            Spacer(Modifier.height(13.dp))
            FormLabel("Established Year")
            ClubTextField("2024", {}, "")
            Spacer(Modifier.height(13.dp))
            FormLabel("Privacy")
            Text("Public (Visible to all)   ⌄", color = UiColor.White, fontSize = 9.sp, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(7.dp)).background(CreatePanel).padding(13.dp))
            Spacer(Modifier.height(13.dp))
            FormLabel("About the Club")
            ClubTextField(about, { about = it }, "Describe your club's vision, history, and achievements...", minLines = 4)
            Spacer(Modifier.height(14.dp))
            Text("●  Pro Tip\nA clear, high-quality logo and cover photo helps your club stand out!", color = CreateMuted, fontSize = 8.sp, lineHeight = 13.sp, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(7.dp)).background(UiColor(8, 29, 49)).padding(12.dp))
            Spacer(Modifier.height(14.dp))
        }
        Text("Continue  →", color = UiColor.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 9.dp).clip(RoundedCornerShape(7.dp)).background(CreateLime).clickable { onContinue() }.padding(vertical = 12.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
private fun FormLabel(text: String) = Text(text, color = CreateMuted, fontSize = 8.sp, modifier = Modifier.padding(bottom = 5.dp))

@Composable
private fun ClubTextField(value: String, onChange: (String) -> Unit, hint: String, minLines: Int = 1) = OutlinedTextField(value = value, onValueChange = onChange, placeholder = { Text(hint, color = CreateMuted, fontSize = 9.sp) }, minLines = minLines, textStyle = androidx.compose.ui.text.TextStyle(color = UiColor.White, fontSize = 10.sp), colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = UiColor(41, 52, 62), focusedBorderColor = CreateLime), modifier = Modifier.fillMaxWidth())

@Composable
private fun TypeOption(label: String, selected: Boolean) = Text(label, color = if (selected) CreateLime else CreateMuted, fontSize = 8.sp, lineHeight = 13.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.width(83.dp).clip(RoundedCornerShape(7.dp)).background(if (selected) UiColor(25, 39, 25) else CreatePanel).padding(vertical = 10.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
