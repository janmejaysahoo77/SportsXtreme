package com.example.sportsxtreme.presentation.notifications

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportsxtreme.data.local.entity.NotificationEntity
import com.example.sportsxtreme.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class AppNotificationUiModel(
    val id: String,
    val title: String,
    val message: String,
    val time: String,
    val icon: ImageVector,
    val category: String,
    val unread: Boolean,
    val targetUrl: String? = null
)

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    val notificationsState: StateFlow<List<AppNotificationUiModel>> =
        notificationRepository.getAllNotifications()
            .map { list -> list.map { it.toUiModel() } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = emptyList()
            )

    fun markAsRead(id: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(id)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead()
        }
    }

    fun deleteNotification(id: String) {
        viewModelScope.launch {
            notificationRepository.deleteNotification(id)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            notificationRepository.clearAllNotifications()
        }
    }

    private fun NotificationEntity.toUiModel(): AppNotificationUiModel {
        val icon = when (iconType.uppercase()) {
            "TEAMS", "GROUPS" -> Icons.Default.Groups
            "TOURNAMENTS", "EVENTS" -> Icons.Default.EmojiEvents
            "UPDATES", "EMAIL" -> Icons.Default.MarkEmailRead
            else -> Icons.Default.SportsCricket
        }
        return AppNotificationUiModel(
            id = id,
            title = title,
            message = message,
            time = formatRelativeTime(timestamp),
            icon = icon,
            category = category.uppercase(),
            unread = !isRead,
            targetUrl = targetUrl
        )
    }

    private fun formatRelativeTime(timestampMs: Long): String {
        val diffMs = System.currentTimeMillis() - timestampMs
        val minutes = diffMs / (1000 * 60)
        val hours = minutes / 60
        val days = hours / 24

        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "$minutes min ago"
            hours < 24 -> if (hours == 1L) "1 hour ago" else "$hours hours ago"
            days == 1L -> "Yesterday"
            days < 7 -> "$days days ago"
            else -> SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestampMs))
        }
    }
}
