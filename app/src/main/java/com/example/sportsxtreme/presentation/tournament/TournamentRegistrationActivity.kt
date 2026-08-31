package com.example.sportsxtreme.presentation.tournament

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material3.Text
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.activity.viewModels
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.example.sportsxtreme.R
import com.example.sportsxtreme.domain.model.Tournament
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@AndroidEntryPoint
class TournamentRegistrationActivity : ComponentActivity() {
    private val viewModel: TournamentRegistrationViewModel by viewModels()
    private var pendingLocation: ((String) -> Unit)? = null
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) fetchRegistrationLocation() else Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        setContent {
            TournamentRegistrationScreen(
                onBack = { finish() },
                onNavigateNext = { tournament ->
                    startActivity(
                        Intent(this, TournamentRequirementsActivity::class.java)
                            .putExtra(TournamentRequirementsActivity.EXTRA_START_DATE, tournament.startDate)
                            .putExtra(TournamentRequirementsActivity.EXTRA_MATCH_FORM, tournament.matchForm)
                            .putExtra(TournamentRequirementsActivity.EXTRA_BALL_TYPE, tournament.ballType)
                            .putExtra(TournamentRequirementsActivity.EXTRA_TOURNAMENT_ID, tournament.id)
                    )
                },
                onLocationClick = { callback -> requestRegistrationLocation(callback) },
                viewModel = viewModel
            )
        }
    }

    private fun requestRegistrationLocation(callback: (String) -> Unit) {
        pendingLocation = callback
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (granted) fetchRegistrationLocation() else locationPermissionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    @SuppressLint("MissingPermission")
    private fun fetchRegistrationLocation() {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val location = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
            .mapNotNull { runCatching { locationManager.getLastKnownLocation(it) }.getOrNull() }
            .maxByOrNull { it.time }
        if (location == null) {
            Toast.makeText(this, "Unable to determine your location", Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch(Dispatchers.IO) {
            val address = runCatching {
                @Suppress("DEPRECATION")
                Geocoder(this@TournamentRegistrationActivity, Locale.getDefault())
                    .getFromLocation(location.latitude, location.longitude, 1)?.firstOrNull()
            }.getOrNull()
            val city = address?.locality ?: address?.subAdminArea ?: address?.adminArea
            withContext(Dispatchers.Main) {
                if (city.isNullOrBlank()) Toast.makeText(this@TournamentRegistrationActivity, "Nearest city not available", Toast.LENGTH_SHORT).show()
                else pendingLocation?.invoke(city)
                pendingLocation = null
            }
        }
    }
}

internal val FormAccent = Color(0xFFC1FF00)
internal val FormBg = Color(0xFF010509)
internal val FormPanel = Color(0xFF0B111C)
internal val FormField = Color(0xFF111828)
internal val FormStroke = Color(0xFF2E3950)
internal val FormMuted = Color(0xFF8E9C9A)
internal val FormCyan = Color(0xFF4DE9FF)
internal val FormWarm = Color(0xFFFFB84D)

@Composable
private fun TournamentRegistrationScreen(
    onBack: () -> Unit,
    onNavigateNext: (Tournament) -> Unit,
    onLocationClick: ((String) -> Unit) -> Unit,
    viewModel: TournamentRegistrationViewModel
) {
    val tournamentState by viewModel.tournamentState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is TournamentRegistrationViewModel.UiState.Success) {
            viewModel.resetUiState()
            onNavigateNext(tournamentState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FormBg)
            .drawBehind {
                drawCircle(
                    color = Color(0x332C4D11),
                    radius = size.width * 0.78f,
                    center = Offset(size.width * 1.02f, size.height * 0.22f)
                )
                drawCircle(
                    color = Color(0x1A00D2FF),
                    radius = size.width * 0.56f,
                    center = Offset(0f, size.height * 0.75f)
                )
            }
    ) {
        FormTopBar(onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            BannerUploader()
            TournamentModePicker(
                selectedType = tournamentState.type,
                onModeSelected = viewModel::updateMode
            )
            TournamentBasicFields(
                state = tournamentState,
                onFieldChange = viewModel::updateField,
                onLocationClick = { onLocationClick { city ->
                    viewModel.updateField(TournamentRegistrationViewModel.Field.CITY, city)
                } }
            )
            TimelineCategorySection(
                date = tournamentState.startDate,
                dateToBeAnnounced = tournamentState.dateToBeAnnounced,
                onDateChange = { viewModel.updateField(TournamentRegistrationViewModel.Field.START_DATE, it) },
                onDateToBeAnnouncedChange = {
                    viewModel.updateField(TournamentRegistrationViewModel.Field.DATE_TO_BE_ANNOUNCED, it.toString())
                    if (it) viewModel.updateField(TournamentRegistrationViewModel.Field.START_DATE, "")
                }
            )
            BallFormatSection(
                selectedBall = tournamentState.ballType,
                onBallSelected = viewModel::updateBallType,
                selectedFormat = tournamentState.matchForm,
                onFormatSelected = viewModel::updateMatchForm
            )
            SettingsSection(
                lookingForTeams = tournamentState.lookingForTeams,
                onLookingForTeamsChange = viewModel::updateLookingForTeams
            )
            
            if (uiState is TournamentRegistrationViewModel.UiState.Error) {
                Text(
                    text = (uiState as TournamentRegistrationViewModel.UiState.Error).message,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
            
            NextButton(
                isLoading = uiState is TournamentRegistrationViewModel.UiState.Loading,
                onClick = viewModel::saveTournament
            )
            Spacer(androidx.compose.ui.Modifier.height(100.dp))
        }
    }
}

@Composable
private fun FormTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF07101A), Color(0xFF0D1C22), Color(0xFF07101A))
                )
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.material3.Icon(
            painter = painterResource(id = R.drawable.outline_arrow_back_ios_24),
            contentDescription = "Back",
            tint = Color(0xFFC7D2CF),
            modifier = Modifier.size(30.dp).clickable(onClick = onBack)
        )
        Text(
            text = "Add Tournament / Series",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(Color(0x1FC1FF00))
                .border(1.dp, Color(0x55C1FF00), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("1", color = FormAccent, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun BannerUploader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(156.dp)
            .shadow(14.dp, RoundedCornerShape(18.dp), clip = false)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF14210B), Color(0xFF0B1A22), Color(0xFF101421))
                )
            )
            .border(1.2.dp, FormAccent.copy(alpha = 0.55f), RoundedCornerShape(18.dp))
    ) {
        Image(
            painter = painterResource(id = R.drawable.ground),
            contentDescription = "Tournament ground",
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .height(156.dp)
                .width(190.dp),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xEE07100B), Color(0xCC07100B), Color(0x3307100B))
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 18.dp, top = 18.dp, end = 118.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text("Build your event", color = FormAccent, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
            Text("Add banner and logo", color = Color.White, fontSize = 23.sp, lineHeight = 25.sp, fontWeight = FontWeight.Black)
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(14.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xE60D1420))
                .border(1.dp, Color(0x664DE9FF), RoundedCornerShape(18.dp))
                .padding(horizontal = 12.dp, vertical = 7.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                androidx.compose.material3.Icon(
                    painter = painterResource(id = R.drawable.baseline_camera_alt_24),
                    contentDescription = null,
                    tint = FormCyan,
                    modifier = Modifier.size(17.dp)
                )
                Text("ADD BANNER", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 18.dp, end = 18.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AddBadge()
            Text(
                "Add logo for your tournament page.",
                color = Color(0xFFC5D0CC),
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f)
            )
        }
    }
}

@Composable
private fun TournamentModePicker(selectedType: String, onModeSelected: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Choose event type", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ModeCard(
                title = "Tournament",
                subtitle = "Knockout or league",
                imageRes = R.drawable.tournamentlogo,
                selected = selectedType == "Tournament",
                accent = FormAccent,
                modifier = Modifier.weight(1f).clickable { onModeSelected("Tournament") }
            )
            ModeCard(
                title = "Series",
                subtitle = "Friendly matches",
                imageRes = R.drawable.freindlymatch,
                selected = selectedType == "Series",
                accent = FormCyan,
                modifier = Modifier.weight(1f).clickable { onModeSelected("Series") }
            )
        }
    }
}

@Composable
private fun ModeCard(title: String, subtitle: String, imageRes: Int, selected: Boolean, accent: Color, modifier: Modifier) {
    Box(
        modifier = modifier
            .height(154.dp)
            .shadow(if (selected) 12.dp else 4.dp, RoundedCornerShape(18.dp), clip = false)
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (selected) {
                    Brush.linearGradient(listOf(Color(0xFF17260D), Color(0xFF0B1714)))
                } else {
                    Brush.linearGradient(listOf(Color(0xFF101928), Color(0xFF0A101A)))
                }
            )
            .border(1.7.dp, if (selected) accent.copy(alpha = 0.85f) else FormStroke, RoundedCornerShape(18.dp))
            .padding(13.dp)
    ) {
        if (selected) {
            CheckPill(modifier = Modifier.align(Alignment.TopEnd), accent = accent)
        }
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ModeImageBubble(imageRes = imageRes, selected = selected, accent = accent)
            Text(
                title,
                color = if (selected) accent else Color(0xFFE2E9E6),
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(top = 12.dp),
                maxLines = 1
            )
            Text(
                subtitle,
                color = Color(0xFF9EAEAA),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 3.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ModeImageBubble(imageRes: Int, selected: Boolean, accent: Color) {
    Box(
        modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(if (selected) accent.copy(alpha = 0.34f) else Color(0xFF243040), Color(0xFF080D12))
                )
            )
            .border(if (selected) 2.dp else 1.dp, if (selected) accent else Color(0xFF40506A), CircleShape)
            .padding(9.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun TournamentBasicFields(state: Tournament, onFieldChange: (TournamentRegistrationViewModel.Field, String) -> Unit, onLocationClick: () -> Unit) {
    FormSection(title = "Tournament details") {
        LabeledInput(
            label = "Tournament/Series Name",
            placeholder = "Enter name...",
            value = state.name,
            onValueChange = { onFieldChange(TournamentRegistrationViewModel.Field.NAME, it) }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            LabeledInput(
                label = "City",
                placeholder = "Location",
                value = state.city,
                onValueChange = { onFieldChange(TournamentRegistrationViewModel.Field.CITY, it) },
                modifier = Modifier.weight(1f),
                trailingIcon = {
                    androidx.compose.material3.Icon(
                        painter = painterResource(id = R.drawable.baseline_edit_location_24),
                        contentDescription = "Choose Location",
                        tint = FormCyan,
                        modifier = Modifier.size(20.dp).clickable(onClick = onLocationClick)
                    )
                }
            )
            LabeledInput(
                label = "Ground",
                placeholder = "Stadium",
                value = state.ground,
                onValueChange = { onFieldChange(TournamentRegistrationViewModel.Field.GROUND, it) },
                modifier = Modifier.weight(1f)
            )
        }
        LabeledInput(
            label = "Organizer Name",
            placeholder = "Person or Club",
            value = state.organizerName,
            onValueChange = { onFieldChange(TournamentRegistrationViewModel.Field.ORGANIZER_NAME, it) }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            LabeledInput(
                label = "Phone",
                placeholder = "+1...",
                value = state.phone,
                onValueChange = { onFieldChange(TournamentRegistrationViewModel.Field.PHONE, it) },
                modifier = Modifier.weight(1f)
            )
            LabeledInput(
                label = "Email",
                placeholder = "example@email.com",
                value = state.email,
                onValueChange = { onFieldChange(TournamentRegistrationViewModel.Field.EMAIL, it) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TimelineCategorySection(
    date: String,
    dateToBeAnnounced: Boolean,
    onDateChange: (String) -> Unit,
    onDateToBeAnnouncedChange: (Boolean) -> Unit
) {
    FormSection(title = "Timeline & Category") {
        DateBox(
            label = "START DATE",
            value = date,
            onValueChange = onDateChange
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDateToBeAnnouncedChange(!dateToBeAnnounced) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = dateToBeAnnounced,
                onCheckedChange = onDateToBeAnnouncedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = FormAccent,
                    uncheckedColor = FormMuted,
                    checkmarkColor = Color(0xFF111604)
                )
            )
            Text("To be announced", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun BallFormatSection(
    selectedBall: String,
    onBallSelected: (String) -> Unit,
    selectedFormat: String,
    onFormatSelected: (String) -> Unit
) {
    FormSection(title = "Ball & Format") {
        SectionSmallLabel("BALL TYPE")
        Row(horizontalArrangement = Arrangement.spacedBy(17.dp), verticalAlignment = Alignment.CenterVertically) {
            BallChoice("Tennis", R.drawable.tennisball, selectedBall == "Tennis", Modifier.clickable { onBallSelected("Tennis") })
            BallChoice("Leather", R.drawable.leatherball, selectedBall == "Leather", Modifier.clickable { onBallSelected("Leather") })
            BallChoice("Other", R.drawable.otherball, selectedBall == "Other", Modifier.clickable { onBallSelected("Other") })
        }
        SectionSmallLabel("MATCH FORM")
        ChipRow(
            labels = listOf("Limited Overs", "Test", "T20", "ODI", "T10", "Box Cricket"),
            selectedLabel = selectedFormat,
            onLabelSelected = onFormatSelected
        )
    }
}

@Composable
private fun SettingsSection(lookingForTeams: Boolean, onLookingForTeamsChange: (Boolean) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TogglePanel(
            title = "Looking for more teams to join?",
            subtitle = "Teams can discover and request entry into your tournament.",
            active = lookingForTeams,
            modifier = Modifier.clickable { onLookingForTeamsChange(!lookingForTeams) }
        )
    }
}
