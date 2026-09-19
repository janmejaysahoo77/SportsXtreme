package com.example.sportsxtreme.presentation.clubs

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.sportsxtreme.R
import com.example.sportsxtreme.presentation.ui.theme.*
import androidx.compose.ui.graphics.Color as ComposeColor

class ClubLandingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = XtremeDarkBlueHex.toInt()
        window.navigationBarColor = XtremeDarkBlueHex.toInt()
        setContent { ClubLandingScreen(onBack = ::finish) }
    }
}

private val Lime = XtremeLime
private val ScreenBlack = XtremeBgBlue
private val CardBlack = XtremeCardBlue
private val Muted = XtremeMuted
private val CardBorder = XtremeCardBorder

@Composable
private fun ClubLandingScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = ScreenBlack,
        topBar = { Header(onBack) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ScreenBlack)
                .padding(innerPadding)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp)
                .padding(bottom = 22.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            ClubHeroPager()
            Spacer(Modifier.height(12.dp))
            MyClubsCard()
            Spacer(Modifier.height(12.dp))
            DiscoverCard()
            Spacer(Modifier.height(12.dp))
            CreateClubCard()
            Spacer(Modifier.height(12.dp))
            BenefitsCard()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Header(onBack: () -> Unit) = Column(
    modifier = Modifier
        .fillMaxWidth()
        .background(ScreenBlack)
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                stringResource(R.string.clubs_title),
                color = ComposeColor.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
        },
        navigationIcon = {
            IconButton(
                onClick = {
                    onBack()
                },

                ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = null,
                    tint = ComposeColor.White,
                    modifier = Modifier.size(30.dp)

                )
            }
        },
        actions = {
            Row(
                modifier = Modifier.padding(end = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = ComposeColor.White
                    )
                }
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = null,
                        tint = ComposeColor.White
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = ScreenBlack
        )
    )

}

@Composable
private fun HeaderButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) = Box(
    modifier = modifier
        .clickable(onClick = onClick),
    contentAlignment = Alignment.Center
) {
    content()
}

private data class HeroPage(
    val eyebrow: String,
    val heading: String,
    val highlightedHeading: String,
    val description: String,
    val actions: List<Pair<Int, String>>,
    val tint: ComposeColor
)

private val clubHeroPages = listOf(
    HeroPage(
        "COMMUNITY HUB",
        "Build Your",
        "Cricket Community",
        "Create, discover and manage cricket clubs, players, tournaments and teams all in one place.",
        listOf(
            R.drawable.group to "10K+ clubs",
            R.drawable.my_club to "2M+ players",
            R.drawable.cricketlogo to "50K+ matches"
        ),
        ComposeColor(18, 53, 39)
    ),
    HeroPage(
        "CLUBS & TEAMS",
        "Manage",
        "Your Clubs",
        "Keep your squads connected, organise members and follow every club activity.",
        listOf(
            R.drawable.group to "Manage members",
            R.drawable.setting to "Club settings",
            R.drawable.outlined_thunder to "Track activity"
        ),
        ComposeColor(35, 58, 33)
    ),
    HeroPage(
        "CLUBS & TEAMS",
        "Create Your",
        "Own Club",
        "Start a cricket club, invite your members and build your community your way.",
        listOf(
            R.drawable.group to "Invite members",
            R.drawable.group to "Run tournaments",
            R.drawable.setting to "Manage club"
        ),
        ComposeColor(59, 47, 28)
    ),
    HeroPage(
        "CLUBS & TEAMS",
        "Discover &",
        "Join Clubs",
        "Find clubs and teams near you, then send a request to become part of the action.",
        listOf(
            R.drawable.group to "Browse clubs",
            R.drawable.location to "Search teams",
            R.drawable.plus to "Send request"
        ),
        ComposeColor(22, 42, 49)
    )
)

@Composable
private fun ClubHeroPager() {
    val pagerState = rememberPagerState { clubHeroPages.size }
    Column {
        HorizontalPager(
            state = pagerState,
            pageSpacing = 10.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
        ) { page -> HeroPageCard(clubHeroPages[page]) }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(clubHeroPages.size) { index ->
                Box(
                    Modifier
                        .padding(horizontal = 3.dp)
                        .size(if (pagerState.currentPage == index) 7.dp else 5.dp)
                        .clip(CircleShape)
                        .background(
                            if (pagerState.currentPage == index) Lime else ComposeColor.White.copy(
                                alpha = .28f
                            )
                        )
                )
            }
        }
    }
}

@Composable
private fun HeroPageCard(page: HeroPage) = Card(
    shape = RoundedCornerShape(22.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, ComposeColor.White.copy(alpha = .22f)),
    colors = CardDefaults.cardColors(containerColor = CardBlack),
    modifier = Modifier.fillMaxSize()
) {
    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.club_stadium),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(.55f)
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            ScreenBlack.copy(alpha = .96f),
                            page.tint.copy(alpha = .78f),
                            ComposeColor.Black.copy(alpha = .20f)
                        )
                    )
                )
        )
        Column(Modifier
            .fillMaxSize()
            .padding(18.dp)) {
            Tag(
                page.eyebrow,
                bgColor = ComposeColor.White.copy(alpha = .12f),
                borderColor = ComposeColor.White.copy(alpha = .28f)
            )
            Spacer(Modifier.height(20.dp))
            Text(
                page.heading,
                color = ComposeColor.White,
                fontSize = 31.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 34.sp
            )
            Text(
                page.highlightedHeading,
                color = Lime,
                fontSize = 31.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 34.sp
            )
            Spacer(Modifier.height(12.dp))
            Text(
                page.description,
                color = ComposeColor.White.copy(alpha = .76f),
                fontSize = 13.sp,
                lineHeight = 19.sp,
                modifier = Modifier.fillMaxWidth(.68f)
            )
            Spacer(Modifier.weight(1f))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                page.actions.forEach { (icon, label) ->
                    HeroActionTile(
                        icon,
                        label,
                        Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroActionTile(icon: Int, label: String, modifier: Modifier = Modifier) = Card(
    modifier = modifier.height(76.dp),
    shape = RoundedCornerShape(14.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, ComposeColor.White.copy(alpha = .20f)),
    colors = CardDefaults.cardColors(containerColor = ComposeColor.Black.copy(alpha = .28f))
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            colorFilter = ColorFilter.tint(ComposeColor.White)
        )
        Spacer(Modifier.height(5.dp))
        Text(
            label,
            color = ComposeColor.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 11.sp,
            maxLines = 2
        )
    }
}

@Composable
private fun MyClubsCard() = ClubCard(
    symbol = R.drawable.my_club,
    title = stringResource(R.string.my_clubs),
    body = stringResource(R.string.my_clubs_body),
    button = stringResource(R.string.view_club),
    extraInTitleRow = true
) {
    Tag(
        text = stringResource(R.string.one_active_club),
        bgColor = Lime.copy(alpha = 0.3f),
        borderColor = Lime
    )
}

@Composable
private fun DiscoverCard() = ClubCard(
    symbol = R.drawable.location,
    title = stringResource(R.string.discover_clubs),
    body = stringResource(R.string.discover_clubs_body),
    button = stringResource(R.string.discover_button)
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Tag(stringResource(R.string.nearby))
        Tag(stringResource(R.string.verified))
        Tag(stringResource(R.string.trending))
    }
}

@Composable
private fun CreateClubCard() = ClubCard(
    R.drawable.plus,
    stringResource(R.string.create_club),
    stringResource(R.string.create_club_body),
    stringResource(R.string.create_club_button)
)

@Composable
private fun ClubCard(
    symbol: Int,
    title: String,
    body: String,
    button: String,
    extraInTitleRow: Boolean = false,
    extra: @Composable (() -> Unit)? = null
) =
    Card(
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        colors = CardDefaults.cardColors(containerColor = ComposeColor.DarkGray.copy(alpha = 0.1f)),
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {
        val context = LocalContext.current
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Card(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        ComposeColor.White.copy(alpha = 0.2f)
                    ),
                    colors = CardDefaults.cardColors(containerColor = ComposeColor.White.copy(alpha = 0.1f)),

                    ) {
                    Image(
                        painter = painterResource(symbol),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(Modifier.height(7.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        title,
                        color = ComposeColor.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (extraInTitleRow && extra != null) {
                        Spacer(Modifier.weight(1f))
                        extra()
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    body,
                    color = Muted,
                    fontSize = 12.sp,

                    modifier = Modifier.fillMaxWidth(.7f)
                )
                if (!extraInTitleRow && extra != null) {
                    Spacer(Modifier.height(10.dp))
                    extra()
                }
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .height(50.dp)
                        .shadow(
                            elevation = 6.dp, shape = RoundedCornerShape(16.dp),
                            clip = true, spotColor = Lime, ambientColor = Lime
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .background(Lime)

                        .clickable {
                            when (title) {
                                context.getString(R.string.my_clubs) -> context.startActivity(
                                    Intent(
                                        context,
                                        MyClubsLandingPageActivity::class.java
                                    )
                                )

                                context.getString(R.string.discover_clubs) -> context.startActivity(
                                    Intent(
                                        context,
                                        DiscoverClubsActivity::class.java
                                    )
                                )

                                context.getString(R.string.create_club) -> context.startActivity(
                                    Intent(
                                        context,
                                        Step_OneCreateClubActivity::class.java
                                    )
                                )
                            }
                        }, contentAlignment = Alignment.Center
                ) {
                    Row {
                        Text(
                            button,
                            color = ComposeColor.Black,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.W800
                        )
                        Image(
                            painter = painterResource(R.drawable.thunder),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

@Composable
private fun BenefitsCard() = Card(
    shape = RoundedCornerShape(18.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
    colors = CardDefaults.cardColors(containerColor = ComposeColor.DarkGray.copy(alpha = 0.1f)),
    modifier = Modifier
        .fillMaxWidth()
) {
    Box {
        Image(
            painter = painterResource(R.drawable.trophy),
            contentDescription = null,
            modifier = Modifier

                .align(Alignment.BottomEnd)
                .alpha(0.15f)
        )
        Column(Modifier.padding(13.dp)) {

            Tag(
                stringResource(R.string.professional_suite),
                bgColor = Lime.copy(alpha = 0.4f),
                borderColor = Lime
            )
            Spacer(Modifier.height(10.dp))
            Text(
                stringResource(R.string.everything_you_need_title),
                color = ComposeColor.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
            Spacer(Modifier.height(8.dp))
            Benefit(
                R.drawable.group,
                stringResource(R.string.team_management),
                stringResource(R.string.team_management_body)
            )
            Spacer(Modifier.height(16.dp))
            Benefit(
                R.drawable.network,
                stringResource(R.string.tournament_hosting),
                stringResource(R.string.tournament_hosting_body)
            )
            Spacer(Modifier.height(16.dp))
            Benefit(
                R.drawable.outlined_thunder,
                stringResource(R.string.ai_analytics),
                stringResource(R.string.ai_analytics_body)
            )
        }
    }
}

@Composable
private fun Benefit(icon: Int, title: String, body: String) = Row(
    Modifier
        .fillMaxWidth()
        .padding(vertical = 5.dp),

    ) {
    Card(
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        colors = CardDefaults.cardColors(containerColor = ComposeColor.DarkGray.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.size(50.dp),

        ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                colorFilter = ColorFilter.tint(
                    Lime
                )

            )
        }
    }
    Spacer(Modifier.width(8.dp))
    Column {
        Text(
            title,
            color = ComposeColor.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(body, color = Muted, fontSize = 10.sp)
    }
}

@Composable
private fun Tag(
    text: String,
    bgColor: ComposeColor = ComposeColor.White.copy(alpha = 0.3f),
    borderColor: ComposeColor? = null
) = Text(
    text,
    color = ComposeColor.White,
    fontSize = 10.sp,
    fontWeight = FontWeight.ExtraBold,
    modifier = Modifier
        .clip(RoundedCornerShape(16.dp))
        .then(
            if (borderColor != null) Modifier.border(
                1.dp,
                borderColor,
                RoundedCornerShape(16.dp)
            ) else Modifier
        )
        .background(bgColor)
        .padding(horizontal = 10.dp, vertical = 6.dp)
)

@Composable
private fun Stat(value: String, label: String) = Column {
    Text(
        value,
        color = ComposeColor.White,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
        label, color = Muted, fontSize = 6.sp,
        modifier = Modifier.align(Alignment.CenterHorizontally)
    )
}


@Composable
private fun IconTile(symbol: String, modifier: Modifier = Modifier.size(25.dp)) {
    Box(
        modifier
            .clip(RoundedCornerShape(7.dp))
            .background(ComposeColor(35, 43, 38)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(15.dp)) {
            val iconColor = Lime
            when (symbol) {
                "+" -> {
                    drawLine(
                        iconColor,
                        Offset(size.width / 2, 2f),
                        Offset(size.width / 2, size.height - 2f),
                        1.8f
                    )
                    drawLine(
                        iconColor,
                        Offset(2f, size.height / 2),
                        Offset(size.width - 2f, size.height / 2),
                        1.8f
                    )
                }

                "◉" -> {
                    drawCircle(iconColor, radius = size.minDimension * .38f, style = Stroke(1.5f))
                    drawCircle(iconColor, radius = size.minDimension * .12f)
                }

                else -> {
                    drawRoundRect(
                        iconColor,
                        topLeft = Offset(3f, 2f),
                        size = androidx.compose.ui.geometry.Size(size.width - 6f, size.height - 4f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f),
                        style = Stroke(1.5f)
                    )
                    drawLine(
                        iconColor,
                        Offset(5f, size.height / 2),
                        Offset(size.width - 5f, size.height / 2),
                        1.2f
                    )
                }
            }
        }
    }
}

@Composable
private fun BackIcon() = Canvas(Modifier.size(16.dp)) {
    drawLine(Muted, Offset(11f, 3f), Offset(5f, 8f), 1.5f)
    drawLine(Muted, Offset(5f, 8f), Offset(11f, 13f), 1.5f)
}

@Composable
private fun SearchIcon(modifier: Modifier = Modifier) = Canvas(modifier) {
    drawCircle(Muted, radius = 5f, center = Offset(7f, 7f), style = Stroke(1.5f))
    drawLine(Muted, Offset(10.5f, 10.5f), Offset(15f, 15f), 1.5f)
}

@Composable
private fun NotificationIcon(modifier: Modifier = Modifier) = Canvas(modifier) {
    drawRoundRect(
        Muted,
        topLeft = Offset(5f, 4f),
        size = androidx.compose.ui.geometry.Size(8f, 10f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f),
        style = Stroke(1.3f)
    )
    drawLine(Muted, Offset(3.5f, 5f), Offset(14.5f, 5f), 1.3f)
    drawCircle(Lime, radius = 1.5f, center = Offset(14f, 4f))
}
