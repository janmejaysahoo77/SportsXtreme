package com.example.sportsxtreme.presentation.match

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.example.sportsxtreme.R
import com.example.sportsxtreme.domain.usecase.MatchUseCases
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FriendlyMatchDetailsActivity : ComponentActivity() {
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
                                Intent(this@FriendlyMatchDetailsActivity, SelectPlayingTeamsActivity::class.java)
                                    .putExtra(SelectPlayingTeamsActivity.EXTRA_MATCH_ID, event.matchId)
                            )
                            finish()
                        }
                        is FriendlyMatchSetupEvent.ShowMessage -> Toast.makeText(
                            this@FriendlyMatchDetailsActivity, event.message, Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            FriendlyMatchDetailsScreen(
                onBack = { finish() },
                onContinue = viewModel::createFriendlyMatch,
                isLoading = uiState.isLoading
            )
        }
    }
}

private val DetailsAccent = Color(0xFFC1FF00)
private val DetailsBg = Color(0xFF030A14)
private val DetailsCard = Color(0xFF0A1422)
private val DetailsBorder = Color(0xFF17283A)
private val DetailsMuted = Color(0xFF8D9B9C)

@Composable
private fun FriendlyMatchDetailsScreen(onBack: () -> Unit, onContinue: () -> Unit, isLoading: Boolean) {
    var ground by remember { mutableIntStateOf(0) }
    var date by remember { mutableIntStateOf(0) }
    var timeMode by remember { mutableIntStateOf(1) }
    Box(
        modifier = Modifier.fillMaxSize().background(DetailsBg).drawBehind {
            drawCircle(Color(0x15214970), size.width * 0.69f, Offset(size.width * 1.08f, size.height * 0.24f))
            drawCircle(Color(0x103A6518), size.width * 0.58f, Offset(size.width * 0.04f, size.height * 0.76f))
            repeat(10) { index ->
                val y = size.height * (0.13f + index * 0.09f)
                drawLine(Color(0x0A78A4BB), Offset(0f, y), Offset(size.width, y - size.width * 0.14f), 1.dp.toPx())
            }
        }
    ) {
        Column(Modifier.fillMaxSize()) {
            DetailsTopBar(onBack)
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 10.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                DetailsHeading("Ground Selection", DetailsIcon.LOCATION)
                DetailsOptionCard("Select Existing Ground", "KRT Stadium, Bhubaneswar", ground == 0) { ground = 0 }
                DetailsOptionCard("Use Current Location", null, ground == 1) { ground = 1 }
                DetailsOptionCard("Add New Ground", null, ground == 2) { ground = 2 }
                DetailsHeading("Match Date", DetailsIcon.CALENDAR)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    DetailsDateCard("Today", "OCT 31", date == 0, Modifier.weight(1f)) { date = 0 }
                    DetailsDateCard("Tomorrow", "OCT 32", date == 1, Modifier.weight(1f)) { date = 1 }
                    DetailsDateCard("Custom", "SELECT", date == 2, Modifier.weight(1f)) { date = 2 }
                }
                DetailsHeading("Match Time", DetailsIcon.CLOCK)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailsTimeChip("Now", timeMode == 0) { timeMode = 0 }
                    DetailsTimeChip("Choose Time", timeMode == 1) { timeMode = 1 }
                    DetailsTimeChip("Custom Time", timeMode == 2) { timeMode = 2 }
                }
                DetailsTimeCard(timeMode)
                DetailsSnapshot()
                Spacer(Modifier.height(70.dp))
            }
        }
        Box(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Transparent, DetailsBg, DetailsBg)))
                .padding(horizontal = 10.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(26.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFFD8FF37), DetailsAccent, Color(0xFF9AFF00))))
                    .clickable(enabled = !isLoading, onClick = onContinue),
                horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
            ) {
                Text(if (isLoading) "CREATING MATCH..." else "CONTINUE", color = Color(0xFF122004), fontSize = 13.sp, fontWeight = FontWeight.Black)
                DetailsArrow(Modifier.padding(start = 9.dp).size(17.dp), true, Color(0xFF122004))
            }
        }
    }
}

@Composable
private fun DetailsTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(48.dp).background(Color(0xEA06101A)).padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DetailsArrow(Modifier.size(18.dp).clickable(onClick = onBack), false, DetailsAccent)
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Match Details", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)
            Text("CONFIGURE MATCH LOGISTICS", color = DetailsMuted, fontSize = 6.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.7.sp)
        }
        DetailsIconCanvas(DetailsIcon.INFO, Modifier.size(17.dp), DetailsMuted)
    }
}

@Composable
private fun DetailsHeading(title: String, icon: DetailsIcon) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        DetailsIconCanvas(icon, Modifier.size(16.dp), DetailsAccent)
        Text(title, color = Color(0xFFE1E8E4), fontSize = 12.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 5.dp))
    }
}

@Composable
private fun DetailsOptionCard(title: String, subtitle: String?, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(53.dp).clip(RoundedCornerShape(10.dp))
            .background(if (selected) Color(0xFF142019) else DetailsCard)
            .border(if (selected) 1.5.dp else 1.dp, if (selected) Color(0xFF6F9427) else DetailsBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick).padding(horizontal = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(28.dp).clip(RoundedCornerShape(7.dp)).background(if (selected) Color(0xFF668900) else Color(0xFF1B2736)), contentAlignment = Alignment.Center) {
            DetailsIconCanvas(DetailsIcon.LOCATION, Modifier.size(16.dp), if (selected) DetailsAccent else DetailsMuted)
        }
        Column(modifier = Modifier.padding(start = 10.dp).weight(1f)) {
            Text(title, color = if (selected) Color.White else Color(0xFF8C989A), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            subtitle?.let { Text(it, color = DetailsAccent, fontSize = 7.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 2.dp)) }
        }
        if (selected) DetailsIconCanvas(DetailsIcon.CHECK, Modifier.size(16.dp), DetailsAccent)
    }
}

@Composable
private fun DetailsDateCard(title: String, subtitle: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier.height(53.dp).clip(RoundedCornerShape(10.dp)).background(if (selected) Color(0xFF152019) else DetailsCard)
            .border(if (selected) 1.5.dp else 1.dp, if (selected) Color(0xFF71962B) else DetailsBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick).padding(vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DetailsIconCanvas(DetailsIcon.CALENDAR, Modifier.size(13.dp), if (selected) DetailsAccent else DetailsMuted)
        Text(title, color = if (selected) DetailsAccent else Color(0xFF9FA9A8), fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 3.dp))
        Text(subtitle, color = DetailsMuted, fontSize = 6.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DetailsTimeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.height(23.dp).clip(RoundedCornerShape(14.dp)).background(if (selected) Color(0xFF1A2814) else DetailsCard)
            .border(1.dp, if (selected) Color(0xFF6E9428) else DetailsBorder, RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(horizontal = 11.dp),
        contentAlignment = Alignment.Center
    ) { Text(label, color = if (selected) DetailsAccent else DetailsMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold) }
}

@Composable
private fun DetailsTimeCard(timeMode: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().height(55.dp).clip(RoundedCornerShape(10.dp)).background(DetailsCard)
            .border(1.dp, DetailsBorder, RoundedCornerShape(10.dp)).padding(horizontal = 13.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        Text(if (timeMode == 0) "NOW" else "06:30", color = Color.White, fontSize = 24.sp)
        Text("PM", color = DetailsAccent, fontSize = 12.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 5.dp, top = 6.dp))
        Spacer(Modifier.weight(1f))
        Box(Modifier.size(25.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFF1A2838)), contentAlignment = Alignment.Center) {
            DetailsIconCanvas(DetailsIcon.EDIT, Modifier.size(13.dp), DetailsMuted)
        }
    }
}

@Composable
private fun DetailsSnapshot() {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(9.dp)).background(Color(0xDD091522)).border(1.dp, DetailsBorder, RoundedCornerShape(9.dp))
            .drawBehind { drawLine(DetailsAccent, Offset(0f, 8.dp.toPx()), Offset(0f, size.height - 8.dp.toPx()), 3.dp.toPx()) }.padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("MATCH SNAPSHOT", color = DetailsMuted, fontSize = 6.sp, fontWeight = FontWeight.Black, letterSpacing = 0.8.sp)
            Text("Friendly • Today • KRT Stadium", color = Color(0xFFCFD8D4), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
        }
        Text("18:30", color = DetailsAccent, fontSize = 13.sp, fontWeight = FontWeight.Black)
    }
}

private enum class DetailsIcon { LOCATION, CALENDAR, CLOCK, EDIT, CHECK, INFO }

@Composable
private fun DetailsArrow(modifier: Modifier, right: Boolean, color: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        val path = Path().apply {
            if (right) { moveTo(size.width * .3f, size.height * .22f); lineTo(size.width * .7f, size.height * .5f); lineTo(size.width * .3f, size.height * .78f) }
            else { moveTo(size.width * .7f, size.height * .22f); lineTo(size.width * .3f, size.height * .5f); lineTo(size.width * .7f, size.height * .78f) }
        }
        drawPath(path, color, style = stroke)
    }
}

@Composable
private fun DetailsIconCanvas(icon: DetailsIcon, modifier: Modifier, color: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        when (icon) {
            DetailsIcon.LOCATION -> { drawCircle(color, size.minDimension * .28f, Offset(size.width * .5f, size.height * .42f), style = stroke); drawLine(color, Offset(size.width * .5f, size.height * .68f), Offset(size.width * .5f, size.height * .87f), strokeWidth = stroke.width, cap = StrokeCap.Round) }
            DetailsIcon.CALENDAR -> { drawRoundRect(color, Offset(size.width * .17f, size.height * .24f), Size(size.width * .66f, size.height * .6f), CornerRadius(2.dp.toPx()), style = stroke); drawLine(color, Offset(size.width * .17f, size.height * .43f), Offset(size.width * .83f, size.height * .43f), strokeWidth = stroke.width); drawLine(color, Offset(size.width * .34f, size.height * .14f), Offset(size.width * .34f, size.height * .34f), strokeWidth = stroke.width); drawLine(color, Offset(size.width * .66f, size.height * .14f), Offset(size.width * .66f, size.height * .34f), strokeWidth = stroke.width) }
            DetailsIcon.CLOCK -> { drawCircle(color, size.minDimension * .35f, style = stroke); drawLine(color, Offset(size.width * .5f, size.height * .5f), Offset(size.width * .5f, size.height * .3f), strokeWidth = stroke.width, cap = StrokeCap.Round); drawLine(color, Offset(size.width * .5f, size.height * .5f), Offset(size.width * .66f, size.height * .6f), strokeWidth = stroke.width, cap = StrokeCap.Round) }
            DetailsIcon.EDIT -> { drawLine(color, Offset(size.width * .28f, size.height * .73f), Offset(size.width * .72f, size.height * .29f), strokeWidth = stroke.width * 1.5f, cap = StrokeCap.Round); drawLine(color, Offset(size.width * .26f, size.height * .76f), Offset(size.width * .43f, size.height * .72f), strokeWidth = stroke.width, cap = StrokeCap.Round) }
            DetailsIcon.CHECK -> { drawCircle(color, size.minDimension * .37f); drawLine(Color(0xFF1C2908), Offset(size.width * .32f, size.height * .5f), Offset(size.width * .46f, size.height * .64f), strokeWidth = stroke.width, cap = StrokeCap.Round); drawLine(Color(0xFF1C2908), Offset(size.width * .46f, size.height * .64f), Offset(size.width * .7f, size.height * .36f), strokeWidth = stroke.width, cap = StrokeCap.Round) }
            DetailsIcon.INFO -> { drawCircle(color, size.minDimension * .38f, style = stroke); drawLine(color, Offset(size.width * .5f, size.height * .45f), Offset(size.width * .5f, size.height * .7f), strokeWidth = stroke.width, cap = StrokeCap.Round); drawCircle(color, size.minDimension * .045f, Offset(size.width * .5f, size.height * .29f)) }
        }
    }
}
