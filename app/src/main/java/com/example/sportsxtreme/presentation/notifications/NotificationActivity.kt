package com.example.sportsxtreme.presentation.notifications

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.sportsxtreme.presentation.ui.theme.XtremeLime

class NotificationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = android.graphics.Color.rgb(10, 16, 28)
        window.navigationBarColor = android.graphics.Color.rgb(10, 16, 28)
        setContent { NotificationScreen(onBack = ::finish) }
    }
}

private data class AppNotification(
    val title: String,
    val message: String,
    val time: String,
    val icon: ImageVector,
    val category: String,
    val unread: Boolean = false
)

private val sampleNotifications = listOf(
    AppNotification("Match starting soon", "Your match with Thunder Strikers begins in 30 minutes.", "10 min ago", Icons.Default.SportsCricket, "MATCHES", true),
    AppNotification("New team invite", "Falcon XI invited you to join their squad.", "1 hour ago", Icons.Default.Groups, "TEAMS", true),
    AppNotification("Tournament update", "The City Premier League fixtures are now available.", "3 hours ago", Icons.Default.EmojiEvents, "TOURNAMENTS", true),
    AppNotification("Match result recorded", "Riverdale CC won their latest league match.", "Yesterday", Icons.Default.SportsCricket, "MATCHES"),
    AppNotification("Welcome to SportsXtreme", "Follow teams and tournaments to keep up with the game.", "Yesterday", Icons.Default.MarkEmailRead, "UPDATES")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationScreen(onBack: () -> Unit) {
    var showUnreadOnly by remember { mutableStateOf(false) }
    var unreadIds by remember { mutableStateOf(sampleNotifications.indices.filter { sampleNotifications[it].unread }.toSet()) }
    val notifications = sampleNotifications.mapIndexedNotNull { index, item ->
        if (!showUnreadOnly || index in unreadIds) item.copy(unread = index in unreadIds) else null
    }

    Scaffold(
        containerColor = Color(0xFF0A101C),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Notifications", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 19.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { unreadIds = emptySet() }) {
                        Icon(Icons.Default.MarkEmailRead, contentDescription = "Mark all as read", tint = XtremeLime)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0A101C))
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(label = "All", selected = !showUnreadOnly) { showUnreadOnly = false }
                FilterChip(label = "Unread${if (unreadIds.isNotEmpty()) " ${unreadIds.size}" else ""}", selected = showUnreadOnly) { showUnreadOnly = true }
            }

            if (notifications.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.MarkEmailRead, null, tint = Color(0xFF657184), modifier = Modifier.size(44.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("You're all caught up", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("New updates will show up here.", color = Color(0xFF8E9AAF), fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text("RECENT", color = Color(0xFF91A0B5), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp, modifier = Modifier.padding(start = 2.dp, bottom = 2.dp))
                    }
                    items(notifications) { item -> NotificationRow(item) }
                }
            }
        }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    androidx.compose.material3.Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) XtremeLime else Color(0xFF151F30),
        contentColor = if (selected) Color(0xFF0A101C) else Color(0xFFB0B9C8)
    ) {
        Text(label, modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun NotificationRow(item: AppNotification) {
    Card(
        colors = CardDefaults.cardColors(containerColor = if (item.unread) Color(0xFF121E2D) else Color(0xFF0E1725)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.Top) {
            Box(
                Modifier.size(42.dp).clip(CircleShape).background(Color(0xFF202E3D)),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.icon, contentDescription = null, tint = XtremeLime, modifier = Modifier.size(21.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    if (item.unread) Box(Modifier.size(7.dp).clip(CircleShape).background(XtremeLime))
                }
                Spacer(Modifier.height(4.dp))
                Text(item.message, color = Color(0xFFB0B9C8), fontSize = 12.sp, lineHeight = 17.sp)
                Spacer(Modifier.height(9.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.category, color = XtremeLime, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                    Spacer(Modifier.width(8.dp))
                    Text("·  ${item.time}", color = Color(0xFF7E8A9D), fontSize = 11.sp)
                }
            }
        }
    }
}
