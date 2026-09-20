package com.example.sportsxtreme.presentation.clubs

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.sportsxtreme.R
import com.example.sportsxtreme.presentation.ui.theme.*

class Step_TwoCreateClubActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            StepTwoTheme {
                CreateClubStepTwoScreen(
                    onBack = { finish() },
                    onContinue = {
                        startActivity(Intent(this, Step_ThreeCreateClubActivity::class.java))
                    }
                )
            }
        }
    }
}

// Unified Design System Colors (matching ClubLandingActivity)
private val ColorBg = BlueBackgroundColor
private val ColorSurface = BlueCardColors
private val ColorLime = Color(190, 255, 24)
private val ColorTextPrimary = Color.White
private val ColorTextSecondary = Color(162, 169, 164)
private val ColorCardBorder = Color(45, 51, 46)

@Composable
fun StepTwoTheme(content: @Composable () -> Unit) {
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
fun CreateClubStepTwoScreen(onBack: () -> Unit, onContinue: () -> Unit) {
    var contactName by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("+91 98765 43210") }
    var emailAddress by remember { mutableStateOf("club@example.com") }
    var alternativeContact by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }

    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        containerColor = ColorBg,
        topBar = { StepTwoTopBar(onBack) },
        bottomBar = { StepTwoBottomBar(onContinue) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            StepTwoIndicator(currentStep = 2)
            Spacer(modifier = Modifier.height(32.dp))
            
            // Hero Section
            ContactHeroSection()
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Form Fields
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                ContactInputField(
                    label = stringResource(R.string.contact_person_label),
                    value = contactName,
                    onValueChange = { contactName = it },
                    placeholder = stringResource(R.string.contact_person_placeholder),
                    icon = Icons.Outlined.Person
                )
                
                ContactInputField(
                    label = stringResource(R.string.mobile_number_label),
                    value = mobileNumber,
                    onValueChange = { mobileNumber = it },
                    icon = Icons.Outlined.Phone,
                    showVerify = true,
                    onVerify = {
                        val intent = Intent(context, ClubVerificationActivity::class.java).apply {
                            putExtra("verification_target", mobileNumber)
                            putExtra("is_phone", true)
                        }
                        context.startActivity(intent)
                    }
                )
                
                ContactInputField(
                    label = stringResource(R.string.email_address_label),
                    value = emailAddress,
                    onValueChange = { emailAddress = it },
                    icon = Icons.Outlined.Email,
                    showVerify = true,
                    onVerify = {
                        val intent = Intent(context, ClubVerificationActivity::class.java).apply {
                            putExtra("verification_target", emailAddress)
                            putExtra("is_phone", false)
                        }
                        context.startActivity(intent)
                    }
                )
                
                ContactInputField(
                    label = stringResource(R.string.alt_contact_label),
                    value = alternativeContact,
                    onValueChange = { alternativeContact = it },
                    placeholder = stringResource(R.string.alt_contact_placeholder),
                    icon = Icons.Outlined.Phone
                )
                
                ContactInputField(
                    label = stringResource(R.string.website_label),
                    value = website,
                    onValueChange = { website = it },
                    placeholder = stringResource(R.string.website_placeholder),
                    icon = Icons.Outlined.Info
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Privacy Matters
            PrivacyMattersSection()
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun StepTwoTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ColorTextPrimary)
        }
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.create_club_header),
                color = ColorTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = stringResource(R.string.step_2_of_4),
                color = ColorLime,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
        Spacer(modifier = Modifier.width(48.dp)) // To balance the back button
    }
}

@Composable
fun StepTwoIndicator(currentStep: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(4) { index ->
            val step = index + 1
            val isActive = step <= currentStep
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(if (isActive) ColorLime else ColorCardBorder)
            )
        }
    }
}

@Composable
fun ContactHeroSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, ColorCardBorder),
        colors = CardDefaults.cardColors(containerColor = ColorSurface)
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Column(modifier = Modifier.fillMaxWidth(0.7f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Phone, contentDescription = null, tint = ColorLime, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.club_contact_badge),
                        color = ColorLime,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.hero_title_stay_connected),
                    color = ColorTextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 28.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.hero_body_step2),
                    color = ColorTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
            
            // Envelope background graphic
            Icon(
                Icons.Filled.Email,
                contentDescription = null,
                tint = ColorLime.copy(alpha = 0.1f),
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.CenterEnd)
            )
        }
    }
}

@Composable
fun ContactInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    icon: ImageVector,
    showVerify: Boolean = false,
    onVerify: () -> Unit = {}
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
            Icon(icon, contentDescription = null, tint = ColorTextSecondary, modifier = Modifier.size(20.dp))
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
            if (showVerify) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Gray.copy(alpha = 0.1f))
                        .border(1.dp, Color.Gray.copy(alpha = 0.4f), CircleShape)
                        .clickable { onVerify() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = stringResource(R.string.verify_btn),
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun PrivacyMattersSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(33, 150, 243, 255).copy(alpha = 0.15f))
            .border(1.dp, Color(33, 150, 243, 255).copy(alpha = 0.3f), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(33, 150, 243, 255).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.PrivacyTip, contentDescription = null, tint = Color(33, 150, 243, 255), modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = stringResource(R.string.privacy_matters_title),
                    color = ColorTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.privacy_matters_body),
                    color = ColorTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun StepTwoBottomBar(onContinue: () -> Unit) {
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
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
            }
        }
    }
}
