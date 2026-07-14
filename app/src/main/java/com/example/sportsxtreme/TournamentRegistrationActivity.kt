package com.example.sportsxtreme

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
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
import kotlin.math.PI
import kotlin.math.sin

class TournamentRegistrationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        setContent {
            TournamentRegistrationScreen(
                onBack = { finish() },
                onNext = {
                    startActivity(Intent(this, TournamentRequirementsActivity::class.java))
                }
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
private fun TournamentRegistrationScreen(onBack: () -> Unit, onNext: () -> Unit) {
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
            TournamentModePicker()
            TournamentBasicFields()
            TimelineCategorySection()
            BallFormatSection()
            SettingsSection()
            NextButton(onClick = onNext)
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
    val pulse = rememberPremiumPulse()
    val glow = premiumGlowAlpha(pulse)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(156.dp)
            .shadow(14.dp, RoundedCornerShape(18.dp), clip = false)
            .drawBehind {
                drawCircle(FormAccent.copy(alpha = glow * 0.34f), radius = size.width * 0.34f, center = Offset(size.width * 0.12f, size.height * 1.05f))
                drawCircle(FormCyan.copy(alpha = glow * 0.22f), radius = size.width * 0.25f, center = Offset(size.width * 0.92f, size.height * 0.08f))
            }
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF14210B), Color(0xFF0B1A22), Color(0xFF101421))
                )
            )
            .border(1.2.dp, FormAccent.copy(alpha = 0.42f + glow * 0.32f), RoundedCornerShape(18.dp))
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
private fun TournamentModePicker() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Choose event type", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ModeCard(
                title = "Tournament",
                subtitle = "Knockout or league",
                imageRes = R.drawable.tournamentlogo,
                selected = true,
                accent = FormAccent,
                modifier = Modifier.weight(1f)
            )
            ModeCard(
                title = "Series",
                subtitle = "Friendly matches",
                imageRes = R.drawable.freindlymatch,
                selected = false,
                accent = FormCyan,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ModeCard(title: String, subtitle: String, imageRes: Int, selected: Boolean, accent: Color, modifier: Modifier) {
    val pulse = rememberPremiumPulse(if (selected) 2400f else 3400f)
    val glow = if (selected) premiumGlowAlpha(pulse) else 0f
    Box(
        modifier = modifier
            .height(154.dp)
            .shadow(if (selected) 12.dp else 4.dp, RoundedCornerShape(18.dp), clip = false)
            .drawBehind {
                if (selected) {
                    drawCircle(accent.copy(alpha = glow * 0.28f), radius = size.width * 0.48f, center = Offset(size.width * 0.5f, size.height * 0.18f))
                }
            }
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (selected) {
                    Brush.linearGradient(listOf(Color(0xFF17260D), Color(0xFF0B1714)))
                } else {
                    Brush.linearGradient(listOf(Color(0xFF101928), Color(0xFF0A101A)))
                }
            )
            .border(1.7.dp, if (selected) accent.copy(alpha = 0.76f + glow * 0.24f) else FormStroke, RoundedCornerShape(18.dp))
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
private fun TournamentBasicFields() {
    FormSection(title = "Tournament details") {
        LabeledInput(label = "Tournament/Series Name", placeholder = "Enter name...")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            LabeledInput(label = "City", placeholder = "Location", modifier = Modifier.weight(1f))
            LabeledInput(label = "Ground", placeholder = "Stadium", modifier = Modifier.weight(1f))
        }
        LabeledInput(label = "Organizer Name", placeholder = "Person or Club")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            LabeledInput(label = "Phone", placeholder = "+1...", modifier = Modifier.weight(1f))
            LabeledInput(label = "Email", placeholder = "example@email.com", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun TimelineCategorySection() {
    FormSection(title = "Timeline & Category") {
        DateBox("START DATE", "DD/MM/YYYY")
    }
}

@Composable
private fun BallFormatSection() {
    FormSection(title = "Ball & Format") {
        SectionSmallLabel("BALL TYPE")
        Row(horizontalArrangement = Arrangement.spacedBy(17.dp), verticalAlignment = Alignment.CenterVertically) {
            BallChoice("Tennis", R.drawable.tennisball, true)
            BallChoice("Leather", R.drawable.leatherball, false)
            BallChoice("Other", R.drawable.otherball, false)
        }
        SectionSmallLabel("MATCH FORM")
        ChipRow(listOf("Limited Overs", "Test", "T20", "ODI", "T10", "Box Cricket"), selectedIndex = 0)
    }
}

@Composable
private fun SettingsSection() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TogglePanel(
            title = "Looking for more teams to join?",
            subtitle = "Teams can discover and request entry into your tournament.",
            active = true
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
private fun LabeledInput(label: String, placeholder: String, modifier: Modifier = Modifier) {
    var value by remember { mutableStateOf("") }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(7.dp)) {
        RequiredTitle(label, compact = true)
        BasicTextField(
            value = value,
            onValueChange = { value = it },
            textStyle = TextStyle(color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
            singleLine = true,
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
                    if (value.isEmpty()) {
                        Text(placeholder, color = Color(0xFF768784), fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
private fun DateBox(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(label, color = Color(0xFF99A9A5), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(FormField)
                .border(1.dp, Color(0xFF42526F), RoundedCornerShape(13.dp))
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CalendarIcon(Modifier.size(19.dp))
                Text(value, color = Color(0xFFE5ECE9), fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 10.dp))
            }
        }
    }
}

@Composable
private fun ChipRow(labels: List<String>, selectedIndex: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        labels.forEachIndexed { index, label ->
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (index == selectedIndex) FormAccent else Color(0xFF1A2231))
                    .border(1.dp, if (index == selectedIndex) Color(0xFFDFFF6C) else Color(0xFF34405A), RoundedCornerShape(18.dp))
                    .padding(horizontal = 15.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(label, color = if (index == selectedIndex) Color(0xFF111604) else Color(0xFFBBC7C4), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun BallChoice(label: String, imageRes: Int, selected: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
private fun TogglePanel(title: String, subtitle: String, active: Boolean) {
    Row(
        modifier = Modifier
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
private fun NextButton(onClick: () -> Unit) {
    val pulse = rememberPremiumPulse(2200f)
    val shineX = 0.1f + pulse * 1.15f
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .shadow(12.dp, RoundedCornerShape(18.dp), clip = false)
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.horizontalGradient(listOf(FormAccent, Color(0xFFDFFF6C), FormWarm)))
            .drawBehind {
                val startX = size.width * (shineX - 0.24f)
                val endX = size.width * shineX
                drawLine(
                    color = Color.White.copy(alpha = 0.32f),
                    start = Offset(startX, 0f),
                    end = Offset(endX, size.height),
                    strokeWidth = 26.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text("Next", color = Color(0xFF111604), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
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
    val pulse = rememberPremiumPulse(2300f)
    val glow = premiumGlowAlpha(pulse)
    Box(
        modifier = modifier
            .size(58.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(FormAccent.copy(alpha = 0.14f + glow * 0.18f), Color(0xFF071109))
                )
            )
            .border(2.dp, FormAccent.copy(alpha = 0.72f + glow * 0.28f), CircleShape),
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

@Composable
private fun rememberPremiumPulse(durationMillis: Float = 2800f): Float {
    var phase by remember { mutableStateOf(0f) }
    LaunchedEffect(durationMillis) {
        val start = withFrameNanos { it }
        while (true) {
            withFrameNanos { now ->
                val elapsed = (now - start) / 1_000_000f
                phase = (elapsed % durationMillis) / durationMillis
            }
        }
    }
    return phase
}

private fun premiumGlowAlpha(phase: Float): Float {
    return (0.45f + 0.35f * sin((phase * 2f * PI).toFloat())).coerceIn(0.12f, 0.82f)
}
