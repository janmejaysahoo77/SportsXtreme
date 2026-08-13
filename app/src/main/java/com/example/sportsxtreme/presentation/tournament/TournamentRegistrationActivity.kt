package com.example.sportsxtreme.presentation.tournament

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.activity.viewModels
import com.example.sportsxtreme.R
import com.example.sportsxtreme.domain.model.Tournament
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TournamentRegistrationActivity : ComponentActivity() {
    private val viewModel: TournamentRegistrationViewModel by viewModels()

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
                viewModel = viewModel
            )
        }
    }
}

private val FormAccent = Color(0xFFC1FF00)
private val FormBg = Color(0xFF010509)
private val FormPanel = Color(0xFF0B111C)
private val FormField = Color(0xFF111828)
private val FormStroke = Color(0xFF2E3950)
private val FormMuted = Color(0xFF8E9C9A)
private val FormCyan = Color(0xFF4DE9FF)
private val FormWarm = Color(0xFFFFB84D)

@Composable
private fun TournamentRegistrationScreen(
    onBack: () -> Unit,
    onNavigateNext: (Tournament) -> Unit,
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
                onFieldChange = viewModel::updateField
            )
            TimelineCategorySection(
                date = tournamentState.startDate,
                onDateChange = { viewModel.updateField(TournamentRegistrationViewModel.Field.START_DATE, it) }
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
        ArrowIcon(modifier = Modifier.size(30.dp).clickable(onClick = onBack), right = false)
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
                CameraIcon(Modifier.size(17.dp), FormCyan)
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
private fun TournamentBasicFields(state: Tournament, onFieldChange: (TournamentRegistrationViewModel.Field, String) -> Unit) {
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
                        modifier = Modifier.size(20.dp).clickable { /* TODO: Choose location */ }
                    )
                }
            )
            LabeledInput(
                label = "Ground",
                placeholder = "Stadium",
                value = state.ground,
                onValueChange = { onFieldChange(TournamentRegistrationViewModel.Field.GROUND, it) },
                modifier = Modifier.weight(1f),
                trailingIcon = {
                    androidx.compose.material3.Icon(
                        painter = painterResource(id = R.drawable.baseline_edit_location_24),
                        contentDescription = "Choose Ground",
                        tint = FormCyan,
                        modifier = Modifier.size(20.dp).clickable { /* TODO: Choose ground */ }
                    )
                }
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
private fun TimelineCategorySection(date: String, onDateChange: (String) -> Unit) {
    FormSection(title = "Timeline & Category") {
        DateBox(
            label = "START DATE",
            value = date,
            onValueChange = onDateChange
        )
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

@Composable
private fun FormSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
        RequiredTitle(title)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(17.dp), clip = false)
                .clip(RoundedCornerShape(17.dp))
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF101827), Color(0xFF0B111C)))
                )
                .border(1.dp, Color(0xFF31405C), RoundedCornerShape(17.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp),
            content = content
        )
    }
}

@Composable
private fun LabeledInput(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(7.dp)) {
        RequiredTitle(label, compact = true)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
            singleLine = true,
            cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(
                            Brush.verticalGradient(listOf(Color(0xFF172033), FormField))
                        )
                        .border(1.dp, Color(0xFF42526F), RoundedCornerShape(13.dp))
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            if (value.isEmpty()) {
                                Text(placeholder, color = Color(0xFF768784), fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            innerTextField()
                        }
                        if (trailingIcon != null) {
                            trailingIcon()
                        }
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateBox(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        onValueChange(sdf.format(Date(millis)))
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(label, color = Color(0xFF99A9A5), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold),
            singleLine = true,
            readOnly = true,
            cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(FormField)
                        .border(1.dp, Color(0xFF42526F), RoundedCornerShape(13.dp))
                        .clickable { showDatePicker = true }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CalendarIcon(Modifier.size(19.dp))
                        Box(modifier = Modifier.padding(start = 10.dp)) {
                            if (value.isEmpty()) {
                                Text("DD/MM/YYYY", color = Color(0xFFE5ECE9), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            } else {
                                innerTextField()
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun ChipRow(labels: List<String>, selectedLabel: String, onLabelSelected: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        labels.forEach { label ->
            val isSelected = label == selectedLabel
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (isSelected) FormAccent else Color(0xFF1A2231))
                    .border(1.dp, if (isSelected) Color(0xFFDFFF6C) else Color(0xFF34405A), RoundedCornerShape(18.dp))
                    .clickable { onLabelSelected(label) }
                    .padding(horizontal = 15.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(label, color = if (isSelected) Color(0xFF111604) else Color(0xFFBBC7C4), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun BallChoice(label: String, imageRes: Int, selected: Boolean, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color(0xFF223018), Color(0xFF080D12))))
                .border(if (selected) 2.dp else 1.dp, if (selected) FormAccent else Color(0xFF463629), CircleShape)
                .padding(7.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = "$label ball",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
        Text(label, color = if (selected) FormAccent else Color(0xFFB9C3C0), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 7.dp))
    }
}

@Composable
private fun TogglePanel(title: String, subtitle: String, active: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(7.dp, RoundedCornerShape(16.dp), clip = false)
            .clip(RoundedCornerShape(16.dp))
            .background(if (active) Color(0xFF101A14) else FormPanel)
            .border(1.dp, if (active) Color(0x994F6B1F) else FormStroke, RoundedCornerShape(16.dp))
            .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (active) FormAccent else Color.Transparent)
                .border(1.dp, if (active) FormAccent else Color(0xFF3B455B), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (active) CheckMark(Modifier.size(15.dp), Color(0xFF111604))
        }
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
            Text(subtitle, color = FormMuted, fontSize = 12.sp, lineHeight = 16.sp, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun NextButton(isLoading: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .shadow(12.dp, RoundedCornerShape(18.dp), clip = false)
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.horizontalGradient(listOf(FormAccent, Color(0xFFDFFF6C), FormWarm)))
            .clickable(enabled = !isLoading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color(0xFF111604),
                strokeWidth = 2.dp
            )
        } else {
            Text("Next", color = Color(0xFF111604), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun RequiredTitle(label: String, compact: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Color.White, fontSize = if (compact) 12.sp else 16.sp, fontWeight = FontWeight.ExtraBold)
        Text("*", color = FormAccent, fontSize = if (compact) 12.sp else 16.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun SectionSmallLabel(text: String) {
    Text(text, color = Color(0xFF99A9A5), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
}

@Composable
private fun AddBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(58.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(FormAccent.copy(alpha = 0.22f), Color(0xFF071109))
                )
            )
            .border(2.dp, FormAccent.copy(alpha = 0.85f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("+", color = FormAccent, fontSize = 23.sp, lineHeight = 23.sp, fontWeight = FontWeight.ExtraBold)
            Text("LOGO", color = FormAccent, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun CheckPill(modifier: Modifier = Modifier, accent: Color) {
    Box(
        modifier = modifier
            .size(27.dp)
            .clip(CircleShape)
            .background(accent),
        contentAlignment = Alignment.Center
    ) {
        CheckMark(Modifier.size(15.dp), Color(0xFF111604))
    }
}

@Composable
private fun CheckMark(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.18f, size.height * 0.54f), Offset(size.width * 0.42f, size.height * 0.76f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.42f, size.height * 0.76f), Offset(size.width * 0.84f, size.height * 0.24f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}

@Composable
private fun CameraIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        drawRoundRect(tint, topLeft = Offset(size.width * 0.18f, size.height * 0.34f), size = androidx.compose.ui.geometry.Size(size.width * 0.64f, size.height * 0.42f), style = stroke)
        drawCircle(tint, radius = size.minDimension * 0.13f, center = center, style = stroke)
        drawLine(tint, Offset(size.width * 0.37f, size.height * 0.34f), Offset(size.width * 0.43f, size.height * 0.23f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.43f, size.height * 0.23f), Offset(size.width * 0.58f, size.height * 0.23f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}

@Composable
private fun CalendarIcon(modifier: Modifier) {
    Canvas(modifier) {
        val tint = FormAccent
        val stroke = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round)
        drawRoundRect(tint, topLeft = Offset(size.width * 0.16f, size.height * 0.22f), size = androidx.compose.ui.geometry.Size(size.width * 0.68f, size.height * 0.62f), style = stroke)
        drawLine(tint, Offset(size.width * 0.16f, size.height * 0.42f), Offset(size.width * 0.84f, size.height * 0.42f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}

@Composable
private fun ArrowIcon(modifier: Modifier, right: Boolean) {
    Canvas(modifier) {
        val tint = Color(0xFFC7D2CF)
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        val path = Path().apply {
            if (right) {
                moveTo(size.width * 0.35f, size.height * 0.25f)
                lineTo(size.width * 0.62f, size.height * 0.5f)
                lineTo(size.width * 0.35f, size.height * 0.75f)
            } else {
                moveTo(size.width * 0.65f, size.height * 0.25f)
                lineTo(size.width * 0.38f, size.height * 0.5f)
                lineTo(size.width * 0.65f, size.height * 0.75f)
            }
        }
        drawPath(path, tint, style = stroke)
    }
}
