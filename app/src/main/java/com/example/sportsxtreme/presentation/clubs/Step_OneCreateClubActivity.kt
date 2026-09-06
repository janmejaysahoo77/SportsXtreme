package com.example.sportsxtreme.presentation.clubs

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CorporateFare
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.SportsCricket
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.sportsxtreme.R

class Step_OneCreateClubActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            StepOneTheme {
                CreateClubStepOneScreen(
                    onBack = { finish() },
                    onContinue = {
                        startActivity(Intent(this, Step_TwoCreateClubActivity::class.java))
                    }
                )
            }
        }
    }
}

// Unified Design System Colors (matching ClubLandingActivity)
private val ColorBg = Color(5, 7, 8)
private val ColorSurface = Color.DarkGray.copy(alpha = 0.1f)
private val ColorLime = Color(190, 255, 24)
private val ColorTextPrimary = Color.White
private val ColorTextSecondary = Color(162, 169, 164)
private val ColorCardBorder = Color(45, 51, 46)

@Composable
fun StepOneTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = ColorBg,
            surface = ColorSurface,
            primary = ColorLime,
            onBackground = ColorTextPrimary,
            onSurface = ColorTextPrimary
        ),
        content = content
    )
}

@Composable
fun CreateClubStepOneScreen(onBack: () -> Unit, onContinue: () -> Unit) {
    var clubName by remember { mutableStateOf("") }
    var establishedYear by remember { mutableStateOf("2024") }
    var selectedType by remember { mutableStateOf("Cricket Club") }
    var aboutClub by remember { mutableStateOf("") }

    Scaffold(
        containerColor = ColorBg,
        topBar = { StepOneTopBar(onBack) },
        bottomBar = { StepOneBottomBar(onContinue) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            StepIndicator(currentIndicatorStep = 1)
            Spacer(modifier = Modifier.height(32.dp))

            // Hero Section
            HeroRegistrationSection()

            Spacer(modifier = Modifier.height(32.dp))

            // Image Upload Section
            ImageUploadSection()

            Spacer(modifier = Modifier.height(32.dp))

            // Form Fields
            ClubFormSection(
                clubName = clubName,
                onClubNameChange = { clubName = it },
                selectedType = selectedType,
                onTypeSelect = { selectedType = it },
                establishedYear = establishedYear,
                onYearChange = { establishedYear = it },
                aboutClub = aboutClub,
                onAboutChange = { aboutClub = it }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Pro Tip
            StepOneProTip()

            Spacer(modifier = Modifier.height(32.dp))

            // Profile Strength
            ProfileStrengthSection()

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun StepOneTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = ColorTextPrimary
            )
        }
        Text(
            text = stringResource(R.string.create_club_header),
            color = ColorTextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.save_draft),
            color = ColorTextSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { /* Save Draft Action */ }
        )
    }
}

@Composable
fun StepIndicator(currentIndicatorStep: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.step_1_of_4),
            color = ColorTextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(4) { index ->
                val step = index + 1
                val isActive = step <= currentIndicatorStep
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(if (isActive) ColorLime else ColorCardBorder),
                    contentAlignment = Alignment.Center
                ) {
                    if (isActive && step == currentIndicatorStep) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ColorLime)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HeroRegistrationSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, ColorCardBorder),
        colors = CardDefaults.cardColors(containerColor = ColorSurface)
    ) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)) {
            Image(
                painter = painterResource(R.drawable.stadium),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.5f)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(ColorLime.copy(alpha = 0.2f))
                        .border(1.dp, ColorLime, RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Bolt,
                            contentDescription = null, tint = ColorLime,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.club_registration_badge),
                            color = ColorTextPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.hero_title_lets_build),
                    color = ColorTextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = stringResource(R.string.hero_title_cricket_club),
                    color = ColorLime,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.hero_body_step1),
                    color = ColorTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun ImageUploadSection() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        // Logo Upload
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .border(1.dp, ColorCardBorder, CircleShape)
                    .background(ColorSurface),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.CameraAlt,
                        contentDescription = null, tint = ColorLime,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(ColorLime)
                    .border(2.dp, ColorBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = null, tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Cover Photo Upload
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(18.dp))
                .border(1.dp, ColorCardBorder, RoundedCornerShape(18.dp))
                .background(ColorSurface),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Outlined.CameraAlt,
                    contentDescription = null, tint = ColorTextSecondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.upload_cover_photo),
                    color = ColorTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.cover_photo_recommendation),
                    color = ColorTextSecondary,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun ClubFormSection(
    clubName: String,
    onClubNameChange: (String) -> Unit,
    selectedType: String,
    onTypeSelect: (String) -> Unit,
    establishedYear: String,
    onYearChange: (String) -> Unit,
    aboutClub: String,
    onAboutChange: (String) -> Unit
) {
    Column {
        // Club Name
        FormInputField(
            label = stringResource(R.string.club_name_label),
            value = clubName,
            onValueChange = onClubNameChange,
            placeholder = stringResource(R.string.club_name_placeholder),
            icon = Icons.Outlined.Shield
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Club Type
        Text(
            text = stringResource(R.string.club_type_label),
            color = ColorTextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                ClubTypeItem(
                    label = stringResource(R.string.type_cricket_club),
                    icon = Icons.Outlined.SportsCricket,
                    isSelected = selectedType == "Cricket Club",
                    onClick = { onTypeSelect("Cricket Club") }
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                ClubTypeItem(
                    label = stringResource(R.string.type_academy),
                    icon = Icons.Outlined.School,
                    isSelected = selectedType == "Academy",
                    onClick = { onTypeSelect("Academy") }
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                ClubTypeItem(
                    label = stringResource(R.string.type_corporate),
                    icon = Icons.Outlined.CorporateFare,
                    isSelected = selectedType == "Corporate",
                    onClick = { onTypeSelect("Corporate") }
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                ClubTypeItem(
                    label = stringResource(R.string.type_social_group),
                    icon = Icons.Outlined.Groups,
                    isSelected = selectedType == "Social Group",
                    onClick = { onTypeSelect("Social Group") }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Established Year
        FormInputField(
            label = stringResource(R.string.established_year_label),
            value = establishedYear,
            onValueChange = onYearChange,
            icon = Icons.Default.DateRange
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Privacy
        Text(
            text = stringResource(R.string.privacy_label),
            color = ColorTextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(ColorSurface)
                .border(1.dp, ColorCardBorder, RoundedCornerShape(18.dp))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.privacy_public),
                color = ColorTextPrimary,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = ColorTextSecondary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // About Club
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.about_club_label),
                color = ColorTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = stringResource(R.string.char_count_300),
                color = ColorTextSecondary,
                fontSize = 10.sp
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(ColorSurface)
                .border(1.dp, ColorCardBorder, RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            if (aboutClub.isEmpty()) {
                Text(
                    text = stringResource(R.string.about_club_placeholder),
                    color = ColorTextSecondary,
                    fontSize = 14.sp
                )
            }
            BasicTextField(
                value = aboutClub,
                onValueChange = onAboutChange,
                textStyle = TextStyle(color = ColorTextPrimary, fontSize = 14.sp),
                cursorBrush = SolidColor(ColorLime),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun FormInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    icon: ImageVector
) {
    Column {
        Text(text = label, color = ColorTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(ColorSurface)
                .border(1.dp, ColorCardBorder, RoundedCornerShape(18.dp))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = ColorTextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(text = placeholder, color = ColorTextSecondary, fontSize = 14.sp)
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(color = ColorTextPrimary, fontSize = 16.sp),
                    cursorBrush = SolidColor(ColorLime),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun ClubTypeItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (isSelected) ColorLime.copy(alpha = 0.05f) else ColorSurface)
            .border(1.dp, if (isSelected) ColorLime else ColorCardBorder, RoundedCornerShape(18.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isSelected) ColorLime else ColorTextSecondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                color = if (isSelected) ColorTextPrimary else ColorTextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StepOneProTip() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(33, 150, 243, 255).copy(alpha = 0.15f))
            .border(1.dp, Color(33, 150, 243, 255).copy(alpha = 0.4f), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                Icons.Outlined.Lightbulb,
                contentDescription = null,
                tint = Color(33, 150, 243, 255),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(R.string.pro_tip_title),
                    color = Color(33, 150, 243, 255),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.pro_tip_body),
                    color = ColorTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun ProfileStrengthSection() {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.RemoveRedEye,
                contentDescription = null,
                tint = ColorTextPrimary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.profile_strength_label),
                color = ColorTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(ColorCardBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.3f)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(ColorLime)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.profile_strength_hint),
            color = ColorTextSecondary,
            fontSize = 10.sp
        )
    }
}

@Composable
fun StepOneBottomBar(onContinue: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(20.dp)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(50.dp)
                .shadow(
                    elevation = 6.dp, shape = RoundedCornerShape(16.dp),
                    clip = true, spotColor = ColorLime, ambientColor = ColorLime
                )
                .clip(RoundedCornerShape(16.dp))
                .background(ColorLime)
                .clickable { onContinue() },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.btn_continue),
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W800
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
