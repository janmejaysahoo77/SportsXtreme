package com.example.sportsxtreme

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

private val HomeNavPrimary = Color(0xFFC1FF00)
private val HomeNavBackground = Color(0xFF05090F)
private val HomeNavInactive = Color(0xFF767F88)
private val HomeNavMuted = Color(0xFF82918E)
private val HomeNavActiveText = Color(0xFF081007)
private val HomeNavStroke = Color(0x3CFFFFFF)
private val HomeTopBarBackground = Color(0xFF060C11)
private val HomeModeSelectedStart = Color(0xFF0C1E18)
private val HomeModeSelectedEnd = Color(0xFF041832)
private val HomeModeUnselectedText = Color(0xFF060C11)
private val HomeModeUnselectedBorder = Color(0x55FFFFFF)

private enum class HomeNavIcon {
    HOME,
    BARS,
    PLUS,
    USERS,
    TROPHY,
    CART
}

private enum class HomeActionIcon {
    MENU,
    SEARCH,
    BELL,
    MESSAGE
}

private data class HomeNavItem(
    val label: String,
    val icon: HomeNavIcon
)

private data class HomeScoreCard(
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

private data class HomeProPass(
    val title: String,
    val passLabel: String,
    val memberLabel: String,
    val oldPrice: String,
    val price: String,
    val priceSuffix: String,
    val accent: Color,
    val background: Color,
    val features: List<Pair<String, String>>,
    val width: Int
)

private val HomeNavItems = listOf(
    HomeNavItem("Home", HomeNavIcon.HOME),
    HomeNavItem("Stats", HomeNavIcon.BARS),
    HomeNavItem("Host", HomeNavIcon.PLUS),
    HomeNavItem("Community", HomeNavIcon.USERS),
    HomeNavItem("Leaderboard", HomeNavIcon.TROPHY)
)

private val HomeScoreCards = listOf(
    HomeScoreCard(
        league = "VALORANT PRO LEAGUE",
        round = "Semi-final - Match 07",
        leftName = "NSC",
        leftScore = "142/4",
        leftOvers = "18.4 OV",
        rightName = "VCR",
        rightScore = "138/6",
        rightOvers = "18.1 OV",
        target = "156",
        rrr = "8.42",
        win = "NSC 61%",
        note = "VCR chose to bowl - Powerplay complete"
    ),
    HomeScoreCard(
        league = "CRICKET PREMIER CUP",
        round = "Qualifier - Match 12",
        leftName = "BBS",
        leftScore = "96/2",
        leftOvers = "11.3 OV",
        rightName = "KDP",
        rightScore = "94/7",
        rightOvers = "15.0 OV",
        target = "148",
        rrr = "7.86",
        win = "BBS 68%",
        note = "BBS need 52 from 51 balls"
    )
)

private val HomeProPasses = listOf(
    HomeProPass(
        title = "PRO",
        passLabel = "PASS",
        memberLabel = "PRO MEMBER",
        oldPrice = "49",
        price = "₹19",
        priceSuffix = " /MONTH",
        accent = HomeNavPrimary,
        background = Color(0xFF05080B),
        features = listOf(
            "ZERO AD" to "INTERRUPTIONS",
            "4K ULTRA HDR" to "STREAMING",
            "PREMIUM INSIDER" to "STATS"
        ),
        width = 332
    ),
    HomeProPass(
        title = "ELITE",
        passLabel = "PASS",
        memberLabel = "ELITE MEMBER",
        oldPrice = "99",
        price = "₹49",
        priceSuffix = "/mo",
        accent = Color(0xFFFF3E46),
        background = Color(0xFF110507),
        features = listOf(
            "1. NO INTERSTITIAL" to "AD",
            "2. STORE" to "DISCOUNT",
            "3. WATCH LIVE" to "STREAMING",
            "4. PREMIUM MATCH" to "INSIGHTS",
            "5. AFTER MATCH" to "HIGHLIGHTS"
        ),
        width = 332
    ),
    HomeProPass(
        title = "LEGEND",
        passLabel = "PASS",
        memberLabel = "LEGEND MEMBER",
        oldPrice = "299",
        price = "₹199",
        priceSuffix = "/Month",
        accent = Color(0xFF9C52FF),
        background = Color(0xFF0D0616),
        features = listOf(
            "1. NOT A" to "SINGLE AD",
            "2. STORE" to "DISCOUNT",
            "3. WATCH LIVE" to "STREAMING",
            "4. PREMIUM MATCH" to "INSIGHTS",
            "5. AFTER MATCH" to "HIGHLIGHTS",
            "6. MATCH PLANNING" to "WITH AI COACH",
            "7. AI PERSONAL COACH" to "FROM YOUR STATS",
            "8. DRS/BALL/SPEED" to "TRACKING"
        ),
        width = 620
    ),
    HomeProPass(
        title = "HALL OF FAME",
        passLabel = "",
        memberLabel = "VIP MEMBER",
        oldPrice = "",
        price = "₹499",
        priceSuffix = "/1yr",
        accent = Color(0xFFFFD700),
        background = Color(0xFF030303),
        features = listOf(
            "1. VERIFIED" to "BADGE",
            "2. NO" to "ADS",
            "3. OFFLINE EVENT" to "INVITE + AWARD",
            "4. LIVE" to "STREAMING",
            "5. HUGE STORE" to "DISCOUNT",
            "6. PREMIUM MATCH" to "INSIGHTS",
            "7. AFTER MATCH" to "HIGHLIGHTS",
            "8. FREE JERSEY" to "+ GOODIES",
            "9. MATCH PLANNING" to "WITH AI COACH",
            "10. AI PERSONAL COACH" to "FROM YOUR STATS",
            "11. DRS/BALL/SPEED" to "TRACKING"
        ),
        width = 920
    )
)

@Composable
fun HomeScreenRoute(
    onHomeViewReady: (HomeScreenView?) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val context = LocalContext.current

    DisposableEffect(Unit) {
        onDispose { onHomeViewReady(null) }
    }

    Column(modifier = modifier.fillMaxSize()) {
        if (selectedTabIndex == 0) {
            HomeTopModeSelector(
                onSportsClick = { (context as? MainActivity)?.showHomeScreen() },
                onMediaClick = { (context as? MainActivity)?.showXtremeMediaScreen() },
                onCartClick = { (context as? MainActivity)?.showXtremeCartScreen() }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    HomeScreenView(
                        context = context,
                        initialSelectedIndex = selectedTabIndex,
                        showBottomNav = false,
                        showTopModeSelector = false,
                        onSelectedTabChanged = { selectedTabIndex = it }
                    ).also(onHomeViewReady)
                },
                update = { view ->
                    view.selectTab(selectedTabIndex)
                    onHomeViewReady(view)
                }
            )
        }

        HomeBottomNavigation(
            selectedIndex = selectedTabIndex,
            onItemSelected = { selectedTabIndex = it }
        )
    }
}

@Composable
private fun HomeTopModeSelector(
    onSportsClick: () -> Unit,
    onMediaClick: () -> Unit,
    onCartClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .background(HomeTopBarBackground)
            .padding(start = 16.dp, top = 24.dp, end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HomeModeButton(
            label = stringResource(R.string.str_sportsxtreme),
            selected = true,
            imageRes = R.drawable.appicon2,
            onClick = onSportsClick,
            modifier = Modifier.weight(1f)
        )
        HomeModeButton(
            label = stringResource(R.string.str_xtrememedia),
            selected = false,
            imageRes = R.drawable.xtrememediaicon,
            onClick = onMediaClick,
            modifier = Modifier.weight(1f)
        )
        HomeModeButton(
            label = stringResource(R.string.str_xtremecart),
            selected = false,
            useCartIcon = true,
            onClick = onCartClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun HomeModeButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    imageRes: Int? = null,
    useCartIcon: Boolean = false
) {
    val shape = RoundedCornerShape(10.dp)
    val background = if (selected) {
        androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(HomeModeSelectedStart, HomeModeSelectedEnd))
    } else {
        androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color.White, Color.White))
    }

    Row(
        modifier = modifier
            .fillMaxHeight()
            .background(background, shape)
            .border(1.dp, if (selected) HomeNavPrimary else HomeModeUnselectedBorder, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 3.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        when {
            imageRes != null -> Image(
                painter = painterResource(imageRes),
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
            useCartIcon -> HomeNavIconCanvas(
                icon = HomeNavIcon.CART,
                color = if (selected) HomeNavPrimary else HomeModeUnselectedText,
                modifier = Modifier.size(17.dp)
            )
        }

        Spacer(modifier = Modifier.size(4.dp))

        Text(
            text = label,
            color = if (selected) Color.White else HomeModeUnselectedText,
            fontSize = 8.4.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SportsHomeActionTopBar(
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(84.dp)
            .background(HomeTopBarBackground)
            .padding(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HomeActionIconButton(
            icon = HomeActionIcon.MENU,
            onClick = onMenuClick,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.size(12.dp))
        Image(
            painter = painterResource(R.drawable.appicon),
            contentDescription = "SportsXtreme",
            modifier = Modifier.size(width = 64.dp, height = 36.dp)
        )
        Spacer(modifier = Modifier.size(7.dp))
        SportsHomeBrandTitle()
        Spacer(modifier = Modifier.weight(1f))
        HomeActionIconCanvas(
            icon = HomeActionIcon.SEARCH,
            color = Color.White,
            modifier = Modifier.size(21.dp)
        )
        Spacer(modifier = Modifier.size(10.dp))
        HomeActionIconCanvas(
            icon = HomeActionIcon.BELL,
            color = Color.White,
            modifier = Modifier.size(21.dp)
        )
        Spacer(modifier = Modifier.size(10.dp))
        HomeActionIconCanvas(
            icon = HomeActionIcon.MESSAGE,
            color = Color.White,
            modifier = Modifier.size(21.dp)
        )
    }
}

@Composable
fun SportsHomeLocationRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HomeLocationPinIcon(
            color = HomeNavPrimary,
            modifier = Modifier.size(17.dp)
        )
        Spacer(modifier = Modifier.size(7.dp))
        Text(
            text = stringResource(R.string.str_madanpurbhubaneswar),
            color = Color(0xFFD2E0DC),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun HomeLocationPinIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = size.minDimension * 0.11f, cap = StrokeCap.Round)
        val pin = Path().apply {
            moveTo(size.width * 0.5f, size.height * 0.92f)
            cubicTo(size.width * 0.2f, size.height * 0.58f, size.width * 0.18f, size.height * 0.34f, size.width * 0.34f, size.height * 0.18f)
            cubicTo(size.width * 0.43f, size.height * 0.09f, size.width * 0.57f, size.height * 0.09f, size.width * 0.66f, size.height * 0.18f)
            cubicTo(size.width * 0.82f, size.height * 0.34f, size.width * 0.8f, size.height * 0.58f, size.width * 0.5f, size.height * 0.92f)
            close()
        }
        drawPath(pin, color, style = stroke)
        drawCircle(
            color = color,
            radius = size.minDimension * 0.12f,
            center = Offset(size.width * 0.5f, size.height * 0.4f),
            style = stroke
        )
    }
}

@Composable
fun SportsHomeScoreCardsSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Matches Near You",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = stringResource(R.string.str_view_all),
                color = HomeNavPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable {
                        context.startActivity(Intent(context, ViewAllScoreCardActivity::class.java))
                    }
                    .padding(start = 10.dp, top = 6.dp, bottom = 6.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(end = 14.dp, bottom = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HomeScoreCards.forEach { score ->
                SportsHomeScoreCard(
                    score = score,
                    onClick = { openScorecard(context, score) },
                    modifier = Modifier.width(320.dp)
                )
            }
        }
    }
}

@Composable
private fun SportsHomeScoreCard(
    score: HomeScoreCard,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)

    Column(
        modifier = modifier
            .background(
                brush = Brush.linearGradient(
                    listOf(Color(0xFF081C3A), Color(0xFF06121F), Color(0xFF08283E))
                ),
                shape = shape
            )
            .border(1.dp, Color(0x303AD7FF), shape)
            .clickable(onClick = onClick)
            .padding(start = 14.dp, top = 11.dp, end = 14.dp, bottom = 12.dp)
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
                Text(
                    text = score.round,
                    color = Color(0xFF6A7989),
                    fontSize = 8.2.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            SportsHomeLiveBadge()
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SportsHomeScoreTeam(
                name = score.leftName,
                score = score.leftScore,
                overs = score.leftOvers,
                accent = HomeNavPrimary,
                icon = HomeNavIcon.TROPHY,
                modifier = Modifier.weight(1f)
            )
            SportsHomeVsBadge(modifier = Modifier.width(54.dp))
            SportsHomeScoreTeam(
                name = score.rightName,
                score = score.rightScore,
                overs = score.rightOvers,
                accent = Color(0xFF00D2FF),
                icon = HomeNavIcon.BARS,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 9.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            SportsHomeScoreStatChip(label = "TARGET", value = score.target)
            Spacer(modifier = Modifier.size(8.dp))
            SportsHomeScoreStatChip(label = "RRR", value = score.rrr)
            Spacer(modifier = Modifier.size(8.dp))
            SportsHomeScoreStatChip(label = "WIN", value = score.win)
        }

        Text(
            text = score.note,
            color = Color(0xFFB2BFCD),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
    }
}

@Composable
private fun SportsHomeLiveBadge(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .size(width = 58.dp, height = 28.dp)
            .background(Color(0xFF10241E), RoundedCornerShape(14.dp))
            .border(1.dp, HomeNavPrimary, RoundedCornerShape(14.dp))
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(HomeNavPrimary, CircleShape)
        )
        Spacer(modifier = Modifier.size(5.dp))
        Text(
            text = "LIVE",
            color = HomeNavPrimary,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
private fun SportsHomeScoreTeam(
    name: String,
    score: String,
    overs: String,
    accent: Color,
    icon: HomeNavIcon,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(96.dp)
            .background(
                brush = Brush.verticalGradient(listOf(Color(0x9623352F), Color(0xA0080F16))),
                shape = RoundedCornerShape(10.dp)
            )
            .border(1.dp, accent.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(accent.copy(alpha = 0.16f), CircleShape)
                .border(1.dp, accent.copy(alpha = 0.55f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            HomeNavIconCanvas(
                icon = icon,
                color = accent,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = name,
            color = accent,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp)
        )
        Text(
            text = score,
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = overs,
            color = Color(0xFF7D8B9A),
            fontSize = 7.5.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SportsHomeVsBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(HomeNavPrimary, CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.str_vs),
                color = Color(0xFF070E14),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SportsHomeScoreStatChip(label: String, value: String) {
    Row(
        modifier = Modifier
            .height(32.dp)
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, Color.Black.copy(alpha = 0.58f), RoundedCornerShape(16.dp))
            .padding(horizontal = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color(0xFF222222),
            fontSize = 7.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Spacer(modifier = Modifier.size(5.dp))
        Text(
            text = value,
            color = Color.Black,
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

private fun openScorecard(context: android.content.Context, score: HomeScoreCard) {
    context.startActivity(Intent(context, ScorecardActivity::class.java).apply {
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
    })
}

@Composable
fun SportsHomeProPassSection(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(end = 14.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HomeProPasses.forEach { pass ->
            SportsHomeProPassCard(
                pass = pass,
                modifier = Modifier.width(pass.width.dp)
            )
        }
    }
}

@Composable
private fun SportsHomeProPassCard(
    pass: HomeProPass,
    modifier: Modifier = Modifier
) {
    val isHallOfFame = pass.title == "HALL OF FAME"
    val contentColor = if (isHallOfFame) Color.Black else Color.White
    val shape = RoundedCornerShape(20.dp)

    Column(
        modifier = modifier
            .height(296.dp)
            .background(
                brush = Brush.linearGradient(
                    listOf(
                        pass.background,
                        pass.background,
                        pass.accent.copy(alpha = if (isHallOfFame) 0.34f else 0.18f)
                    )
                ),
                shape = shape
            )
            .border(1.dp, pass.accent.copy(alpha = if (isHallOfFame) 0.65f else 0.35f), shape)
            .padding(horizontal = if (pass.width > 400) 20.dp else 18.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 5.dp, height = 20.dp)
                        .background(pass.accent, RoundedCornerShape(4.dp))
                )
                Text(
                    text = pass.memberLabel,
                    color = if (isHallOfFame) Color(0xFF2B1800) else pass.accent,
                    fontSize = 9.2.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 7.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color.White.copy(alpha = 0.14f), RoundedCornerShape(9.dp))
                    .border(1.dp, pass.accent.copy(alpha = 0.58f), RoundedCornerShape(9.dp)),
                contentAlignment = Alignment.Center
            ) {
                HomeNavIconCanvas(
                    icon = HomeNavIcon.TROPHY,
                    color = if (isHallOfFame) Color(0xFF2B1800) else pass.accent,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(if (pass.width > 400) 0.72f else 0.88f)) {
                Text(
                    text = pass.title,
                    color = contentColor,
                    fontSize = when {
                        isHallOfFame -> 30.sp
                        pass.title.length > 5 -> 33.sp
                        else -> 39.sp
                    },
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    maxLines = if (isHallOfFame) 2 else 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (pass.passLabel.isNotBlank()) {
                    Text(
                        text = pass.passLabel,
                        color = pass.accent,
                        fontSize = 39.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        maxLines = 1,
                        modifier = Modifier.offset(y = (-5).dp)
                    )
                }
                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    if (pass.oldPrice.isNotBlank()) {
                        Text(
                            text = pass.oldPrice,
                            color = Color(0xFF9CA69C),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
                            textDecoration = TextDecoration.LineThrough,
                            maxLines = 1,
                            modifier = Modifier.padding(end = 7.dp, bottom = 6.dp)
                        )
                    }
                    Text(
                        text = pass.price,
                        color = contentColor,
                        fontSize = if (isHallOfFame) 38.sp else if (pass.price.length > 3) 31.sp else 35.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        maxLines = 1
                    )
                    Text(
                        text = pass.priceSuffix,
                        color = if (isHallOfFame) Color(0xFF2B1800) else Color(0xFF9CA69C),
                        fontSize = 7.2.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                    )
                }
            }

            SportsHomeProFeatureGrid(
                pass = pass,
                modifier = Modifier
                    .weight(if (pass.width > 400) 1.58f else 1.12f)
                    .padding(start = if (pass.width > 400) 16.dp else 12.dp)
            )
        }

        Text(
            text = stringResource(R.string.str_cancel_anytime__no_h),
            color = if (isHallOfFame) Color(0xFF2B1800) else pass.accent,
            fontSize = 6.3.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
        SportsHomeActivateButton(
            pass = pass,
            darkText = pass.accent == HomeNavPrimary || isHallOfFame,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 7.dp)
        )
    }
}

@Composable
private fun SportsHomeProFeatureGrid(
    pass: HomeProPass,
    modifier: Modifier = Modifier
) {
    val columns = if (pass.features.size > 5) pass.features.chunked(4) else listOf(pass.features)
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        columns.forEach { column ->
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(if (pass.features.size > 3) 5.dp else 9.dp)) {
                column.forEach { feature ->
                    SportsHomeProFeatureLine(
                        title = feature.first,
                        subtitle = feature.second,
                        accent = pass.accent
                    )
                }
            }
        }
    }
}

@Composable
private fun SportsHomeProFeatureLine(
    title: String,
    subtitle: String,
    accent: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .background(accent.copy(alpha = 0.16f), CircleShape)
                .border(1.dp, accent.copy(alpha = 0.55f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            HomeNavIconCanvas(
                icon = HomeNavIcon.PLUS,
                color = accent,
                modifier = Modifier.size(12.dp)
            )
        }
        Column(modifier = Modifier.padding(start = 7.dp)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                color = Color(0xFF9EA9A4),
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SportsHomeActivateButton(
    pass: HomeProPass,
    darkText: Boolean,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(8.dp)
    Row(
        modifier = modifier
            .height(38.dp)
            .background(
                brush = Brush.verticalGradient(listOf(pass.accent.copy(alpha = 0.82f), pass.accent)),
                shape = shape
            )
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.str_activate_pro),
            color = if (darkText) Color(0xFF040904) else Color.White,
            fontSize = 9.3.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Spacer(modifier = Modifier.size(9.dp))
        HomeNavIconCanvas(
            icon = HomeNavIcon.PLUS,
            color = if (darkText) Color(0xFF040904) else Color.White,
            modifier = Modifier.size(15.dp)
        )
    }
}

@Composable
private fun SportsHomeBrandTitle() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Sports",
            color = Color(0xFFE8F1F6),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic,
            maxLines = 1,
            style = TextStyle(
                shadow = Shadow(
                    color = Color(0x5F0078FF),
                    blurRadius = 2f
                )
            )
        )
        Text(
            text = "Xtreme",
            color = Color(0xFF007FFF),
            fontSize = 16.5.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic,
            maxLines = 1,
            style = TextStyle(
                shadow = Shadow(
                    color = Color(0x730078FF),
                    blurRadius = 2f
                )
            )
        )
    }
}

@Composable
private fun HomeActionIconButton(
    icon: HomeActionIcon,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    HomeActionIconCanvas(
        icon = icon,
        color = Color.White,
        modifier = modifier.clickable(onClick = onClick)
    )
}

@Composable
private fun HomeActionIconCanvas(
    icon: HomeActionIcon,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = size.minDimension * 0.1f, cap = StrokeCap.Round)
        when (icon) {
            HomeActionIcon.MENU -> {
                listOf(0.25f, 0.5f, 0.75f).forEach { y ->
                    drawLine(
                        color = color,
                        start = Offset(size.width * 0.18f, size.height * y),
                        end = Offset(size.width * 0.82f, size.height * y),
                        strokeWidth = size.minDimension * 0.1f,
                        cap = StrokeCap.Round
                    )
                }
            }
            HomeActionIcon.SEARCH -> {
                drawCircle(
                    color = color,
                    radius = size.minDimension * 0.28f,
                    center = Offset(size.width * 0.43f, size.height * 0.43f),
                    style = stroke
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.64f, size.height * 0.64f),
                    end = Offset(size.width * 0.84f, size.height * 0.84f),
                    strokeWidth = size.minDimension * 0.1f,
                    cap = StrokeCap.Round
                )
            }
            HomeActionIcon.BELL -> {
                val bell = Path().apply {
                    moveTo(size.width * 0.3f, size.height * 0.55f)
                    cubicTo(size.width * 0.3f, size.height * 0.32f, size.width * 0.7f, size.height * 0.32f, size.width * 0.7f, size.height * 0.55f)
                    lineTo(size.width * 0.78f, size.height * 0.7f)
                    lineTo(size.width * 0.22f, size.height * 0.7f)
                    close()
                }
                drawPath(bell, color, style = stroke)
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.44f, size.height * 0.82f),
                    end = Offset(size.width * 0.56f, size.height * 0.82f),
                    strokeWidth = size.minDimension * 0.1f,
                    cap = StrokeCap.Round
                )
            }
            HomeActionIcon.MESSAGE -> {
                val bubble = Path().apply {
                    moveTo(size.width * 0.2f, size.height * 0.28f)
                    lineTo(size.width * 0.8f, size.height * 0.28f)
                    lineTo(size.width * 0.8f, size.height * 0.66f)
                    lineTo(size.width * 0.55f, size.height * 0.66f)
                    lineTo(size.width * 0.42f, size.height * 0.82f)
                    lineTo(size.width * 0.42f, size.height * 0.66f)
                    lineTo(size.width * 0.2f, size.height * 0.66f)
                    close()
                }
                drawPath(bubble, color, style = stroke)
            }
        }
    }
}

@Composable
private fun HomeBottomNavigation(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(HomeNavBackground)
            .border(1.dp, HomeNavStroke)
            .padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HomeNavItems.forEachIndexed { index, item ->
            if (index == 2) {
                HomeHostNavItem(
                    item = item,
                    onClick = { onItemSelected(index) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            } else {
                HomeStandardNavItem(
                    item = item,
                    active = selectedIndex == index,
                    onClick = { onItemSelected(index) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }
    }
}

@Composable
private fun HomeStandardNavItem(
    item: HomeNavItem,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .background(
                color = if (active) HomeNavPrimary else Color.Transparent,
                shape = RoundedCornerShape(9.dp)
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        HomeNavIconCanvas(
            icon = item.icon,
            color = if (active) HomeNavActiveText else HomeNavInactive,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = item.label,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp),
            color = if (active) HomeNavActiveText else HomeNavMuted,
            fontSize = if (item.label.length > 9) 7.2.sp else 8.5.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun HomeHostNavItem(
    item: HomeNavItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-32).dp)
                .size(74.dp)
                .background(Color(0x30C1FF00), CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-24).dp)
                .size(58.dp)
                .shadow(10.dp, CircleShape, clip = false)
                .background(HomeNavPrimary, CircleShape)
                .border(2.dp, Color(0xFFE6FF6E), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            HomeNavIconCanvas(
                icon = item.icon,
                color = HomeNavActiveText,
                modifier = Modifier.size(29.dp)
            )
        }
        Text(
            text = item.label,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 5.dp),
            color = HomeNavPrimary,
            fontSize = 8.8.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun HomeNavIconCanvas(
    icon: HomeNavIcon,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = size.minDimension * 0.1f, cap = StrokeCap.Round)
        when (icon) {
            HomeNavIcon.HOME -> {
                val roof = Path().apply {
                    moveTo(size.width * 0.15f, size.height * 0.48f)
                    lineTo(size.width * 0.5f, size.height * 0.18f)
                    lineTo(size.width * 0.85f, size.height * 0.48f)
                }
                drawPath(roof, color, style = stroke)
                drawRoundRect(
                    color = color,
                    topLeft = Offset(size.width * 0.25f, size.height * 0.45f),
                    size = Size(size.width * 0.5f, size.height * 0.38f),
                    style = stroke
                )
            }
            HomeNavIcon.BARS -> {
                val barWidth = size.width * 0.14f
                listOf(0.68f, 0.48f, 0.28f).forEachIndexed { index, height ->
                    val left = size.width * (0.22f + index * 0.22f)
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(left, size.height * (0.82f - height)),
                        size = Size(barWidth, size.height * height),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2f, barWidth / 2f)
                    )
                }
            }
            HomeNavIcon.PLUS -> {
                drawLine(color, Offset(size.width * 0.5f, size.height * 0.2f), Offset(size.width * 0.5f, size.height * 0.8f), strokeWidth = size.minDimension * 0.13f, cap = StrokeCap.Round)
                drawLine(color, Offset(size.width * 0.2f, size.height * 0.5f), Offset(size.width * 0.8f, size.height * 0.5f), strokeWidth = size.minDimension * 0.13f, cap = StrokeCap.Round)
            }
            HomeNavIcon.USERS -> {
                drawCircle(color, radius = size.minDimension * 0.16f, center = Offset(size.width * 0.38f, size.height * 0.35f), style = stroke)
                drawCircle(color, radius = size.minDimension * 0.13f, center = Offset(size.width * 0.68f, size.height * 0.4f), style = stroke)
                drawArc(color, startAngle = 200f, sweepAngle = 140f, useCenter = false, topLeft = Offset(size.width * 0.18f, size.height * 0.5f), size = Size(size.width * 0.42f, size.height * 0.35f), style = stroke)
                drawArc(color, startAngle = 205f, sweepAngle = 130f, useCenter = false, topLeft = Offset(size.width * 0.5f, size.height * 0.55f), size = Size(size.width * 0.32f, size.height * 0.28f), style = stroke)
            }
            HomeNavIcon.TROPHY -> {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(size.width * 0.32f, size.height * 0.2f),
                    size = Size(size.width * 0.36f, size.height * 0.34f),
                    style = stroke
                )
                drawArc(color, startAngle = 90f, sweepAngle = 180f, useCenter = false, topLeft = Offset(size.width * 0.12f, size.height * 0.24f), size = Size(size.width * 0.28f, size.height * 0.26f), style = stroke)
                drawArc(color, startAngle = 270f, sweepAngle = 180f, useCenter = false, topLeft = Offset(size.width * 0.6f, size.height * 0.24f), size = Size(size.width * 0.28f, size.height * 0.26f), style = stroke)
                drawLine(color, Offset(size.width * 0.5f, size.height * 0.55f), Offset(size.width * 0.5f, size.height * 0.72f), strokeWidth = size.minDimension * 0.1f, cap = StrokeCap.Round)
                drawLine(color, Offset(size.width * 0.34f, size.height * 0.78f), Offset(size.width * 0.66f, size.height * 0.78f), strokeWidth = size.minDimension * 0.1f, cap = StrokeCap.Round)
            }
            HomeNavIcon.CART -> {
                drawLine(color, Offset(size.width * 0.18f, size.height * 0.25f), Offset(size.width * 0.28f, size.height * 0.25f), strokeWidth = size.minDimension * 0.1f, cap = StrokeCap.Round)
                drawLine(color, Offset(size.width * 0.28f, size.height * 0.25f), Offset(size.width * 0.4f, size.height * 0.62f), strokeWidth = size.minDimension * 0.1f, cap = StrokeCap.Round)
                drawLine(color, Offset(size.width * 0.4f, size.height * 0.62f), Offset(size.width * 0.78f, size.height * 0.62f), strokeWidth = size.minDimension * 0.1f, cap = StrokeCap.Round)
                drawLine(color, Offset(size.width * 0.36f, size.height * 0.38f), Offset(size.width * 0.84f, size.height * 0.38f), strokeWidth = size.minDimension * 0.1f, cap = StrokeCap.Round)
                drawLine(color, Offset(size.width * 0.84f, size.height * 0.38f), Offset(size.width * 0.78f, size.height * 0.62f), strokeWidth = size.minDimension * 0.1f, cap = StrokeCap.Round)
                drawCircle(color, radius = size.minDimension * 0.06f, center = Offset(size.width * 0.45f, size.height * 0.78f))
                drawCircle(color, radius = size.minDimension * 0.06f, center = Offset(size.width * 0.74f, size.height * 0.78f))
            }
        }
    }
}
