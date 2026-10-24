package com.example.sportsxtreme.presentation.team

import com.example.sportsxtreme.R
import com.example.sportsxtreme.presentation.ui.theme.*
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
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import android.widget.Toast
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.runtime.saveable.rememberSaveable
import com.google.firebase.functions.FirebaseFunctions
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AddPlayerByMobileNumberActivity : ComponentActivity() {
    @Inject lateinit var functions: FirebaseFunctions
    private var isSubmitting by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        val teamId = intent.getStringExtra(ManagePlayersInsideTeamActivity.EXTRA_TEAM_ID).orEmpty()
        setContent {
            AddPlayerByMobileNumberScreen(isSubmitting = isSubmitting, onBack = { finish() }, onDone = { players -> submitPlayers(teamId, players) })
        }
    }

    private fun submitPlayers(teamId: String, players: List<ManualPlayerInput>) {
        if (isSubmitting) return
        if (teamId.isBlank()) {
            Toast.makeText(this, "Team information is missing. Reopen Add Player and try again.", Toast.LENGTH_LONG).show()
            return
        }
        isSubmitting = true
        functions.getHttpsCallable("addTeamPlayersByMobile")
            .call(mapOf("teamId" to teamId, "players" to players.map { mapOf("name" to it.name, "mobileNumber" to it.mobileNumber) }))
            .addOnSuccessListener { result ->
                val addedCount = ((result.data as? Map<*, *>)?.get("addedCount") as? Number)?.toInt() ?: players.size
                Toast.makeText(this, "Added $addedCount player${if (addedCount == 1) "" else "s"} to the team roster.", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { error ->
                isSubmitting = false
                Toast.makeText(this, error.message ?: "Unable to add players. Please try again.", Toast.LENGTH_LONG).show()
            }
    }
}

private data class ManualPlayerInput(val name: String, val mobileNumber: String)

private val MobileAccent = Color(0xFFC1FF00)
private val MobileBg = XtremeBgBlue
private val MobilePanel = XtremeCardBlue
private val MobileCard = XtremeCardBlue
private val MobileMuted = Color(0xFFB7C0C7)
private val MobileDim = Color(0xFF485061)

@Composable
private fun AddPlayerByMobileNumberScreen(isSubmitting: Boolean, onBack: () -> Unit, onDone: (List<ManualPlayerInput>) -> Unit) {
    var mobileNumber by rememberSaveable { mutableStateOf("") }
    var playerName by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val queuedPlayers = remember { mutableStateListOf<ManualPlayerInput>() }
    val focusManager = LocalFocusManager.current

    fun addCurrentPlayer(): Boolean {
        val mobile = mobileNumber.filter(Char::isDigit)
        val name = playerName.trim().replace(Regex("\\s+"), " ")
        if (name.isBlank()) { errorMessage = "Enter the player's name."; return false }
        if (name.length > 80) { errorMessage = "Player name must be 80 characters or fewer."; return false }
        if (!Regex("[6-9]\\d{9}").matches(mobile)) { errorMessage = "Enter a valid 10-digit Indian mobile number."; return false }
        if (queuedPlayers.any { it.mobileNumber == mobile }) { errorMessage = "This number is already in the list."; return false }
        queuedPlayers.add(ManualPlayerInput(name, mobile))
        mobileNumber = ""
        playerName = ""
        errorMessage = null
        focusManager.clearFocus()
        return true
    }

    fun submitPlayers() {
        val hasCurrentInput = playerName.isNotBlank() || mobileNumber.isNotBlank()
        val players = queuedPlayers.toMutableList()
        if (hasCurrentInput) {
            if (!addCurrentPlayer()) return
            players.add(queuedPlayers.last())
        }
        if (players.isEmpty()) { errorMessage = "Add at least one player before continuing."; return }
        onDone(players)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MobileBg)
            .drawBehind {
                repeat(22) { index ->
                    val x = size.width * ((index * 37 % 100) / 100f)
                    val y = size.height * (0.14f + ((index * 19 % 72) / 100f))
                    drawCircle(Color(0x24465A6D), radius = 1.1.dp.toPx(), center = Offset(x, y))
                }
                drawCircle(Color(0x152C7500), radius = size.width * 0.52f, center = Offset(size.width * 0.48f, size.height * 0.44f))
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            MobileTopBar(onBack)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(34.dp))
                MobileInputCard(mobileNumber, { value -> mobileNumber = value.filter(Char::isDigit).take(10) }, playerName, { playerName = it.take(80) })
                Text(
                    "Adds the player to the roster now. Phone ownership verification will be added later.",
                    color = MobileMuted,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(horizontal = 3.dp).padding(top = 10.dp)
                )
                if (errorMessage != null) {
                    Text(errorMessage.orEmpty(), color = Color(0xFFFF8A80), fontSize = 12.sp, modifier = Modifier.padding(top = 10.dp, start = 4.dp))
                }
                AddAnotherButton(Modifier.padding(top = 24.dp), enabled = !isSubmitting) {
                    addCurrentPlayer()
                }
                Text(
                    "PLAYERS TO ADD (${queuedPlayers.size})",
                    color = MobileDim,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(top = 15.dp, start = 3.dp)
                )
                if (queuedPlayers.isNotEmpty()) {
                    Column(Modifier.fillMaxWidth().padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        queuedPlayers.forEachIndexed { index, player ->
                            Row(
                                Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(MobilePanel).padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${index + 1}", color = MobileAccent, fontWeight = FontWeight.Black, fontSize = 15.sp)
                                Column(Modifier.padding(start = 12.dp).weight(1f)) {
                                    Text(player.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("+91 ${player.mobileNumber}", color = MobileMuted, fontSize = 12.sp)
                                }
                                Text("REMOVE", color = Color(0xFFFF8A80), fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.clickable(enabled = !isSubmitting) { queuedPlayers.removeAt(index) })
                            }
                        }
                    }
                }
                Spacer(Modifier.height(60.dp))
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MobileBg)
                    .padding(horizontal = 26.dp, vertical = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(67.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(MobileAccent)
                        .clickable(enabled = !isSubmitting, onClick = ::submitPlayers),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSubmitting) CircularProgressIndicator(color = Color(0xFF111604), strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                    else Text("D O N E", color = Color(0xFF111604), fontSize = 19.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
                }
            }
        }
    }
}

@Composable
private fun MobileTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(102.dp)
            .background(MobilePanel)
            .padding(start = 27.dp, end = 18.dp, top = 26.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MobileBackArrow(Modifier.size(24.dp).clickable(onClick = onBack), Color.White)
        Text(
            "Add player by mobile number",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.3.sp,
            modifier = Modifier.padding(start = 26.dp).weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MobileInputCard(mobileNumber: String, onMobileChange: (String) -> Unit, playerName: String, onNameChange: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(263.dp)
            .clip(RoundedCornerShape(34.dp))
            .background(MobileCard)
            .border(1.dp, Color(0xFF4D721A), RoundedCornerShape(34.dp))
            .padding(horizontal = 35.dp, vertical = 35.dp)
    ) {
        FieldLabel("MOBILE NUMBER")
        Row(
            modifier = Modifier.padding(top = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("+91", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
            BasicTextField(value = mobileNumber, onValueChange = onMobileChange, modifier = Modifier.padding(start = 13.dp).weight(1f), singleLine = true,
                textStyle = TextStyle(color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black), cursorBrush = SolidColor(MobileAccent), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                decorationBox = { inner -> Box { if (mobileNumber.isEmpty()) Text("Mobile Number", color = MobileDim, fontSize = 20.sp, fontWeight = FontWeight.Bold); inner() } })
        }
        DividerLine(Modifier.padding(top = 14.dp))
        FieldLabel("PLAYER NAME", Modifier.padding(top = 42.dp))
        Row(
            modifier = Modifier.padding(top = 17.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayerIcon(Modifier.size(23.dp), MobileAccent)
            BasicTextField(value = playerName, onValueChange = onNameChange, modifier = Modifier.padding(start = 17.dp).weight(1f), singleLine = true,
                textStyle = TextStyle(color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold), cursorBrush = SolidColor(MobileAccent), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                decorationBox = { inner -> Box { if (playerName.isEmpty()) Text("e.g. John Doe", color = MobileDim, fontSize = 20.sp, fontWeight = FontWeight.Bold); inner() } })
        }
        DividerLine(Modifier.padding(top = 15.dp))
    }
}

@Composable
private fun FieldLabel(text: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text,
            color = MobileMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.1.sp
        )
        Text(
            "*",
            color = MobileAccent,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}

@Composable
private fun DividerLine(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFF34404C))
    )
}

@Composable
private fun AddAnotherButton(modifier: Modifier = Modifier, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp)
            .height(37.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFF4D721A), RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "+ ADD ANOTHER PLAYER",
            color = MobileAccent,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun MobileBackArrow(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.25f, size.height * 0.5f), Offset(size.width * 0.78f, size.height * 0.5f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        val path = Path().apply {
            moveTo(size.width * 0.45f, size.height * 0.25f)
            lineTo(size.width * 0.22f, size.height * 0.5f)
            lineTo(size.width * 0.45f, size.height * 0.75f)
        }
        drawPath(path, tint, style = stroke)
    }
}

@Composable
private fun PlayerIcon(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(tint, radius = size.minDimension * 0.14f, center = Offset(size.width * 0.5f, size.height * 0.27f), style = stroke)
        drawArc(
            color = tint,
            startAngle = 205f,
            sweepAngle = 130f,
            useCenter = false,
            topLeft = Offset(size.width * 0.23f, size.height * 0.5f),
            size = Size(size.width * 0.54f, size.height * 0.34f),
            style = stroke
        )
        drawLine(tint, Offset(size.width * 0.27f, size.height * 0.84f), Offset(size.width * 0.73f, size.height * 0.84f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    }
}
