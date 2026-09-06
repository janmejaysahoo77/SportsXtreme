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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color as ComposeColor

class ClubLandingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = Color.rgb(5, 7, 8)
        window.navigationBarColor = Color.rgb(5, 7, 8)
        setContent { ClubLandingScreen(onBack = ::finish) }
    }
}

private val Lime = ComposeColor(190, 255, 24)
private val ScreenBlack = ComposeColor(5, 7, 8)
private val CardBlack = ComposeColor(15, 17, 17)
private val Muted = ComposeColor(162, 169, 164)
private val CardBorder = ComposeColor(45, 51, 46)

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
            CommunityHero()
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

@Composable
private fun CommunityHero() = Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = CardBlack),
    modifier = Modifier
        .fillMaxWidth()
        .height(290.dp)
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(
                        ComposeColor.White.copy(alpha = 0.1f),
                        ComposeColor.Black.copy(alpha = 0.6f)
                    ),
                    start = Offset(Float.POSITIVE_INFINITY, 0f),
                    end = Offset(0f, Float.POSITIVE_INFINITY)

                )
            )
    ) {
        Box(
            modifier = Modifier

                .align(Alignment.BottomEnd)
                .clip(RoundedCornerShape(topStart = 8.dp)),
        ) {
            Image(
                painter = painterResource(R.drawable.club_stadium),
                contentDescription = null,
                modifier = Modifier.alpha(0.25f),
                contentScale = ContentScale.FillBounds
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                ComposeColor.White.copy(alpha = 0.03f),
                                ComposeColor.Black.copy(alpha = 0.38f)
                            ),
                            start = Offset(Float.POSITIVE_INFINITY, 0f),
                            end = Offset(0f, Float.POSITIVE_INFINITY)

                        )
                    )
            )
        }

        Column(modifier = Modifier.padding(14.dp)) {
            Tag(stringResource(R.string.community_hub))
            Spacer(Modifier.height(11.dp))
            Text(
                stringResource(R.string.build_cricket_community),
                color = ComposeColor.White,
                fontSize = 28.sp,

                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(7.dp))
            Text(
                stringResource(R.string.community_hero_body),
                color = Muted,
                fontSize = 12.sp,

                )
            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(21.dp)) {
                Stat(
                    stringResource(R.string.stat_10k_plus),
                    stringResource(R.string.clubs_created)
                );
                Stat(stringResource(R.string.stat_2m_plus), stringResource(R.string.players));
                Stat(stringResource(R.string.stat_50k_plus), stringResource(R.string.matches))
            }
        }
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
