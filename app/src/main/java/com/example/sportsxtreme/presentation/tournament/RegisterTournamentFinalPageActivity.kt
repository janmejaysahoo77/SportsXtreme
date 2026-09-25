package com.example.sportsxtreme.presentation.tournament

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.example.sportsxtreme.R
import com.example.sportsxtreme.presentation.ui.theme.*
import com.example.sportsxtreme.domain.model.Tournament
import com.example.sportsxtreme.presentation.home.HomeScreenView
import com.example.sportsxtreme.presentation.match.StartMatchActivity
import com.google.firebase.functions.FirebaseFunctions
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterTournamentFinalPageActivity : ComponentActivity() {
    private val viewModel: TournamentFlowViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.splash_window_bg)
        val tournamentId = intent.getStringExtra(EXTRA_TOURNAMENT_ID).orEmpty()
        val initialTab = intent.getIntExtra(EXTRA_INITIAL_TAB, 0).coerceIn(0, 4)
        viewModel.load(tournamentId)
        setContent {
            RegisterTournamentFinalPage(
                tournament = viewModel.tournament.collectAsState().value,
                initialTab = initialTab,
                onBack = { finish() },
                onSaveTournamentDetails = viewModel::saveTournamentDetails,
                onSaveTeamDetails = viewModel::saveTeamDetails
            )
        }
    }

    companion object {
        const val EXTRA_TOURNAMENT_ID = "tournament_id"
        const val EXTRA_INITIAL_TAB = "initial_tab"
        const val TEAMS_TAB_INDEX = 1
    }
}

private val FinalBg = XtremeBgBlue
private val FinalPanel = XtremeCardBlue
private val FinalPanelLight = XtremeCardBlue
private val FinalAccent = XtremeLime
private val FinalMuted = XtremeMuted
private val FinalDivider = XtremeCardBorder

@Composable
private fun RegisterTournamentFinalPage(
    tournament: Tournament?,
    initialTab: Int,
    onBack: () -> Unit,
    onSaveTournamentDetails: (String, String, String, String) -> Unit,
    onSaveTeamDetails: (String, String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    val tabs = listOf("Overview", "Teams", "Matches", "Points", "Leaderboard")
    Column(Modifier.fillMaxSize().background(FinalBg)) {
        FinalTopBar(onBack)
        FinalTournamentHeader(tournament)
        FinalTournamentTabs(tabs, selectedTab) { selectedTab = it }
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(selectedTab) {
                var horizontalDrag = 0f
                var verticalDrag = 0f
                detectDragGestures(
                    onDragStart = {
                        horizontalDrag = 0f
                        verticalDrag = 0f
                    },
                    onDrag = { _, dragAmount ->
                        horizontalDrag += dragAmount.x
                        verticalDrag += dragAmount.y
                    },
                    onDragEnd = {
                        if (kotlin.math.abs(horizontalDrag) > kotlin.math.abs(verticalDrag)) {
                            when {
                                horizontalDrag <= -80 && selectedTab < tabs.lastIndex -> selectedTab++
                                horizontalDrag >= 80 && selectedTab > 0 -> selectedTab--
                            }
                        }
                    },
                    onDragCancel = {
                        horizontalDrag = 0f
                        verticalDrag = 0f
                    }
                )
            }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        0 -> AboutTab(tournament, onSaveTournamentDetails, onSaveTeamDetails)
                        1 -> TeamsTab(tournament)
                        2 -> MatchesTab(tournament)
                        3 -> PointsTab()
                        else -> LeaderboardTab()
                    }
                }
            }
        }
    }
}

@Composable
private fun FinalTournamentTabs(
    tabs: List<String>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(FinalPanel)
            .border(1.dp, FinalDivider, RoundedCornerShape(14.dp))
            .horizontalScroll(rememberScrollState())
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        tabs.forEachIndexed { index, label ->
            val selected = selectedTab == index
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (selected) Brush.horizontalGradient(listOf(FinalAccent.copy(alpha = .20f), FinalAccent.copy(alpha = .07f)))
                        else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                    )
                    .border(1.dp, if (selected) FinalAccent.copy(alpha = .55f) else Color.Transparent, RoundedCornerShape(10.dp))
                    .clickable { onTabSelected(index) }
                    .padding(horizontal = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    label.uppercase(),
                    color = if (selected) FinalAccent else FinalMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                Box(
                    Modifier.width(if (selected) 18.dp else 0.dp)
                        .height(3.dp)
                        .shadow(if (selected) 8.dp else 0.dp, RoundedCornerShape(2.dp), clip = false)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (selected) FinalAccent else Color.Transparent)
                )
            }
        }
    }
}

@Composable
private fun FinalTopBar(onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(FinalPanelLight).clickable(onClick = onBack), contentAlignment = Alignment.Center) {
            BackGlyph(Modifier.size(21.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text("Tournament hub", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("Manage every detail in one place", color = FinalMuted, fontSize = 10.sp)
        }
        Spacer(Modifier.weight(1f))
        Box(Modifier.size(40.dp).clip(CircleShape).background(FinalPanelLight), contentAlignment = Alignment.Center) {
            Text("⋮", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FinalTournamentHeader(tournament: Tournament?) {
    val context = LocalContext.current
    val tournamentName = tournament?.name?.ifBlank { "Tournament" } ?: "Loading tournament…"
    val tournamentDate = tournament?.startDate?.ifBlank { "Date to be announced" } ?: ""
    val location = listOf(tournament?.ground, tournament?.city).filterNotNull().filter { it.isNotBlank() }.joinToString(" · ")
    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(52.dp).clip(RoundedCornerShape(15.dp)).background(Color(0xFFECF8D2)).border(1.dp, FinalAccent.copy(alpha = .7f), RoundedCornerShape(15.dp)), contentAlignment = Alignment.Center) {
            Text("🏏", fontSize = 23.sp)
        }
        Column(Modifier.padding(start = 12.dp).weight(1f)) {
            Text(tournamentName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(3.dp))
            Text(tournamentDate.ifBlank { "Date to be announced" }, color = FinalMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (location.isNotBlank()) Text("⌖  $location", color = FinalMuted.copy(alpha = .8f), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Box(
            Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(FinalAccent)
                .clickable {
                    val shareText = "Join $tournamentName${tournamentDate.takeIf { it.isNotBlank() }?.let { " • $it" }.orEmpty()}"
                    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }, "Share tournament"))
                }
                .padding(horizontal = 13.dp, vertical = 9.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Share", color = FinalBg, fontSize = 12.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun AboutTab(
    tournament: Tournament?,
    onSaveTournamentDetails: (String, String, String, String) -> Unit,
    onSaveTeamDetails: (String, String) -> Unit
) {
    val requirements = tournament?.requirements
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OverviewStatsCard(
            teams = requirements?.numberOfTeams?.ifBlank { "0" } ?: "0",
            matches = "0",
            startDate = tournament?.startDate?.ifBlank { "Not set" } ?: "Not set",
            location = listOf(tournament?.ground, tournament?.city).filterNotNull().filter { it.isNotBlank() }.joinToString(", ").ifBlank { "Not set" }
        )
        TournamentDetailsCard(tournament, onSaveTournamentDetails)
        TeamDetailsCard(tournament, onSaveTeamDetails)
        OrganizerCard(tournament)
        TournamentQrCard(tournament)
        SetupHelpCard()
        TournamentPromoCard()
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun OverviewStatsCard(teams: String, matches: String, startDate: String, location: String) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).background(FinalPanel).border(1.dp, FinalDivider, RoundedCornerShape(13.dp)).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(Modifier.fillMaxWidth()) {
            OverviewMetric("TEAMS", teams, Modifier.weight(1f))
            OverviewMetric("MATCHES", matches, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth()) {
            OverviewMetric("START DATE", startDate, Modifier.weight(1f))
            OverviewMetric("LOCATION", location, Modifier.weight(1f))
        }
    }
}

@Composable
private fun OverviewMetric(label: String, value: String, modifier: Modifier) {
    Column(modifier.padding(horizontal = 5.dp, vertical = 3.dp)) {
        Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(3.dp))
        Text(label, color = FinalMuted, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
private fun SetupHelpCard() {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).background(FinalPanel).padding(14.dp)
    ) {
        Text("Tournament setup guide/help", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(13.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(FinalDivider))
        Row(Modifier.fillMaxWidth().padding(top = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Help videos", color = FinalMuted, fontSize = 13.sp, modifier = Modifier.weight(1f))
            Box(Modifier.clip(RoundedCornerShape(6.dp)).border(1.dp, FinalAccent.copy(alpha = .85f), RoundedCornerShape(6.dp)).padding(horizontal = 18.dp, vertical = 6.dp)) {
                Text("View", color = FinalAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(17.dp))
        Text("SportsXtreme helpline", color = FinalMuted, fontSize = 13.sp)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            HelpContactButton("Call", Modifier.weight(1f))
            HelpContactButton("WhatsApp", Modifier.weight(1f))
        }
    }
}

@Composable
private fun TournamentPromoCard() {
    Box(
        Modifier.fillMaxWidth().height(104.dp).clip(RoundedCornerShape(13.dp)).background(FinalPanel)
    ) {
        Image(
            painter = painterResource(R.drawable.batsman_onboarding2),
            contentDescription = "SportsXtreme tournament promotion",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun OrganizerCard(tournament: Tournament?) {
    val organizerName = tournament?.organizerName?.ifBlank { "Organizer" } ?: "Organizer"
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).background(FinalPanel).padding(14.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(38.dp).clip(CircleShape).background(FinalPanelLight), contentAlignment = Alignment.Center) {
                Text(organizerName.take(1).uppercase(), color = FinalAccent, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.padding(start = 11.dp).weight(1f)) {
                Text("Organizer", color = FinalMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(2.dp))
                Text(organizerName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text("›", color = FinalMuted, fontSize = 24.sp)
        }
    }
}

@Composable
private fun TournamentDetailsCard(
    tournament: Tournament?,
    onSave: (String, String, String, String) -> Unit
) {
    var isEditing by remember(tournament?.id) { mutableStateOf(false) }
    var name by remember(tournament?.id) { mutableStateOf(tournament?.name.orEmpty()) }
    var startDate by remember(tournament?.id) { mutableStateOf(tournament?.startDate.orEmpty()) }
    var ground by remember(tournament?.id) { mutableStateOf(tournament?.ground.orEmpty()) }
    var ballType by remember(tournament?.id) { mutableStateOf(tournament?.ballType.orEmpty()) }
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).background(FinalPanel).padding(14.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Tournament details", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text(
                if (isEditing) "SAVE" else "✎ Edit",
                color = FinalAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    if (isEditing) onSave(name.trim(), startDate.trim(), ground.trim(), ballType.trim())
                    isEditing = !isEditing
                }.padding(4.dp)
            )
        }
        Spacer(Modifier.height(13.dp))
        OverviewEditableDetail("Name", name, isEditing, { name = it })
        OverviewEditableDetail("Date", startDate, isEditing, { startDate = it })
        OverviewEditableDetail("Grounds", ground, isEditing, { ground = it }, valueColor = FinalAccent)
        OverviewEditableDetail("Ball type", ballType, isEditing, { ballType = it })
        OverviewDetail("Category", tournament?.type?.ifBlank { "Tournament" } ?: "Tournament")
        OverviewDetail("Tournament ID", tournament?.id?.ifBlank { "Not available" } ?: "Not available")
    }
}

@Composable
private fun TeamDetailsCard(tournament: Tournament?, onSave: (String, String) -> Unit) {
    var isEditing by remember(tournament?.id) { mutableStateOf(false) }
    var entryFee by remember(tournament?.id) { mutableStateOf(tournament?.requirements?.entryFee.orEmpty()) }
    var teamCapacity by remember(tournament?.id) { mutableStateOf(tournament?.requirements?.numberOfTeams.orEmpty()) }
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).background(FinalPanel).padding(14.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Team details", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text(
                if (isEditing) "SAVE" else "✎ Edit",
                color = FinalAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    if (isEditing) onSave(entryFee.trim(), teamCapacity.trim())
                    isEditing = !isEditing
                }.padding(4.dp)
            )
        }
        Spacer(Modifier.height(13.dp))
        OverviewEditableDetail("Entry fee", entryFee, isEditing, { entryFee = it }, emptyLabel = "0")
        OverviewEditableDetail("Team capacity", teamCapacity, isEditing, { teamCapacity = it })
    }
}

@Composable
private fun OverviewEditableDetail(
    label: String,
    value: String,
    isEditing: Boolean,
    onValueChange: (String) -> Unit,
    valueColor: Color = Color.White,
    emptyLabel: String = "Not set"
) {
    if (isEditing) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
        )
    } else {
        OverviewDetail(label, value.ifBlank { emptyLabel }, valueColor)
    }
}

@Composable
private fun OverviewDetail(label: String, value: String, valueColor: Color = Color.White) {
    Column(Modifier.padding(bottom = 10.dp)) {
        Text(label, color = FinalMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(2.dp))
        Text(value, color = valueColor, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun TournamentQrCard(tournament: Tournament?) {
    val context = LocalContext.current
    val tournamentLink = tournament?.id
        ?.takeIf { it.isNotBlank() }
        ?.let { tournamentId ->
            Uri.parse(context.getString(R.string.tournament_share_base_url))
                .buildUpon()
                .appendQueryParameter("id", tournamentId)
                .build()
                .toString()
        }
    val qrBitmap = remember(tournamentLink) {
        tournamentLink?.let(::tournamentQrBitmap)
    }
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).background(FinalPanel).padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Tournament QR code", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("↗  ↶", color = FinalMuted, fontSize = 16.sp)
        }
        Spacer(Modifier.height(16.dp))
        Box(Modifier.size(142.dp).clip(RoundedCornerShape(10.dp)).background(Color.White).padding(8.dp)) {
            if (qrBitmap != null) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "Tournament QR code",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(
            if (qrBitmap == null) "Preparing tournament QR code…" else "Let cricketers find this tournament\neasily with QR code.",
            color = FinalMuted,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            lineHeight = 17.sp
        )
    }
}

private fun tournamentQrBitmap(value: String): Bitmap {
    val matrix = QRCodeWriter().encode(
        value,
        BarcodeFormat.QR_CODE,
        512,
        512,
        mapOf(EncodeHintType.MARGIN to 1)
    )
    return Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.ARGB_8888).apply {
        for (y in 0 until matrix.height) {
            for (x in 0 until matrix.width) {
                setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
    }
}

@Composable
private fun HelpContactButton(label: String, modifier: Modifier) {
    Box(modifier.height(36.dp).clip(RoundedCornerShape(7.dp)).background(FinalPanelLight), contentAlignment = Alignment.Center) {
        Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AboutStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(14.dp)).background(FinalPanel).border(1.dp, FinalDivider, RoundedCornerShape(14.dp)).padding(11.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(5.dp))
        Text(label, color = FinalMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun AboutCard(title: String, content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(FinalPanel).border(1.dp, FinalDivider, RoundedCornerShape(16.dp)).padding(15.dp)
    ) {
        Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun AboutDetail(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = FinalMuted, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun MatchesTab(tournament: Tournament?) {
    val context = LocalContext.current
    val homeCardFactory = remember(context) { HomeScreenView(context) }
    val tournamentName = tournament?.name?.ifBlank { "Tournament" } ?: "Tournament"
    var selectedMatchTab by remember { mutableIntStateOf(0) }
    val matchTabs = listOf("Live", "Upcoming", "Completed")

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Matches", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Black)
                Text("Manage every tournament fixture", color = FinalMuted, fontSize = 12.sp)
            }
            Box(Modifier.clip(RoundedCornerShape(16.dp)).background(FinalAccent.copy(alpha = .13f)).padding(horizontal = 11.dp, vertical = 7.dp)) {
                Text("2 LIVE", color = FinalAccent, fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
        }
        MatchStatusTabs(matchTabs, selectedMatchTab) { selectedMatchTab = it }
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MatchStatusContent(selectedMatchTab, tournamentName, homeCardFactory)
        }
        MatchActions(
            modifier = Modifier,
            onSchedule = {
                context.startActivity(
                    Intent(context, StartMatchActivity::class.java)
                        .putExtra(StartMatchActivity.EXTRA_SCHEDULE_FLOW, true)
                        .putExtra(RegisterTournamentFinalPageActivity.EXTRA_TOURNAMENT_ID, tournament?.id)
                )
            },
            onStart = { context.startActivity(Intent(context, StartMatchActivity::class.java)) }
        )
    }
}

@Composable
private fun MatchStatusTabs(tabs: List<String>, selectedTab: Int, onTabSelected: (Int) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 18.dp).clip(RoundedCornerShape(14.dp)).background(FinalPanel).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tabs.forEachIndexed { index, label ->
            val selected = selectedTab == index
            Box(
                Modifier.weight(1f).clip(RoundedCornerShape(11.dp)).background(if (selected) FinalAccent else Color.Transparent)
                    .clickable { onTabSelected(index) }.padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) { Text(label, color = if (selected) FinalBg else FinalMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun MatchStatusContent(selectedTab: Int, tournamentName: String, homeCardFactory: HomeScreenView) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (selectedTab == 0) {
            AndroidView(factory = { homeCardFactory.createHeroScoreCard(it, tournamentName.uppercase(), "Match 01") }, modifier = Modifier.fillMaxWidth().height(272.dp))
            AndroidView(factory = { homeCardFactory.createHeroScoreCard(it, tournamentName.uppercase(), "Match 02", "BBS", "96/2", "11.3 OV", "KDP", "94/7", "15.0 OV", "148", "7.86", "BBS 68%", "BBS need 52 from 51 balls") }, modifier = Modifier.fillMaxWidth().height(272.dp))
        } else {
            val title = if (selectedTab == 1) "No upcoming matches" else "No completed matches"
            val message = if (selectedTab == 1) "Schedule a match to add it here." else "Finished scorecards will appear here."
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(FinalPanel).border(1.dp, FinalDivider, RoundedCornerShape(16.dp)).padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(7.dp))
                Text(message, color = FinalMuted, fontSize = 12.sp, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun MatchActions(modifier: Modifier, onSchedule: () -> Unit, onStart: () -> Unit) {
    Row(modifier.fillMaxWidth().background(FinalBg).padding(horizontal = 18.dp, vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.weight(1f).height(50.dp).clip(RoundedCornerShape(14.dp)).border(1.dp, FinalAccent, RoundedCornerShape(14.dp)).clickable(onClick = onSchedule), contentAlignment = Alignment.Center) {
            Text("Schedule Match", color = FinalAccent, fontSize = 12.sp, fontWeight = FontWeight.Black)
        }
        Box(Modifier.weight(1f).height(50.dp).clip(RoundedCornerShape(14.dp)).background(FinalAccent).clickable(onClick = onStart), contentAlignment = Alignment.Center) {
            Text("Start A Match", color = FinalBg, fontSize = 12.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun TeamsTab(tournament: Tournament?) {
    val context = LocalContext.current
    var isCreatingInvite by remember { mutableStateOf(false) }
    var joinedTeams by remember(tournament?.id) { mutableStateOf(emptyList<TournamentJoinedTeam>()) }
    DisposableEffect(tournament?.id) {
        val tournamentId = tournament?.id.orEmpty()
        if (tournamentId.isBlank()) return@DisposableEffect onDispose { }
        val registration = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("tournaments").document(tournamentId).collection("teams")
            .addSnapshotListener { snapshots, _ ->
                joinedTeams = snapshots?.documents.orEmpty().map { document ->
                    TournamentJoinedTeam(
                        id = document.id,
                        name = document.getString("teamName")?.trim().orEmpty().ifBlank { "Team" },
                        captainUserId = document.getString("captainUserId").orEmpty()
                    )
                }.sortedBy { it.name.lowercase() }
            }
        onDispose { registration.remove() }
    }
    fun shareInvite() {
        val tournamentId = tournament?.id.orEmpty()
        if (tournamentId.isBlank()) {
            Toast.makeText(context, "Tournament is still loading", Toast.LENGTH_SHORT).show()
            return
        }
        isCreatingInvite = true
        FirebaseFunctions.getInstance().getHttpsCallable("createTournamentInvite")
            .call(mapOf("tournamentId" to tournamentId))
            .addOnSuccessListener { result ->
                isCreatingInvite = false
                val data = result.data as? Map<*, *> ?: return@addOnSuccessListener
                val url = data["invitationUrl"] as? String ?: return@addOnSuccessListener
                context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "Register your team for ${tournament?.name ?: "this tournament"}: $url")
                }, "Share tournament invitation"))
            }
            .addOnFailureListener { error ->
                isCreatingInvite = false
                Toast.makeText(context, error.message ?: "Unable to create invitation", Toast.LENGTH_LONG).show()
            }
    }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Teams", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text("Build your lineup before fixtures begin", color = FinalMuted, fontSize = 12.sp)
            }
            Box(Modifier.clip(RoundedCornerShape(20.dp)).background(FinalAccent.copy(alpha = .13f)).padding(horizontal = 11.dp, vertical = 7.dp)) {
                Text("${joinedTeams.size} TEAMS", color = FinalAccent, fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
        }
        Spacer(Modifier.height(18.dp))
        Box(Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(14.dp)).background(FinalPanel), contentAlignment = Alignment.CenterStart) {
            Text("⌕   Search teams", color = FinalMuted, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 16.dp))
        }
        Spacer(Modifier.height(16.dp))
        if (joinedTeams.isNotEmpty()) {
            Text("REGISTERED TEAMS", color = FinalMuted, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            joinedTeams.forEachIndexed { index, team ->
                TournamentTeamCard(team, index + 1)
                Spacer(Modifier.height(10.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(FinalPanel).padding(horizontal = 20.dp, vertical = 26.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(84.dp).clip(CircleShape).background(FinalAccent.copy(alpha = .12f)), contentAlignment = Alignment.Center) {
                    Text("01", color = FinalAccent, fontSize = 26.sp, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.height(20.dp))
                Text("Invite captains to add teams", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                Spacer(Modifier.height(9.dp))
                Text("Share one link and let captains submit their\nteams and player details directly.", color = FinalMuted, fontSize = 13.sp, lineHeight = 19.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(24.dp))
                FinalActionButton(if (isCreatingInvite) "CREATING LINK…" else "SHARE INVITE LINK", filled = true, enabled = !isCreatingInvite, onClick = ::shareInvite)
                Text("OR", color = Color(0xFF65718A), fontSize = 11.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(vertical = 17.dp))
                FinalActionButton("ADD A TEAM MANUALLY", filled = false)
            }
        }
        Spacer(Modifier.height(20.dp))
        Text("You can edit teams and players anytime before\npublishing the tournament.", color = FinalMuted, fontSize = 12.sp, textAlign = TextAlign.Center, lineHeight = 17.sp)
    }
}

private data class TournamentJoinedTeam(val id: String, val name: String, val captainUserId: String)

@Composable
private fun TournamentTeamCard(team: TournamentJoinedTeam, position: Int) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(FinalPanel).border(1.dp, FinalDivider, RoundedCornerShape(15.dp)).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(FinalAccent.copy(alpha = .13f)), contentAlignment = Alignment.Center) {
            Text(position.toString().padStart(2, '0'), color = FinalAccent, fontSize = 12.sp, fontWeight = FontWeight.Black)
        }
        Column(Modifier.padding(start = 12.dp).weight(1f)) {
            Text(team.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("REGISTERED TEAM", color = FinalAccent, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 3.dp))
        }
        Text("✓", color = FinalAccent, fontSize = 18.sp, fontWeight = FontWeight.Black)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PointsTab() {
    var groupName by remember { mutableStateOf<String?>(null) }
    var draftGroupName by remember { mutableStateOf("") }
    var showCreateGroupSheet by remember { mutableStateOf(false) }
    var showNameSuggestions by remember { mutableStateOf(false) }

    if (groupName != null) {
        GroupPointsTab(
            groupName = groupName.orEmpty(),
            onDeleteGroup = { groupName = null }
        )
    } else {
        NoGroupsPointsTab(onCreateGroup = {
            draftGroupName = ""
            showCreateGroupSheet = true
        })
    }

    if (showCreateGroupSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCreateGroupSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = FinalPanel,
            contentColor = Color.White
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Create new group", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text("Name the group for its teams and points table.", color = FinalMuted, fontSize = 13.sp)
                ExposedDropdownMenuBox(
                    expanded = showNameSuggestions,
                    onExpandedChange = { showNameSuggestions = !showNameSuggestions }
                ) {
                    OutlinedTextField(
                        value = draftGroupName,
                        onValueChange = { if (it.length <= 30) draftGroupName = it },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        label = { Text("Group name") },
                        placeholder = { Text("Enter a name or select GROUP A") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showNameSuggestions) },
                        singleLine = true
                    )
                    DropdownMenu(
                        expanded = showNameSuggestions,
                        onDismissRequest = { showNameSuggestions = false }
                    ) {
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text("GROUP A") },
                            onClick = {
                                draftGroupName = "GROUP A"
                                showNameSuggestions = false
                            }
                        )
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(9.dp))
                            .border(1.dp, FinalAccent.copy(alpha = .7f), RoundedCornerShape(9.dp))
                            .clickable { showCreateGroupSheet = false },
                        contentAlignment = Alignment.Center
                    ) { Text("CANCEL", color = FinalAccent, fontSize = 12.sp, fontWeight = FontWeight.Black) }
                    Box(
                        Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(9.dp)).background(FinalAccent)
                            .clickable {
                                groupName = draftGroupName.trim().ifBlank { "GROUP A" }
                                showCreateGroupSheet = false
                            },
                        contentAlignment = Alignment.Center
                    ) { Text("OK", color = FinalBg, fontSize = 12.sp, fontWeight = FontWeight.Black) }
                }
                Spacer(Modifier.height(14.dp))
            }
        }
    }
}

@Composable
private fun NoGroupsPointsTab(onCreateGroup: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 3.dp, vertical = 12.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(FinalBg)
                .border(1.dp, FinalDivider.copy(alpha = .72f), RoundedCornerShape(18.dp))
                .padding(horizontal = 28.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.nonstriker),
                contentDescription = "Cricketer",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(22.dp))
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .border(1.dp, FinalAccent.copy(alpha = .45f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                TeamGlyph(Modifier.size(28.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "No Groups Yet",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Create groups and add teams\nto generate a points table.",
                color = FinalMuted,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(28.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .shadow(12.dp, RoundedCornerShape(7.dp), clip = false)
                    .clip(RoundedCornerShape(7.dp))
                    .background(FinalAccent)
                    .clickable(onClick = onCreateGroup),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(19.dp)
                            .border(1.5.dp, FinalBg, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", color = FinalBg, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(12.dp))
                    Text("CREATE NEW GROUP", color = FinalBg, fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
private fun GroupPointsTab(
    groupName: String,
    onDeleteGroup: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
                modifier = Modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(10.dp))
                    .background(FinalBg).border(1.dp, FinalDivider, RoundedCornerShape(10.dp)).padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(groupName.uppercase(), color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                DeleteGlyph(Modifier.size(22.dp).clickable(onClick = onDeleteGroup))
            }
        Spacer(Modifier.height(58.dp))
        WicketGlyph(Modifier.size(184.dp))
        Spacer(Modifier.height(32.dp))
        Text("This group has no teams", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
        Spacer(Modifier.height(10.dp))
        Text("Create teams in this group to\ngenerate the points table.", color = FinalMuted, fontSize = 13.sp, lineHeight = 19.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        Box(
            Modifier.height(40.dp).clip(RoundedCornerShape(7.dp)).border(1.dp, FinalAccent.copy(alpha = .7f), RoundedCornerShape(7.dp)).padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("♙+", color = FinalAccent, fontSize = 17.sp)
                Spacer(Modifier.width(9.dp))
                Text("Add Team", color = FinalAccent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun LeaderboardTab() {
    var selectedLeaderboardTab by remember { mutableIntStateOf(0) }
    val leaderboardTabs = listOf("Bat", "Bowl", "Field", "MVP")

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 16.dp)
    ) {
        Text("Leaderboard", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(4.dp))
        Text("Top performers from this tournament", color = FinalMuted, fontSize = 12.sp)
        Spacer(Modifier.height(18.dp))
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(FinalPanel).padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            leaderboardTabs.forEachIndexed { index, label ->
                val selected = selectedLeaderboardTab == index
                Box(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                        .background(if (selected) FinalAccent else Color.Transparent)
                        .clickable { selectedLeaderboardTab = index }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(label, color = if (selected) FinalBg else FinalMuted, fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
            }
        }
        Spacer(Modifier.height(22.dp))
        val category = leaderboardTabs[selectedLeaderboardTab]
        Column(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(FinalPanel)
                .border(1.dp, FinalDivider, RoundedCornerShape(16.dp)).padding(horizontal = 20.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("No $category leaders yet", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Player rankings will appear here after matches are scored.", color = FinalMuted, fontSize = 13.sp, lineHeight = 19.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun PointTableRow(team: String, played: String, won: String, points: String, highlighted: Boolean) {
    Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(team, color = if (highlighted) Color.White else FinalMuted, fontSize = 12.sp, fontWeight = if (highlighted) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.weight(1f))
        Text(played, color = FinalMuted, fontSize = 12.sp, modifier = Modifier.width(30.dp), textAlign = TextAlign.Center)
        Text(won, color = FinalMuted, fontSize = 12.sp, modifier = Modifier.width(30.dp), textAlign = TextAlign.Center)
        Text(points, color = FinalAccent, fontSize = 12.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(38.dp), textAlign = TextAlign.Center)
    }
}

@Composable
private fun FinalActionButton(label: String, filled: Boolean, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit = {}) {
    Box(modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(14.dp)).background(if (filled) FinalAccent else FinalPanelLight).border(if (filled) 0.dp else 1.dp, FinalAccent.copy(alpha = .55f), RoundedCornerShape(14.dp)).clickable(enabled = enabled, onClick = onClick), contentAlignment = Alignment.Center) {
        Text(label, color = Color(0xFF101604).takeIf { filled } ?: FinalAccent, fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
    }
}

@Composable
private fun ComingSoonTab(label: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("$label will appear here once teams are added.", color = FinalMuted, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(32.dp))
    }
}

@Composable
private fun TeamGlyph(modifier: Modifier) = Canvas(modifier) {
    val stroke = Stroke(2.7.dp.toPx(), cap = StrokeCap.Round)
    val color = FinalAccent
    drawCircle(color, radius = size.width * .16f, center = androidx.compose.ui.geometry.Offset(size.width * .36f, size.height * .33f), style = stroke)
    drawCircle(color, radius = size.width * .16f, center = androidx.compose.ui.geometry.Offset(size.width * .67f, size.height * .33f), style = stroke)
    drawRoundRect(color, topLeft = androidx.compose.ui.geometry.Offset(size.width * .16f, size.height * .55f), size = androidx.compose.ui.geometry.Size(size.width * .68f, size.height * .3f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx()), style = stroke)
}

@Composable
private fun WicketGlyph(modifier: Modifier) = Canvas(modifier) {
    val accent = FinalAccent.copy(alpha = .85f)
    val faint = FinalAccent.copy(alpha = .13f)
    val stroke = 1.5.dp.toPx()
    val baseY = size.height * .82f
    val topY = size.height * .22f
    val leftX = size.width * .36f
    val middleX = size.width * .5f
    val rightX = size.width * .64f
    drawCircle(faint, radius = size.width * .46f, center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2), style = Stroke(stroke))
    drawLine(faint, androidx.compose.ui.geometry.Offset(size.width * .08f, baseY), androidx.compose.ui.geometry.Offset(size.width * .92f, baseY), strokeWidth = stroke)
    listOf(leftX, middleX, rightX).forEach { x ->
        drawLine(accent, androidx.compose.ui.geometry.Offset(x, topY), androidx.compose.ui.geometry.Offset(x, baseY), strokeWidth = stroke)
    }
    drawLine(accent, androidx.compose.ui.geometry.Offset(leftX - 5.dp.toPx(), topY), androidx.compose.ui.geometry.Offset(middleX - 4.dp.toPx(), topY), strokeWidth = stroke)
    drawLine(accent, androidx.compose.ui.geometry.Offset(middleX + 4.dp.toPx(), topY), androidx.compose.ui.geometry.Offset(rightX + 5.dp.toPx(), topY), strokeWidth = stroke)
    drawCircle(FinalAccent, radius = 2.dp.toPx(), center = androidx.compose.ui.geometry.Offset(size.width * .18f, size.height * .24f))
    drawCircle(FinalAccent, radius = 2.dp.toPx(), center = androidx.compose.ui.geometry.Offset(size.width * .81f, size.height * .48f))
}

@Composable
private fun DeleteGlyph(modifier: Modifier) = Canvas(modifier) {
    val stroke = Stroke(1.8.dp.toPx(), cap = StrokeCap.Round)
    val c = FinalAccent
    val left = size.width * .28f
    val right = size.width * .72f
    val top = size.height * .3f
    val bottom = size.height * .78f
    drawLine(c, androidx.compose.ui.geometry.Offset(left, top), androidx.compose.ui.geometry.Offset(right, top), strokeWidth = stroke.width, cap = StrokeCap.Round)
    drawLine(c, androidx.compose.ui.geometry.Offset(left + 2.dp.toPx(), top), androidx.compose.ui.geometry.Offset(left + 3.dp.toPx(), bottom), strokeWidth = stroke.width, cap = StrokeCap.Round)
    drawLine(c, androidx.compose.ui.geometry.Offset(right - 2.dp.toPx(), top), androidx.compose.ui.geometry.Offset(right - 3.dp.toPx(), bottom), strokeWidth = stroke.width, cap = StrokeCap.Round)
    drawLine(c, androidx.compose.ui.geometry.Offset(left + 3.dp.toPx(), bottom), androidx.compose.ui.geometry.Offset(right - 3.dp.toPx(), bottom), strokeWidth = stroke.width, cap = StrokeCap.Round)
    drawLine(c, androidx.compose.ui.geometry.Offset(size.width * .42f, size.height * .17f), androidx.compose.ui.geometry.Offset(size.width * .58f, size.height * .17f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    drawLine(c, androidx.compose.ui.geometry.Offset(size.width * .43f, size.height * .41f), androidx.compose.ui.geometry.Offset(size.width * .43f, size.height * .65f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    drawLine(c, androidx.compose.ui.geometry.Offset(size.width * .57f, size.height * .41f), androidx.compose.ui.geometry.Offset(size.width * .57f, size.height * .65f), strokeWidth = stroke.width, cap = StrokeCap.Round)
}

@Composable
private fun BackGlyph(modifier: Modifier) = Canvas(modifier) {
    val stroke = Stroke(2.dp.toPx(), cap = StrokeCap.Round)
    val c = Color.White
    drawLine(c, androidx.compose.ui.geometry.Offset(size.width * .75f, size.height * .5f), androidx.compose.ui.geometry.Offset(size.width * .2f, size.height * .5f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    drawLine(c, androidx.compose.ui.geometry.Offset(size.width * .2f, size.height * .5f), androidx.compose.ui.geometry.Offset(size.width * .48f, size.height * .22f), strokeWidth = stroke.width, cap = StrokeCap.Round)
    drawLine(c, androidx.compose.ui.geometry.Offset(size.width * .2f, size.height * .5f), androidx.compose.ui.geometry.Offset(size.width * .48f, size.height * .78f), strokeWidth = stroke.width, cap = StrokeCap.Round)
}
