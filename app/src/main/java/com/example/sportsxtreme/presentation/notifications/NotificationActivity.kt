package com.example.sportsxtreme.presentation.notifications

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.example.sportsxtreme.common.PushNotificationHelper
import com.example.sportsxtreme.presentation.ui.theme.XtremeLime
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationActivity : ComponentActivity() {

    private val viewModel: NotificationViewModel by viewModels()

    private var hasNotificationPermission by mutableStateOf(false)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            Toast.makeText(this, "Push notifications enabled", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = android.graphics.Color.rgb(10, 16, 28)
        window.navigationBarColor = android.graphics.Color.rgb(10, 16, 28)

        // Initialize notification channel & sync FCM token
        PushNotificationHelper.createNotificationChannel(this)
        PushNotificationHelper.fetchAndLogFcmToken(this)

        checkNotificationPermission()

        setContent {
            NotificationScreen(
                viewModel = viewModel,
                hasNotificationPermission = hasNotificationPermission,
                onRequestPermission = ::requestNotificationPermission,
                onBack = ::finish,
                onNotificationClick = { notification ->
                    viewModel.markAsRead(notification.id)
                    notification.targetUrl?.let { url ->
                        if (url.isNotBlank()) {
                            runCatching {
                                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            }
                        }
                    }
                }
            )
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasNotificationPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            hasNotificationPermission = true
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationScreen(
    viewModel: NotificationViewModel,
    hasNotificationPermission: Boolean,
    onRequestPermission: () -> Unit,
    onBack: () -> Unit,
    onNotificationClick: (AppNotificationUiModel) -> Unit
) {
    var showUnreadOnly by remember { mutableStateOf(false) }
    val allNotifications by viewModel.notificationsState.collectAsState()

    val filteredNotifications = remember(allNotifications, showUnreadOnly) {
        if (showUnreadOnly) {
            allNotifications.filter { it.unread }
        } else {
            allNotifications
        }
    }

    val unreadCount = remember(allNotifications) {
        allNotifications.count { it.unread }
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
                    if (unreadCount > 0) {
                        IconButton(onClick = { viewModel.markAllAsRead() }) {
                            Icon(Icons.Default.MarkEmailRead, contentDescription = "Mark all as read", tint = XtremeLime)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0A101C))
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            // Permission Banner if notifications are disabled on Android 13+
            if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                PermissionBanner(onRequestPermission = onRequestPermission)
            }

            Row(
                Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(label = "All (${allNotifications.size})", selected = !showUnreadOnly) { showUnreadOnly = false }
                FilterChip(label = "Unread${if (unreadCount > 0) " $unreadCount" else ""}", selected = showUnreadOnly) { showUnreadOnly = true }
            }

            if (filteredNotifications.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.MarkEmailRead, null, tint = Color(0xFF657184), modifier = Modifier.size(44.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("You're all caught up", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (showUnreadOnly) "No unread notifications." else "New updates will show up here via push notifications.",
                            color = Color(0xFF8E9AAF),
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            "RECENT",
                            color = Color(0xFF91A0B5),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            modifier = Modifier.padding(start = 2.dp, bottom = 2.dp)
                        )
                    }
                    items(filteredNotifications, key = { it.id }) { item ->
                        NotificationRow(item = item, onClick = { onNotificationClick(item) })
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionBanner(onRequestPermission: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B283A)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = XtremeLime, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Enable Push Notifications", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Get live match alerts and team updates in real-time.", color = Color(0xFFB0B9C8), fontSize = 11.sp)
            }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(containerColor = XtremeLime, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Enable", fontWeight = FontWeight.Bold, fontSize = 11.sp)
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
private fun NotificationRow(item: AppNotificationUiModel, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = if (item.unread) Color(0xFF121E2D) else Color(0xFF0E1725)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
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
