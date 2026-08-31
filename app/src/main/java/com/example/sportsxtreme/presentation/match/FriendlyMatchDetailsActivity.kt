package com.example.sportsxtreme.presentation.match

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.annotation.SuppressLint
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.example.sportsxtreme.R
import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.usecase.MatchUseCases
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.coroutines.resume

@AndroidEntryPoint
class FriendlyMatchDetailsActivity : ComponentActivity() {
    @Inject lateinit var matchUseCases: MatchUseCases
    private val viewModel: FriendlyMatchDetailsViewModel by viewModels {
        FriendlyMatchDetailsViewModel.factory(matchUseCases)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        val matchId = intent.getStringExtra(EXTRA_MATCH_ID).orEmpty()
        setContent {
            val uiState by viewModel.uiState.collectAsState()
            LaunchedEffect(Unit) {
                viewModel.events.collect { event ->
                    when (event) {
                        is FriendlyMatchDetailsEvent.NavigateToTeamSelection -> {
                            startActivity(
                                Intent(this@FriendlyMatchDetailsActivity, SelectPlayingTeamsActivity::class.java)
                                    .putExtra(SelectPlayingTeamsActivity.EXTRA_MATCH_ID, event.matchId)
                            )
                            finish()
                        }
                        is FriendlyMatchDetailsEvent.ShowMessage -> Toast.makeText(
                            this@FriendlyMatchDetailsActivity, event.message, Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            FriendlyMatchDetailsScreen(
                onBack = { finish() },
                onContinue = { venue, matchDateEpochMs, matchTime ->
                    viewModel.updateMatchDetails(matchId, venue, matchDateEpochMs, matchTime)
                },
                isLoading = uiState.isLoading
            )
        }
    }

    companion object {
        const val EXTRA_MATCH_ID = "match_id"
    }
}

private val DetailsAccent = Color(0xFFC1FF00)
private val DetailsBg = Color(0xFF030A14)
private val DetailsCard = Color(0xFF0A1422)
private val DetailsBorder = Color(0xFF17283A)
private val DetailsMuted = Color(0xFF8D9B9C)

@Composable
private fun FriendlyMatchDetailsScreen(
    onBack: () -> Unit,
    onContinue: (String, Long, String) -> Unit,
    isLoading: Boolean
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var ground by remember { mutableIntStateOf(0) }
    var date by remember { mutableIntStateOf(0) }
    var timeMode by remember { mutableIntStateOf(1) }
    var venue by remember { mutableStateOf("KRT Stadium, Bhubaneswar") }
    var customVenue by remember { mutableStateOf("") }
    var showVenueDialog by remember { mutableStateOf(false) }
    var showGroundPicker by remember { mutableStateOf(false) }
    var showLocationConfirmation by remember { mutableStateOf(false) }
    var showLocationSettingsDialog by remember { mutableStateOf(false) }
    var groundSearch by remember { mutableStateOf("") }
    var matchDateEpochMs by remember { mutableLongStateOf(startOfToday()) }
    var matchTime by remember { mutableStateOf("06:30 PM") }
    val applyCurrentLocation: (String?) -> Unit = { locationLabel ->
        if (locationLabel == null) {
            Toast.makeText(context, "Unable to find your current location. Please enable location services and try again.", Toast.LENGTH_LONG).show()
        } else {
            ground = 1
            venue = locationLabel
        }
    }
    val fetchAndApplyCurrentLocation: () -> Unit = {
        coroutineScope.launch { applyCurrentLocation(fetchCurrentLocationLabel(context)) }
    }
    val locationSettingsLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (isDeviceLocationEnabled(context)) {
            fetchAndApplyCurrentLocation()
        } else {
            Toast.makeText(context, "Turn on Location to use your current location.", Toast.LENGTH_LONG).show()
        }
    }
    val locationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            if (isDeviceLocationEnabled(context)) {
                fetchAndApplyCurrentLocation()
            } else {
                showLocationSettingsDialog = true
            }
        } else {
            Toast.makeText(context, "Location permission is needed to use your current location.", Toast.LENGTH_LONG).show()
        }
    }
    val requestCurrentLocation: () -> Unit = {
        val hasLocationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasLocationPermission) {
            if (isDeviceLocationEnabled(context)) {
                fetchAndApplyCurrentLocation()
            } else {
                showLocationSettingsDialog = true
            }
        } else {
            locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }
    val openTimePicker = {
        val calendar = Calendar.getInstance()
        TimePickerDialog(context, { _, hour, minute ->
            timeMode = 2
            matchTime = formatMatchTime(hour, minute)
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
    }
    Box(
        modifier = Modifier.fillMaxSize().background(DetailsBg).drawBehind {
            drawCircle(Color(0x15214970), size.width * 0.69f, Offset(size.width * 1.08f, size.height * 0.24f))
            drawCircle(Color(0x103A6518), size.width * 0.58f, Offset(size.width * 0.04f, size.height * 0.76f))
            repeat(10) { index ->
                val y = size.height * (0.13f + index * 0.09f)
                drawLine(Color(0x0A78A4BB), Offset(0f, y), Offset(size.width, y - size.width * 0.14f), 1.dp.toPx())
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                Column(Modifier.fillMaxSize()) {
                    DetailsTopBar(onBack)
                    Column(
                        modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        Column {
                            Text("Set up your match", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                            Text("Choose the venue, date and start time.", color = DetailsMuted, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp))
                        }
                        DetailsHeading("Ground Selection", DetailsIcon.LOCATION)
                        DetailsOptionCard("Select Existing Ground", "KRT Stadium, Bhubaneswar", ground == 0) {
                            showGroundPicker = true
                        }
                        DetailsOptionCard(
                            "Use Current Location",
                            if (ground == 1) venue else null,
                            ground == 1
                        ) {
                            showLocationConfirmation = true
                        }
                        DetailsOptionCard("Add New Ground", null, ground == 2) {
                            showVenueDialog = true
                        }
                        DetailsHeading("Match Date", DetailsIcon.CALENDAR)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            DetailsDateCard("Today", formatShortDate(startOfToday()), date == 0, Modifier.weight(1f)) {
                                date = 0
                                matchDateEpochMs = startOfToday()
                            }
                            DetailsDateCard("Tomorrow", formatShortDate(startOfTomorrow()), date == 1, Modifier.weight(1f)) {
                                date = 1
                                matchDateEpochMs = startOfTomorrow()
                            }
                            DetailsDateCard("Custom", formatShortDate(matchDateEpochMs), date == 2, Modifier.weight(1f)) {
                                val calendar = Calendar.getInstance().apply { timeInMillis = matchDateEpochMs }
                                DatePickerDialog(context, { _, year, month, dayOfMonth ->
                                    date = 2
                                    matchDateEpochMs = startOfDay(year, month, dayOfMonth)
                                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                            }
                        }
                        DetailsHeading("Match Time", DetailsIcon.CLOCK)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            DetailsTimeChip("Now", timeMode == 0) {
                                timeMode = 0
                                matchTime = formatMatchTime(Calendar.getInstance())
                            }
                            DetailsTimeChip("Choose Time", timeMode == 1) {
                                timeMode = 1
                                matchTime = "06:30 PM"
                            }
                            DetailsTimeChip("Custom Time", timeMode == 2, openTimePicker)
                        }
                        DetailsTimeCard(matchTime, openTimePicker)
                        DetailsSnapshot(venue, matchDateEpochMs, matchTime)
                        Spacer(Modifier.height(82.dp))
                    }
                }
                Box(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(Color.Transparent, DetailsBg, DetailsBg)))
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(28.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFFD8FF37), DetailsAccent, Color(0xFF9AFF00))))
                            .clickable(
                                enabled = !isLoading && venue.isNotBlank() && matchDateEpochMs > 0L && matchTime.isNotBlank(),
                                onClick = { onContinue(venue, matchDateEpochMs, matchTime) }
                            ),
                        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isLoading) "SAVING DETAILS..." else "CONTINUE", color = Color(0xFF122004), fontSize = 15.sp, fontWeight = FontWeight.Black)
                        DetailsArrow(Modifier.padding(start = 10.dp).size(20.dp), true, Color(0xFF122004))
                    }
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
        if (showVenueDialog) {
            AlertDialog(
                onDismissRequest = { showVenueDialog = false },
                title = { Text("Add New Ground") },
                text = {
                    OutlinedTextField(
                        value = customVenue,
                        onValueChange = { customVenue = it },
                        label = { Text("Ground name") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val newVenue = customVenue.trim()
                            if (newVenue.isNotBlank()) {
                                ground = 2
                                venue = newVenue
                                showVenueDialog = false
                            }
                        }
                    ) { Text("SAVE", color = DetailsAccent) }
                },
                dismissButton = {
                    TextButton(onClick = { showVenueDialog = false }) { Text("CANCEL", color = DetailsMuted) }
                }
            )
        }
        if (showGroundPicker) {
            GroundPickerDialog(
                searchQuery = groundSearch,
                onSearchQueryChange = { groundSearch = it },
                onGroundSelected = { selectedGround ->
                    ground = 0
                    venue = selectedGround
                    showGroundPicker = false
                    groundSearch = ""
                },
                onDismiss = {
                    showGroundPicker = false
                    groundSearch = ""
                }
            )
        }
        if (showLocationConfirmation) {
            LocationConfirmationDialog(
                onConfirm = {
                    showLocationConfirmation = false
                    requestCurrentLocation()
                },
                onDismiss = { showLocationConfirmation = false }
            )
        }
        if (showLocationSettingsDialog) {
            LocationSettingsDialog(
                onConfirm = {
                    showLocationSettingsDialog = false
                    locationSettingsLauncher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                },
                onDismiss = { showLocationSettingsDialog = false }
            )
        }
    }
}

@Composable
private fun GroundPickerDialog(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onGroundSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val grounds = listOf("KRT Stadium, Bhubaneswar")
    val matchingGrounds = grounds.filter { it.contains(searchQuery.trim(), ignoreCase = true) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DetailsCard,
        titleContentColor = Color.White,
        textContentColor = DetailsMuted,
        title = { Text("Select ground", fontSize = 20.sp, fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Choose a saved ground for this match.", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Search grounds") },
                    placeholder = { Text("Type to search") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = DetailsAccent,
                        unfocusedBorderColor = DetailsBorder,
                        focusedLabelColor = DetailsAccent,
                        unfocusedLabelColor = DetailsMuted,
                        cursorColor = DetailsAccent
                    )
                )
                matchingGrounds.forEach { ground ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFF142019))
                            .border(1.dp, Color(0xFF6F9427), RoundedCornerShape(12.dp)).clickable { onGroundSelected(ground) }
                            .padding(horizontal = 14.dp, vertical = 15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DetailsIconCanvas(DetailsIcon.LOCATION, Modifier.size(21.dp), DetailsAccent)
                        Text(ground, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 11.dp).weight(1f))
                        DetailsIconCanvas(DetailsIcon.CHECK, Modifier.size(19.dp), DetailsAccent)
                    }
                }
                if (matchingGrounds.isEmpty()) {
                    Text("No grounds match your search.", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("CLOSE", color = DetailsAccent) } }
    )
}

@Composable
private fun LocationConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DetailsCard,
        titleContentColor = Color.White,
        textContentColor = DetailsMuted,
        title = { Text("Use current location?", fontSize = 20.sp, fontWeight = FontWeight.Black) },
        text = { Text("We'll ask for location access to use your current ground location.", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
        confirmButton = { TextButton(onClick = onConfirm) { Text("FETCH LOCATION", color = DetailsAccent) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("NOT NOW", color = DetailsMuted) } }
    )
}

@Composable
private fun LocationSettingsDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DetailsCard,
        titleContentColor = Color.White,
        textContentColor = DetailsMuted,
        title = { Text("Turn on location", fontSize = 20.sp, fontWeight = FontWeight.Black) },
        text = { Text("Location services are off. Turn them on in Settings to fetch your current location.", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
        confirmButton = { TextButton(onClick = onConfirm) { Text("OPEN SETTINGS", color = DetailsAccent) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCEL", color = DetailsMuted) } }
    )
}

private fun isDeviceLocationEnabled(context: android.content.Context): Boolean {
    val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
    return listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
        .any { provider -> runCatching { locationManager.isProviderEnabled(provider) }.getOrDefault(false) }
}

@SuppressLint("MissingPermission")
private suspend fun fetchCurrentLocationLabel(context: android.content.Context): String? {
    val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
    val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
    val lastKnownLocation = providers.mapNotNull { provider ->
        runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull()
    }.maxByOrNull { location -> location.time }
    val location = lastKnownLocation ?: requestFreshLocation(locationManager) ?: return null
    return withContext(Dispatchers.IO) {
        val address = runCatching {
            @Suppress("DEPRECATION")
            Geocoder(context, Locale.getDefault()).getFromLocation(location.latitude, location.longitude, 1)?.firstOrNull()
        }.getOrNull()
        listOfNotNull(address?.featureName, address?.locality, address?.adminArea)
            .distinct()
            .joinToString(", ")
            .ifBlank { "%.4f, %.4f".format(Locale.US, location.latitude, location.longitude) }
    }
}

@SuppressLint("MissingPermission")
private suspend fun requestFreshLocation(locationManager: LocationManager): Location? {
    val enabledProviders = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
        .filter { provider -> runCatching { locationManager.isProviderEnabled(provider) }.getOrDefault(false) }
    if (enabledProviders.isEmpty()) return null
    return withTimeoutOrNull(12_000L) {
        suspendCancellableCoroutine<Location?> { continuation ->
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    if (continuation.isActive) continuation.resume(location)
                    locationManager.removeUpdates(this)
                }
            }
            continuation.invokeOnCancellation { locationManager.removeUpdates(listener) }
            var requestStarted = false
            enabledProviders.forEach { provider ->
                runCatching {
                    locationManager.requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper())
                    requestStarted = true
                }
            }
            if (!requestStarted && continuation.isActive) continuation.resume(null)
        }
    }
}

@Composable
private fun DetailsTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(64.dp).background(Color(0xEA06101A)).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DetailsArrow(Modifier.size(24.dp).clickable(onClick = onBack), false, DetailsAccent)
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Match Details", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
            Text("CONFIGURE MATCH LOGISTICS", color = DetailsMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
        }
        DetailsIconCanvas(DetailsIcon.INFO, Modifier.size(22.dp), DetailsMuted)
    }
}

@Composable
private fun DetailsHeading(title: String, icon: DetailsIcon) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        DetailsIconCanvas(icon, Modifier.size(20.dp), DetailsAccent)
        Text(title, color = Color(0xFFE1E8E4), fontSize = 16.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
private fun DetailsOptionCard(title: String, subtitle: String?, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(68.dp).clip(RoundedCornerShape(14.dp))
            .background(if (selected) Color(0xFF142019) else DetailsCard)
            .border(if (selected) 2.dp else 1.dp, if (selected) Color(0xFF6F9427) else DetailsBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick).padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(if (selected) Color(0xFF668900) else Color(0xFF1B2736)), contentAlignment = Alignment.Center) {
            DetailsIconCanvas(DetailsIcon.LOCATION, Modifier.size(21.dp), if (selected) DetailsAccent else DetailsMuted)
        }
        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
            Text(title, color = if (selected) Color.White else Color(0xFFB1BCB8), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            subtitle?.let { Text(it, color = DetailsAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 3.dp)) }
        }
        if (selected) DetailsIconCanvas(DetailsIcon.CHECK, Modifier.size(20.dp), DetailsAccent)
    }
}

@Composable
private fun DetailsDateCard(title: String, subtitle: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier.height(76.dp).clip(RoundedCornerShape(14.dp)).background(if (selected) Color(0xFF152019) else DetailsCard)
            .border(if (selected) 2.dp else 1.dp, if (selected) Color(0xFF71962B) else DetailsBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick).padding(vertical = 9.dp), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DetailsIconCanvas(DetailsIcon.CALENDAR, Modifier.size(18.dp), if (selected) DetailsAccent else DetailsMuted)
        Text(title, color = if (selected) DetailsAccent else Color(0xFFB5BFBB), fontSize = 12.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 5.dp))
        Text(subtitle, color = DetailsMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DetailsTimeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.height(34.dp).clip(RoundedCornerShape(18.dp)).background(if (selected) Color(0xFF1A2814) else DetailsCard)
            .border(if (selected) 1.5.dp else 1.dp, if (selected) Color(0xFF6E9428) else DetailsBorder, RoundedCornerShape(18.dp)).clickable(onClick = onClick).padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) { Text(label, color = if (selected) DetailsAccent else DetailsMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
}

@Composable
private fun DetailsTimeCard(matchTime: String, onEditTime: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(76.dp).clip(RoundedCornerShape(14.dp)).background(DetailsCard)
            .border(1.dp, DetailsBorder, RoundedCornerShape(14.dp)).padding(horizontal = 17.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        Text(matchTime.substringBefore(" "), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.SemiBold)
        Text(matchTime.substringAfter(" ", ""), color = DetailsAccent, fontSize = 15.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 6.dp, top = 10.dp))
        Spacer(Modifier.weight(1f))
        Box(Modifier.size(36.dp).clip(RoundedCornerShape(9.dp)).background(Color(0xFF1A2838)).clickable(onClick = onEditTime), contentAlignment = Alignment.Center) {
            DetailsIconCanvas(DetailsIcon.EDIT, Modifier.size(18.dp), DetailsMuted)
        }
    }
}

@Composable
private fun DetailsSnapshot(venue: String, matchDateEpochMs: Long, matchTime: String) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xDD091522)).border(1.dp, DetailsBorder, RoundedCornerShape(14.dp))
            .drawBehind { drawLine(DetailsAccent, Offset(0f, 12.dp.toPx()), Offset(0f, size.height - 12.dp.toPx()), 4.dp.toPx()) }.padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("MATCH SNAPSHOT", color = DetailsMuted, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 0.9.sp)
            Text(
                "Friendly match • ${formatShortDate(matchDateEpochMs)}",
                color = Color(0xFFCFD8D4),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 6.dp)
            )
            Text(
                venue,
                color = DetailsAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 3.dp)
            )
        }
        Text(matchTime, color = DetailsAccent, fontSize = 16.sp, fontWeight = FontWeight.Black)
    }
}

private fun startOfToday(): Long = Calendar.getInstance().apply {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

private fun startOfTomorrow(): Long = Calendar.getInstance().apply {
    add(Calendar.DAY_OF_YEAR, 1)
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

private fun startOfDay(year: Int, month: Int, dayOfMonth: Int): Long = Calendar.getInstance().apply {
    set(year, month, dayOfMonth, 0, 0, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

private fun formatShortDate(epochMs: Long): String =
    SimpleDateFormat("MMM dd", Locale.getDefault()).format(epochMs).uppercase(Locale.getDefault())

private fun formatMatchTime(calendar: Calendar): String = formatMatchTime(
    calendar.get(Calendar.HOUR_OF_DAY),
    calendar.get(Calendar.MINUTE)
)

private fun formatMatchTime(hour: Int, minute: Int): String {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
    }
    return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(calendar.time)
}

private enum class DetailsIcon { LOCATION, CALENDAR, CLOCK, EDIT, CHECK, INFO }

@Composable
private fun DetailsArrow(modifier: Modifier, right: Boolean, color: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        val path = Path().apply {
            if (right) { moveTo(size.width * .3f, size.height * .22f); lineTo(size.width * .7f, size.height * .5f); lineTo(size.width * .3f, size.height * .78f) }
            else { moveTo(size.width * .7f, size.height * .22f); lineTo(size.width * .3f, size.height * .5f); lineTo(size.width * .7f, size.height * .78f) }
        }
        drawPath(path, color, style = stroke)
    }
}

@Composable
private fun DetailsIconCanvas(icon: DetailsIcon, modifier: Modifier, color: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        when (icon) {
            DetailsIcon.LOCATION -> { drawCircle(color, size.minDimension * .28f, Offset(size.width * .5f, size.height * .42f), style = stroke); drawLine(color, Offset(size.width * .5f, size.height * .68f), Offset(size.width * .5f, size.height * .87f), strokeWidth = stroke.width, cap = StrokeCap.Round) }
            DetailsIcon.CALENDAR -> { drawRoundRect(color, Offset(size.width * .17f, size.height * .24f), Size(size.width * .66f, size.height * .6f), CornerRadius(2.dp.toPx()), style = stroke); drawLine(color, Offset(size.width * .17f, size.height * .43f), Offset(size.width * .83f, size.height * .43f), strokeWidth = stroke.width); drawLine(color, Offset(size.width * .34f, size.height * .14f), Offset(size.width * .34f, size.height * .34f), strokeWidth = stroke.width); drawLine(color, Offset(size.width * .66f, size.height * .14f), Offset(size.width * .66f, size.height * .34f), strokeWidth = stroke.width) }
            DetailsIcon.CLOCK -> { drawCircle(color, size.minDimension * .35f, style = stroke); drawLine(color, Offset(size.width * .5f, size.height * .5f), Offset(size.width * .5f, size.height * .3f), strokeWidth = stroke.width, cap = StrokeCap.Round); drawLine(color, Offset(size.width * .5f, size.height * .5f), Offset(size.width * .66f, size.height * .6f), strokeWidth = stroke.width, cap = StrokeCap.Round) }
            DetailsIcon.EDIT -> { drawLine(color, Offset(size.width * .28f, size.height * .73f), Offset(size.width * .72f, size.height * .29f), strokeWidth = stroke.width * 1.5f, cap = StrokeCap.Round); drawLine(color, Offset(size.width * .26f, size.height * .76f), Offset(size.width * .43f, size.height * .72f), strokeWidth = stroke.width, cap = StrokeCap.Round) }
            DetailsIcon.CHECK -> { drawCircle(color, size.minDimension * .37f); drawLine(Color(0xFF1C2908), Offset(size.width * .32f, size.height * .5f), Offset(size.width * .46f, size.height * .64f), strokeWidth = stroke.width, cap = StrokeCap.Round); drawLine(Color(0xFF1C2908), Offset(size.width * .46f, size.height * .64f), Offset(size.width * .7f, size.height * .36f), strokeWidth = stroke.width, cap = StrokeCap.Round) }
            DetailsIcon.INFO -> { drawCircle(color, size.minDimension * .38f, style = stroke); drawLine(color, Offset(size.width * .5f, size.height * .45f), Offset(size.width * .5f, size.height * .7f), strokeWidth = stroke.width, cap = StrokeCap.Round); drawCircle(color, size.minDimension * .045f, Offset(size.width * .5f, size.height * .29f)) }
        }
    }
}

private data class FriendlyMatchDetailsUiState(val isLoading: Boolean = false)

private sealed interface FriendlyMatchDetailsEvent {
    data class NavigateToTeamSelection(val matchId: String) : FriendlyMatchDetailsEvent
    data class ShowMessage(val message: String) : FriendlyMatchDetailsEvent
}

private class FriendlyMatchDetailsViewModel(private val matchUseCases: MatchUseCases) : ViewModel() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _uiState = MutableStateFlow(FriendlyMatchDetailsUiState())
    private val _events = MutableSharedFlow<FriendlyMatchDetailsEvent>()

    val uiState: StateFlow<FriendlyMatchDetailsUiState> = _uiState.asStateFlow()
    val events: SharedFlow<FriendlyMatchDetailsEvent> = _events.asSharedFlow()

    fun updateMatchDetails(matchId: String, venue: String, matchDateEpochMs: Long, matchTime: String) {
        if (_uiState.value.isLoading) return
        if (matchId.isBlank()) {
            scope.launch { showError("Match id is missing") }
            return
        }
        if (venue.isBlank() || matchDateEpochMs <= 0L || matchTime.isBlank()) {
            scope.launch { showError("Complete all match details") }
            return
        }
        scope.launch {
            _uiState.value = FriendlyMatchDetailsUiState(isLoading = true)
            when (val result = matchUseCases.updateMatchDetails(matchId, venue, matchDateEpochMs, matchTime)) {
                is Resource.Success -> {
                    _uiState.value = FriendlyMatchDetailsUiState()
                    _events.emit(FriendlyMatchDetailsEvent.NavigateToTeamSelection(matchId))
                }
                is Resource.Error -> showError(result.message ?: "Unable to save match details")
                is Resource.Loading -> Unit
            }
        }
    }

    override fun onCleared() {
        scope.cancel()
    }

    private suspend fun showError(message: String) {
        _uiState.value = FriendlyMatchDetailsUiState()
        _events.emit(FriendlyMatchDetailsEvent.ShowMessage(message))
    }

    companion object {
        fun factory(matchUseCases: MatchUseCases): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass.isAssignableFrom(FriendlyMatchDetailsViewModel::class.java))
                return FriendlyMatchDetailsViewModel(matchUseCases) as T
            }
        }
    }
}
