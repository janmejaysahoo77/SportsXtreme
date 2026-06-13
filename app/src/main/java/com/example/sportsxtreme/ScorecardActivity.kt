package com.example.sportsxtreme

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat

class ScorecardActivity : ComponentActivity() {

    companion object {
        const val EXTRA_LEAGUE = "scorecard.extra.LEAGUE"
        const val EXTRA_ROUND = "scorecard.extra.ROUND"
        const val EXTRA_LEFT_NAME = "scorecard.extra.LEFT_NAME"
        const val EXTRA_LEFT_SCORE = "scorecard.extra.LEFT_SCORE"
        const val EXTRA_LEFT_OVERS = "scorecard.extra.LEFT_OVERS"
        const val EXTRA_RIGHT_NAME = "scorecard.extra.RIGHT_NAME"
        const val EXTRA_RIGHT_SCORE = "scorecard.extra.RIGHT_SCORE"
        const val EXTRA_RIGHT_OVERS = "scorecard.extra.RIGHT_OVERS"
        const val EXTRA_TARGET = "scorecard.extra.TARGET"
        const val EXTRA_RRR = "scorecard.extra.RRR"
        const val EXTRA_WIN = "scorecard.extra.WIN"
        const val EXTRA_NOTE = "scorecard.extra.NOTE"
    }

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)

        setContent {
            ScorecardScreen(match = readMatch(), onBack = { finish() })
        }
    }

    private fun readMatch(): MatchDetail {
        return MatchDetail(
            league = intent.getStringExtra(EXTRA_LEAGUE) ?: "VALORANT PRO LEAGUE",
            round = intent.getStringExtra(EXTRA_ROUND) ?: "Semi-final - Match 07",
            leftName = intent.getStringExtra(EXTRA_LEFT_NAME) ?: "NSC",
            leftScore = intent.getStringExtra(EXTRA_LEFT_SCORE) ?: "142/4",
            leftOvers = intent.getStringExtra(EXTRA_LEFT_OVERS) ?: "18.4 OV",
            rightName = intent.getStringExtra(EXTRA_RIGHT_NAME) ?: "VCR",
            rightScore = intent.getStringExtra(EXTRA_RIGHT_SCORE) ?: "138/6",
            rightOvers = intent.getStringExtra(EXTRA_RIGHT_OVERS) ?: "18.1 OV",
            target = intent.getStringExtra(EXTRA_TARGET) ?: "156",
            rrr = intent.getStringExtra(EXTRA_RRR) ?: "8.42",
            win = intent.getStringExtra(EXTRA_WIN) ?: "NSC 61%",
            note = intent.getStringExtra(EXTRA_NOTE) ?: "VCR chose to bowl - Powerplay complete"
        )
    }
}

private data class MatchDetail(
    val league: String,
    val round: String,
    val leftName: String,
    val leftScore: String,
    val leftOvers: String,
    val rightName: String,
    val rightScore: String,
    val rightOvers: String,
    val target: String,
    val rrr: String,
    val win: String,
    val note: String
)

private val Accent = Color(0xFFC1FF00)
private val ElectricBlue = Color(0xFF007FFF)
private val CyanLine = Color(0xFF00D2FF)
private val ScreenBg = Color(0xFF010509)
private val Surface = Color(0xFF060C11)
private val Panel = Color(0xFF071016)
private val SoftText = Color(0xFF84938F)

private enum class DetailIcon { BACK, SEARCH, BELL, MESSAGE, SHIELD, BOLT, DOTS }
private enum class MetricIcon { SPEED, TARGET, TREND, TROPHY }

@Composable
private fun ScorecardScreen(match: MatchDetail, onBack: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(1) }
    val tabs = listOf("INFO", "LIVE", "SCORECARD", "SUMMARY", "COMMENTARY")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF061019), ScreenBg, Color(0xFF031006))
                )
            )
    ) {
        Column(Modifier.fillMaxSize()) {
            ScorecardTopBar(match = match, onBack = onBack)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .pointerInput(selectedTab) {
                        var dragTotal = 0f
                        detectHorizontalDragGestures(
                            onDragStart = { dragTotal = 0f },
                            onHorizontalDrag = { _, dragAmount -> dragTotal += dragAmount },
                            onDragEnd = {
                                when {
                                    dragTotal < -90f && selectedTab < tabs.lastIndex -> selectedTab += 1
                                    dragTotal > 90f && selectedTab > 0 -> selectedTab -= 1
                                }
                            }
                        )
                    }
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MatchDetailsStrip(match)
                MatchHero(match)
                ScorecardTabs(
                    tabs = tabs,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        val direction = if (targetState > initialState) {
                            AnimatedContentTransitionScope.SlideDirection.Left
                        } else {
                            AnimatedContentTransitionScope.SlideDirection.Right
                        }
                        slideIntoContainer(direction, animationSpec = tween(320)) + fadeIn(tween(180)) togetherWith
                            slideOutOfContainer(direction, animationSpec = tween(320)) + fadeOut(tween(180))
                    },
                    label = "scorecardSwipeTransition"
                ) { tab ->
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        when (tab) {
                            0 -> InfoTab(match)
                            1 -> LiveTab(match)
                            2 -> ScorecardTab(match)
                            3 -> SummaryTab(match)
                            else -> CommentaryTab(match)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScorecardTopBar(
    match: MatchDetail,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Surface)
            .padding(start = 10.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF02070B))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            DetailIconView(DetailIcon.BACK, Color.White, Modifier.size(20.dp))
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp, end = 8.dp)
        ) {
            Text(
                text = match.league,
                color = Color.White,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = match.round,
                color = SoftText,
                fontSize = 7.2.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        DetailIconView(DetailIcon.SEARCH, Color.White, Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        DetailIconView(DetailIcon.BELL, Color.White, Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        DetailIconView(DetailIcon.MESSAGE, Color.White, Modifier.size(18.dp))
    }
}

@Composable
private fun ScorecardTabs(
    tabs: List<String>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(16.dp, RoundedCornerShape(28.dp), clip = false)
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White.copy(alpha = 0.055f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(28.dp))
            .horizontalScroll(rememberScrollState())
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        tabs.forEachIndexed { index, label ->
            TabButton(
                label = label,
                selected = selectedTab == index,
                modifier = Modifier,
                onClick = { onTabSelected(index) }
            )
        }
    }
}

@Composable
private fun TabButton(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val glow by animateFloatAsState(targetValue = if (selected) 1f else 0f, label = "tabGlow")
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(23.dp))
            .background(
                if (selected) {
                    Brush.horizontalGradient(listOf(Accent.copy(alpha = 0.24f), ElectricBlue.copy(alpha = 0.12f)))
                } else {
                    Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                }
            )
            .border(
                1.dp,
                if (selected) Accent.copy(alpha = 0.28f + glow * 0.32f) else Color.Transparent,
                RoundedCornerShape(23.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            color = if (selected) Accent else Color(0xFFB7C1BD),
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(5.dp))
        Box(
            modifier = Modifier
                .width(if (selected) 18.dp else 0.dp)
                .height(4.dp)
                .shadow(if (selected) 9.dp else 0.dp, RoundedCornerShape(2.dp), clip = false)
                .clip(RoundedCornerShape(2.dp))
                .background(if (selected) Accent else Color.Transparent)
        )
    }
}

@Composable
private fun MatchDetailsStrip(match: MatchDetail) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 5.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${match.leftName} vs ${match.rightName}, ${match.round} - Live Score",
                color = Color.White,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Black,
                maxLines = 2,
                lineHeight = 18.sp,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            ViewsPill("30.6Cr")
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            DetailMeta("League", match.league, Modifier.weight(1f))
            HeaderDot()
            DetailMeta("Venue", "Narendra Modi Stadium, Ahmedabad", Modifier.weight(1.22f))
        }
    }
}

@Composable
private fun DetailMeta(label: String, value: String, modifier: Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text("$label: ", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Black)
        Text(value, color = Color.White.copy(alpha = 0.7f), fontSize = 9.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun HeaderDot() {
    Box(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .size(4.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.24f))
    )
}

@Composable
private fun ViewsPill(value: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.065f))
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        EyeIcon(Color.White, Modifier.size(14.dp))
        Spacer(Modifier.width(5.dp))
        Text(value, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun MatchHero(match: MatchDetail) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(30.dp, RoundedCornerShape(20.dp), clip = false)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Accent.copy(alpha = 0.14f),
                        Color(0xFF07121A).copy(alpha = 0.96f),
                        ElectricBlue.copy(alpha = 0.16f),
                        Color(0xFF050B10).copy(alpha = 0.98f)
                    )
                )
            )
            .padding(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LiveBadge()
            Spacer(Modifier.weight(1f))
            Text(
                text = match.league,
                color = Color(0xFFE8F0ED),
                fontSize = 8.4.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            CompactTeam(match.leftName, match.leftScore, match.leftOvers, DetailIcon.SHIELD, Modifier.weight(1f))
            CompactTeam(match.rightName, match.rightScore, match.rightOvers, DetailIcon.BOLT, Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricPill(MetricIcon.SPEED, "CRR", "7.61", Modifier.weight(1f))
            MetricPill(MetricIcon.TARGET, "TARGET", match.target, Modifier.weight(1f))
            MetricPill(MetricIcon.TREND, "RRR", match.rrr, Modifier.weight(1f))
            MetricPill(MetricIcon.TROPHY, "WIN", match.win, Modifier.weight(1.1f))
        }
        Spacer(Modifier.height(6.dp))
        Text("${match.rightName} won toss and chose to bowl", color = Color(0xFFB8C6C1), fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun CompactTeam(name: String, score: String, detail: String, icon: DetailIcon, modifier: Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .shadow(13.dp, RoundedCornerShape(14.dp), clip = false)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            DetailIconView(icon, Color(0xFFE2E9E6), Modifier.size(18.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(name, color = SoftText, fontSize = 8.sp, fontWeight = FontWeight.Black)
        Text(score, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
        Text("($detail)", color = Color(0xFF8B9692), fontSize = 8.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MetricPill(icon: MetricIcon, label: String, value: String, modifier: Modifier) {
    Row(
        modifier = modifier
            .height(32.dp)
            .shadow(9.dp, RoundedCornerShape(16.dp), clip = false)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.075f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(horizontal = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MetricIconView(icon, Accent, Modifier.size(12.dp))
        Spacer(Modifier.width(4.dp))
        Column {
            Text(label, color = SoftText, fontSize = 5.5.sp, fontWeight = FontWeight.Black)
            Text(value, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun InfoTab(match: MatchDetail) {
    SectionPanel("MATCH INFO") {
        InfoRow("Venue", "Madanpur Sports Arena")
        InfoRow("Toss", "${match.rightName} chose to bowl")
        InfoRow("Target", match.target)
        InfoRow("Required rate", match.rrr)
        InfoRow("Win projection", match.win)
    }
    TwoColumnStats("CURRENT SNAPSHOT", listOf("Partnership" to "38 (24)", "Run rate" to "7.61", "Last 5 overs" to "43/2", "Momentum" to match.leftName))
}

@Composable
private fun LiveTab(match: MatchDetail) {
    LiveScorePanel(match)
    CurrentBattingCard()
    PartnershipCard()
    LiveKeyStats(match)
    CurrentBowlingCard()
    LastSixBalls()
    WinPredictorPanel(match)
}

@Composable
private fun LiveScorePanel(match: MatchDetail) {
    SectionPanel("LIVE SCORE") {
        Text(
            text = "${match.rightName} ${match.rightScore} (${match.rightOvers})",
            color = SoftText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(5.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "${match.leftName} ${match.leftScore}",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "(${match.leftOvers})",
                color = SoftText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
            )
        }
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            LiveRateChip("CRR", "7.61")
            Spacer(Modifier.width(7.dp))
            LiveRateChip("REQ", match.rrr)
            Spacer(Modifier.width(7.dp))
            LiveRateChip("TARGET", match.target)
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = "${match.leftName} need 18 runs in 8 balls",
            color = Color(0xFFFF4D5E),
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun LiveRateChip(label: String, value: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color.White.copy(alpha = 0.07f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
            .padding(horizontal = 7.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = SoftText, fontSize = 7.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.width(4.dp))
        Text(value, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun LiveKeyStats(match: MatchDetail) {
    SectionPanel("KEY STATS") {
        LiveStatLine("Partnership", "38 (26)")
        ThinRule()
        LiveStatLine("Last wicket", "R. Singh c deep mid-wicket b K. Roy - 28 (21)")
        ThinRule()
        LiveStatLine("Last 5 overs", "43 runs, 2 wickets")
        ThinRule()
        LiveStatLine("Toss", "${match.rightName} chose to bowl")
    }
}

@Composable
private fun LiveStatLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            color = SoftText,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.width(96.dp)
        )
        Text(
            text = value,
            color = Color(0xFFD7E2DF),
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 14.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun WinPredictorPanel(match: MatchDetail) {
    SectionPanel("MATCH PULSE") {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(match.leftName, color = Accent, fontSize = 10.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.weight(1f))
            Text(match.win, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.weight(1f))
            Text(match.rightName, color = ElectricBlue, fontSize = 10.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(8.dp))
        WinPredictor()
    }
}

@Composable
private fun ScorecardTab(match: MatchDetail) {
    var selectedTeamTab by remember { mutableIntStateOf(0) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.055f)),
        horizontalArrangement = Arrangement.Center
    ) {
        val leftSelected = selectedTeamTab == 0
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(if (leftSelected) Accent.copy(alpha = 0.15f) else Color.Transparent)
                .clickable { selectedTeamTab = 0 }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(match.leftName, color = if (leftSelected) Accent else Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
        }
        
        val rightSelected = selectedTeamTab == 1
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(if (rightSelected) Accent.copy(alpha = 0.15f) else Color.Transparent)
                .clickable { selectedTeamTab = 1 }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(match.rightName, color = if (rightSelected) Accent else Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
        }
    }
    
    Spacer(Modifier.height(14.dp))

    if (selectedTeamTab == 0) {
        SectionPanel("${match.leftName} BATTING") {
            TableHeader("BATTER", "R", "B", "SR")
            ThinRule()
            ScoreLine("A. Kumar", "54", "31", "174.1", Accent)
            ThinRule()
            ScoreLine("R. Singh", "28", "21", "133.3", Color.White)
            ThinRule()
            ScoreLine("M. Das", "17", "12", "141.6", Color.White)
        }
        Spacer(Modifier.height(14.dp))
        SectionPanel("${match.rightName} BOWLING") {
            TableHeader("BOWLER", "O", "R", "W")
            ThinRule()
            ScoreLine("K. Roy", "4.0", "29", "2", CyanLine)
            ThinRule()
            ScoreLine("D. Sen", "3.4", "26", "1", Color.White)
            ThinRule()
            ScoreLine("P. Malik", "4.0", "34", "1", Color.White)
        }
    } else {
        SectionPanel("${match.rightName} BATTING") {
            TableHeader("BATTER", "R", "B", "SR")
            ThinRule()
            ScoreLine("P. Patel", "42", "28", "150.0", Accent)
            ThinRule()
            ScoreLine("S. Yadav", "31", "24", "129.1", Color.White)
            ThinRule()
            ScoreLine("K. Sharma", "12", "9", "133.3", Color.White)
        }
        Spacer(Modifier.height(14.dp))
        SectionPanel("${match.leftName} BOWLING") {
            TableHeader("BOWLER", "O", "R", "W")
            ThinRule()
            ScoreLine("J. Bumrah", "4.0", "24", "2", CyanLine)
            ThinRule()
            ScoreLine("M. Siraj", "3.1", "30", "1", Color.White)
            ThinRule()
            ScoreLine("H. Pandya", "4.0", "38", "1", Color.White)
        }
    }
}

@Composable
private fun SummaryTab(match: MatchDetail) {
    LastSixBalls()
    CurrentBattingCard()
    PartnershipCard()
    CurrentBowlingCard()
    RunWormPanel()
    MatchInsights(match)
    FallOfWicketsPremium()
}

@Composable
private fun CommentaryTab(match: MatchDetail) {
    SectionPanel("LIVE COMMENTARY") {
        CommentaryLine("18.4", "Four through cover. ${match.leftName} keep the chase alive.", Accent)
        CommentaryLine("18.3", "Short ball pulled to deep square for two.", Color.White)
        CommentaryLine("18.2", "Dot ball. Clever slower one outside off.", Color.White)
        CommentaryLine("18.1", "Single clipped behind square.", Color.White)
    }
}

@Composable
private fun SectionPanel(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(18.dp, RoundedCornerShape(18.dp), clip = false)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.085f),
                        Color(0xFF151B20).copy(alpha = 0.92f),
                        Color(0xFF0B1116).copy(alpha = 0.96f)
                    )
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.105f), RoundedCornerShape(18.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.07f), Accent.copy(alpha = 0.035f))))
                .padding(horizontal = 16.dp, vertical = 13.dp)
        ) {
            Text(title, color = SoftText, fontSize = 10.8.sp, fontWeight = FontWeight.Black)
        }
        Column(Modifier.padding(horizontal = 16.dp, vertical = 15.dp)) {
            content()
        }
    }
}

@Composable
private fun TwoColumnStats(title: String, stats: List<Pair<String, String>>) {
    SectionPanel(title) {
        stats.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { stat ->
                    StatTile(stat.first, stat.second, Modifier.weight(1f))
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
private fun LastSixBalls() {
    SectionPanel("LAST 6 BALLS") {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(11.dp), verticalAlignment = Alignment.CenterVertically) {
            listOf("6", "1", "4", "W", "2", ".").forEachIndexed { index, ball ->
                BallBubble(ball = ball, index = index)
            }
            Spacer(Modifier.weight(1f))
            Text("18.4 OV", color = SoftText, fontSize = 9.5.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun BallBubble(ball: String, index: Int) {
    val scale by animateFloatAsState(
        targetValue = if (index == 0) 1.08f else 1f,
        animationSpec = tween(420),
        label = "ballEntry"
    )
    val colors = when (ball) {
        "6" -> listOf(Accent, Color(0xFF7BFF2B))
        "4" -> listOf(Color(0xFF8EEA42), Color(0xFF16B85E))
        "W" -> listOf(Color(0xFFFF5F6D), Color(0xFF99001E))
        "." -> listOf(Color(0xFF30383D), Color(0xFF11171B))
        else -> listOf(Color(0xFF40484E), Color(0xFF1A2227))
    }
    val textColor = if (ball == "6" || ball == "4") ScreenBg else Color.White
    Box(
        modifier = Modifier
            .size(44.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .shadow(12.dp, CircleShape, clip = false)
            .clip(CircleShape)
            .background(Brush.linearGradient(colors))
            .border(1.dp, Color.White.copy(alpha = 0.14f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(ball, color = textColor, fontSize = 15.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun CurrentBattingCard() {
    SectionPanel("CURRENT BATTING") {
        PlayerTableHeader("BATTER", listOf("R", "B", "4S", "6S", "SR"))
        ThinRule()
        BatterTableRow("A. Kumar*", "34", "20", "3", "1", "170.0", active = true)
        ThinRule()
        BatterTableRow("R. Singh", "14", "11", "1", "0", "127.2", active = false)
    }
}

@Composable
private fun PlayerTableHeader(first: String, columns: List<String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.055f))
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(first, color = SoftText.copy(alpha = 0.75f), fontSize = 8.4.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1.9f))
        columns.forEach { label ->
            Text(label, color = SoftText.copy(alpha = 0.75f), fontSize = 8.4.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.End, modifier = Modifier.weight(0.62f))
        }
    }
}

@Composable
private fun BatterTableRow(name: String, runs: String, balls: String, fours: String, sixes: String, sr: String, active: Boolean) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.985f else 1f, label = "batterRowPress")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clip(RoundedCornerShape(14.dp))
            .background(if (active) Accent.copy(alpha = 0.105f) else Color.Transparent)
            .clickable(interactionSource = interactionSource, indication = null, onClick = {})
            .padding(horizontal = 10.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            name,
            color = if (active) Accent else Color(0xFFE4ECE8),
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            fontStyle = FontStyle.Italic,
            modifier = Modifier.weight(1.9f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        listOf(runs, balls, fours, sixes, sr).forEachIndexed { index, value ->
            Text(
                value,
                color = if (index == 0) Accent else Color(0xFFD9E0DD),
                fontSize = if (index == 4) 11.sp else 12.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(0.62f)
            )
        }
    }
}

@Composable
private fun PartnershipCard() {
    SectionPanel("CURRENT PARTNERSHIP") {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("52", color = Accent, fontSize = 31.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
                Text("RUNS", color = SoftText, fontSize = 8.5.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.width(10.dp))
            Text("(31 BALLS)", color = Color.White.copy(alpha = 0.72f), fontSize = 10.5.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.weight(1f))
            Text("RR 10.06", color = SoftText, fontSize = 9.5.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(16.dp))
        ContributionBar(leftFraction = 0.65f)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth()) {
            Text("A. Kumar 65%", color = Accent, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
            Text("R. Singh 35%", color = Color(0xFFD7E2DF), fontSize = 9.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(14.dp))
        PartnershipRate()
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth()) {
            PartnershipPlayer("A. Kumar", "34 (20)", Modifier.weight(1f), alignEnd = false)
            PartnershipPlayer("R. Singh", "14 (11)", Modifier.weight(1f), alignEnd = true)
        }
    }
}

@Composable
private fun PartnershipPlayer(name: String, score: String, modifier: Modifier, alignEnd: Boolean) {
    Column(modifier, horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start) {
        Text(name, color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
        Text(score, color = SoftText, fontSize = 9.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun ContributionBar(leftFraction: Float) {
    val animated by animateFloatAsState(targetValue = leftFraction, animationSpec = tween(760), label = "partnershipContribution")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .shadow(8.dp, RoundedCornerShape(7.dp), clip = false)
            .clip(RoundedCornerShape(7.dp))
            .background(Color(0xFF232A2F))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animated)
                .height(12.dp)
                .background(Brush.horizontalGradient(listOf(Accent, Color(0xFFA3D800))))
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(12.dp)
                .background(Color.White.copy(alpha = 0.24f))
        )
    }
}

@Composable
private fun PartnershipRate() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF02080D).copy(alpha = 0.72f))
            .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text("RATE VISUAL", color = SoftText, fontSize = 7.5.sp, fontWeight = FontWeight.Black)
            Text("10.06 RPO", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black)
        }
        repeat(8) { index ->
            val height = (16 + index * 4).dp
            Box(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .width(7.dp)
                    .height(height)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (index > 4) Accent else Color.White.copy(alpha = 0.22f))
            )
        }
    }
}

@Composable
private fun CurrentBowlingCard() {
    SectionPanel("CURRENT BOWLING") {
        PlayerTableHeader("BOWLER", listOf("O", "M", "R", "W", "ECN"))
        ThinRule()
        BowlerTableRow("J. Hazlewood", "3.0", "0", "22", "2", "7.33")
        ThinRule()
        BowlerTableRow("R. Khan", "4.0", "0", "31", "1", "7.75")
    }
}

@Composable
private fun BowlerTableRow(name: String, overs: String, maidens: String, runs: String, wickets: String, economy: String) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.985f else 1f, label = "bowlerRowPress")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clip(RoundedCornerShape(14.dp))
            .background(if (wickets.toIntOrNull() ?: 0 >= 2) ElectricBlue.copy(alpha = 0.105f) else Color.Transparent)
            .clickable(interactionSource = interactionSource, indication = null, onClick = {})
            .padding(horizontal = 10.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, color = if (wickets.toIntOrNull() ?: 0 >= 2) CyanLine else Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic, modifier = Modifier.weight(1.9f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        listOf(overs, maidens, runs, wickets, economy).forEachIndexed { index, value ->
            Text(value, color = if (index == 3) Accent else Color(0xFFD9E0DD), fontSize = if (index == 4) 11.sp else 12.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.End, modifier = Modifier.weight(0.62f))
        }
    }
}

@Composable
private fun ThinRule() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color.White.copy(alpha = 0.055f))
    )
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier) {
    Column(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(Color(0xFF02080D))
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(3.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(label.uppercase(), color = SoftText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(5.dp))
        Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun WormPanel() {
    SectionPanel("RUN WORM") {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(124.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(Color(0xFF02070B))
                .padding(8.dp)
        ) {
            val w = size.width
            val h = size.height
            repeat(4) { i ->
                val y = h * (0.2f + i * 0.19f)
                drawLine(Color.White.copy(alpha = 0.07f), Offset(0f, y), Offset(w, y), 1.2f)
            }
            val first = Path().apply {
                moveTo(w * 0.04f, h * 0.82f)
                lineTo(w * 0.22f, h * 0.73f)
                lineTo(w * 0.43f, h * 0.57f)
                lineTo(w * 0.64f, h * 0.39f)
                lineTo(w * 0.95f, h * 0.18f)
            }
            val second = Path().apply {
                moveTo(w * 0.04f, h * 0.88f)
                lineTo(w * 0.24f, h * 0.77f)
                lineTo(w * 0.45f, h * 0.65f)
                lineTo(w * 0.68f, h * 0.48f)
                lineTo(w * 0.95f, h * 0.31f)
            }
            drawPath(second, ElectricBlue, style = Stroke(3.4f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawPath(first, Accent, style = Stroke(3.8f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }
}

@Composable
private fun RunWormPanel() {
    SectionPanel("RUN WORM") {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.weight(1f))
            LegendLine("NSC", Accent, dashed = false)
            Spacer(Modifier.width(14.dp))
            LegendLine("VCR", Color(0xFFB4B5B7), dashed = true)
        }
        Spacer(Modifier.height(10.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(194.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Accent.copy(alpha = 0.075f), Color(0xFF02070B), Color(0xFF02070B))
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.055f), RoundedCornerShape(5.dp))
                .padding(12.dp)
        ) {
            val w = size.width
            val h = size.height
            repeat(6) { i ->
                val y = h * (0.12f + i * 0.145f)
                drawLine(Color.White.copy(alpha = 0.05f), Offset(w * 0.02f, y), Offset(w * 0.98f, y), 1.05f)
            }
            repeat(5) { i ->
                val x = w * (0.08f + i * 0.22f)
                drawLine(Color.White.copy(alpha = 0.032f), Offset(x, h * 0.1f), Offset(x, h * 0.9f), 1f)
            }
            val nsc = Path().apply {
                moveTo(w * 0.04f, h * 0.88f)
                cubicTo(w * 0.14f, h * 0.82f, w * 0.25f, h * 0.75f, w * 0.35f, h * 0.68f)
                cubicTo(w * 0.48f, h * 0.58f, w * 0.57f, h * 0.48f, w * 0.68f, h * 0.4f)
                cubicTo(w * 0.79f, h * 0.31f, w * 0.88f, h * 0.22f, w * 0.96f, h * 0.12f)
            }
            val vcr = Path().apply {
                moveTo(w * 0.04f, h * 0.9f)
                cubicTo(w * 0.17f, h * 0.86f, w * 0.29f, h * 0.8f, w * 0.41f, h * 0.7f)
                cubicTo(w * 0.53f, h * 0.61f, w * 0.64f, h * 0.56f, w * 0.76f, h * 0.47f)
                cubicTo(w * 0.86f, h * 0.39f, w * 0.92f, h * 0.33f, w * 0.96f, h * 0.25f)
            }
            drawLine(Color.White.copy(alpha = 0.08f), Offset(w * 0.02f, h * 0.92f), Offset(w * 0.98f, h * 0.92f), 1.2f)
            drawLine(Color.White.copy(alpha = 0.08f), Offset(w * 0.02f, h * 0.1f), Offset(w * 0.02f, h * 0.92f), 1.2f)
            drawPath(vcr, Color(0xFFB4B5B7), style = Stroke(2.8f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawPath(nsc, Accent, style = Stroke(3.3f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
        Spacer(Modifier.height(7.dp))
        Row(Modifier.fillMaxWidth()) {
            Text("0 OVER", color = SoftText, fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
            Text("10", color = SoftText, fontSize = 8.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            Text("20", color = SoftText, fontSize = 8.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun LegendLine(label: String, color: Color, dashed: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            repeat(if (dashed) 2 else 1) {
                Box(
                    Modifier
                        .width(if (dashed) 5.dp else 12.dp)
                        .height(2.dp)
                        .background(color)
                )
            }
        }
        Spacer(Modifier.width(4.dp))
        Text(label, color = SoftText, fontSize = 8.5.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun MatchInsights(match: MatchDetail) {
    SectionPanel("MATCH INSIGHTS") {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            InsightMetric("Projected", "168", Modifier.weight(1f))
            InsightMetric("CRR", "7.61", Modifier.weight(1f))
            InsightMetric("RRR", match.rrr, Modifier.weight(1f))
        }
        Spacer(Modifier.height(7.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            InsightMetric("Best Bowler", "JH 2/22", Modifier.weight(1f))
            InsightMetric("High P'ship", "52", Modifier.weight(1f))
        }
        Spacer(Modifier.height(9.dp))
        WinPredictor()
    }
}

@Composable
private fun InsightMetric(label: String, value: String, modifier: Modifier) {
    Column(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF02080D))
            .border(1.dp, Color.White.copy(alpha = 0.055f), RoundedCornerShape(4.dp))
            .padding(7.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(label.uppercase(), color = SoftText, fontSize = 7.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(value, color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun WinPredictor() {
    val animated by animateFloatAsState(targetValue = 0.61f, label = "winPredictor")
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("NSC 61%", color = Accent, fontSize = 10.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.weight(1f))
        Text("VCR 39%", color = ElectricBlue, fontSize = 10.sp, fontWeight = FontWeight.Black)
    }
    Spacer(Modifier.height(5.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(Color(0xFF111A20))
    ) {
        Box(Modifier.fillMaxWidth(animated).height(10.dp).background(Accent))
        Box(Modifier.weight(1f).height(10.dp).background(ElectricBlue))
    }
}

@Composable
private fun FallOfWicketsPremium() {
    SectionPanel("FALL OF WICKETS") {
        listOf(
            Triple("1-28", "L. Shaw, 3.2", false),
            Triple("2-45", "S. Smith, 6.4", false),
            Triple("3-82", "K. Williamson, 12.1", false),
            Triple("4-135", "T. Head, 16.2", true)
        ).forEach { wicket ->
            PremiumWicketRow(score = wicket.first, detail = wicket.second, latest = wicket.third)
        }
    }
}

@Composable
private fun PremiumWicketRow(score: String, detail: String, latest: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(if (latest) 11.dp else 9.dp)
                .clip(CircleShape)
                .background(if (latest) Accent else Color(0xFF52605D))
        )
        Spacer(Modifier.width(10.dp))
        Text(
            if (latest) "Last Wicket: $score ($detail)" else "$score ($detail)",
            color = if (latest) Accent else Color.White,
            fontSize = 11.2.sp,
            fontWeight = if (latest) FontWeight.Black else FontWeight.Bold,
            fontStyle = if (latest) FontStyle.Italic else FontStyle.Normal,
            modifier = Modifier.weight(1f)
        )
        if (latest) {
            Text(
                "LIVE",
                color = ScreenBg,
                fontSize = 7.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(Accent)
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun FallOfWicketsTimeline(match: MatchDetail) {
    SectionPanel("FALL OF WICKETS") {
        listOf(
            "24/1" to "3.2 ov",
            "68/2" to "8.5 ov",
            "111/3" to "14.1 ov",
            match.leftScore to "17.0 ov"
        ).forEachIndexed { index, wicket ->
            WicketTimelineRow(score = wicket.first, over = wicket.second, latest = index == 3)
        }
    }
}

@Composable
private fun WicketTimelineRow(score: String, over: String, latest: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(if (latest) 9.dp else 7.dp)
                .clip(CircleShape)
                .background(if (latest) Accent else Color(0xFF52605D))
        )
        Spacer(Modifier.width(8.dp))
        Text(score, color = if (latest) Accent else Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.width(6.dp))
        Text("•", color = SoftText, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(6.dp))
        Text(over, color = SoftText, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        if (latest) {
            Spacer(Modifier.weight(1f))
            Text("LATEST", color = ScreenBg, fontSize = 5.8.sp, fontWeight = FontWeight.Black, modifier = Modifier.clip(RoundedCornerShape(3.dp)).background(Accent).padding(horizontal = 5.dp, vertical = 2.dp))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label.uppercase(), color = SoftText, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(1.3f))
    }
}

@Composable
private fun TableHeader(first: String, second: String, third: String, fourth: String) {
    Row(Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(first, color = SoftText, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.9f))
        listOf(second, third, fourth).forEach {
            Text(it, color = SoftText, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(0.7f))
        }
    }
}

@Composable
private fun ScoreLine(name: String, a: String, b: String, c: String, color: Color) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(name, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.9f))
        listOf(a, b, c).forEach {
            Text(it, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.weight(0.7f))
        }
    }
}

@Composable
private fun CommentaryLine(over: String, text: String, color: Color) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(over, color = color, fontSize = 13.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(46.dp))
        Text(text, color = Color(0xFFD7E2DF), fontSize = 13.sp, fontWeight = FontWeight.Bold, lineHeight = 18.sp, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun LiveBadge() {
    val infinite = rememberInfiniteTransition(label = "livePulse")
    val pulse by infinite.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(820), repeatMode = RepeatMode.Reverse),
        label = "livePulseAlpha"
    )
    val halo by animateDpAsState(targetValue = (7 + pulse * 5).dp, label = "livePulseHalo")
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Accent.copy(alpha = 0.12f))
            .border(1.dp, Accent.copy(alpha = 0.55f), RoundedCornerShape(18.dp))
            .padding(horizontal = 9.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(halo)
                    .clip(CircleShape)
                    .background(Accent.copy(alpha = 0.18f * pulse))
            )
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(Accent)
            )
        }
        Spacer(Modifier.width(5.dp))
        Text("LIVE", color = Accent, fontSize = 8.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun StatusPill(text: String, color: Color) {
    Text(
        text = text,
        color = color,
        fontSize = 6.2.sp,
        fontWeight = FontWeight.Black,
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.55f), RoundedCornerShape(3.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    )
}

@Composable
private fun MetricIconView(icon: MetricIcon, tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val s = minOf(w, h)
        val stroke = Stroke(width = s * 0.11f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        when (icon) {
            MetricIcon.SPEED -> {
                drawArc(tint, 205f, 130f, false, style = stroke)
                drawLine(tint, Offset(w * 0.5f, h * 0.55f), Offset(w * 0.74f, h * 0.34f), stroke.width, StrokeCap.Round)
                drawCircle(tint, s * 0.06f, Offset(w * 0.5f, h * 0.55f))
            }
            MetricIcon.TARGET -> {
                drawCircle(tint, s * 0.38f, Offset(w * 0.5f, h * 0.5f), style = stroke)
                drawCircle(tint, s * 0.2f, Offset(w * 0.5f, h * 0.5f), style = stroke)
                drawCircle(tint, s * 0.06f, Offset(w * 0.5f, h * 0.5f))
            }
            MetricIcon.TREND -> {
                val path = Path().apply {
                    moveTo(w * 0.14f, h * 0.72f)
                    lineTo(w * 0.38f, h * 0.5f)
                    lineTo(w * 0.55f, h * 0.58f)
                    lineTo(w * 0.84f, h * 0.26f)
                }
                drawPath(path, tint, style = stroke)
                drawLine(tint, Offset(w * 0.7f, h * 0.26f), Offset(w * 0.84f, h * 0.26f), stroke.width, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.84f, h * 0.26f), Offset(w * 0.84f, h * 0.42f), stroke.width, StrokeCap.Round)
            }
            MetricIcon.TROPHY -> {
                drawRoundRect(tint, Offset(w * 0.3f, h * 0.18f), androidx.compose.ui.geometry.Size(w * 0.4f, h * 0.38f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(s * 0.08f), style = stroke)
                drawLine(tint, Offset(w * 0.5f, h * 0.56f), Offset(w * 0.5f, h * 0.78f), stroke.width, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.33f, h * 0.82f), Offset(w * 0.67f, h * 0.82f), stroke.width, StrokeCap.Round)
                drawArc(tint, 90f, 120f, false, topLeft = Offset(w * 0.08f, h * 0.22f), size = androidx.compose.ui.geometry.Size(w * 0.34f, h * 0.32f), style = stroke)
                drawArc(tint, -30f, 120f, false, topLeft = Offset(w * 0.58f, h * 0.22f), size = androidx.compose.ui.geometry.Size(w * 0.34f, h * 0.32f), style = stroke)
            }
        }
    }
}

@Composable
private fun EyeIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val s = minOf(w, h)
        val stroke = Stroke(width = s * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val eye = Path().apply {
            moveTo(w * 0.08f, h * 0.5f)
            cubicTo(w * 0.24f, h * 0.24f, w * 0.42f, h * 0.18f, w * 0.5f, h * 0.18f)
            cubicTo(w * 0.58f, h * 0.18f, w * 0.76f, h * 0.24f, w * 0.92f, h * 0.5f)
            cubicTo(w * 0.76f, h * 0.76f, w * 0.58f, h * 0.82f, w * 0.5f, h * 0.82f)
            cubicTo(w * 0.42f, h * 0.82f, w * 0.24f, h * 0.76f, w * 0.08f, h * 0.5f)
            close()
        }
        drawPath(eye, tint, style = stroke)
        drawOval(
            color = tint,
            topLeft = Offset(w * 0.39f, h * 0.35f),
            size = Size(w * 0.22f, h * 0.3f)
        )
    }
}

@Composable
private fun IconTile(icon: DetailIcon, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(Color(0xFF02070B))
            .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(9.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        DetailIconView(icon, Color.White, Modifier.size(22.dp))
    }
}

@Composable
private fun DetailIconView(icon: DetailIcon, tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val s = minOf(w, h)
        val stroke = Stroke(width = s * 0.085f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        when (icon) {
            DetailIcon.BACK -> {
                drawLine(tint, Offset(w * 0.27f, h * 0.5f), Offset(w * 0.76f, h * 0.5f), stroke.width, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.27f, h * 0.5f), Offset(w * 0.48f, h * 0.3f), stroke.width, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.27f, h * 0.5f), Offset(w * 0.48f, h * 0.7f), stroke.width, StrokeCap.Round)
            }
            DetailIcon.SEARCH -> {
                drawCircle(tint, s * 0.2f, Offset(w * 0.45f, h * 0.43f), style = stroke)
                drawLine(tint, Offset(w * 0.6f, h * 0.58f), Offset(w * 0.78f, h * 0.76f), stroke.width, StrokeCap.Round)
            }
            DetailIcon.BELL -> {
                val path = Path().apply {
                    moveTo(w * 0.32f, h * 0.58f)
                    cubicTo(w * 0.34f, h * 0.36f, w * 0.38f, h * 0.25f, w * 0.5f, h * 0.25f)
                    cubicTo(w * 0.62f, h * 0.25f, w * 0.66f, h * 0.36f, w * 0.68f, h * 0.58f)
                    lineTo(w * 0.76f, h * 0.7f)
                    lineTo(w * 0.24f, h * 0.7f)
                    close()
                }
                drawPath(path, tint, style = stroke)
                drawLine(tint, Offset(w * 0.44f, h * 0.8f), Offset(w * 0.56f, h * 0.8f), stroke.width, StrokeCap.Round)
            }
            DetailIcon.MESSAGE -> {
                drawRoundRect(tint, Offset(w * 0.22f, h * 0.28f), androidx.compose.ui.geometry.Size(w * 0.56f, h * 0.38f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(s * 0.08f), style = stroke)
                val path = Path().apply {
                    moveTo(w * 0.4f, h * 0.66f)
                    lineTo(w * 0.32f, h * 0.8f)
                    lineTo(w * 0.52f, h * 0.66f)
                }
                drawPath(path, tint, style = stroke)
            }
            DetailIcon.SHIELD -> {
                val path = Path().apply {
                    moveTo(w * 0.5f, h * 0.16f)
                    lineTo(w * 0.77f, h * 0.3f)
                    lineTo(w * 0.7f, h * 0.66f)
                    lineTo(w * 0.5f, h * 0.85f)
                    lineTo(w * 0.3f, h * 0.66f)
                    lineTo(w * 0.23f, h * 0.3f)
                    close()
                }
                drawPath(path, tint)
            }
            DetailIcon.BOLT -> {
                val path = Path().apply {
                    moveTo(w * 0.58f, h * 0.12f)
                    lineTo(w * 0.3f, h * 0.56f)
                    lineTo(w * 0.52f, h * 0.56f)
                    lineTo(w * 0.42f, h * 0.88f)
                    lineTo(w * 0.72f, h * 0.42f)
                    lineTo(w * 0.5f, h * 0.42f)
                    close()
                }
                drawPath(path, tint)
            }
            DetailIcon.DOTS -> {
                drawCircle(tint, s * 0.055f, Offset(w * 0.3f, h * 0.5f))
                drawCircle(tint, s * 0.055f, Offset(w * 0.5f, h * 0.5f))
                drawCircle(tint, s * 0.055f, Offset(w * 0.7f, h * 0.5f))
            }
        }
    }
}
