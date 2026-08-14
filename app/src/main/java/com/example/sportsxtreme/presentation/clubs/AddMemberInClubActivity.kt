package com.example.sportsxtreme.presentation.clubs

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color as UiColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

class AddMemberInClubActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = Color.rgb(3, 9, 18)
        window.navigationBarColor = Color.rgb(3, 9, 18)
        setContent { AddMemberPage(::finish) }
    }
}

private val AddBg = UiColor(3, 9, 18)
private val FieldBg = UiColor(8, 20, 34)
private val FieldBorder = UiColor(33, 57, 79)
private val AddLime = UiColor(198, 255, 13)
private val AddMuted = UiColor(169, 180, 194)

@Composable
private fun AddMemberPage(onBack: () -> Unit) {
    var selectedRole by remember { mutableStateOf("Admin") }
    var search by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().background(AddBg).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            BackButton(onBack)
            Box(Modifier.size(82.dp).padding(start = 15.dp).background(UiColor(18, 28, 20)), contentAlignment = Alignment.Center) { Text("VICTORY\nCRICKET\nCLUB", color = UiColor.White, fontSize = 11.sp, lineHeight = 12.sp, fontWeight = FontWeight.Bold) }
            Column(Modifier.weight(1f).padding(start = 17.dp)) { Text("Add Member", color = UiColor.White, fontSize = 29.sp, fontWeight = FontWeight.Bold); Text("Victory Cricket Club  ✦", color = AddMuted, fontSize = 18.sp) }
            Text("♧", color = UiColor.White, fontSize = 31.sp); Spacer(Modifier.width(18.dp)); MenuButton()
        }
        Spacer(Modifier.height(25.dp))
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).border(1.dp, FieldBorder, RoundedCornerShape(10.dp)).padding(26.dp)) {
            Text("Select Role", color = UiColor.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth().height(230.dp).clip(RoundedCornerShape(12.dp)).border(1.dp, FieldBorder, RoundedCornerShape(12.dp))) {
                RoleOption("♙", "Admin", "Full access to manage\nthe club", selectedRole == "Admin", Modifier.weight(1f)) { selectedRole = "Admin" }
                RoleOption("♧", "Captain", "Manage team, players\nand matches", selectedRole == "Captain", Modifier.weight(1f)) { selectedRole = "Captain" }
                RoleOption("♙", "Player", "Add as a player in\na team", selectedRole == "Player", Modifier.weight(1f)) { selectedRole = "Player" }
            }
            Spacer(Modifier.height(31.dp))
            Text("Search Existing User", color = UiColor.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(13.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f).height(70.dp).clip(RoundedCornerShape(10.dp)).background(FieldBg).border(1.dp, FieldBorder, RoundedCornerShape(10.dp)).padding(horizontal = 18.dp), contentAlignment = Alignment.CenterStart) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Text("⌕", color = AddMuted, fontSize = 30.sp); Spacer(Modifier.width(16.dp)); BasicTextField(value = search, onValueChange = { search = it }, singleLine = true, textStyle = androidx.compose.ui.text.TextStyle(color = UiColor.White, fontSize = 17.sp), decorationBox = { inner -> if (search.isEmpty()) Text("Search by name, email or phone number", color = AddMuted, fontSize = 17.sp); inner() }) }
                }
                Spacer(Modifier.width(18.dp)); Box(Modifier.size(70.dp).clip(RoundedCornerShape(10.dp)).background(FieldBg).border(1.dp, FieldBorder, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) { Text("▽", color = UiColor.White, fontSize = 28.sp) }
            }
            Spacer(Modifier.height(14.dp))
            Text("ⓘ  Only existing users of ", color = AddMuted, fontSize = 15.sp)
            Text("SportsXtreme", color = AddLime, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(" can be added.", color = AddMuted, fontSize = 15.sp)
            Spacer(Modifier.height(28.dp))
            Text("Select User", color = UiColor.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(14.dp))
            SelectedUser()
            Spacer(Modifier.height(30.dp))
            Text("$selectedRole Information", color = UiColor.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
            Text("Provide additional details for the ${selectedRole.lowercase()}.", color = AddMuted, fontSize = 16.sp)
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth()) { Field("Date of Birth", "Select date", Modifier.weight(1f)); Spacer(Modifier.width(18.dp)); Field("Gender", "Select gender   ⌄", Modifier.weight(1f)) }
            Spacer(Modifier.height(16.dp)); Field("Address", "Enter full address                         ◉", Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp)); Row(Modifier.fillMaxWidth()) { Field("Phone Number", "Enter phone number", Modifier.weight(1f)); Spacer(Modifier.width(18.dp)); Field("Alternate Phone (Optional)", "Enter alternate number", Modifier.weight(1f)) }
            Spacer(Modifier.height(16.dp)); Field("Bio / Description", "Enter a short bio or description about the ${selectedRole.lowercase()}\n\n\n                                               0/250", Modifier.fillMaxWidth(), 150.dp)
            Spacer(Modifier.height(31.dp))
            Box(Modifier.fillMaxWidth().height(63.dp).clip(RoundedCornerShape(10.dp)).background(AddLime), contentAlignment = Alignment.Center) { Text("♧   Add $selectedRole", color = UiColor.Black, fontSize = 22.sp, fontWeight = FontWeight.Bold) }
        }
        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun BackButton(onBack: () -> Unit) = Box(Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)).background(FieldBg).border(1.dp, FieldBorder, RoundedCornerShape(12.dp)).clickable { onBack() }, contentAlignment = Alignment.Center) { Text("<", color = UiColor.White, fontSize = 38.sp) }
@Composable
private fun MenuButton() = Box(Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)).background(FieldBg).border(1.dp, FieldBorder, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) { Text("⋮", color = UiColor.White, fontSize = 34.sp) }
@Composable
private fun RoleOption(icon: String, title: String, body: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) = Column(modifier.fillMaxHeight().then(if (selected) Modifier.border(2.dp, AddLime, RoundedCornerShape(12.dp)) else Modifier).clickable { onClick() }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Text(icon, color = if (selected) AddLime else UiColor.White, fontSize = 47.sp); Spacer(Modifier.height(12.dp)); Text(title, color = if (selected) AddLime else UiColor.White, fontSize = 21.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(8.dp)); Text(body, color = AddMuted, fontSize = 15.sp, lineHeight = 22.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center); Spacer(Modifier.height(19.dp)); Text(if (selected) "◉" else "○", color = if (selected) AddLime else AddMuted, fontSize = 27.sp) }
@Composable
private fun SelectedUser() = Box(Modifier.fillMaxWidth().height(151.dp).clip(RoundedCornerShape(10.dp)).background(FieldBg).border(1.dp, FieldBorder, RoundedCornerShape(10.dp)).padding(16.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(95.dp).clip(RoundedCornerShape(50.dp)).background(UiColor(51, 72, 91)), contentAlignment = Alignment.Center) { Text("S", color = UiColor.White, fontSize = 37.sp, fontWeight = FontWeight.Bold) }; Column(Modifier.padding(start = 21.dp)) { Text("Suresh Nayak", color = UiColor.White, fontSize = 25.sp, fontWeight = FontWeight.Bold); Text("suresh.nayak@sportsxtreme.com", color = AddMuted, fontSize = 16.sp); Spacer(Modifier.height(10.dp)); Text("+91 9876543210    ✓ Verified", color = UiColor.White, fontSize = 17.sp) } }; Text("×", color = UiColor.White, fontSize = 35.sp, modifier = Modifier.align(Alignment.TopEnd)) }
@Composable
private fun Field(title: String, hint: String, modifier: Modifier, height: androidx.compose.ui.unit.Dp = 92.dp) = Column(modifier.height(height).clip(RoundedCornerShape(10.dp)).background(FieldBg).border(1.dp, FieldBorder, RoundedCornerShape(10.dp)).padding(16.dp)) { Text(title, color = UiColor.White, fontSize = 15.sp); Spacer(Modifier.height(8.dp)); Text(hint, color = AddMuted, fontSize = 16.sp, lineHeight = 21.sp) }
