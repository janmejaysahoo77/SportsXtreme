package com.example.sportsxtreme.presentation.match

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sportsxtreme.R
import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.MatchType
import com.example.sportsxtreme.domain.usecase.MatchUseCases
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FriendlyMatchSetupActivity : ComponentActivity() {
    @Inject lateinit var matchUseCases: MatchUseCases

    private val viewModel: FriendlyMatchSetupViewModel by viewModels {
        FriendlyMatchSetupViewModel.factory(matchUseCases)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        setContent {
            val uiState by viewModel.uiState.collectAsState()
            LaunchedEffect(Unit) {
                viewModel.events.collect { event ->
                    when (event) {
                        is FriendlyMatchSetupEvent.NavigateToTeamSelection -> {
                            startActivity(
                                Intent(this@FriendlyMatchSetupActivity, SelectPlayingTeamsActivity::class.java)
                                    .putExtra(SelectPlayingTeamsActivity.EXTRA_MATCH_ID, event.matchId)
                            )
                            finish()
                        }
                        is FriendlyMatchSetupEvent.ShowMessage -> {
                            Toast.makeText(this@FriendlyMatchSetupActivity, event.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            FriendlyMatchSetupScreen(
                onBack = { finish() },
                onContinue = {
                    startActivity(Intent(this@FriendlyMatchSetupActivity, FriendlyMatchDetailsActivity::class.java))
                },
                isLoading = false
            )
        }
    }
}

private val FriendlyAccent = Color(0xFFC1FF00)
private val FriendlyBackground = Color(0xFF020914)
private val FriendlyCard = Color(0xFF091320)
private val FriendlyStroke = Color(0xFF1B293A)
private val FriendlyMuted = Color(0xFF9AA69E)

@Composable
private fun FriendlyMatchSetupScreen(
    onBack: () -> Unit,
    onContinue: () -> Unit,
    isLoading: Boolean
) {
    var format by remember { mutableIntStateOf(1) }
    var ballType by remember { mutableIntStateOf(1) }
    var selectedOvers by remember { mutableIntStateOf(1) }
    val formats = listOf("Limited Overs", "T20 Match", "Test Match")
    val overs = listOf("5", "10", "20", "Custom")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FriendlyBackground)
            .drawBehind {
                drawCircle(Color(0x18235E86), size.width * 0.7f, Offset(size.width * 1.04f, size.height * 0.16f))
                drawCircle(Color(0x123A6B18), size.width * 0.58f, Offset(size.width * 0.1f, size.height * 0.77f))
                repeat(12) { index ->
                    val y = size.height * (0.14f + index * 0.074f)
                    drawLine(Color(0x0A88B7D4), Offset(0f, y), Offset(size.width, y - size.width * 0.09f), 1.dp.toPx())
                }
            }
    ) {
        Column(Modifier.fillMaxSize()) {
            FriendlyTopBar(onBack)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text("Friendly Match", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(
                        "Quickly configure your match settings.",
                        color = FriendlyMuted,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }
                FriendlySectionTitle("Choose Format")
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    formats.take(2).forEachIndexed { index, option ->
                        FriendlyFormatChip(option, format == index) { format = index }
                    }
                }
                FriendlyFormatChip("Test Match", format == 2) { format = 2 }

                FriendlySectionTitle("Select Ball Type")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FriendlyBallCard("LEATHER", R.drawable.leatherball, ballType == 0, Modifier.weight(1f)) { ballType = 0 }
                    FriendlyBallCard("TENNIS", R.drawable.tennisball, ballType == 1, Modifier.weight(1f)) { ballType = 1 }
                    FriendlyBallCard("TAPE", R.drawable.otherball, ballType == 2, Modifier.weight(1f)) { ballType = 2 }
                }

                FriendlySectionTitle("Number of Overs")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(FriendlyCard)
                        .border(1.dp, FriendlyStroke, RoundedCornerShape(24.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    overs.forEachIndexed { index, value ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (selectedOvers == index) FriendlyAccent else Color.Transparent)
                                .clickable { selectedOvers = index },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                value,
                                color = if (selectedOvers == index) Color(0xFF142008) else Color(0xFFBAC4C0),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
                FriendlyMatchPreview(
                    format = formats[format],
                    ball = listOf("Leather Ball", "Tennis Ball", "Tape Ball")[ballType],
                    overs = if (selectedOvers == 3) "Custom Overs" else "${overs[selectedOvers]} Overs"
                )
                Spacer(Modifier.height(74.dp))
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Transparent, FriendlyBackground, FriendlyBackground)))
                .padding(horizontal = 14.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFFD8FF38), FriendlyAccent, Color(0xFF9BFA00))))
                    .clickable(enabled = !isLoading, onClick = onContinue),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(if (isLoading) "CREATING MATCH..." else "Continue", color = Color(0xFF101904), fontSize = 14.sp, fontWeight = FontWeight.Black)
                FriendlyArrow(Modifier.padding(start = 10.dp).size(18.dp), true, Color(0xFF101904))
            }
        }
    }
}

@Composable
private fun FriendlyTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(Color(0xE9050B16))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FriendlyArrow(Modifier.size(22.dp).clickable(onClick = onBack), false, Color.White)
        Text(
            "SPORTSXTREME",
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black
        )
        FriendlyHelpIcon(Modifier.size(21.dp))
    }
}

@Composable
private fun FriendlySectionTitle(title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(17.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(FriendlyAccent)
        )
        Text(title, color = Color(0xFFE1E9E5), fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 7.dp))
    }
}

@Composable
private fun FriendlyFormatChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(39.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(if (selected) FriendlyAccent else Color(0xFF101A29))
            .border(1.dp, if (selected) Color(0xFF9BBE00) else FriendlyStroke, RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 17.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (selected) Color(0xFF1A2609) else Color(0xFFC6CFCC), fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
private fun FriendlyBallCard(label: String, imageRes: Int, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .height(92.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(if (selected) Color(0xFF172419) else Color(0xFF0B1522))
            .border(if (selected) 2.dp else 1.dp, if (selected) Color(0xFF719B25) else Color.Transparent, RoundedCornerShape(11.dp))
            .clickable(onClick = onClick)
            .padding(top = 7.dp, bottom = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = "$label cricket ball",
            modifier = Modifier.size(55.dp),
            contentScale = ContentScale.Fit
        )
        Text(label, color = if (selected) Color(0xFFD9F79F) else Color(0xFFE8ECE9), fontSize = 9.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun FriendlyMatchPreview(format: String, ball: String, overs: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(9.dp))
            .background(Color(0xE3091421))
            .border(1.dp, FriendlyStroke, RoundedCornerShape(9.dp))
            .drawBehind { drawLine(FriendlyAccent, Offset(0f, 8.dp.toPx()), Offset(0f, size.height - 8.dp.toPx()), 3.dp.toPx()) }
            .padding(start = 15.dp, top = 12.dp, end = 14.dp, bottom = 12.dp)
    ) {
        Text("MATCH PREVIEW", color = FriendlyAccent, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 0.7.sp)
        Row(modifier = Modifier.padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("⚡", fontSize = 16.sp)
            Text(format, color = Color(0xFFDDE5E2), fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
        }
        Column(modifier = Modifier.padding(start = 24.dp, top = 8.dp)) {
            Text(ball, color = FriendlyMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text(overs, color = FriendlyMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

@Composable
private fun FriendlyArrow(modifier: Modifier, right: Boolean, color: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.9.dp.toPx(), cap = StrokeCap.Round)
        val path = Path().apply {
            if (right) {
                moveTo(size.width * 0.32f, size.height * 0.22f)
                lineTo(size.width * 0.68f, size.height * 0.5f)
                lineTo(size.width * 0.32f, size.height * 0.78f)
            } else {
                moveTo(size.width * 0.68f, size.height * 0.22f)
                lineTo(size.width * 0.32f, size.height * 0.5f)
                lineTo(size.width * 0.68f, size.height * 0.78f)
            }
        }
        drawPath(path, color, style = stroke)
    }
}

@Composable
private fun FriendlyHelpIcon(modifier: Modifier) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(Color(0xFFE1E8E5), size.minDimension * 0.38f, style = stroke)
        drawArc(Color(0xFFE1E8E5), 205f, 235f, false, style = stroke)
        drawCircle(Color(0xFFE1E8E5), size.minDimension * 0.05f, Offset(size.width * 0.5f, size.height * 0.72f))
    }
}

data class FriendlyMatchSetupUiState(val isLoading: Boolean = false)

sealed interface FriendlyMatchSetupEvent {
    data class NavigateToTeamSelection(val matchId: String) : FriendlyMatchSetupEvent
    data class ShowMessage(val message: String) : FriendlyMatchSetupEvent
}

class FriendlyMatchSetupViewModel(private val matchUseCases: MatchUseCases) : ViewModel() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _uiState = MutableStateFlow(FriendlyMatchSetupUiState())
    private val _events = MutableSharedFlow<FriendlyMatchSetupEvent>()

    val uiState: StateFlow<FriendlyMatchSetupUiState> = _uiState.asStateFlow()
    val events: SharedFlow<FriendlyMatchSetupEvent> = _events.asSharedFlow()

    fun createFriendlyMatch() {
        if (_uiState.value.isLoading) return
        scope.launch {
            _uiState.value = FriendlyMatchSetupUiState(isLoading = true)
            when (val result = matchUseCases.createMatch(MatchType.FRIENDLY)) {
                is Resource.Success -> result.data?.let { match ->
                    _uiState.value = FriendlyMatchSetupUiState()
                    _events.emit(FriendlyMatchSetupEvent.NavigateToTeamSelection(match.id))
                } ?: showError("Friendly match was not created")
                is Resource.Error -> showError(result.message ?: "Unable to create friendly match")
                is Resource.Loading -> Unit
            }
        }
    }

    override fun onCleared() {
        scope.cancel()
    }

    private suspend fun showError(message: String) {
        _uiState.value = FriendlyMatchSetupUiState()
        _events.emit(FriendlyMatchSetupEvent.ShowMessage(message))
    }

    companion object {
        fun factory(matchUseCases: MatchUseCases): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass.isAssignableFrom(FriendlyMatchSetupViewModel::class.java))
                return FriendlyMatchSetupViewModel(matchUseCases) as T
            }
        }
    }
}
