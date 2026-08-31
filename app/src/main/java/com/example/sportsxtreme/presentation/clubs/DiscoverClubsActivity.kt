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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

class DiscoverClubsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = Color.rgb(5, 7, 8)
        window.navigationBarColor = Color.rgb(5, 7, 8)
        setContent { DiscoverClubsScreen(onBack = ::finish) }
    }
}

private val DiscoverBg = UiColor(5, 7, 8)
private val DiscoverPanel = UiColor(18, 22, 25)
private val DiscoverBorder = UiColor(47, 54, 57)
private val DiscoverMuted = UiColor(150, 157, 160)
private val DiscoverLime = UiColor(190, 255, 24)

@Composable
private fun DiscoverClubsScreen(onBack: () -> Unit) {
    var selectedFilter by remember { mutableStateOf("All Clubs") }
    var showLocationSheet by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize().background(DiscoverBg)) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 10.dp)) {
            DiscoverHeader(onBack)
            SearchPlaceholder()
            Spacer(Modifier.height(9.dp))
            FilterRow(selectedFilter) { selectedFilter = it }
            if (selectedFilter == "Corporate") CorporateClubsContent() else AllClubsContent()
            Spacer(Modifier.height(100.dp))
        }
        ExploreNearbyButton(Modifier.align(Alignment.BottomEnd).padding(end = 13.dp, bottom = 18.dp)) { showLocationSheet = true }
    }
    if (showLocationSheet) LocationSelectionSheet(onDismiss = { showLocationSheet = false })
}

@Composable
private fun ExploreNearbyButton(modifier: Modifier = Modifier, onClick: () -> Unit) = Box(
    modifier = modifier
        .size(64.dp)
        .clip(CircleShape)
        .background(DiscoverLime)
        .clickable(onClick = onClick),
    contentAlignment = Alignment.Center
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("●", color = UiColor.Black, fontSize = 14.sp)
        Text("EXPLORE", color = UiColor.Black, fontSize = 6.sp, fontWeight = FontWeight.Bold)
        Text("NEARBY CLUBS", color = UiColor.Black, fontSize = 5.sp, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationSelectionSheet(onDismiss: () -> Unit) = ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = DiscoverBg,
    contentColor = UiColor.White,
    dragHandle = null
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 9.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("‹", color = UiColor.White, fontSize = 32.sp, modifier = Modifier.width(34.dp).clickable(onClick = onDismiss))
            Column {
                Text("●  Select Your Location", color = UiColor.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("Find clubs near you", color = DiscoverMuted, fontSize = 10.sp, modifier = Modifier.padding(start = 24.dp))
            }
        }
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth().height(49.dp).clip(RoundedCornerShape(15.dp)).background(DiscoverPanel).padding(horizontal = 15.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("⌕", color = DiscoverMuted, fontSize = 25.sp)
            Spacer(Modifier.width(11.dp))
            Text("Search an area or address", color = DiscoverMuted, fontSize = 12.sp, modifier = Modifier.weight(1f))
            Text("⌕", color = DiscoverLime, fontSize = 25.sp)
        }
        Spacer(Modifier.height(17.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            LocationAction("◎", "Use Current\nLocation", "Detect my location", Modifier.weight(1f))
            LocationAction("+", "Add New\nAddress", "Add address manually", Modifier.weight(1f))
            LocationAction("◉", "Request\nAddress", "Ask location from\na friend", Modifier.weight(1f))
        }
        Spacer(Modifier.height(29.dp))
        LocationSectionHeader("SAVED ADDRESSES", "View All  ›")
        Spacer(Modifier.height(10.dp))
        AddressCard("➤", "698 m", "Gita", "Bh 12., Gita Autonomous College,\nCollege Road, Beside National Highway,\nBhubaneswar, Odisha 751003")
        Spacer(Modifier.height(28.dp))
        LocationSectionHeader("RECENTLY SEARCHED", "Clear All  ▣")
        Spacer(Modifier.height(10.dp))
        AddressCard("●", "920 m", "Gita autonomous college front gate", "Badaraghunathpur Road,\nGadajagasora, Badaraghunathpur,\nOdisha 754029")
        Spacer(Modifier.height(25.dp))
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(DiscoverPanel).padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("◎", color = DiscoverLime, fontSize = 24.sp)
            Spacer(Modifier.width(13.dp))
            Text("Location helps us show nearby cricket clubs,\ntournaments and communities.", color = DiscoverMuted, fontSize = 10.sp, lineHeight = 14.sp)
        }
        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun LocationAction(symbol: String, title: String, subtitle: String, modifier: Modifier) = Card(
    modifier = modifier.height(151.dp), shape = RoundedCornerShape(15.dp),
    colors = CardDefaults.cardColors(containerColor = DiscoverPanel)
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 6.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(Modifier.size(42.dp).clip(CircleShape).background(UiColor(12, 17, 19)), contentAlignment = Alignment.Center) { Text(symbol, color = DiscoverLime, fontSize = 23.sp) }
        Spacer(Modifier.height(10.dp))
        Text(title, color = UiColor.White, fontSize = 12.sp, lineHeight = 15.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(Modifier.height(7.dp))
        Text(subtitle, color = DiscoverMuted, fontSize = 8.sp, lineHeight = 11.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
private fun LocationSectionHeader(title: String, action: String) = Row(Modifier.fillMaxWidth()) {
    Text(title, color = DiscoverLime, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
    Text(action, color = DiscoverLime, fontSize = 10.sp)
}

@Composable
private fun AddressCard(symbol: String, distance: String, title: String, details: String) = Card(
    shape = RoundedCornerShape(15.dp), colors = CardDefaults.cardColors(containerColor = DiscoverPanel), modifier = Modifier.fillMaxWidth()
) {
    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.width(75.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(symbol, color = DiscoverLime, fontSize = 27.sp)
            Text(distance, color = DiscoverLime, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Column(Modifier.weight(1f)) {
            Text(title, color = UiColor.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(details, color = DiscoverMuted, fontSize = 10.sp, lineHeight = 14.sp)
        }
        Text("⋮", color = DiscoverLime, fontSize = 20.sp, modifier = Modifier.align(Alignment.Top))
    }
}

@Composable
private fun AllClubsContent() {
    Spacer(Modifier.height(13.dp))
    SectionTitle("✦ FEATURED CLUB", "VIEW ALL ›")
    Spacer(Modifier.height(7.dp))
    FeaturedClubCard()
    Spacer(Modifier.height(17.dp))
    ClubsListHeader("ALL CLUBS", "Popular")
    Spacer(Modifier.height(8.dp))
    ClubResults()
}

@Composable
private fun CorporateClubsContent() {
    Spacer(Modifier.height(10.dp))
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(DiscoverPanel).padding(horizontal = 9.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("Showing clubs near  Bhubaneswar,", color = DiscoverMuted, fontSize = 6.sp)
            Text("Odisha", color = UiColor.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
        }
        Text("Change\nLocation", color = DiscoverLime, fontSize = 7.sp, fontWeight = FontWeight.Bold)
    }
    Spacer(Modifier.height(17.dp))
    ClubsListHeader("◉  NEARBY CLUBS", "Distance")
    Spacer(Modifier.height(9.dp))
    ClubResults()
}

@Composable
private fun ClubsListHeader(title: String, sort: String) = Row(verticalAlignment = Alignment.CenterVertically) {
    Text(title, color = UiColor.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
    Text("SORT BY: ", color = DiscoverMuted, fontSize = 6.sp)
    Text("$sort⌄", color = DiscoverLime, fontSize = 7.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun ClubResults() {
    ClubResultCard("Warriors Cricket Club", "CUTTACK, ODISHA", "A competitive cricket club focused on performance, discipline and...", "12", "42", "255", "4.5")
    Spacer(Modifier.height(8.dp))
    ClubResultCard("Royal Kings Cricket Club", "PURI, ODISHA", "Uniting talent and passion for cricket. Join us and be a part of the legacy.", "8", "28", "180", "4.7")
    Spacer(Modifier.height(8.dp))
    ClubResultCard("Speedster Cricket Academy", "BHUBANESWAR, ODISHA", "Professional coaching for all age groups. Build your cricketing future with us.", "6", "35", "390", "4.6")
}

@Composable
private fun DiscoverHeader(onBack: () -> Unit) = Row(Modifier.fillMaxWidth().height(42.dp), verticalAlignment = Alignment.CenterVertically) {
    Text("‹", color = UiColor.White, fontSize = 31.sp, modifier = Modifier.width(35.dp).clickable { onBack() })
    Column(Modifier.weight(1f)) {
        Text("DISCOVER CLUBS", color = UiColor.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text("EXPLORE CRICKET COMMUNITIES", color = DiscoverMuted, fontSize = 5.sp, letterSpacing = 0.6.sp)
    }
    Text("⌕", color = DiscoverMuted, fontSize = 22.sp)
    Spacer(Modifier.width(13.dp))
    Text("⋮", color = DiscoverMuted, fontSize = 20.sp)
}

@Composable
private fun SearchPlaceholder() = Row(Modifier.fillMaxWidth().height(33.dp).clip(RoundedCornerShape(8.dp)).background(DiscoverPanel).padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
    Text("⌕", color = DiscoverMuted, fontSize = 16.sp)
    Spacer(Modifier.width(6.dp))
    Text("Search clubs, locations, academies...", color = DiscoverMuted, fontSize = 8.sp)
}

@Composable
private fun FilterRow(selected: String, onSelected: (String) -> Unit) = Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
    listOf("All Clubs", "Corporate", "Verified").forEach { label ->
        val active = label == selected
        Text(label, color = if (active) UiColor.Black else DiscoverMuted, fontSize = 8.sp, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clip(RoundedCornerShape(14.dp)).background(if (active) DiscoverLime else DiscoverPanel).clickable { onSelected(label) }.padding(horizontal = 12.dp, vertical = 7.dp))
    }
}

@Composable
private fun SectionTitle(title: String, action: String) = Row(Modifier.fillMaxWidth()) {
    Text(title, color = UiColor.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
    Text(action, color = DiscoverLime, fontSize = 7.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun FeaturedClubCard() = Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = DiscoverPanel)) {
    val context = LocalContext.current
    Column(Modifier.padding(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PlaceholderLogo("VC")
            Spacer(Modifier.width(9.dp))
            Column(Modifier.weight(1f)) {
                Text("Victory Cricket Club  ●", color = UiColor.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("BHUBANESWAR, ODISHA", color = DiscoverMuted, fontSize = 6.sp)
                Spacer(Modifier.height(3.dp))
                Text("A passionate cricket club focused on developing talent and promoting th...", color = DiscoverMuted, fontSize = 7.sp, lineHeight = 9.sp)
            }
            Text("●", color = UiColor(54, 152, 255), fontSize = 9.sp)
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            MiniStat("2.3K", "FOLLOWERS"); MiniStat("488", "MEMBERS"); MiniStat("24", "TEAMS"); MiniStat("4.8", "RATING")
        }
        Spacer(Modifier.height(9.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("• • •", color = DiscoverMuted, fontSize = 11.sp, modifier = Modifier.weight(1f))
            Text("View Club  ➜", color = UiColor.Black, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clip(RoundedCornerShape(14.dp)).background(DiscoverLime).clickable { context.startActivity(Intent(context, ViewClubPageActivity::class.java)) }.padding(horizontal = 15.dp, vertical = 8.dp))
        }
    }
}

@Composable
private fun PlaceholderLogo(label: String) = Box(Modifier.size(45.dp).clip(RoundedCornerShape(8.dp)).background(UiColor(30, 43, 39)), contentAlignment = Alignment.Center) {
    Text(label, color = DiscoverLime, fontSize = 13.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun MiniStat(value: String, label: String) = Column(Modifier.width(37.dp).clip(RoundedCornerShape(5.dp)).background(UiColor(26, 31, 34)).padding(vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
    Text(value, color = DiscoverLime, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    Text(label, color = DiscoverMuted, fontSize = 4.sp)
}

@Composable
private fun ClubResultCard(name: String, location: String, description: String, teams: String, members: String, followers: String, rating: String) = Card(
    shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = DiscoverPanel),
    modifier = Modifier.fillMaxWidth()
) {
    Column(Modifier.padding(9.dp)) {
        Row {
            PlaceholderLogo(name.take(2).uppercase())
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(name, color = UiColor.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Text("•••", color = DiscoverMuted, fontSize = 11.sp)
                }
                Text("●  $location", color = DiscoverMuted, fontSize = 6.sp)
                Spacer(Modifier.height(3.dp))
                Text(description, color = DiscoverMuted, fontSize = 7.sp, lineHeight = 9.sp, maxLines = 2)
                Spacer(Modifier.height(7.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatValue(teams, "TEAMS"); StatValue(members, "MEMBERS"); StatValue(followers, "FOLLOWERS"); StatValue(rating, "RATING", lime = true)
                    Spacer(Modifier.weight(1f))
                    Text("Join Request  ➜", color = UiColor.Black, fontSize = 7.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(DiscoverLime).padding(horizontal = 8.dp, vertical = 6.dp))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { listOf("VERIFIED", "TOURNAMENT ORGANIZER", "ACADEMY").forEach { Chip(it) } }
    }
}

@Composable
private fun StatValue(value: String, label: String, lime: Boolean = false) = Column(Modifier.width(34.dp), horizontalAlignment = Alignment.CenterHorizontally) {
    Text(value, color = if (lime) DiscoverLime else UiColor.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
    Text(label, color = DiscoverMuted, fontSize = 4.sp)
}

@Composable
private fun Chip(label: String) = Text(label, color = DiscoverMuted, fontSize = 4.sp, modifier = Modifier.clip(RoundedCornerShape(3.dp)).background(UiColor(34, 40, 43)).padding(horizontal = 5.dp, vertical = 3.dp))
