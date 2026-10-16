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
import android.content.Intent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.functions.FirebaseFunctions

class FinalSquadActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        val matchId = intent.getStringExtra(SelectPlayingTeamsActivity.EXTRA_MATCH_ID).orEmpty()
        val teamSlot = intent.getStringExtra(SelectPlayingTeamsActivity.EXTRA_TEAM_SLOT).orEmpty()
        val selectedTeamId = intent.getStringExtra(SelectPlayingTeamsActivity.EXTRA_SELECTED_TEAM_ID).orEmpty()
        val selectedTeamName = intent.getStringExtra(SelectPlayingTeamsActivity.EXTRA_SELECTED_TEAM_NAME).orEmpty()
        setContent {
            val squadState = rememberFinalSquadMembers(selectedTeamId)
            FinalSquadScreen(
                squadState = squadState,
                onBack = { finish() },
                onNext = { matchRoles ->
                    saveMatchSquad(matchId, teamSlot, selectedTeamId, selectedTeamName, squadState.members, matchRoles) {
                    val resultIntent = Intent().apply {
                        putExtra(SelectPlayingTeamsActivity.EXTRA_TEAM_SLOT, teamSlot)
                        if (teamSlot == "A") {
                            putExtra(SelectPlayingTeamsActivity.EXTRA_TEAM_A_ID, selectedTeamId)
                            putExtra(SelectPlayingTeamsActivity.EXTRA_TEAM_A_NAME, selectedTeamName)
                        } else {
                            putExtra(SelectPlayingTeamsActivity.EXTRA_TEAM_B_ID, selectedTeamId)
                            putExtra(SelectPlayingTeamsActivity.EXTRA_TEAM_B_NAME, selectedTeamName)
                        }
                    }
                    setResult(android.app.Activity.RESULT_OK, resultIntent)
                    finish()
                    }
                }
            )
        }
    }

    private fun saveMatchSquad(
        matchId: String,
        teamSlot: String,
        teamId: String,
        teamName: String,
        members: List<FinalSquadMember>,
        matchRoles: Map<String, Set<String>>,
        onSaved: () -> Unit
    ) {
        if (matchId.isBlank() || teamId.isBlank()) return
        val squad = mapOf(
            "teamId" to teamId,
            "teamName" to teamName,
            "players" to members.map { member ->
                mapOf(
                    "userId" to member.userId,
                    "displayName" to member.name,
                    "playingRole" to member.playingRole,
                    "permanentTeamRoles" to member.teamRoles,
                    "matchRoles" to matchRoles[member.userId].orEmpty().sorted()
                )
            },
            "updatedAtEpochMs" to System.currentTimeMillis()
        )
        FirebaseFirestore.getInstance().collection("matches").document(matchId)
            .set(mapOf("matchSquads" to mapOf(teamSlot to squad)), SetOptions.merge())
            .addOnSuccessListener { onSaved() }
            .addOnFailureListener { error ->
                Toast.makeText(this, error.message ?: "Unable to save match squad", Toast.LENGTH_SHORT).show()
            }
    }
}

private data class FinalSquadMember(
    val userId: String,
    val name: String,
    val playingRole: String,
    val teamRoles: List<String>
)

private data class FinalSquadMembersState(
    val members: List<FinalSquadMember> = emptyList(),
    val isLoading: Boolean = true
)

@Composable
private fun rememberFinalSquadMembers(teamId: String): FinalSquadMembersState {
    var state by remember(teamId) { mutableStateOf(FinalSquadMembersState()) }
    DisposableEffect(teamId) {
        if (teamId.isBlank()) {
            state = FinalSquadMembersState(isLoading = false)
            onDispose { }
        } else {
            val registration = FirebaseFirestore.getInstance().collection("teams").document(teamId)
                .addSnapshotListener { document, error ->
                    if (error != null) {
                        state = FinalSquadMembersState(isLoading = false)
                        return@addSnapshotListener
                    }
                    if (document == null) return@addSnapshotListener
                    val baseMembers = (document?.get("members") as? List<*>).orEmpty()
                        .mapNotNull { it as? Map<*, *> }
                        .mapNotNull { value ->
                            val userId = value["userId"] as? String ?: return@mapNotNull null
                            FinalSquadMember(
                                userId = userId,
                                name = (value["displayName"] as? String).orEmpty(),
                                playingRole = (value["playingRole"] as? String).orEmpty().ifBlank { "Player" },
                                teamRoles = ((value["roles"] ?: value["teamRoles"]) as? List<*>)
                                    ?.filterIsInstance<String>().orEmpty()
                            )
                        }.distinctBy { it.userId }
                    if (baseMembers.isEmpty()) {
                        state = FinalSquadMembersState(isLoading = false)
                        return@addSnapshotListener
                    }
                    state = FinalSquadMembersState(members = baseMembers, isLoading = true)
                    // The profile function is already used by Team Profile and supplies current member names.
                    FirebaseFunctions.getInstance().getHttpsCallable("getTeamMemberProfiles")
                        .call(mapOf("teamId" to teamId))
                        .addOnSuccessListener { result ->
                            val profiles = ((result.data as? Map<*, *>)?.get("members") as? List<*>).orEmpty()
                                .mapNotNull { it as? Map<*, *> }
                                .associate { (it["userId"] as? String).orEmpty() to (it["displayName"] as? String).orEmpty() }
                            state = FinalSquadMembersState(members = baseMembers.map { member ->
                                member.copy(name = profiles[member.userId].orEmpty().ifBlank { member.name.ifBlank { "Team member" } })
                            }, isLoading = false)
                        }
                        .addOnFailureListener {
                            state = FinalSquadMembersState(
                                members = baseMembers.map { it.copy(name = it.name.ifBlank { "Team member" }) },
                                isLoading = false
                            )
                        }
                }
            onDispose { registration.remove() }
        }
    }
    return state
}

private fun Intent.copyTeamSelectionExtras(): Bundle = Bundle().apply {
    putString(
        SelectPlayingTeamsActivity.EXTRA_TEAM_A_ID,
        getStringExtra(SelectPlayingTeamsActivity.EXTRA_TEAM_A_ID)
    )
    putString(
        SelectPlayingTeamsActivity.EXTRA_TEAM_A_NAME,
        getStringExtra(SelectPlayingTeamsActivity.EXTRA_TEAM_A_NAME)
    )
    putString(
        SelectPlayingTeamsActivity.EXTRA_TEAM_B_ID,
        getStringExtra(SelectPlayingTeamsActivity.EXTRA_TEAM_B_ID)
    )
    putString(
        SelectPlayingTeamsActivity.EXTRA_TEAM_B_NAME,
        getStringExtra(SelectPlayingTeamsActivity.EXTRA_TEAM_B_NAME)
    )
}

private val SquadAccent = Color(0xFFC1FF00)
private val SquadBg = Color(0xFF0A101C)
private val SquadPanel = BlueCardBackGround
private val SquadCard = BlueCardBackGround
private val SquadStroke = Color(0xFF26394B)
private val SquadMuted = Color(0xFFAAB6C4)

@Composable
private fun FinalSquadScreen(
    squadState: FinalSquadMembersState,
    onBack: () -> Unit,
    onNext: (Map<String, Set<String>>) -> Unit
) {
    val members = squadState.members
    var matchRoles by remember(members) {
        mutableStateOf<Map<String, Set<String>>>(members.associate { member ->
            member.userId to buildSet<String> {
                if ("CAPTAIN" in member.teamRoles) add("CAPTAIN")
                if ("VICE_CAPTAIN" in member.teamRoles) add("VICE_CAPTAIN")
                if (member.playingRole == "WICKET_KEEPER") add("WICKET_KEEPER")
            }
        })
    }
    fun setRole(userId: String, role: String) {
        val alreadySelected = role in matchRoles[userId].orEmpty()
        matchRoles = matchRoles.mapValues { (memberId, roles) ->
            when {
                memberId == userId && alreadySelected -> roles - role
                memberId == userId -> roles + role
                else -> roles - role // Captain, VC and WK are unique for this match only.
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SquadBg)
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(SquadAccent.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(size.width / 2f, -50f),
                        radius = size.height * 0.6f
                    )
                )
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            FinalSquadTopBar(onBack)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 20.dp)
            ) {
                FinalSquadHeader()
                if (squadState.isLoading) {
                    repeat(6) { index ->
                        SquadPlayerSkeleton(Modifier.padding(top = if (index == 0) 22.dp else 12.dp))
                    }
                } else if (members.isEmpty()) {
                    Text(
                        "No players found in this squad.",
                        color = SquadMuted,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 22.dp)
                    )
                } else {
                    members.forEachIndexed { index, member ->
                        val roles = matchRoles[member.userId].orEmpty()
                        SquadPlayerCard(
                            name = member.name,
                            role = member.playingRole,
                            isCaptain = "CAPTAIN" in roles,
                            isViceCaptain = "VICE_CAPTAIN" in roles,
                            isWicketKeeper = "WICKET_KEEPER" in roles,
                            onRoleClick = { setRole(member.userId, it) },
                            modifier = Modifier.padding(top = if (index == 0) 22.dp else 12.dp)
                        )
                    }
                }
                Spacer(Modifier.height(110.dp))
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Transparent, SquadBg, SquadBg)))
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .bounceClick {
                        if (!squadState.isLoading && members.isNotEmpty()) onNext(matchRoles)
                    }
                    .clip(RoundedCornerShape(12.dp))
                    .background(SquadAccent.copy(alpha = if (squadState.isLoading || members.isEmpty()) 0.55f else 1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (squadState.isLoading) "Loading squad..." else "Next",
                    color = Color(0xFF111604),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun SquadPlayerSkeleton(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "squad-player-skeleton")
    val shimmer by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(animation = tween(850), repeatMode = RepeatMode.Reverse),
        label = "squad-player-skeleton-alpha"
    )
    val bone = Color(0xFF36506B).copy(alpha = shimmer)
    Row(
        modifier = modifier.fillMaxWidth().height(96.dp).clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0B1522)).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(56.dp).clip(CircleShape).background(bone))
        Column(
            modifier = Modifier.padding(start = 16.dp).weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(Modifier.fillMaxWidth(0.72f).height(15.dp).clip(RoundedCornerShape(8.dp)).background(bone))
            Box(Modifier.fillMaxWidth(0.43f).height(10.dp).clip(RoundedCornerShape(6.dp)).background(bone.copy(alpha = shimmer * 0.75f)))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(3) { Box(Modifier.size(30.dp).clip(CircleShape).background(bone.copy(alpha = shimmer * 0.8f))) }
        }
    }
}

@Composable
private fun FinalSquadTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp)
            .background(SquadPanel)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SquadArrow(Modifier.size(25.dp).bounceClick(onBack), Color.White)
        Text(
            "Final Squad",
            color = Color.White,
            fontSize = 21.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 48.dp),
            maxLines = 1
        )
    }
}

@Composable
private fun FinalSquadHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Your Squad",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.weight(1f),
            maxLines = 1
        )
    }
}

@Composable
private fun SquadRoleButton(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .bounceClick(onClick)
            .clip(CircleShape)
            .background(if (selected) SquadAccent else Color.Transparent)
            .border(1.dp, if (selected) Color.Transparent else Color(0xFF25314A), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (selected) Color(0xFF111604) else SquadMuted,
            fontSize = if (text.length == 1) 14.sp else 11.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun SquadPlayerCard(name: String, role: String, isCaptain: Boolean, isViceCaptain: Boolean, isWicketKeeper: Boolean, onRoleClick: (String) -> Unit, modifier: Modifier = Modifier) {
    val isSelected = isCaptain || isViceCaptain || isWicketKeeper
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp)
            .bounceClick()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF111A29))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(Color(0xFF1A2840), Color(0xFF101820)), radius = 62f)),
                contentAlignment = Alignment.Center
            ) {
                SquadPersonIcon(Modifier.size(36.dp))
            }
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(SquadAccent)
                        .border(1.5.dp, Color(0xFF111A29), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("v", color = Color(0xFF111604), fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(bottom = 2.dp))
                }
            }
        }
        Column(modifier = Modifier.padding(start = 16.dp).weight(1f)) {
            Text(name, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(modifier = Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) SquadAccent else Color(0xFF555B66))
                )
                Text(role, color = SquadMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 6.dp))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SquadRoleButton("C", selected = isCaptain, onClick = { onRoleClick("CAPTAIN") })
            SquadRoleButton("VC", selected = isViceCaptain, onClick = { onRoleClick("VICE_CAPTAIN") })
            SquadRoleButton("WK", selected = isWicketKeeper, onClick = { onRoleClick("WICKET_KEEPER") })
        }
    }
}

@Composable
private fun SquadArrow(modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        val path = Path().apply {
            moveTo(size.width * 0.66f, size.height * 0.24f)
            lineTo(size.width * 0.34f, size.height * 0.5f)
            lineTo(size.width * 0.66f, size.height * 0.76f)
        }
        drawPath(path, tint, style = stroke)
    }
}

@Composable
private fun SquadPersonIcon(modifier: Modifier) {
    Canvas(modifier) {
        drawCircle(Color(0xFFD0B190), radius = size.minDimension * 0.17f, center = Offset(size.width * 0.5f, size.height * 0.28f))
        drawRoundRect(
            Color(0xFF253D58),
            topLeft = Offset(size.width * 0.24f, size.height * 0.49f),
            size = Size(size.width * 0.52f, size.height * 0.34f),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )
    }
}

fun Modifier.bounceClick(onClick: (() -> Unit)? = null) = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )
    this
        .scale(scale)
        .pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                isPressed = true
                val up = waitForUpOrCancellation()
                isPressed = false
                if (up != null && onClick != null) {
                    onClick()
                }
            }
        }
}

fun Modifier.pureNeonGlow(color: Color, radius: androidx.compose.ui.unit.Dp = 16.dp, cornerRadius: androidx.compose.ui.unit.Dp = 12.dp, isCircle: Boolean = false) = this.drawBehind {
    if (color == Color.Transparent) return@drawBehind
    val paint = androidx.compose.ui.graphics.Paint().apply {
        this.color = color
        this.asFrameworkPaint().apply {
            maskFilter = android.graphics.BlurMaskFilter(radius.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
        }
    }
    drawIntoCanvas { canvas ->
        if (isCircle) {
            canvas.drawCircle(center, size.width / 2f, paint)
        } else {
            canvas.drawRoundRect(0f, 0f, size.width, size.height, cornerRadius.toPx(), cornerRadius.toPx(), paint)
        }
    }
}
