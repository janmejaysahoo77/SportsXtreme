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

class Step_TwoCreateClubActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = Color.rgb(5, 7, 8)
        window.navigationBarColor = Color.rgb(5, 7, 8)
        setContent {
            CreateClubStepTwo(
                onBack = ::finish,
                onContinue = { startActivity(Intent(this, Step_ThreeCreateClubActivity::class.java)) }
            )
        }
    }
}

private val StepTwoBg = UiColor(5, 7, 13)
private val StepTwoPanel = UiColor(15, 20, 29)
private val StepTwoLime = UiColor(190, 255, 24)
private val StepTwoMuted = UiColor(143, 155, 169)

@Composable
private fun CreateClubStepTwo(onBack: () -> Unit, onContinue: () -> Unit) {
    var contactName by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("+91 98765 43210") }
    var emailAddress by remember { mutableStateOf("club@example.com") }
    var alternativeContact by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().background(StepTwoBg)) {
        Row(Modifier.fillMaxWidth().height(47.dp).padding(horizontal = 13.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(26.dp).clip(CircleShape).background(StepTwoPanel).clickable { onBack() }, contentAlignment = Alignment.Center) {
                Text("‹", color = UiColor.White, fontSize = 25.sp)
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Create Club", color = UiColor.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("STEP 2 OF 4", color = StepTwoLime, fontSize = 6.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(26.dp))
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 13.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(4) { index ->
                    Box(Modifier.weight(1f).height(3.dp).clip(CircleShape).background(if (index < 2) StepTwoLime else UiColor(31, 38, 49)))
                }
            }
            Spacer(Modifier.height(20.dp))
            ContactHero()
            Spacer(Modifier.height(21.dp))
            StepTwoLabel("Contact Person Name")
            StepTwoField(contactName, { contactName = it }, "Enter full name")
            Spacer(Modifier.height(14.dp))
            StepTwoLabel("Mobile Number")
            StepTwoField(mobileNumber, { mobileNumber = it }, "", verify = true)
            Spacer(Modifier.height(14.dp))
            StepTwoLabel("Email Address")
            StepTwoField(emailAddress, { emailAddress = it }, "", verify = true)
            Spacer(Modifier.height(14.dp))
            StepTwoLabel("Alternative Contact (Optional)")
            StepTwoField(alternativeContact, { alternativeContact = it }, "Optional mobile number")
            Spacer(Modifier.height(14.dp))
            StepTwoLabel("Website (Optional)")
            StepTwoField(website, { website = it }, "https://yourclub.com")
            Spacer(Modifier.height(24.dp))
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(UiColor(9, 26, 48)).padding(12.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Box(Modifier.size(27.dp).clip(CircleShape).background(UiColor(17, 52, 90)), contentAlignment = Alignment.Center) { Text("♧", color = UiColor(116, 176, 255), fontSize = 15.sp) }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Your Privacy Matters", color = UiColor.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(3.dp))
                        Text("Your contact information is secure. Only the\ndetails you choose will appear on your club\nprofile.", color = StepTwoMuted, fontSize = 7.sp, lineHeight = 10.sp)
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
        }
        Row(Modifier.fillMaxWidth().padding(13.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Back", color = UiColor.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(.8f).clip(RoundedCornerShape(18.dp)).background(StepTwoPanel).clickable { onBack() }.padding(vertical = 12.dp))
            Text("Continue  →", color = UiColor.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(1.3f).clip(RoundedCornerShape(18.dp)).background(StepTwoLime).clickable { onContinue() }.padding(vertical = 12.dp))
        }
    }
}

@Composable
private fun ContactHero() {
    Box(Modifier.fillMaxWidth().height(119.dp).clip(RoundedCornerShape(16.dp)).background(UiColor(15, 23, 29))) {
        Column(Modifier.padding(14.dp).align(Alignment.CenterStart)) {
            Text("♣   CLUB CONTACT", color = StepTwoLime, fontSize = 7.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(7.dp))
            Text("Let's Stay\nConnected", color = UiColor.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, lineHeight = 18.sp)
            Spacer(Modifier.height(6.dp))
            Text("Help players and organizers\ncontact your club easily. You can\nupdate these details anytime.", color = StepTwoMuted, fontSize = 7.sp, lineHeight = 10.sp)
        }
        Text("✉", color = UiColor(72, 91, 43), fontSize = 54.sp, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 18.dp))
    }
}

@Composable
private fun StepTwoLabel(text: String) = Text(text, color = StepTwoMuted, fontSize = 8.sp, modifier = Modifier.padding(bottom = 5.dp))

@Composable
private fun StepTwoField(value: String, onChange: (String) -> Unit, hint: String, verify: Boolean = false) {
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(StepTwoPanel)) {
        OutlinedTextField(value = value, onValueChange = onChange, placeholder = { Text(hint, color = StepTwoMuted, fontSize = 9.sp) }, singleLine = true, textStyle = androidx.compose.ui.text.TextStyle(color = UiColor.White, fontSize = 10.sp), colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = UiColor.Transparent, focusedBorderColor = StepTwoLime), modifier = Modifier.fillMaxWidth())
        if (verify) Text("✓  Verify", color = StepTwoLime, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 10.dp).clip(RoundedCornerShape(11.dp)).background(UiColor(42, 58, 20)).padding(horizontal = 7.dp, vertical = 4.dp))
    }
}
