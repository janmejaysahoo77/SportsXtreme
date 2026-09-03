package com.example.sportsxtreme.presentation.phoneAuth

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.sportsxtreme.R
import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.data.di.AuthDependencies
import com.example.sportsxtreme.presentation.auth.MainActivity
import com.example.sportsxtreme.presentation.components.AuthBackgroundView
import kotlinx.coroutines.launch

@Composable
fun PhoneAuth() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val authViewModel = remember { AuthDependencies.authViewModel() }

    var selectedCountry by remember { mutableStateOf(Country.DEFAULT) }
    var phoneNumber by remember { mutableStateOf("") }
    var isCountryPickerOpen by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isInputFocused by remember { mutableStateOf(false) }

    val primaryFixed = Color(0xFFC1FF00)
    val onPrimary = Color(0xFF182200)
    val splashTitleBlue = Color(0xFF007FFF)
    val errorColor = Color(0xFFFF7070)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        SportsXtremeBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            LogoHeader()

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "PHONE AUTH",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                style = TextStyle(
                    shadow = Shadow(
                        color = splashTitleBlue,
                        offset = Offset(2f, 2f),
                        blurRadius = 4f
                    )
                )
            )

            Text(
                text = "ENTER YOUR PHONE NUMBER TO CONTINUE",
                color = Color(0xFFDAE5D2),
                fontSize = 11.5.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
                textAlign = TextAlign.Center
            )

            // Input Field Label & Limit indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PHONE NUMBER",
                    color = primaryFixed,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    letterSpacing = 0.5.sp
                )

                Text(
                    text = "${phoneNumber.length} / ${selectedCountry.digitCount} DIGITS",
                    color = if (phoneNumber.length == selectedCountry.digitCount) primaryFixed else Color(0xFF88998D),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Phone Input Combined Row (Country Code Selector + Phone Field)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(
                        color = if (isInputFocused) Color(0xDE070D0D) else Color(0xBA040707),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = if (errorMessage != null) errorColor else if (isInputFocused) primaryFixed else Color(0x3AFFFFFF),
                        shape = RoundedCornerShape(8.dp)
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Country Code Selector Button
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                        .clickable(enabled = !isLoading) {
                            isCountryPickerOpen = true
                        }
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedCountry.flagEmoji,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(end = 6.dp)
                    )

                    Text(
                        text = selectedCountry.dialCode,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic
                    )

                    Text(
                        text = " ▾",
                        color = primaryFixed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(28.dp)
                        .background(Color(0x33FFFFFF))
                )

                // Actual Phone Input Field
                BasicTextField(
                    value = phoneNumber,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }
                        if (digits.length <= selectedCountry.digitCount) {
                            phoneNumber = digits
                            errorMessage = null
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .onFocusChanged { isInputFocused = it.isFocused }
                        .padding(horizontal = 12.dp),
                    singleLine = true,
                    enabled = !isLoading,
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic
                    ),
                    cursorBrush = SolidColor(primaryFixed),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (phoneNumber.isEmpty()) {
                                Text(
                                    text = selectedCountry.placeholder,
                                    color = Color(0xFF5A6668),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }

            // Error Message Display
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = errorMessage.orEmpty(),
                    color = errorColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    textAlign = TextAlign.Start
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Continue Button
            Button(
                onClick = {
                    if (isLoading) return@Button

                    val digits = phoneNumber.filter { it.isDigit() }
                    if (digits.length != selectedCountry.digitCount) {
                        errorMessage = "Please enter a valid ${selectedCountry.digitCount}-digit phone number"
                        return@Button
                    }

                    errorMessage = null
                    val fullPhoneNumber = "${selectedCountry.dialCode}$digits"
                    val activity = context as? Activity
                    if (activity != null) {
                        AuthDependencies.bindPhoneAuthActivity(activity)
                    }

                    isLoading = true
                    coroutineScope.launch {
                        when (val result = authViewModel.sendPhoneOtp(fullPhoneNumber)) {
                            is Resource.Success -> {
                                (context as? MainActivity)?.showOtpVerificationScreen(
                                    result.data?.phoneNumber ?: fullPhoneNumber
                                )
                            }
                            is Resource.Error -> errorMessage = result.message ?: "Could not send OTP"
                            is Resource.Loading -> Unit
                        }
                        isLoading = false
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryFixed,
                    contentColor = onPrimary,
                    disabledContainerColor = primaryFixed.copy(alpha = 0.6f),
                    disabledContentColor = onPrimary.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(7.dp)
            ) {
                if (isLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "SENDING CODE...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
                            letterSpacing = 0.5.sp
                        )
                    }
                } else {
                    Text(
                        text = "CONTINUE",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Back to Login / Signup prompt
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BACK TO LOGIN",
                    color = splashTitleBlue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable {
                            (context as? MainActivity)?.showLoginScreen()
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )

                Text(
                    text = " • ",
                    color = Color(0xFF637477),
                    fontSize = 11.sp
                )

                Text(
                    text = "BACK TO SIGN UP",
                    color = primaryFixed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable {
                            (context as? MainActivity)?.showSignupScreen()
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Country Picker Dialog
        if (isCountryPickerOpen) {
            CountryPickerDialog(
                selectedCountry = selectedCountry,
                onCountrySelected = { country ->
                    selectedCountry = country
                    if (phoneNumber.length > country.digitCount) {
                        phoneNumber = phoneNumber.take(country.digitCount)
                    }
                    errorMessage = null
                    isCountryPickerOpen = false
                },
                onDismissRequest = {
                    isCountryPickerOpen = false
                }
            )
        }
    }
}

@Composable
fun SportsXtremeBackground(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            AuthBackgroundView(context)
        }
    )
}

@Composable
fun LogoHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = R.drawable.appicon),
            contentDescription = "App Icon",
            modifier = Modifier
                .size(98.dp, 58.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "SPORTS",
                color = Color(0xFFECF3EE),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic
            )
            Text(
                text = "X",
                color = Color(0xFF007FFF),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic
            )
            Text(
                text = "TREME",
                color = Color(0xFFECF3EE),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic
            )
        }
    }
}
