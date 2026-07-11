package com.example.sportsxtreme

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.ComponentActivity
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class ViewAllScoreCardActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applySportsXtremeWindowStyle()
        setContent {
            ViewAllScoreCardScreen(
                onBack = { finish() },
                onOpenScorecard = { score ->
                    startActivity(
                        Intent(this, ScorecardActivity::class.java).apply {
                            putExtra(ScorecardActivity.EXTRA_LEAGUE, score.league)
                            putExtra(ScorecardActivity.EXTRA_ROUND, score.round)
                            putExtra(ScorecardActivity.EXTRA_LEFT_NAME, score.leftName)
                            putExtra(ScorecardActivity.EXTRA_LEFT_SCORE, score.leftScore)
                            putExtra(ScorecardActivity.EXTRA_LEFT_OVERS, score.leftOvers)
                            putExtra(ScorecardActivity.EXTRA_RIGHT_NAME, score.rightName)
                            putExtra(ScorecardActivity.EXTRA_RIGHT_SCORE, score.rightScore)
                            putExtra(ScorecardActivity.EXTRA_RIGHT_OVERS, score.rightOvers)
                            putExtra(ScorecardActivity.EXTRA_TARGET, score.target)
                            putExtra(ScorecardActivity.EXTRA_RRR, score.rrr)
                            putExtra(ScorecardActivity.EXTRA_WIN, score.win)
                            putExtra(ScorecardActivity.EXTRA_NOTE, score.note)
                        }
                    )
                }
            )
        }
    }
}

private data class ScoreCardItem(
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

private val scoreCards = listOf(
    ScoreCardItem("VALORANT PRO LEAGUE", "Semi-final - Match 07", "NSC", "142/4", "18.4 OV", "VCR", "138/6", "18.1 OV", "156", "8.42", "NSC 61%", "VCR chose to bowl - Powerplay complete"),
    ScoreCardItem("CRICKET PREMIER CUP", "Qualifier - Match 12", "BBS", "96/2", "11.3 OV", "KDP", "94/7", "15.0 OV", "148", "7.86", "BBS 68%", "BBS need 52 from 51 balls"),
    ScoreCardItem("FOOTBALL NIGHT CUP", "Group A - Match 04", "FCR", "2", "62 MIN", "BLU", "1", "62 MIN", "90", "LIVE", "FCR 54%", "High press active in the second half"),
    ScoreCardItem("KABADDI ARENA", "Eliminator - Match 03", "KAL", "31", "HT", "TIT", "27", "HT", "40", "BONUS", "KAL 58%", "KAL lead by 4 with two super tackles"),
    ScoreCardItem("BADMINTON OPEN", "Court 2 - Set 03", "RAY", "18", "SET 3", "DEV", "16", "SET 3", "21", "RALLY", "RAY 63%", "Both players trading long baseline rallies")
)

private val Primary = Color(0xFFC1FF00)
private val Cyan = Color(0xFF00D2FF)
private val PageBg = Color(0xFF000307)
private val Panel = Color(0xFF060E14)
private val Muted = Color(0xFF7F9196)

private enum class ScoreUiIcon { BACK, SORT, SEARCH, LOCATION, SHIELD, BOLT }

@Composable
private fun ViewAllScoreCardScreen(onBack: () -> Unit, onOpenScorecard: (ScoreCardItem) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF00384C),
                        PageBg,
                        Color(0xFF243D05)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, top = 22.dp, end = 16.dp)
        ) {
            TopBar(onBack)
            Spacer(Modifier.height(22.dp))
            Text(
                text = "Live matches near you",
                color = Primary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(18.dp))
            ControlRow()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(top = 18.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                scoreCards.forEach { score ->
                    ScoreCard(score, onClick = { onOpenScorecard(score) })
                }
            }
        }
    }
}

@Composable
private fun TopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButtonIcon(ScoreUiIcon.BACK, onBack)
        Text(
            text = "All Scorecards",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic,
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        )
        LocationChip()
    }
}

@Composable
private fun ControlRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ActionPill(ScoreUiIcon.SORT, "Sort", Modifier.weight(1f))
        ActionPill(ScoreUiIcon.SEARCH, "Search", Modifier.weight(1f))
        ActionPill(ScoreUiIcon.LOCATION, "Choose Location", Modifier.weight(1.28f), accent = Primary)
    }
}

@Composable
private fun IconButtonIcon(icon: ScoreUiIcon, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(Panel.copy(alpha = 0.92f))
            .border(1.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(9.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        ScoreIcon(icon = icon, tint = Color.White, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun LocationChip() {
    Row(
        modifier = Modifier
            .height(42.dp)
            .clip(RoundedCornerShape(21.dp))
            .background(Primary.copy(alpha = 0.16f))
            .border(1.dp, Primary.copy(alpha = 0.55f), RoundedCornerShape(21.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ScoreIcon(icon = ScoreUiIcon.LOCATION, tint = Primary, modifier = Modifier.size(17.dp))
        Spacer(Modifier.width(6.dp))
        Text(text = "Madanpur", color = Color(0xFFE4EEE8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ActionPill(icon: ScoreUiIcon, label: String, modifier: Modifier = Modifier, accent: Color = Color.White) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(9.dp))
            .background(Panel.copy(alpha = 0.92f))
            .border(1.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(9.dp))
            .clickable { },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ScoreIcon(icon = icon, tint = accent, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(7.dp))
        Text(
            text = label,
            color = accent,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ScoreCard(score: ScoreCardItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF0D3263), Color(0xFF07111C), Color(0xFF071C3A))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = score.league,
                    color = Color(0xFFCCD9E5),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(text = score.round, color = Muted, fontSize = 8.2.sp, fontWeight = FontWeight.Bold)
            }
            Text(text = "LIVE", color = Color(0xFFFF3A48), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(18.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(116.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TeamBox(score.leftName, score.leftScore, score.leftOvers, ScoreUiIcon.SHIELD, Primary, Modifier.weight(1f))
            VsBubble()
            TeamBox(score.rightName, score.rightScore, score.rightOvers, ScoreUiIcon.BOLT, Cyan, Modifier.weight(1f))
        }

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            StatChip("TARGET", score.target)
            Spacer(Modifier.width(8.dp))
            StatChip("RRR", score.rrr)
            Spacer(Modifier.width(8.dp))
            StatChip("WIN", score.win)
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = score.note,
            color = Color(0xFFB2BFCD),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun TeamBox(
    name: String,
    score: String,
    detail: String,
    icon: ScoreUiIcon,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(10.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF17232F).copy(alpha = 0.92f), Color(0xFF080F16).copy(alpha = 0.95f))))
            .border(1.dp, accent.copy(alpha = 0.32f), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.16f))
                .border(1.dp, accent.copy(alpha = 0.55f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            ScoreIcon(icon = icon, tint = accent, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(text = name, color = accent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Text(
            text = score,
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic
        )
        Text(text = detail, color = Color(0xFF7D8B9A), fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun VsBubble() {
    Box(
        modifier = Modifier
            .width(54.dp)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Primary)
                .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "VS", color = Color(0xFF070E14), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StatChip(label: String, value: String) {
    Row(
        modifier = Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, Color.Black.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
            .padding(horizontal = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color(0xFF222222), fontSize = 7.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(5.dp))
        Text(text = value, color = Color.Black, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ScoreIcon(icon: ScoreUiIcon, tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val s = minOf(w, h)
        val stroke = Stroke(
            width = s * 0.085f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

        when (icon) {
            ScoreUiIcon.BACK -> {
                drawLine(tint, Offset(w * 0.28f, h * 0.5f), Offset(w * 0.75f, h * 0.5f), stroke.width, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.28f, h * 0.5f), Offset(w * 0.48f, h * 0.3f), stroke.width, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.28f, h * 0.5f), Offset(w * 0.48f, h * 0.7f), stroke.width, StrokeCap.Round)
            }
            ScoreUiIcon.SORT -> {
                drawLine(tint, Offset(w * 0.22f, h * 0.32f), Offset(w * 0.78f, h * 0.32f), stroke.width, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.32f, h * 0.5f), Offset(w * 0.68f, h * 0.5f), stroke.width, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.42f, h * 0.68f), Offset(w * 0.58f, h * 0.68f), stroke.width, StrokeCap.Round)
            }
            ScoreUiIcon.SEARCH -> {
                drawCircle(
                    color = tint,
                    radius = s * 0.2f,
                    center = Offset(w * 0.45f, h * 0.43f),
                    style = stroke
                )
                drawLine(tint, Offset(w * 0.6f, h * 0.58f), Offset(w * 0.78f, h * 0.76f), stroke.width, StrokeCap.Round)
            }
            ScoreUiIcon.LOCATION -> {
                val path = Path().apply {
                    moveTo(w * 0.5f, h * 0.9f)
                    cubicTo(w * 0.22f, h * 0.58f, w * 0.2f, h * 0.38f, w * 0.2f, h * 0.32f)
                    cubicTo(w * 0.2f, h * 0.14f, w * 0.34f, h * 0.08f, w * 0.5f, h * 0.08f)
                    cubicTo(w * 0.66f, h * 0.08f, w * 0.8f, h * 0.14f, w * 0.8f, h * 0.32f)
                    cubicTo(w * 0.8f, h * 0.38f, w * 0.78f, h * 0.58f, w * 0.5f, h * 0.9f)
                }
                drawPath(path, tint, style = stroke)
                drawCircle(tint, radius = w * 0.11f, center = Offset(w * 0.5f, h * 0.32f), style = stroke)
            }
            ScoreUiIcon.SHIELD -> {
                val path = Path().apply {
                    moveTo(w * 0.5f, h * 0.18f)
                    lineTo(w * 0.76f, h * 0.3f)
                    lineTo(w * 0.7f, h * 0.66f)
                    lineTo(w * 0.5f, h * 0.84f)
                    lineTo(w * 0.3f, h * 0.66f)
                    lineTo(w * 0.24f, h * 0.3f)
                    close()
                }
                drawPath(path, tint)
            }
            ScoreUiIcon.BOLT -> {
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
        }
    }
}
