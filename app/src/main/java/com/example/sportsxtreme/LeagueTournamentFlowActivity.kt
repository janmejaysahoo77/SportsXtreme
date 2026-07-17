package com.example.sportsxtreme

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat

class LeagueTournamentFlowActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        setContent {
            LeagueTournamentFlowScreen(
                onBack = { finish() },
                onContinue = {
                    startActivity(Intent(this, LeagueMatchSetupActivity::class.java))
                }
            )
        }
    }
}

private val FlowAccent = Color(0xFFC1FF00)
private val FlowBg = Color(0xFF010509)
private val FlowPanel = Color(0xFF07101A)
private val FlowCard = Color(0xFF0C1524)
private val FlowCardAlt = Color(0xFF111B2C)
private val FlowStroke = Color(0xFF28344A)
private val FlowMuted = Color(0xFF9AA9A6)

@Composable
private fun LeagueTournamentFlowScreen(onBack: () -> Unit, onContinue: () -> Unit) {
    var advancedOpen by remember { mutableStateOf(false) }
    var qualifiers by remember {
        mutableStateOf(
            listOf(
                FlowOption("Qualifier 1", false),
                FlowOption("Eliminator", false),
                FlowOption("Qualifier 2", false),
                FlowOption("Super 4", false),
                FlowOption("Super 6", false),
                FlowOption("Gold Final", false),
                FlowOption("Silver Final", false)
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FlowBg)
            .drawBehind { drawFlowBackground() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            FlowTopBar(onBack)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Choose the stages that will be used in this league tournament.",
                    color = FlowMuted,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                TournamentHeaderCard()
                SectionLabel("RECOMMENDED STAGES", "MOST LEAGUE TOURNAMENTS FOLLOW THIS STRUCTURE")
                RecommendedStageList()
                TournamentFlowPreview()
                AdvancedStagesCard(
                    expanded = advancedOpen,
                    options = qualifiers,
                    onToggleExpanded = { advancedOpen = !advancedOpen },
                    onToggleOption = { index ->
                        qualifiers = qualifiers.mapIndexed { optionIndex, option ->
                            if (optionIndex == index) option.copy(selected = !option.selected) else option
                        }
                    }
                )
                InfoNote("Tournament stages generate rankings, standings and progression automatically based on real-time data entry.")
                ContinueFlowButton(onContinue)
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFlowBackground() {
    drawRect(Brush.verticalGradient(listOf(Color(0xFF02060B), Color(0xFF07101A), Color(0xFF02060B))))
    drawCircle(Color(0x20264112), radius = size.width * 0.78f, center = Offset(size.width * 0.96f, size.height * 0.13f))
    drawCircle(Color(0x141E72FF), radius = size.width * 0.56f, center = Offset(size.width * 0.06f, size.height * 0.72f))
}

@Composable
private fun FlowTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(FlowPanel)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FlowArrow(Modifier.size(23.dp).clickable(onClick = onBack), right = false, tint = Color.White)
        Text(
            "League Tournament Flow",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 13.dp).weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        HelpCircle(Modifier.size(22.dp))
    }
}

@Composable
private fun TournamentHeaderCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(136.dp)
            .drawBehind {
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.46f),
                    topLeft = Offset(0f, 10.dp.toPx()),
                    size = Size(size.width, size.height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(14.dp.toPx(), 14.dp.toPx())
                )
                drawRoundRect(
                    color = FlowAccent.copy(alpha = 0.12f),
                    topLeft = Offset(0f, 3.dp.toPx()),
                    size = Size(size.width, size.height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(14.dp.toPx(), 14.dp.toPx())
                )
            }
            .shadow(22.dp, RoundedCornerShape(14.dp), clip = false)
            .clip(RoundedCornerShape(14.dp))
            .background(FlowCardAlt)
            .drawBehind {
                drawCircle(
                    color = Color.White.copy(alpha = 0.22f),
                    radius = size.width * 0.34f,
                    center = Offset(size.width * 1.02f, -size.height * 0.08f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.12f),
                    radius = size.width * 0.48f,
                    center = Offset(size.width * 0.94f, size.height * 0.02f)
                )
                drawCircle(
                    color = FlowAccent.copy(alpha = 0.1f),
                    radius = size.width * 0.42f,
                    center = Offset(size.width * 0.88f, size.height * 0.18f)
                )
            }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(6.dp)
                .height(104.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(FlowAccent)
        )
        Column(modifier = Modifier.padding(start = 13.dp).weight(1f)) {
            Text("Dubai Premier League", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Black, maxLines = 1)
            Box(
                modifier = Modifier
                    .padding(top = 5.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(FlowAccent)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text("ACTIVE SEASON 2024", color = Color(0xFF111604), fontSize = 8.8.sp, fontWeight = FontWeight.Black)
            }
            Row(
                modifier = Modifier.padding(top = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(17.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeaderMetric("Dubai", FlowGlyph.PIN)
                HeaderMetric("8\nTeams", FlowGlyph.TEAMS)
                HeaderMetric("CHAMPIONSHIP", FlowGlyph.TROPHY)
            }
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
                .drawBehind {
                    drawCircle(FlowAccent.copy(alpha = 0.2f), radius = size.minDimension * 0.7f)
                },
            contentAlignment = Alignment.Center
        ) {
            FlowGlyphIcon(FlowGlyph.SHARE, Modifier.size(22.dp), FlowAccent)
        }
    }
}

@Composable
private fun HeaderMetric(text: String, glyph: FlowGlyph) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        FlowGlyphIcon(glyph, Modifier.size(12.dp), FlowAccent)
        Text(text, color = Color.White, fontSize = 10.sp, lineHeight = 10.5.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 5.dp))
    }
}

@Composable
private fun SectionLabel(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, color = Color(0xFFB8C4C0), fontSize = 13.sp, fontWeight = FontWeight.Black)
        Text(subtitle, color = Color(0xFF7F8E8A), fontSize = 8.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun RecommendedStageList() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StageCard(
            title = "League Stage",
            badge = "REQUIRED",
            description = "Every team plays against every other team and earns points.",
            glyph = FlowGlyph.LEAGUE,
            iconRes = R.drawable.tournamentlogo,
            active = true
        )
        StageCard(
            title = "Semi Final",
            description = "Top teams qualify for knockout matches.",
            glyph = FlowGlyph.FLAME,
            iconRes = R.drawable.otherball,
            active = true
        )
        StageCard(
            title = "Final",
            description = "Championship match to determine the winner.",
            glyph = FlowGlyph.TROPHY,
            iconRes = R.drawable.finalicon,
            active = true
        )
        StageCard(
            title = "Third Place Match",
            badge = "OPTIONAL",
            description = "Semi-final losers compete for third position.",
            glyph = FlowGlyph.MEDAL,
            iconRes = null,
            active = true,
            muted = true
        )
    }
}

@Composable
private fun StageCard(
    title: String,
    description: String,
    glyph: FlowGlyph,
    iconRes: Int?,
    active: Boolean,
    badge: String? = null,
    muted: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .shadow(16.dp, RoundedCornerShape(12.dp), clip = false)
            .clip(RoundedCornerShape(12.dp))
            .background(if (muted) Color(0xFF132131) else FlowCard)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (muted) Color(0xFF1C2A35) else Color(0xFF172235)),
            contentAlignment = Alignment.Center
        ) {
            if (iconRes != null) {
                Image(
                    painter = painterResource(iconRes),
                    contentDescription = title,
                    modifier = Modifier.size(40.dp)
                )
            } else {
                FlowGlyphIcon(glyph, Modifier.size(34.dp), Color(0xFF6E7C85))
            }
        }
        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black, maxLines = 1)
                badge?.let { StageBadge(it) }
            }
            Text(
                description,
                color = if (muted) Color(0xFF7F8C91) else FlowMuted,
                fontSize = 9.2.sp,
                lineHeight = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        CheckMarkBox(checked = active, enabled = true)
    }
}

@Composable
private fun StageBadge(text: String) {
    Box(
        modifier = Modifier
            .padding(start = 5.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(if (text == "REQUIRED") FlowAccent else Color(0xFF263449))
            .padding(horizontal = 5.dp, vertical = 1.dp)
    ) {
        Text(text, color = if (text == "REQUIRED") Color(0xFF111604) else Color(0xFFAEB9C0), fontSize = 5.5.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun IconFrame(glyph: FlowGlyph, muted: Boolean = false) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (muted) Color(0xFF1C2A35) else Color(0xFF172235)),
        contentAlignment = Alignment.Center
    ) {
        FlowGlyphIcon(glyph, Modifier.size(24.dp), if (muted) Color(0xFF6E7C85) else FlowAccent)
    }
}

@Composable
private fun TournamentFlowPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .shadow(18.dp, RoundedCornerShape(14.dp), clip = false)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF121B2B), Color(0xFF08111F))
                )
            )
            .padding(horizontal = 16.dp, vertical = 11.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("TOURNAMENT FLOW", color = Color(0xFF97A4AA), fontSize = 11.5.sp, fontWeight = FontWeight.Black)
        Text("VISUAL PREVIEW", color = Color(0xFF475566), fontSize = 7.4.sp, fontWeight = FontWeight.Black)
        Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxWidth().height(100.dp)) {
                val y = 42.dp.toPx()
                val start = size.width * 0.12f
                val mid = size.width * 0.5f
                val end = size.width * 0.88f
                drawLine(FlowAccent.copy(alpha = 0.12f), Offset(start, y), Offset(end, y), strokeWidth = 8.dp.toPx(), cap = StrokeCap.Round)
                drawLine(FlowAccent.copy(alpha = 0.38f), Offset(start, y), Offset(end, y), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
                drawLine(FlowAccent, Offset(start, y), Offset(end, y), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                PreviewNode("LEAGUE", R.drawable.tournamentlogo)
                PreviewNode("SEMI", R.drawable.otherball)
                PreviewNode("FINAL", R.drawable.finalicon)
            }
        }
    }
}

@Composable
private fun PreviewNode(label: String, iconRes: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(82.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val center = Offset(size.width * 0.5f, size.height * 0.5f)
                val maxGlow = size.minDimension * 0.48f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.16f),
                            FlowAccent.copy(alpha = 0.30f),
                            FlowAccent.copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = maxGlow
                    ),
                    radius = maxGlow,
                    center = center
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            FlowAccent.copy(alpha = 0.34f),
                            FlowAccent.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = size.minDimension * 0.62f
                    ),
                    radius = size.minDimension * 0.62f,
                    center = center
                )
                drawCircle(FlowAccent.copy(alpha = 0.18f), radius = size.minDimension * 0.36f, center = center, style = Stroke(width = 10.dp.toPx()))
                drawCircle(FlowAccent.copy(alpha = 0.56f), radius = size.minDimension * 0.33f, center = center, style = Stroke(width = 4.dp.toPx()))
                drawCircle(Color.White.copy(alpha = 0.32f), radius = size.minDimension * 0.31f, center = center, style = Stroke(width = 1.1.dp.toPx()))
                drawCircle(Color(0xFF0B1410), radius = size.minDimension * 0.27f, center = center)
                drawArc(
                    Color.White.copy(alpha = 0.72f),
                    startAngle = -80f,
                    sweepAngle = 48f,
                    useCenter = false,
                    topLeft = Offset(size.width * 0.2f, size.height * 0.2f),
                    size = Size(size.width * 0.6f, size.height * 0.6f),
                    style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    FlowAccent.copy(alpha = 0.8f),
                    startAngle = 132f,
                    sweepAngle = 64f,
                    useCenter = false,
                    topLeft = Offset(size.width * 0.18f, size.height * 0.18f),
                    size = Size(size.width * 0.64f, size.height * 0.64f),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF101923))
                    .drawBehind {
                        drawCircle(FlowAccent.copy(alpha = 0.22f), radius = size.minDimension * 0.62f)
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(iconRes),
                    contentDescription = label,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }
        Text(label, color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun AdvancedStagesCard(
    expanded: Boolean,
    options: List<FlowOption>,
    onToggleExpanded: () -> Unit,
    onToggleOption: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(18.dp, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 8.dp, bottomEnd = 8.dp), clip = false)
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 8.dp, bottomEnd = 8.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xF20C1624), Color(0xF2070D16))
                )
            )
            .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(38.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFF26394E))
        )
        Spacer(Modifier.height(7.dp))
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onToggleExpanded),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Advanced Tournament Stages", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black)
                Text("FOR IPL-STYLE AND PROFESSIONAL TOURNAMENTS", color = Color(0xFF8E9AA5), fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 2.dp))
            }
            FlowChevron(Modifier.size(16.dp), expanded)
        }
        if (expanded) {
            Spacer(Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.chunked(2).forEachIndexed { rowIndex, rowItems ->
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        rowItems.forEachIndexed { columnIndex, option ->
                            val index = rowIndex * 2 + columnIndex
                            BottomSheetOptionRow(
                                option = option,
                                modifier = Modifier.weight(1f),
                                onClick = { onToggleOption(index) }
                            )
                        }
                        if (rowItems.size == 1) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomSheetOptionRow(option: FlowOption, modifier: Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .height(46.dp)
            .shadow(7.dp, RoundedCornerShape(8.dp), clip = false)
            .clip(RoundedCornerShape(8.dp))
            .background(if (option.selected) Color(0xFF17291D) else Color(0xFF111C2A))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FlowGlyphIcon(FlowGlyph.DOT, Modifier.size(8.dp), Color(0xFFFFA63C))
        Text(
            option.title,
            color = Color.White,
            fontSize = 10.2.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 8.dp).weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        CheckMarkBox(checked = option.selected, enabled = true, small = true)
    }
}

@Composable
private fun AdvancedOptionRow(option: FlowOption, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FlowGlyphIcon(FlowGlyph.CROSSED, Modifier.size(14.dp), Color.White)
        Text(option.title, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(start = 11.dp).weight(1f))
        CheckMarkBox(checked = option.selected, enabled = true)
    }
}

@Composable
private fun MiniOptionChip(option: FlowOption, modifier: Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .height(38.dp)
            .shadow(6.dp, RoundedCornerShape(8.dp), clip = false)
            .clip(RoundedCornerShape(8.dp))
            .background(if (option.selected) Color(0xFF1A2C20) else Color(0xFF111C2A))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FlowGlyphIcon(FlowGlyph.DOT, Modifier.size(9.dp), Color(0xFFFFA63C))
        Text(option.title, color = Color.White, fontSize = 7.5.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(start = 6.dp).weight(1f))
        CheckMarkBox(checked = option.selected, enabled = true, small = true)
    }
}

@Composable
private fun InfoNote(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(10.dp, RoundedCornerShape(8.dp), clip = false)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xB20A111B))
            .padding(horizontal = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        InfoGlyph(Modifier.size(15.dp))
        Text(text, color = Color(0xFFAEBBB8), fontSize = 9.sp, lineHeight = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 9.dp).weight(1f))
    }
}

@Composable
private fun ContinueFlowButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(12.dp, RoundedCornerShape(10.dp), clip = false)
            .clip(RoundedCornerShape(8.dp))
            .background(FlowAccent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text("CONTINUE", color = Color(0xFF111604), fontSize = 12.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun CheckMarkBox(checked: Boolean, enabled: Boolean, small: Boolean = false) {
    val boxSize = if (small) 12.dp else 18.dp
    Canvas(Modifier.size(boxSize)) {
        val stroke = Stroke(width = if (small) 1.1.dp.toPx() else 1.4.dp.toPx(), cap = StrokeCap.Round)
        val radius = if (small) 3.dp.toPx() else 5.dp.toPx()
        drawRoundRect(
            color = if (checked && enabled) FlowAccent else Color(0xFF263448),
            size = Size(size.width, size.height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
            style = if (checked && enabled) androidx.compose.ui.graphics.drawscope.Fill else stroke
        )
        if (checked && enabled) {
            val path = Path().apply {
                moveTo(size.width * 0.28f, size.height * 0.52f)
                lineTo(size.width * 0.43f, size.height * 0.67f)
                lineTo(size.width * 0.73f, size.height * 0.35f)
            }
            drawPath(path, Color(0xFF111604), style = stroke)
        }
    }
}

@Composable
private fun FlowArrow(modifier: Modifier, right: Boolean, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.9.dp.toPx(), cap = StrokeCap.Round)
        val path = Path().apply {
            if (right) {
                moveTo(size.width * 0.35f, size.height * 0.26f)
                lineTo(size.width * 0.63f, size.height * 0.5f)
                lineTo(size.width * 0.35f, size.height * 0.74f)
            } else {
                moveTo(size.width * 0.65f, size.height * 0.26f)
                lineTo(size.width * 0.37f, size.height * 0.5f)
                lineTo(size.width * 0.65f, size.height * 0.74f)
            }
        }
        drawPath(path, tint, style = stroke)
    }
}

@Composable
private fun FlowChevron(modifier: Modifier, expanded: Boolean) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round)
        val path = Path().apply {
            if (expanded) {
                moveTo(size.width * 0.28f, size.height * 0.58f)
                lineTo(size.width * 0.5f, size.height * 0.38f)
                lineTo(size.width * 0.72f, size.height * 0.58f)
            } else {
                moveTo(size.width * 0.28f, size.height * 0.42f)
                lineTo(size.width * 0.5f, size.height * 0.62f)
                lineTo(size.width * 0.72f, size.height * 0.42f)
            }
        }
        drawPath(path, Color(0xFF93A09E), style = stroke)
    }
}

@Composable
private fun HelpCircle(modifier: Modifier) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(Color(0xFFAAB7B4), radius = size.minDimension * 0.38f, style = stroke)
        drawArc(Color(0xFFAAB7B4), 210f, 215f, false, style = stroke)
        drawCircle(Color(0xFFAAB7B4), radius = size.minDimension * 0.035f, center = Offset(size.width * 0.5f, size.height * 0.68f))
    }
}

@Composable
private fun InfoGlyph(modifier: Modifier) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(Color(0xFF5FA8FF), radius = size.minDimension * 0.42f, style = stroke)
        drawLine(Color(0xFF5FA8FF), Offset(size.width * 0.5f, size.height * 0.46f), Offset(size.width * 0.5f, size.height * 0.72f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawCircle(Color(0xFF5FA8FF), radius = size.minDimension * 0.04f, center = Offset(size.width * 0.5f, size.height * 0.3f))
    }
}

@Composable
private fun FlowGlyphIcon(glyph: FlowGlyph, modifier: Modifier, tint: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
        when (glyph) {
            FlowGlyph.PIN -> {
                drawCircle(tint, radius = size.minDimension * 0.21f, center = Offset(size.width * 0.5f, size.height * 0.38f), style = stroke)
                val path = Path().apply {
                    moveTo(size.width * 0.5f, size.height * 0.88f)
                    cubicTo(size.width * 0.18f, size.height * 0.55f, size.width * 0.24f, size.height * 0.18f, size.width * 0.5f, size.height * 0.14f)
                    cubicTo(size.width * 0.76f, size.height * 0.18f, size.width * 0.82f, size.height * 0.55f, size.width * 0.5f, size.height * 0.88f)
                }
                drawPath(path, tint, style = stroke)
            }
            FlowGlyph.TEAMS -> {
                drawCircle(tint, radius = size.minDimension * 0.17f, center = Offset(size.width * 0.38f, size.height * 0.35f), style = stroke)
                drawCircle(tint, radius = size.minDimension * 0.14f, center = Offset(size.width * 0.66f, size.height * 0.39f), style = stroke)
                drawArc(tint, 205f, 130f, false, topLeft = Offset(size.width * 0.14f, size.height * 0.5f), size = Size(size.width * 0.5f, size.height * 0.38f), style = stroke)
                drawArc(tint, 205f, 130f, false, topLeft = Offset(size.width * 0.45f, size.height * 0.53f), size = Size(size.width * 0.38f, size.height * 0.3f), style = stroke)
            }
            FlowGlyph.SHARE -> {
                val points = listOf(Offset(size.width * 0.28f, size.height * 0.5f), Offset(size.width * 0.68f, size.height * 0.28f), Offset(size.width * 0.68f, size.height * 0.72f))
                drawLine(tint, points[0], points[1], strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, points[0], points[2], strokeWidth = stroke.width, cap = StrokeCap.Round)
                points.forEach { drawCircle(tint, radius = size.minDimension * 0.1f, center = it, style = stroke) }
            }
            FlowGlyph.LEAGUE -> {
                drawLine(tint, Offset(size.width * 0.3f, size.height * 0.78f), Offset(size.width * 0.72f, size.height * 0.26f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawRoundRect(tint, topLeft = Offset(size.width * 0.54f, size.height * 0.13f), size = Size(size.width * 0.28f, size.height * 0.34f), style = stroke)
                drawCircle(tint, radius = size.minDimension * 0.09f, center = Offset(size.width * 0.29f, size.height * 0.78f), style = stroke)
            }
            FlowGlyph.FLAME -> {
                val path = Path().apply {
                    moveTo(size.width * 0.52f, size.height * 0.1f)
                    cubicTo(size.width * 0.85f, size.height * 0.42f, size.width * 0.72f, size.height * 0.86f, size.width * 0.5f, size.height * 0.9f)
                    cubicTo(size.width * 0.22f, size.height * 0.84f, size.width * 0.14f, size.height * 0.52f, size.width * 0.38f, size.height * 0.32f)
                    cubicTo(size.width * 0.44f, size.height * 0.5f, size.width * 0.58f, size.height * 0.43f, size.width * 0.52f, size.height * 0.1f)
                }
                drawPath(path, tint, style = stroke)
            }
            FlowGlyph.TROPHY -> {
                drawRoundRect(tint, topLeft = Offset(size.width * 0.3f, size.height * 0.16f), size = Size(size.width * 0.4f, size.height * 0.36f), style = stroke)
                drawArc(tint, 90f, 165f, false, topLeft = Offset(size.width * 0.1f, size.height * 0.2f), size = Size(size.width * 0.28f, size.height * 0.28f), style = stroke)
                drawArc(tint, -75f, 165f, false, topLeft = Offset(size.width * 0.62f, size.height * 0.2f), size = Size(size.width * 0.28f, size.height * 0.28f), style = stroke)
                drawLine(tint, Offset(size.width * 0.5f, size.height * 0.52f), Offset(size.width * 0.5f, size.height * 0.76f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.34f, size.height * 0.78f), Offset(size.width * 0.66f, size.height * 0.78f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            FlowGlyph.MEDAL -> {
                drawCircle(tint, radius = size.minDimension * 0.22f, center = Offset(size.width * 0.5f, size.height * 0.62f), style = stroke)
                drawLine(tint, Offset(size.width * 0.35f, size.height * 0.16f), Offset(size.width * 0.47f, size.height * 0.43f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.65f, size.height * 0.16f), Offset(size.width * 0.53f, size.height * 0.43f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            FlowGlyph.CROSSED -> {
                drawLine(tint, Offset(size.width * 0.24f, size.height * 0.25f), Offset(size.width * 0.76f, size.height * 0.78f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.76f, size.height * 0.25f), Offset(size.width * 0.24f, size.height * 0.78f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            FlowGlyph.DOT -> {
                drawCircle(tint, radius = size.minDimension * 0.25f)
            }
        }
    }
}

private data class FlowOption(val title: String, val selected: Boolean)

private enum class FlowGlyph {
    PIN,
    TEAMS,
    SHARE,
    LEAGUE,
    FLAME,
    TROPHY,
    MEDAL,
    CROSSED,
    DOT
}
