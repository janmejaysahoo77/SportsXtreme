package com.example.sportsxtreme.presentation.scoring

import com.example.sportsxtreme.R
import com.example.sportsxtreme.presentation.tournament.*
import com.example.sportsxtreme.presentation.components.*
import com.example.sportsxtreme.presentation.auth.*
import com.example.sportsxtreme.presentation.scoring.*
import com.example.sportsxtreme.presentation.match.*
import com.example.sportsxtreme.presentation.media.*
import com.example.sportsxtreme.presentation.home.*
import com.example.sportsxtreme.presentation.team.*
import com.example.sportsxtreme.presentation.profile.*
import com.example.sportsxtreme.presentation.store.*
import com.example.sportsxtreme.domain.model.LiveMatch
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ViewAllScoreCardActivity : ComponentActivity() {

    private val liveMatchViewModel: LiveMatchViewModel by viewModels()

    companion object {
        const val EXTRA_CURRENT_LOCATION = "extra_current_location"
    }

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        val homeLocation = intent.getStringExtra(EXTRA_CURRENT_LOCATION)
            ?.takeIf(String::isNotBlank)
            ?: "Location unavailable"
        val deviceLocation = homeLocation.takeUnless {
            it.startsWith("Finding your location", ignoreCase = true) || it == "Location unavailable"
        }

        setContent {
            val matchState by liveMatchViewModel.uiState.collectAsState()
            ViewAllScoreCardScreen(
                initialLocation = homeLocation,
                deviceLocation = deviceLocation,
                matches = matchState.matches,
                isLoading = matchState.isLoading,
                isOffline = matchState.isOffline,
                errorMessage = matchState.errorMessage,
                onBack = { finish() },
                onResolveLocation = { query, onResolved ->
                    lifecycleScope.launch {
                        val resolved = resolveLocation(query)
                        onResolved(resolved)
                    }
                },
                onOpenScorecard = { match ->
                    startActivity(
                        Intent(this, ScorecardActivity::class.java)
                            .putExtra(ScorecardActivity.EXTRA_MATCH_ID, match.matchId)
                    )
                }
            )
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun resolveLocation(query: String): String? = withContext(Dispatchers.IO) {
        runCatching {
            val address = Geocoder(this@ViewAllScoreCardActivity, Locale.getDefault())
                .getFromLocationName(query, 1)
                ?.firstOrNull()
                ?: return@withContext null
            val locationLabel = listOfNotNull(
                address.locality,
                address.subAdminArea,
                address.adminArea,
                address.countryName
            )
                .map(String::trim)
                .filter(String::isNotBlank)
                .distinct()
                .joinToString(", ")
            locationLabel.ifBlank {
                address.featureName?.takeIf(String::isNotBlank).orEmpty()
            }.takeIf(String::isNotBlank)
        }.getOrNull()
    }
}

private val Primary = Color(0xFFC1FF00)
private val Cyan = Color(0xFF00D2FF)
private val PageBg = Color(0xFF000307)
private val Panel = Color(0xFF060E14)
private val Muted = Color(0xFF7F9196)

private enum class ScoreUiIcon { BACK, SORT, SEARCH, LOCATION, SHIELD, BOLT }

@Composable
private fun ViewAllScoreCardScreen(
    initialLocation: String,
    deviceLocation: String?,
    matches: List<LiveMatch>,
    isLoading: Boolean,
    isOffline: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onResolveLocation: (String, (String?) -> Unit) -> Unit,
    onOpenScorecard: (LiveMatch) -> Unit
) {
    var currentLocation by remember { mutableStateOf(initialLocation) }
    var showLocationPicker by remember { mutableStateOf(false) }
    var isSearchOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val filteredMatches = remember(matches, searchQuery) {
        val query = searchQuery.trim()
        if (query.isBlank()) matches
        else matches.filter { match ->
            sequenceOf(
                match.tournamentName,
                match.teamAName,
                match.teamBName,
                match.teamAShortName,
                match.teamBShortName
            ).any { name -> name.contains(query, ignoreCase = true) }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF00384C),
                        PageBg,
                        Color(0xFF243D05)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, top = 22.dp, end = 16.dp)
        ) {
            TopBar(currentLocation, onBack) { showLocationPicker = true }
            Spacer(Modifier.height(22.dp))
            Text(
                text = "Live matches",
                color = Primary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(18.dp))
            ControlRow(
                onChooseLocation = { showLocationPicker = true },
                onSearch = {
                    isSearchOpen = !isSearchOpen
                    if (!isSearchOpen) searchQuery = ""
                }
            )
            if (isSearchOpen) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    label = { Text("Tournament or team name") },
                    placeholder = { Text("Search tournaments or teams") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Muted,
                        focusedLabelColor = Primary,
                        unfocusedLabelColor = Muted,
                        cursorColor = Primary
                    )
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(top = 18.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (isOffline && matches.isNotEmpty()) {
                    Text("Showing last synced scores while offline", color = Color(0xFFFFD660), fontSize = 10.sp)
                }
                when {
                    isLoading && matches.isEmpty() -> {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 56.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(color = Primary, strokeWidth = 2.dp)
                            Text("Loading live scorecards…", color = Muted, fontSize = 12.sp)
                        }
                    }
                    matches.isEmpty() -> {
                        Text(
                            text = errorMessage ?: "No live scorecards are available right now.",
                            color = Muted,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(top = 56.dp)
                        )
                    }
                    filteredMatches.isEmpty() -> {
                        Text(
                            text = "No tournaments or teams match \"${searchQuery.trim()}\".",
                            color = Muted,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(top = 56.dp)
                        )
                    }
                    else -> {
                        for (match in filteredMatches) {
                            AndroidView(
                                factory = { context ->
                                    LiveMatchCardView(context).apply {
                                        setOnClickListener { onOpenScorecard(match) }
                                    }
                                },
                                update = { card ->
                                    card.bind(match)
                                    card.setOnClickListener { onOpenScorecard(match) }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }

    if (showLocationPicker) {
        LocationPickerDialog(
            currentLocation = currentLocation,
            deviceLocation = deviceLocation,
            onDismiss = { showLocationPicker = false },
            onUseCurrentLocation = {
                currentLocation = deviceLocation ?: "Location unavailable"
                showLocationPicker = false
            },
            onResolveLocation = { query, onResolved ->
                onResolveLocation(query) { resolved ->
                    if (resolved != null) {
                        currentLocation = resolved
                    }
                    onResolved(resolved)
                }
            }
        )
    }
}

@Composable
private fun TopBar(location: String, onBack: () -> Unit, onChooseLocation: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButtonIcon(ScoreUiIcon.BACK, onBack)
        Text(
            text = "All Scorecards",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic,
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        )
        LocationChip(location, onChooseLocation)
    }
}

@Composable
private fun ControlRow(onChooseLocation: () -> Unit, onSearch: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ActionPill(ScoreUiIcon.SORT, "Sort", Modifier.weight(1f))
        ActionPill(ScoreUiIcon.SEARCH, "Search", Modifier.weight(1f), onClick = onSearch)
        ActionPill(ScoreUiIcon.LOCATION, "Choose Location", Modifier.weight(1.28f), accent = Primary, onClick = onChooseLocation)
    }
}

@Composable
private fun IconButtonIcon(icon: ScoreUiIcon, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(Panel.copy(alpha = 0.92f))
            .border(1.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(9.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        ScoreIcon(icon = icon, tint = Color.White, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun LocationChip(location: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .height(42.dp)
            .clip(RoundedCornerShape(21.dp))
            .background(Primary.copy(alpha = 0.16f))
            .border(1.dp, Primary.copy(alpha = 0.55f), RoundedCornerShape(21.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ScoreIcon(icon = ScoreUiIcon.LOCATION, tint = Primary, modifier = Modifier.size(17.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            text = location,
            color = Color(0xFFE4EEE8),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 116.dp)
        )
    }
}

@Composable
private fun ActionPill(
    icon: ScoreUiIcon,
    label: String,
    modifier: Modifier = Modifier,
    accent: Color = Color.White,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(9.dp))
            .background(Panel.copy(alpha = 0.92f))
            .border(1.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(9.dp))
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ScoreIcon(icon = icon, tint = accent, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(7.dp))
        Text(
            text = label,
            color = accent,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun LocationPickerDialog(
    currentLocation: String,
    deviceLocation: String?,
    onDismiss: () -> Unit,
    onUseCurrentLocation: () -> Unit,
    onResolveLocation: (String, (String?) -> Unit) -> Unit
) {
    var query by remember(currentLocation) { mutableStateOf(currentLocation.takeUnless { it == "Location unavailable" }.orEmpty()) }
    var isResolving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    AlertDialog(
        onDismissRequest = { if (!isResolving) onDismiss() },
        containerColor = Panel,
        title = { Text("Choose Location", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Enter the city or area to show on this screen.", color = Muted, fontSize = 12.sp)
                OutlinedTextField(
                    value = query,
                    onValueChange = {
                        query = it
                        errorMessage = null
                    },
                    label = { Text("City or area") },
                    placeholder = { Text("e.g. Bhubaneswar") },
                    singleLine = true,
                    enabled = !isResolving,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                    }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Muted,
                        focusedLabelColor = Primary,
                        unfocusedLabelColor = Muted,
                        cursorColor = Primary
                    )
                )
                errorMessage?.let { Text(it, color = Color(0xFFFF7180), fontSize = 11.sp) }
                if (!deviceLocation.isNullOrBlank()) {
                    TextButton(
                        onClick = onUseCurrentLocation,
                        enabled = !isResolving
                    ) {
                        Text("Use current location · $deviceLocation", color = Primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isResolving,
                onClick = {
                    val value = query.trim()
                    if (value.isBlank()) {
                        errorMessage = "Enter a city or area."
                    } else {
                        isResolving = true
                        errorMessage = null
                        onResolveLocation(value) { resolved ->
                            isResolving = false
                            if (resolved == null) {
                                errorMessage = "Location not found. Try a nearby city or area."
                            } else {
                                onDismiss()
                            }
                        }
                    }
                }
            ) {
                if (isResolving) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Primary, strokeWidth = 2.dp)
                else Text("Set location", color = Primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isResolving) { Text("Cancel", color = Muted) }
        }
    )
}

@Composable
private fun ScoreIcon(icon: ScoreUiIcon, tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val s = minOf(w, h)
        val stroke = Stroke(
            width = s * 0.085f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

        when (icon) {
            ScoreUiIcon.BACK -> {
                drawLine(tint, Offset(w * 0.28f, h * 0.5f), Offset(w * 0.75f, h * 0.5f), stroke.width, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.28f, h * 0.5f), Offset(w * 0.48f, h * 0.3f), stroke.width, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.28f, h * 0.5f), Offset(w * 0.48f, h * 0.7f), stroke.width, StrokeCap.Round)
            }
            ScoreUiIcon.SORT -> {
                drawLine(tint, Offset(w * 0.22f, h * 0.32f), Offset(w * 0.78f, h * 0.32f), stroke.width, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.32f, h * 0.5f), Offset(w * 0.68f, h * 0.5f), stroke.width, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.42f, h * 0.68f), Offset(w * 0.58f, h * 0.68f), stroke.width, StrokeCap.Round)
            }
            ScoreUiIcon.SEARCH -> {
                drawCircle(
                    color = tint,
                    radius = s * 0.2f,
                    center = Offset(w * 0.45f, h * 0.43f),
                    style = stroke
                )
                drawLine(tint, Offset(w * 0.6f, h * 0.58f), Offset(w * 0.78f, h * 0.76f), stroke.width, StrokeCap.Round)
            }
            ScoreUiIcon.LOCATION -> {
                val path = Path().apply {
                    moveTo(w * 0.5f, h * 0.9f)
                    cubicTo(w * 0.22f, h * 0.58f, w * 0.2f, h * 0.38f, w * 0.2f, h * 0.32f)
                    cubicTo(w * 0.2f, h * 0.14f, w * 0.34f, h * 0.08f, w * 0.5f, h * 0.08f)
                    cubicTo(w * 0.66f, h * 0.08f, w * 0.8f, h * 0.14f, w * 0.8f, h * 0.32f)
                    cubicTo(w * 0.8f, h * 0.38f, w * 0.78f, h * 0.58f, w * 0.5f, h * 0.9f)
                }
                drawPath(path, tint, style = stroke)
                drawCircle(tint, radius = w * 0.11f, center = Offset(w * 0.5f, h * 0.32f), style = stroke)
            }
            ScoreUiIcon.SHIELD -> {
                val path = Path().apply {
                    moveTo(w * 0.5f, h * 0.18f)
                    lineTo(w * 0.76f, h * 0.3f)
                    lineTo(w * 0.7f, h * 0.66f)
                    lineTo(w * 0.5f, h * 0.84f)
                    lineTo(w * 0.3f, h * 0.66f)
                    lineTo(w * 0.24f, h * 0.3f)
                    close()
                }
                drawPath(path, tint)
            }
            ScoreUiIcon.BOLT -> {
                val path = Path().apply {
                    moveTo(w * 0.58f, h * 0.12f)
                    lineTo(w * 0.3f, h * 0.56f)
                    lineTo(w * 0.52f, h * 0.56f)
                    lineTo(w * 0.42f, h * 0.88f)
                    lineTo(w * 0.72f, h * 0.42f)
                    lineTo(w * 0.5f, h * 0.42f)
                    close()
                }
                drawPath(path, tint)
            }
        }
    }
}
