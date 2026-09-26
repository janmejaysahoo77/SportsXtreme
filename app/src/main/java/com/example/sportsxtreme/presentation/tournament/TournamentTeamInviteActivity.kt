package com.example.sportsxtreme.presentation.tournament

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.sportsxtreme.R
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TournamentTeamInviteActivity : ComponentActivity() {

    private val viewModel: TournamentTeamInviteViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val token = intent.getStringExtra(EXTRA_TOKEN).orEmpty()
        val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        
        if (token.isBlank() || uid.isBlank()) {
            finish()
            return
        }

        viewModel.loadTeams(uid)

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val registrationState by viewModel.registrationState.collectAsState()
            
            var selectedTeam by remember { mutableStateOf<TeamInviteUiModel?>(null) }
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            val scope = rememberCoroutineScope()
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(registrationState) {
                if (registrationState is RegistrationState.Error) {
                    val message = (registrationState as RegistrationState.Error).message
                    snackbarHostState.showSnackbar(
                        message = message,
                        actionLabel = "Retry",
                        duration = SnackbarDuration.Long
                    ).let { result ->
                        if (result == SnackbarResult.ActionPerformed) {
                            selectedTeam?.let { team ->
                                viewModel.registerTeam(token, team.teamId, team.teamName)
                            }
                        } else {
                            viewModel.resetRegistrationState()
                        }
                    }
                } else if (registrationState is RegistrationState.Success) {
                    scope.launch { sheetState.hide() }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color(0xFF0D1B2A), Color(0xFF1B263B))))
            ) {
                Column(Modifier.fillMaxSize()) {
                    TopBar(onBack = { finish() })
                    
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(22.dp)
                    ) {
                        Text("JOIN TOURNAMENT", color = Color(0xFFC6FF00), fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Select your team", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.height(8.dp))
                        Text("Choose a team to continue. The organiser will see your entry immediately.", color = Color(0xFF9CAAB8), fontSize = 14.sp, lineHeight = 20.sp)
                        Spacer(Modifier.height(24.dp))
                        
                        when (val state = uiState) {
                            is TournamentTeamUiState.Loading -> {
                                SkeletonList()
                            }
                            is TournamentTeamUiState.Error -> {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(state.message, color = Color(0xFFFF6B6B))
                                }
                            }
                            is TournamentTeamUiState.Success -> {
                                if (state.teams.isEmpty()) {
                                    EmptyState()
                                } else {
                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                        items(state.teams, key = { it.teamId }) { team ->
                                            val isLoadingThis = registrationState is RegistrationState.Loading && (registrationState as RegistrationState.Loading).teamId == team.teamId
                                            TeamCard(
                                                team = team,
                                                isLoading = isLoadingThis,
                                                onClick = {
                                                    selectedTeam = team
                                                    scope.launch { sheetState.show() }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )

                if (selectedTeam != null && registrationState !is RegistrationState.Success) {
                    ModalBottomSheet(
                        onDismissRequest = { 
                            if (registrationState !is RegistrationState.Loading) {
                                selectedTeam = null 
                            }
                        },
                        sheetState = sheetState,
                        containerColor = Color(0xFF142133)
                    ) {
                        BottomSheetContent(
                            team = selectedTeam!!,
                            isSubmitting = registrationState is RegistrationState.Loading,
                            onConfirm = { viewModel.registerTeam(token, selectedTeam!!.teamId, selectedTeam!!.teamName) },
                            onCancel = { scope.launch { sheetState.hide() }.invokeOnCompletion { selectedTeam = null } }
                        )
                    }
                }
                
                if (registrationState is RegistrationState.Success) {
                    SuccessOverlay(
                        teamName = (registrationState as RegistrationState.Success).teamName,
                        onFinished = { finish() }
                    )
                }
            }
        }
    }

    companion object { const val EXTRA_TOKEN = "tournament_invite_token" }
}

@Composable
private fun TopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Text("←", color = Color.White, fontSize = 20.sp)
        }
    }
}

@Composable
private fun TeamCard(team: TeamInviteUiModel, isLoading: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.96f else 1f, label = "scale")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1B2838))
            .border(1.dp, Color(0xFF2C3E50), RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = !isLoading,
                onClick = onClick
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(team.avatarGradient)),
            contentAlignment = Alignment.Center
        ) {
            Text(team.avatarLetter, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
        }
        
        Spacer(Modifier.width(16.dp))
        
        Column(Modifier.weight(1f)) {
            Text(team.teamName, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("👥 ${team.memberCount} Players  ·  🛡️ Captain", color = Color(0xFF9CAAB8), fontSize = 12.sp)
        }
        
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color(0xFFC6FF00),
                strokeWidth = 2.dp
            )
        } else {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFC6FF00).copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("JOIN →", color = Color(0xFFC6FF00), fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun SkeletonList() {
    val transition = rememberInfiniteTransition()
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeleton_alpha"
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        repeat(3) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1B2838))
                    .border(1.dp, Color(0xFF2C3E50), RoundedCornerShape(16.dp))
                    .padding(16.dp)
                    .alpha(alpha),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(48.dp).clip(CircleShape).background(Color(0xFF2C3E50)))
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Box(Modifier.height(18.dp).fillMaxWidth(0.6f).clip(RoundedCornerShape(4.dp)).background(Color(0xFF2C3E50)))
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.height(12.dp).fillMaxWidth(0.4f).clip(RoundedCornerShape(4.dp)).background(Color(0xFF2C3E50)))
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFF2C3E50).copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Text("🛡️", fontSize = 32.sp)
        }
        Spacer(Modifier.height(24.dp))
        Text("No teams available", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "You need to be a captain or admin of a team to register it in this tournament.",
            color = Color(0xFF9CAAB8),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
private fun BottomSheetContent(
    team: TeamInviteUiModel,
    isSubmitting: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Confirm Registration", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text(
            "Are you sure you want to register ${team.teamName} for this tournament? This action will notify the organiser.",
            color = Color(0xFF9CAAB8),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
        
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2C3E50))
                    .clickable(enabled = !isSubmitting, onClick = onCancel),
                contentAlignment = Alignment.Center
            ) {
                Text("Cancel", color = Color.White, fontWeight = FontWeight.Bold)
            }
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSubmitting) Color(0xFF88A800) else Color(0xFFC6FF00))
                    .clickable(enabled = !isSubmitting, onClick = onConfirm),
                contentAlignment = Alignment.Center
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black, strokeWidth = 2.dp)
                } else {
                    Text("Confirm", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SuccessOverlay(teamName: String, onFinished: () -> Unit) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.success))
    val context = LocalContext.current
    val cheerPlayer = remember { MediaPlayer.create(context, R.raw.success_sound) }

    LaunchedEffect(Unit) {
        cheerPlayer?.start()
        delay(2200)
        cheerPlayer?.release()
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A).copy(alpha = 0.95f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            LottieAnimation(
                composition = composition,
                iterations = 1,
                modifier = Modifier.size(200.dp)
            )
            Spacer(Modifier.height(24.dp))
            Text("Tournament Joined!", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(8.dp))
            Text("$teamName is now registered.", color = Color(0xFF9CAAB8), fontSize = 16.sp)
        }
    }
}
