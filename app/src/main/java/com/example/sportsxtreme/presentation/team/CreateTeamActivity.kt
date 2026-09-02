package com.example.sportsxtreme.presentation.team

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.example.sportsxtreme.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@AndroidEntryPoint
class CreateTeamActivity : ComponentActivity() {
    @Inject lateinit var firebaseAuth: FirebaseAuth
    @Inject lateinit var firestore: FirebaseFirestore
    private var isSavingTeam = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = android.graphics.Color.rgb(2, 11, 18)
        window.navigationBarColor = android.graphics.Color.rgb(2, 11, 18)
        setContent {
            CreateTeamScreen(::finish) { teamName, city, addMyself ->
                saveTeamAndContinue(teamName, city, addMyself)
            }
        }
    }

    private fun saveTeamAndContinue(teamName: String, city: String, addMyself: Boolean) {
        if (isSavingTeam) return
        val userId = firebaseAuth.currentUser?.uid ?: run {
            Toast.makeText(this, "Sign in to create a team", Toast.LENGTH_SHORT).show()
            return
        }
        isSavingTeam = true
        lifecycleScope.launch {
            try {
                val teamRef = firestore.collection("teams").document()
                val members: List<Map<String, Any>> = if (addMyself) {
                    listOf(mapOf("userId" to userId, "role" to "OWNER", "joinedAtEpochMs" to System.currentTimeMillis()))
                } else {
                    emptyList()
                }
                teamRef.set(
                    mapOf(
                        "teamId" to teamRef.id,
                        "teamName" to teamName,
                        "shortName" to teamName.take(3).uppercase(),
                        "city" to city,
                        "cityTown" to city,
                        "ownerUserId" to userId,
                        "memberIds" to if (addMyself) listOf(userId) else emptyList<String>(),
                        "members" to members,
                        "type" to "USER_CREATED",
                        "createdAtEpochMs" to System.currentTimeMillis(),
                        "updatedAtEpochMs" to System.currentTimeMillis()
                    )
                ).await()
                startActivity(
                    Intent(this@CreateTeamActivity, ManagePlayersInsideTeamActivity::class.java)
                        .putExtra(ManagePlayersInsideTeamActivity.EXTRA_TEAM_NAME, teamName)
                        .putExtra(ManagePlayersInsideTeamActivity.EXTRA_TEAM_ID, teamRef.id)
                )
            } catch (error: Exception) {
                Toast.makeText(this@CreateTeamActivity, error.message ?: "Unable to create team", Toast.LENGTH_SHORT).show()
            } finally {
                isSavingTeam = false
            }
        }
    }

    private val background = Color(0xFF020B12)
    private val surface = Color(0xED07131D)
    private val elevatedSurface = Color(0xF20B1822)
    private val accent = Color(0xFFC8FF00)
    private val accentGreen = Color(0xFF9FE000)
    private val brandBlue = Color(0xFF18B9FF)
    private val textPrimary = Color(0xFFF5F7F8)
    private val textSecondary = Color(0xFF8B969D)
    private val border = Color(0xFF26343C)

    @Composable
    private fun CreateTeamScreen(onBack: () -> Unit, onContinue: (String, String, Boolean) -> Unit) {
        var teamName by rememberSaveable { mutableStateOf("") }; var city by rememberSaveable { mutableStateOf("") }
        var mobile by rememberSaveable { mutableStateOf("") }; var captain by rememberSaveable { mutableStateOf("") }
        var addMyself by rememberSaveable { mutableStateOf(true) }
        var showRequiredErrors by rememberSaveable { mutableStateOf(false) }
        val focusManager = LocalFocusManager.current
        Box(Modifier.fillMaxSize().background(background).clickable(
            interactionSource = remember { MutableInteractionSource() }, indication = null
        ) { focusManager.clearFocus(force = true) }) {
            // The stadium belongs to the hero area only; this fade keeps the form calm and readable.
            Box(Modifier.fillMaxWidth().height(342.dp).align(Alignment.TopCenter)) {
                Image(painterResource(R.drawable.create_team_bg), null, Modifier.fillMaxSize().alpha(.42f), contentScale = ContentScale.Crop, alignment = Alignment.TopCenter)
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(background.copy(.42f), background.copy(.30f), background.copy(.98f)))))
                Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(brandBlue.copy(.10f), Color.Transparent), radius = 780f)))
            }
            Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                Header(onBack)
                Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()).padding(bottom = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    ScreenTitle(); LogoPicker()
                    Text("Team Logo", color = textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.padding(top = 10.dp))
                    Text("Upload your team logo", color = textSecondary, fontSize = 11.sp); Spacer(Modifier.height(14.dp))
                    TeamTextField("TEAM NAME *", "Enter your team name", teamName, { if (it.length <= 30) teamName = it }, R.drawable.baseline_check_circle_24, counter = "${teamName.length}/30", error = if (showRequiredErrors && teamName.isBlank()) "Team name is required" else null)
                    TeamTextField("CITY / TOWN *", "Enter city / town", city, { city = it }, R.drawable.baseline_edit_location_24, error = if (showRequiredErrors && city.isBlank()) "City / town is required" else null)
                    TeamTextField("TEAM CAPTAIN / COORDINATOR (OPTIONAL)", "+91   Enter mobile number", mobile, { mobile = it }, R.drawable.baseline_local_phone_24, KeyboardType.Phone)
                    TeamTextField("TEAM CAPTAIN NAME (OPTIONAL)", "Enter captain name", captain, { captain = it }, R.drawable.baseline_person_outline_24)
                    AddMyselfCard(addMyself) { addMyself = it }
                    Spacer(Modifier.height(100.dp))
                }
                CreateButton(enabled = teamName.isNotBlank() && city.isNotBlank()) {
                    if (teamName.isBlank() || city.isBlank()) showRequiredErrors = true else onContinue(teamName.trim(), city.trim(), addMyself)
                }
            }
        }
    }

    @Composable private fun Header(onBack: () -> Unit) = Box(Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 5.dp)) {
        Box(Modifier.size(36.dp).align(Alignment.CenterStart).clip(RoundedCornerShape(9.dp)).background(elevatedSurface).border(1.dp, accent.copy(.55f), RoundedCornerShape(9.dp)).clickable(onClick = onBack), contentAlignment = Alignment.Center) { Icon(painterResource(R.drawable.outline_arrow_back_ios_24), null, tint = accent, modifier = Modifier.size(17.dp)) }
        Row(Modifier.align(Alignment.Center), verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(R.drawable.appicon2), "SportXtreme", Modifier.size(width = 33.dp, height = 24.dp), contentScale = ContentScale.Fit)
            Spacer(Modifier.width(4.dp))
            Text("Sports", color = textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text("Xtreme", color = brandBlue, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
    @Composable private fun ScreenTitle() { Row(Modifier.padding(top = 3.dp), verticalAlignment = Alignment.CenterVertically) { Text("CREATE ", color = brandBlue, fontSize = 23.sp, fontWeight = FontWeight.Black); Text("YOUR TEAM", color = textPrimary, fontSize = 23.sp, fontWeight = FontWeight.Black) }; Text("Let's Build Your Xtreme Team", color = textPrimary.copy(.76f), fontSize = 12.sp, fontStyle = FontStyle.Italic, modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)) }
    @Composable private fun LogoPicker() = Box(Modifier.size(132.dp), contentAlignment = Alignment.Center) {
        Box(Modifier.fillMaxSize().border(1.dp, accent.copy(.58f), CircleShape)); Box(Modifier.size(118.dp).background(elevatedSurface, CircleShape).border(1.dp, accent.copy(.65f), CircleShape), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(painterResource(R.drawable.baseline_camera_alt_24), null, tint = textPrimary, modifier = Modifier.size(34.dp)); Text("ADD LOGO", color = textPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 5.dp)) } }; Box(Modifier.align(Alignment.TopEnd).size(30.dp).background(accent, CircleShape).border(2.dp, background, CircleShape), contentAlignment = Alignment.Center) { Icon(painterResource(R.drawable.baseline_add_24), null, tint = background, modifier = Modifier.size(19.dp)) }
    }
    @Composable
    private fun TeamTextField(label: String, placeholder: String, value: String, onValueChange: (String) -> Unit, icon: Int, keyboardType: KeyboardType = KeyboardType.Text, counter: String? = null, error: String? = null) {
        val focusManager = LocalFocusManager.current
        val focusRequester = remember { FocusRequester() }
        val interactionSource = remember { MutableInteractionSource() }
        val isFocused by interactionSource.collectIsFocusedAsState()
        val shouldFloat = isFocused || value.isNotEmpty()
        val fieldBorder by animateColorAsState(if (error != null) Color(0xFFFF6B6B) else if (isFocused) accent.copy(.86f) else border, tween(110), label = "field-border")
        val labelColor by animateColorAsState(if (error != null) Color(0xFFFF8A8A) else if (isFocused) accent else textSecondary, tween(110), label = "field-label-color")
        val labelOffset by animateDpAsState(if (shouldFloat) 0.dp else 24.dp, tween(120), label = "field-label-position")
        Box(Modifier.fillMaxWidth().padding(top = 10.dp).height(70.dp)) {
            Row(
                Modifier.fillMaxWidth().height(64.dp).align(Alignment.BottomCenter)
                    .background(surface, RoundedCornerShape(10.dp)).border(1.dp, fieldBorder, RoundedCornerShape(10.dp))
                    .clickable { focusRequester.requestFocus() }.padding(horizontal = 13.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(painterResource(icon), null, tint = accent, modifier = Modifier.size(20.dp))
                Box(Modifier.padding(start = 12.dp).weight(1f).height(46.dp)) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth().align(Alignment.CenterStart).focusRequester(focusRequester),
                    singleLine = true,
                    textStyle = TextStyle(color = textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Normal),
                    cursorBrush = SolidColor(accent),
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Done),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions { focusManager.clearFocus(force = true) },
                    interactionSource = interactionSource,
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (value.isEmpty() && shouldFloat) Text(placeholder, color = textSecondary, fontSize = 14.sp, fontWeight = FontWeight.Normal)
                            innerTextField()
                        }
                    }
                )
                }
                if (counter != null) Text(counter, color = textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Normal, modifier = Modifier.padding(start = 8.dp))
            }
            // Only floating labels receive a surface fill, creating the outline notch.
            Text(label, color = labelColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = .1.sp,
                modifier = Modifier.align(Alignment.TopStart).padding(start = 44.dp).offset(y = labelOffset)
                    .then(if (shouldFloat) Modifier.background(surface) else Modifier).padding(horizontal = 5.dp, vertical = 1.dp))
            error?.let { Text(it, color = Color(0xFFFF8A8A), fontSize = 10.sp, modifier = Modifier.align(Alignment.BottomStart).padding(start = 13.dp)) }
        }
    }
    @Composable private fun AddMyselfCard(checked: Boolean, onCheckedChange: (Boolean) -> Unit) = Row(Modifier.fillMaxWidth().padding(top = 11.dp).background(surface, RoundedCornerShape(10.dp)).border(1.dp, accent.copy(.55f), RoundedCornerShape(10.dp)).padding(10.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(34.dp).background(accent.copy(.13f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) { Icon(painterResource(R.drawable.outline_groups_24), null, tint = accent, modifier = Modifier.size(21.dp)) }; Column(Modifier.padding(start = 10.dp).weight(1f)) { Text("Add yourself in the team", color = textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp); Text("You will be added as a member", color = textSecondary, fontSize = 10.sp) }; Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent, uncheckedThumbColor = textSecondary, uncheckedTrackColor = border)) }
    @Composable private fun CreateButton(enabled: Boolean, onContinue: () -> Unit) = Box(Modifier.fillMaxWidth().padding(bottom = 10.dp, top = 4.dp).height(54.dp).background(if (enabled) Brush.horizontalGradient(listOf(accent, accentGreen)) else Brush.horizontalGradient(listOf(border, border)), RoundedCornerShape(10.dp)).clickable(onClick = onContinue), contentAlignment = Alignment.Center) { Text("Create Team", color = if (enabled) background else textSecondary, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold); Icon(painterResource(R.drawable.outline_arrow_back_ios_24), null, tint = if (enabled) background else textSecondary, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 17.dp).size(20.dp).graphicsLayer(rotationZ = 180f)) }
}
