package com.example.sportsxtreme.presentation.clubs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.sportsxtreme.R

class ClubSubmissionSuccessActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            SubmissionSuccessTheme {
                ClubSubmissionSuccessScreen(
                    onBackToDashboard = {
                        finish()
                    }
                )
            }
        }
    }
}

private val ColorBg = Color(5, 7, 8)
private val ColorSurface = Color(18, 20, 21)
private val ColorLime = Color(190, 255, 24)
private val ColorTextPrimary = Color.White
private val ColorTextSecondary = Color(162, 169, 164)
private val ColorBorder = Color(45, 51, 46)

@Composable
fun SubmissionSuccessTheme(content: @Composable () -> Unit) {
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
fun ClubSubmissionSuccessScreen(onBackToDashboard: () -> Unit) {
    Scaffold(
        containerColor = ColorBg,
        bottomBar = {
            Box(

                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(20.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ColorSurface)
                    .border(1.dp, ColorBorder, RoundedCornerShape(12.dp))
                    .clickable { onBackToDashboard() }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center

            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Home,
                        contentDescription = null,
                        tint = ColorLime,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Back to Dashboard",
                        color = ColorTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Logo Placeholder
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.White.copy(alpha = 0.5f), ColorSurface)
                                )

                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.appicon2),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "SportsXtreme",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Surface(
                    color = ColorSurface,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, ColorBorder)
                ) {
                    Text(
                        text = "Step 4 of 4",
                        color = ColorTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            // Stepper
            ProfessionalStepper()

            Spacer(modifier = Modifier.height(30.dp))

            // Hero Section
            HeroSection()

            Spacer(modifier = Modifier.height(30.dp))

            // Status Card
            SubmissionStatusCard()

            Spacer(modifier = Modifier.height(40.dp))

            // Flow Section
            WhatHappensNextSection()

            Spacer(modifier = Modifier.height(40.dp))

            // Action Cards
            TipsCard()
            Spacer(modifier = Modifier.height(16.dp))
            HelpCard()

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun ProfessionalStepper() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 30.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(ColorLime),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check, // Using shields asset
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(14.dp)
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(2.dp)
                    .background(ColorLime)
            )
        }
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .border(2.dp, ColorLime, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("4", color = ColorLime, fontSize = 12.sp, fontWeight = FontWeight.Black)
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        val labels = listOf("Basic Info", "Contact & Location", "Club Location", "Proof of Club")
        labels.forEachIndexed { index, label ->
            Text(
                text = label,
                color = if (index == 3) ColorLime else ColorTextSecondary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun HeroSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {


            Box(
                modifier = Modifier
                    .size(160.dp)
                    .shadow(50.dp, CircleShape, spotColor = ColorLime, ambientColor = ColorLime)
            )
            Image(
                painter = painterResource(id = R.drawable.shields),
                contentDescription = null,
                modifier = Modifier.size(120.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Verification ",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Submitted!",
                color = ColorLime,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Thank you! Your club details and documents\nhave been submitted successfully.\nOur team will review your submission.",
            color = ColorTextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

@Composable
fun SubmissionStatusCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, ColorBorder),
        colors = CardDefaults.cardColors(containerColor = ColorSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Submission Status",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.todo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.width(2.dp))

                Column(modifier = Modifier) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Under Review",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Box(
                            modifier = Modifier
                                .border(1.dp, Color(0xFFEAB308).copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                            ,
                        ) {
                            Text(
                                text = "Pending",
                                color = Color(0xFFEAB308),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(4.dp).align(Alignment.Center)
                            )
                        }
                    }
                    Text(
                        text = "You will be notified once the\nverification is complete.",
                        color = ColorTextSecondary,
                        fontSize = 10.sp,
                        lineHeight = 18.sp
                    )
                }
                Spacer(
                    modifier = Modifier.width(6.dp)
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(60.dp)
                        .background(ColorBorder.copy(alpha = 0.5f))
                )
                Spacer(
                    modifier = Modifier.width(6.dp)
                )

                Column {
                    Text(text = "Estimated Time", color = ColorTextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(5.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.timer),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "24 – 48 hours",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WhatHappensNextSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "What happens next?",
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(35.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Box(modifier = Modifier.weight(1f)) {
                FlowItem(
                    icon = R.drawable.todo_1,
                    step = "1",
                    label = "Review",
                    desc = "Our team will review\nyour documents and\nclub details."
                )
            }

            Box(
                modifier = Modifier
                    .weight(0.5f)
                    .padding(top = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                DashedArrow()
            }

            Box(modifier = Modifier.weight(1f)) {
                FlowItem(
                    icon = R.drawable.mesage,
                    step = "2",
                    label = "Notification",
                    desc = "You will receive an email\nand in-app notification\nonce reviewed."
                )
            }

            Box(
                modifier = Modifier
                    .weight(0.5f)
                    .padding(top = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                DashedArrow()
            }

            Box(modifier = Modifier.weight(1f)) {
                FlowItem(
                    icon = R.drawable.shields,
                    step = "3",
                    desc = "Once approved, your club\nwill be verified and you can\naccess all premium features.",
                    label = "Verification ✓"
                )
            }
        }
    }
}

@Composable
fun DashedArrow() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
    ) {
        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
        val strokeWidth = 1.5.dp.toPx()

        drawLine(
            color = ColorLime.copy(alpha = 0.4f),
            start = Offset(0f, size.height / 2),
            end = androidx.compose.ui.geometry.Offset(size.width, size.height / 2),
            strokeWidth = strokeWidth,
            pathEffect = pathEffect
        )

        // Small arrow head
        val arrowSize = 6.dp.toPx()
        val path = Path().apply {
            moveTo(size.width, size.height / 2)
            lineTo(size.width - arrowSize, size.height / 2 - arrowSize / 1.5f)
            lineTo(size.width - arrowSize, size.height / 2 + arrowSize / 1.5f)
            close()
        }
        drawPath(path, ColorLime.copy(alpha = 0.4f))
    }
}

@Composable
fun FlowItem(icon: Int, step: String, label: String, desc: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.BottomCenter) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(ColorSurface)
                    .border(1.dp, ColorBorder.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    modifier = Modifier.size(30.dp)
                )
            }
            Surface(
                color = ColorLime,
                shape = CircleShape,
                modifier = Modifier.offset(y = 8.dp).size(20.dp)
            ) {
                Text(
                    text = step,
                    color = Color.Black,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(bottom = 3.dp, start = 6.dp).align(Alignment.Center)
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

    }
}

@Composable
fun TipsCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ColorSurface),
        border = BorderStroke(1.dp, ColorBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                painter = painterResource(id = R.drawable.bulbs),
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Tips",
                    color = ColorLime,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "You can track the status of your verification in the My Club section.",
                    color = ColorTextSecondary,
                    fontSize = 8.sp
                )
            }
            Surface(
                color = ColorLime,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.clickable { }
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Go to My Club",
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HelpCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ColorSurface),
        border = BorderStroke(1.dp, ColorBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.headphone),
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Need Help?",
                    color = ColorLime,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "If you have any questions, feel free to reach out to our team.",
                    color = ColorTextSecondary,
                    fontSize = 8.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Surface(
                color = Color.Transparent,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, ColorLime),
                modifier = Modifier.clickable { }
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.headphone),
                        contentDescription = null,
                        tint = ColorLime,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Contact Support",
                        color = ColorLime,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
