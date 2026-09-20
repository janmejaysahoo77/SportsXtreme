package com.example.sportsxtreme.presentation.clubs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.sportsxtreme.R
import com.example.sportsxtreme.presentation.ui.theme.*

class ClubVerificationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val target = intent.getStringExtra("verification_target") ?: "club@example.com"
        val isPhone = intent.getBooleanExtra("is_phone", false)
        
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            ClubVerificationTheme {
                ClubVerificationScreen(
                    target = target,
                    isPhone = isPhone,
                    onBack = { finish() }
                )
            }
        }
    }
}

// Reusing design system colors
private val ColorBg = BlueBackgroundColor
private val ColorSurface = BlueCardColors
private val ColorLime = Color(190, 255, 24)
private val ColorTextPrimary = Color.White
private val ColorTextSecondary = Color(162, 169, 164)
private val ColorBorder = Color(45, 51, 46).copy(alpha = 0.5f)
private val ColorBlue = Color(59, 130, 246)

@Composable
fun ClubVerificationTheme(content: @Composable () -> Unit) {
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
fun ClubVerificationScreen(
    target: String,
    isPhone: Boolean,
    onBack: () -> Unit
) {
    var otpValue by remember { mutableStateOf("") }

    Scaffold(
        containerColor = ColorBg,
        topBar = { VerificationTopBar(onBack) },
        bottomBar = { VerificationBottomBar() }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            // Professional Progress Indicator
            VerificationProgressIndicator()
            
            Spacer(modifier = Modifier.height(60.dp))

            // Hero Icon Section
            Box(
                modifier = Modifier
                    .size(110.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background Glow Circle
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(ColorLime.copy(alpha = 0.03f))
                        .border(1.dp, ColorLime.copy(alpha = 0.1f), CircleShape)
                )

                // Inner Circle with Border
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F1112))
                        .border(1.dp, ColorLime.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = null,
                            tint = ColorLime,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = ColorLime,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.verify_identity_title),
                color = ColorTextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(if (isPhone) R.string.enter_code_sent_phone else R.string.enter_code_sent_email),
                color = ColorTextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Text(
                text = target,
                color = ColorTextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // OTP Input Fields
            OtpInputField(
                otpValue = otpValue,
                onValueChange = { if (it.length <= 4) otpValue = it }
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Resend Section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.didnt_receive_code),
                    color = ColorTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "00:59",
                    color = ColorBlue.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.resend_code),
                color = ColorLime,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { /* Resend logic */ }
            )
        }
    }
}

@Composable
fun VerificationTopBar(onBack: () -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = ColorTextPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.verification_header),
                color = ColorTextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Surface(
                color = ColorSurface,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, ColorBorder)
            ) {
                Text(
                    text = "Step 2 of 4",
                    color = ColorTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
        HorizontalDivider(
            thickness = 0.5.dp,
            color = ColorBorder.copy(alpha = 0.3f)
        )
    }
}

@Composable
fun VerificationProgressIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Step 1 - Completed
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(ColorLime)
        )
        Box(
            modifier = Modifier
                .width(50.dp)
                .height(3.dp)
                .background(ColorLime)
        )

        // Step 2 - Current
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(ColorLime.copy(alpha = 0.15f))
                .border(2.5.dp, ColorLime, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(ColorLime)
            )
        }
        
        Box(
            modifier = Modifier
                .width(50.dp)
                .height(2.dp)
                .background(ColorBorder.copy(alpha = 0.3f))
        )

        // Step 3
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(ColorBorder.copy(alpha = 0.5f))
        )
        Box(
            modifier = Modifier
                .width(50.dp)
                .height(2.dp)
                .background(ColorBorder.copy(alpha = 0.3f))
        )

        // Step 4
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(ColorBorder.copy(alpha = 0.5f))
        )
    }
}

@Composable
fun OtpInputField(otpValue: String, onValueChange: (String) -> Unit) {
    BasicTextField(
        value = otpValue,
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        decorationBox = {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                repeat(4) { index ->
                    val char = otpValue.getOrNull(index)?.toString() ?: ""
                    val isFocused = otpValue.length == index
                    
                    Box(
                        modifier = Modifier
                            .size(width = 68.dp, height = 84.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(ColorSurface)
                            .border(
                                width = if (isFocused) 1.5.dp else 1.dp,
                                color = if (isFocused) ColorLime.copy(alpha = 0.8f) else ColorBorder.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(18.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char,
                            style = TextStyle(
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun VerificationBottomBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(24.dp)
    ) {
        Button(
            onClick = { /* Verify action */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(32.dp),
                    spotColor = ColorLime.copy(alpha = 0.5f),
                    ambientColor = ColorLime.copy(alpha = 0.5f)
                ),
            colors = ButtonDefaults.buttonColors(containerColor = ColorLime),
            shape = RoundedCornerShape(32.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.btn_verify_continue),
                    color = Color.Black,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.W900,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
