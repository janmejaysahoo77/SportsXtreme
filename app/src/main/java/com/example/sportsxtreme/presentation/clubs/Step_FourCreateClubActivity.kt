package com.example.sportsxtreme.presentation.clubs

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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

class Step_FourCreateClubActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = Color.rgb(5, 7, 13)
        window.navigationBarColor = Color.rgb(5, 7, 13)
        setContent { CreateClubStepFour(::finish) }
    }
}

private val ProofBg = UiColor(5, 7, 13)
private val ProofPanel = UiColor(21, 26, 36)
private val ProofLime = UiColor(190, 255, 24)
private val ProofMuted = UiColor(143, 154, 169)

@Composable
private fun CreateClubStepFour(onBack: () -> Unit) {
    var ownerDocument by remember { mutableStateOf("Aadhaar Card") }
    Column(Modifier.fillMaxSize().background(ProofBg)) {
        Row(Modifier.fillMaxWidth().height(45.dp).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("‹", color = UiColor.White, fontSize = 25.sp, modifier = Modifier.width(25.dp).clickable { onBack() })
            Text("Create Club", color = UiColor.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            Text("Step 4 of 4", color = ProofMuted, fontSize = 6.sp, modifier = Modifier.clip(RoundedCornerShape(7.dp)).background(ProofPanel).padding(horizontal = 6.dp, vertical = 3.dp))
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) { repeat(4) { index -> ProofProgress("${index + 1}", index == 3) } }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 7.dp)) {
            Spacer(Modifier.height(14.dp))
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Box(Modifier.size(27.dp).clip(CircleShape).background(UiColor(42, 63, 18)), contentAlignment = Alignment.Center) { Text("✓", color = ProofLime, fontSize = 15.sp) }; Spacer(Modifier.height(7.dp)); Text("Proof of Club", color = UiColor.White, fontSize = 14.sp, fontWeight = FontWeight.Bold); Text("Verify your club and owner to build trust and unlock\nmore features.", color = ProofMuted, fontSize = 7.sp, lineHeight = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 5.dp)) } }
            Spacer(Modifier.height(18.dp))
            ProofSectionHeader("1. Proof of Club", "Upload any one valid document that proves your\nclub is legally registered.")
            UploadDropZone("Drag & drop a file here")
            Spacer(Modifier.height(12.dp))
            Text("ACCEPTED DOCUMENTS (ANY ONE)", color = ProofMuted, fontSize = 6.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(7.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) { DocumentCard("Club Registration\nCertificate", "REC."); DocumentCard("Trust / Society\nRegistration Certificate", "REC.") }
            Spacer(Modifier.height(7.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) { DocumentCard("PAN Card of Club", "OPT."); DocumentCard("Address Proof (Utility\nBill)", "OPT.") }
            Spacer(Modifier.height(7.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) { DocumentCard("Club Ground Photo", "OPT."); DocumentCard("Any Other Supporting\nDocument", "OPT.") }
            Spacer(Modifier.height(11.dp))
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(7.dp)).background(UiColor(28, 38, 20)).padding(9.dp)) { Text("●  Make sure the document is clear and valid. Our\n    team will review it within 24–48 hours.", color = ProofMuted, fontSize = 7.sp, lineHeight = 10.sp) }
            Spacer(Modifier.height(18.dp))
            ProofSectionHeader("2. Owner Identity", "Upload any valid government ID of the owner.")
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) { OwnerOption("Aadhaar Card", ownerDocument == "Aadhaar Card") { ownerDocument = "Aadhaar Card" }; OwnerOption("PAN Card", ownerDocument == "PAN Card") { ownerDocument = "PAN Card" }; OwnerOption("Passport", ownerDocument == "Passport") { ownerDocument = "Passport" } }
            Spacer(Modifier.height(7.dp))
            OwnerOption("Voter ID", ownerDocument == "Voter ID") { ownerDocument = "Voter ID" }
            Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth().height(47.dp).clip(RoundedCornerShape(7.dp)).background(ProofPanel), contentAlignment = Alignment.Center) { Text("⌁\nClick to upload ID front & back", color = ProofMuted, fontSize = 7.sp, lineHeight = 11.sp, textAlign = TextAlign.Center) }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) { InfoCard("▣", "Secure & Private", "Your documents are\nencrypted and safe."); InfoCard("⚠", "Important Note", "Only real documents will\nbe verified.") }
            Spacer(Modifier.height(18.dp))
        }
        Text("Submit for Verification  →", color = UiColor.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(8.dp).clip(RoundedCornerShape(8.dp)).background(ProofLime).padding(vertical = 14.dp))
    }
}

@Composable
private fun RowScope.ProofProgress(number: String, current: Boolean) = Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) { Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(15.dp).clip(CircleShape).background(if (current) UiColor(38, 55, 20) else ProofLime), contentAlignment = Alignment.Center) { Text(if (current) number else "✓", color = if (current) ProofLime else UiColor.Black, fontSize = 7.sp, fontWeight = FontWeight.Bold) }; Spacer(Modifier.width(2.dp)); Box(Modifier.weight(1f).height(2.dp).background(ProofLime)) }; Text(if (number == "1") "BASIC" else if (number == "2") "CONTACT" else if (number == "3") "LOCATION" else "PROOF", color = if (current) ProofLime else ProofMuted, fontSize = 5.sp) }

@Composable
private fun ProofSectionHeader(title: String, description: String) { Text(title, color = UiColor.White, fontSize = 10.sp, fontWeight = FontWeight.Bold); Text(description, color = ProofMuted, fontSize = 7.sp, lineHeight = 9.sp, modifier = Modifier.padding(top = 3.dp, bottom = 9.dp)) }

@Composable
private fun UploadDropZone(text: String) = Box(Modifier.fillMaxWidth().height(118.dp).clip(RoundedCornerShape(8.dp)).background(ProofPanel), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("⇧", color = ProofLime, fontSize = 19.sp); Text(text, color = UiColor.White, fontSize = 7.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 5.dp)); Text("Choose File", color = UiColor.Black, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 9.dp).clip(RoundedCornerShape(13.dp)).background(ProofLime).padding(horizontal = 17.dp, vertical = 6.dp)); Text("JPG, JPEG, PNG, PDF (Max 10MB)", color = ProofMuted, fontSize = 5.sp, modifier = Modifier.padding(top = 6.dp)) } }

@Composable
private fun RowScope.DocumentCard(title: String, badge: String) = Box(Modifier.weight(1f).height(49.dp).clip(RoundedCornerShape(6.dp)).background(ProofPanel).padding(7.dp)) { Column { Text("▧", color = ProofMuted, fontSize = 9.sp); Text(title, color = UiColor.White, fontSize = 6.sp, lineHeight = 8.sp, modifier = Modifier.padding(top = 2.dp)); Text(badge, color = ProofLime, fontSize = 5.sp, modifier = Modifier.align(Alignment.End)) } }

@Composable
private fun OwnerOption(label: String, selected: Boolean, onSelect: () -> Unit) = Text(label, color = if (selected) UiColor.Black else ProofMuted, fontSize = 7.sp, modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(if (selected) ProofLime else ProofPanel).clickable { onSelect() }.padding(horizontal = 10.dp, vertical = 6.dp))

@Composable
private fun RowScope.InfoCard(icon: String, title: String, description: String) = Box(Modifier.weight(1f).clip(RoundedCornerShape(7.dp)).background(ProofPanel).padding(9.dp)) { Column { Text(icon, color = ProofLime, fontSize = 10.sp); Text(title, color = UiColor.White, fontSize = 7.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 5.dp)); Text(description, color = ProofMuted, fontSize = 6.sp, lineHeight = 8.sp, modifier = Modifier.padding(top = 3.dp)) } }
