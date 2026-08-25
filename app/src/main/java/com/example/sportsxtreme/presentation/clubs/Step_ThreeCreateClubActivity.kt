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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

class Step_ThreeCreateClubActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = Color.rgb(5, 7, 13)
        window.navigationBarColor = Color.rgb(5, 7, 13)
        setContent {
            CreateClubStepThree(
                onBack = ::finish,
                onContinue = { startActivity(Intent(this, Step_FourCreateClubActivity::class.java)) }
            )
        }
    }
}

private val LocationBg = UiColor(5, 7, 13)
private val LocationPanel = UiColor(21, 26, 36)
private val LocationLime = UiColor(190, 255, 24)
private val LocationMuted = UiColor(146, 156, 171)

@Composable
private fun CreateClubStepThree(onBack: () -> Unit, onContinue: () -> Unit) {
    var city by remember { mutableStateOf("Bhubaneswar") }
    var district by remember { mutableStateOf("Khordha") }
    var postcode by remember { mutableStateOf("751030") }
    var address by remember { mutableStateOf("123, Khandagiri Marg, Near\nKalinga Stadium, Bhubaneswar,\nKhordha District - 751030,\nIndia") }
    Column(Modifier.fillMaxSize().background(LocationBg)) {
        Row(Modifier.fillMaxWidth().height(45.dp).padding(horizontal = 13.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("‹", color = UiColor.White, fontSize = 25.sp, modifier = Modifier.width(25.dp).clickable { onBack() })
            Text("CREATE CLUB", color = UiColor.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            Text("Step 3 of\n4", color = LocationLime, fontSize = 6.sp, textAlign = TextAlign.Center, modifier = Modifier.clip(RoundedCornerShape(9.dp)).background(UiColor(41, 53, 28)).padding(horizontal = 7.dp, vertical = 3.dp))
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) { repeat(4) { index -> Box(Modifier.weight(1f).height(3.dp).clip(CircleShape).background(if (index < 3) LocationLime else UiColor(35, 43, 54))) } }
        Text("Club Location", color = UiColor.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().padding(vertical = 15.dp), textAlign = TextAlign.Center)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 12.dp)) {
            LocationHero()
            Spacer(Modifier.height(14.dp))
            LocationLabel("LOCATION DETAILS")
            LocationLabel("Country *")
            LocationSelector("◉  India")
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Column(Modifier.weight(1f)) { LocationLabel("State / Province *"); LocationSelector("▥  Odisha") }
                Column(Modifier.weight(1f)) { LocationLabel("City *"); LocationField(city, { city = it }, "▦") }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Column(Modifier.weight(1f)) { LocationLabel("District (Optional)"); LocationField(district, { district = it }, "◉") }
                Column(Modifier.weight(1f)) { LocationLabel("Pincode *"); LocationField(postcode, { postcode = it }, "⌁") }
            }
            Spacer(Modifier.height(14.dp))
            SetLocationCard()
            Spacer(Modifier.height(14.dp))
            LocationLabel("FULL ADDRESS")
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp)).background(LocationPanel).padding(12.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Text("⌖", color = LocationLime, fontSize = 13.sp)
                    Spacer(Modifier.width(9.dp))
                    OutlinedTextField(value = address, onValueChange = { address = it }, textStyle = androidx.compose.ui.text.TextStyle(color = UiColor.White, fontSize = 8.sp, lineHeight = 11.sp), colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = UiColor.Transparent, focusedBorderColor = LocationLime), modifier = Modifier.weight(1f))
                    Text("✎", color = LocationLime, fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(13.dp))
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp)).background(UiColor(27, 37, 21)).padding(12.dp)) {
                Row {
                    Box(Modifier.size(22.dp).clip(CircleShape).background(UiColor(62, 85, 21)), contentAlignment = Alignment.Center) { Text("●", color = LocationLime, fontSize = 8.sp) }
                    Spacer(Modifier.width(9.dp))
                    Column { Text("Why accurate location matters?", color = UiColor.White, fontSize = 8.sp, fontWeight = FontWeight.Bold); Text("Clubs with accurate location appear higher in\nnearby search results and get more visibility.", color = LocationMuted, fontSize = 7.sp, lineHeight = 10.sp, modifier = Modifier.padding(top = 3.dp)) }
                }
            }
            Spacer(Modifier.height(15.dp))
        }
        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            Text("BACK", color = UiColor.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(1f).clip(RoundedCornerShape(5.dp)).background(LocationPanel).clickable { onBack() }.padding(vertical = 10.dp))
            Text("CONTINUE  →", color = UiColor.Black, fontSize = 8.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(1.8f).clip(RoundedCornerShape(5.dp)).background(LocationLime).clickable { onContinue() }.padding(vertical = 10.dp))
        }
    }
}

@Composable
private fun LocationHero() = Box(Modifier.fillMaxWidth().height(75.dp).clip(RoundedCornerShape(9.dp)).background(LocationPanel).padding(12.dp)) {
    Column {
        Text("⌾  CLUB LOCATION", color = LocationLime, fontSize = 6.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(5.dp))
        Text("Where is your club located?", color = UiColor.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text("Add your club's location so players can find you\neasily.", color = LocationMuted, fontSize = 7.sp, lineHeight = 9.sp, modifier = Modifier.padding(top = 3.dp))
    }
}

@Composable
private fun LocationLabel(text: String) = Text(text, color = LocationMuted, fontSize = 7.sp, fontWeight = if (text == "LOCATION DETAILS" || text == "FULL ADDRESS") FontWeight.Bold else FontWeight.Normal, modifier = Modifier.padding(bottom = 5.dp))

@Composable
private fun LocationSelector(value: String) = Text("$value                                      ⌄", color = UiColor.White, fontSize = 8.sp, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(LocationPanel).padding(11.dp))

@Composable
private fun LocationField(value: String, onChange: (String) -> Unit, prefix: String) = Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(LocationPanel)) {
    OutlinedTextField(value = value, onValueChange = onChange, leadingIcon = { Text(prefix, color = LocationLime, fontSize = 9.sp) }, singleLine = true, textStyle = androidx.compose.ui.text.TextStyle(color = UiColor.White, fontSize = 8.sp), colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = UiColor.Transparent, focusedBorderColor = LocationLime), modifier = Modifier.fillMaxWidth())
}

@Composable
private fun SetLocationCard() = Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp)).background(LocationPanel).padding(11.dp)) {
    Column {
        Text("⌘  Set Exact Location", color = UiColor.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Text("Help players find your club on the map.", color = LocationMuted, fontSize = 7.sp, modifier = Modifier.padding(top = 2.dp))
        Spacer(Modifier.height(10.dp))
        Box(Modifier.fillMaxWidth().height(92.dp).clip(RoundedCornerShape(7.dp)).background(UiColor(26, 31, 41)), contentAlignment = Alignment.Center) { Text("●", color = LocationLime, fontSize = 28.sp) }
        Spacer(Modifier.height(7.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            Text("⚠  Use Current\n     Location", color = UiColor.White, fontSize = 7.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f).clip(RoundedCornerShape(6.dp)).background(UiColor(43, 48, 59)).padding(vertical = 7.dp))
            Text("▦  Choose on Map", color = UiColor.White, fontSize = 7.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f).clip(RoundedCornerShape(6.dp)).background(UiColor(43, 48, 59)).padding(vertical = 11.dp))
        }
    }
}
