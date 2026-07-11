package com.example.sportsxtreme

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class XtremeSectionMode { SPORTS, MEDIA, CART }

data class XtremeSectionActions(
    val openSports: () -> Unit = {},
    val openMedia: () -> Unit = {},
    val openCart: () -> Unit = {}
)

private val XtremeBackground = Color(0xFF010509)
private val XtremeTopBarBackground = Color(0xFF060C11)
private val XtremeAccent = Color(0xFFC1FF00)
private val XtremeInactiveText = Color(0xFF060C11)
private val XtremeMediaBlue = Color(0xFF007FFF)
private val XtremeMediaWhite = Color(0xFFE8F1F6)
private val XtremeSelectedTabStart = Color(0xFF0C1E18)
private val XtremeSelectedTabEnd = Color(0xFF041832)
private val XtremeTabBorder = Color.White.copy(alpha = 0.33f)

@Composable
fun XtremeSectionScreen(
    title: String,
    bodyText: String,
    selectedMode: XtremeSectionMode,
    @DrawableRes logoRes: Int? = null,
    useCartLogo: Boolean = false,
    actions: XtremeSectionActions = XtremeSectionActions()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(XtremeBackground)
    ) {
        XtremeSectionTopBar(
            title = title,
            selectedMode = selectedMode,
            logoRes = logoRes,
            useCartLogo = useCartLogo,
            actions = actions
        )
        XtremeSectionBody(text = bodyText)
    }
}

@Composable
private fun XtremeSectionBody(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 28.sp,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun XtremeSectionTopBar(
    title: String,
    selectedMode: XtremeSectionMode,
    @DrawableRes logoRes: Int?,
    useCartLogo: Boolean,
    actions: XtremeSectionActions
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(XtremeTopBarBackground)
            .padding(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 18.dp)
    ) {
        XtremeModeButtons(
            selectedMode = selectedMode,
            actions = actions
        )
        XtremeBrandRow(
            title = title,
            selectedMode = selectedMode,
            logoRes = logoRes,
            useCartLogo = useCartLogo
        )
    }
}

@Composable
private fun XtremeBrandRow(
    title: String,
    selectedMode: XtremeSectionMode,
    @DrawableRes logoRes: Int?,
    useCartLogo: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .padding(top = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        XtremeBrandLogo(logoRes = logoRes, useCartLogo = useCartLogo)
        Spacer(modifier = Modifier.width(8.dp))
        XtremeSectionTitle(title = title, selectedMode = selectedMode)
        Spacer(modifier = Modifier.weight(1f))
        XtremeTopBarActions(selectedMode = selectedMode)
    }
}

@Composable
private fun XtremeBrandLogo(@DrawableRes logoRes: Int?, useCartLogo: Boolean) {
    when {
        useCartLogo -> CartIcon(tint = XtremeAccent, modifier = Modifier.size(36.dp))
        logoRes != null -> Image(
            painter = painterResource(logoRes),
            contentDescription = null,
            modifier = Modifier.size(width = 42.dp, height = 36.dp)
        )
    }
}

@Composable
private fun XtremeTopBarActions(selectedMode: XtremeSectionMode) {
    TopBarIcon(icon = TopIcon.Search, modifier = Modifier.size(21.dp))
    if (selectedMode != XtremeSectionMode.CART) {
        Spacer(modifier = Modifier.width(10.dp))
        TopBarIcon(icon = TopIcon.Bell, modifier = Modifier.size(21.dp))
        Spacer(modifier = Modifier.width(10.dp))
        TopBarIcon(icon = TopIcon.Message, modifier = Modifier.size(21.dp))
    }
}

@Composable
private fun XtremeSectionTitle(title: String, selectedMode: XtremeSectionMode) {
    val shadow = Modifier.shadow(2.dp, ambientColor = Color(0x690078FF), spotColor = Color(0x690078FF))
    if (selectedMode != XtremeSectionMode.MEDIA) {
        Text(
            text = title,
            modifier = shadow,
            color = Color.White,
            fontSize = 18.sp,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Bold
        )
        return
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Xtreme",
            modifier = shadow,
            color = XtremeMediaBlue,
            fontSize = 18.sp,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Media",
            modifier = shadow,
            color = XtremeMediaWhite,
            fontSize = 16.5.sp,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun XtremeModeButtons(
    selectedMode: XtremeSectionMode,
    actions: XtremeSectionActions
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        XtremeModeButton(
            label = stringResource(R.string.str_sportsxtreme),
            selected = selectedMode == XtremeSectionMode.SPORTS,
            imageRes = R.drawable.appicon2,
            modifier = Modifier.weight(1f),
            onClick = actions.openSports
        )
        XtremeModeButton(
            label = stringResource(R.string.str_xtrememedia),
            selected = selectedMode == XtremeSectionMode.MEDIA,
            imageRes = R.drawable.xtrememediaicon,
            modifier = Modifier.weight(1f),
            onClick = {
                if (selectedMode != XtremeSectionMode.MEDIA) {
                    actions.openMedia()
                }
            }
        )
        XtremeModeButton(
            label = stringResource(R.string.str_xtremecart),
            selected = selectedMode == XtremeSectionMode.CART,
            useCartIcon = true,
            modifier = Modifier.weight(1f),
            onClick = {
                if (selectedMode != XtremeSectionMode.CART) {
                    actions.openCart()
                }
            }
        )
    }
}

@Composable
private fun XtremeModeButton(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    @DrawableRes imageRes: Int? = null,
    useCartIcon: Boolean = false,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(10.dp)
    val background = if (selected) {
        Brush.horizontalGradient(listOf(XtremeSelectedTabStart, XtremeSelectedTabEnd))
    } else {
        Brush.linearGradient(listOf(Color.White, Color.White))
    }
    Row(
        modifier = modifier
            .fillMaxHeight()
            .background(background, shape)
            .border(1.dp, if (selected) XtremeAccent else XtremeTabBorder, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 3.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        imageRes?.let {
            Image(
                painter = painterResource(it),
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        if (useCartIcon) {
            CartIcon(
                tint = if (selected) XtremeAccent else XtremeInactiveText,
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = label,
            color = if (selected) Color.White else XtremeInactiveText,
            fontSize = 8.4.sp,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            style = TextStyle(textAlign = TextAlign.Center)
        )
    }
}

private enum class TopIcon { Search, Bell, Message }

@Composable
private fun CartIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val s = minOf(w, h)
        val stroke = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val path = Path().apply {
            moveTo(w * 0.16f, h * 0.24f)
            lineTo(w * 0.26f, h * 0.24f)
            lineTo(w * 0.36f, h * 0.66f)
            lineTo(w * 0.78f, h * 0.66f)
            lineTo(w * 0.86f, h * 0.38f)
            lineTo(w * 0.32f, h * 0.38f)
        }
        drawPath(path, tint, style = stroke)
        drawCircle(tint, radius = s * 0.055f, center = Offset(w * 0.43f, h * 0.82f))
        drawCircle(tint, radius = s * 0.055f, center = Offset(w * 0.72f, h * 0.82f))
    }
}

@Composable
private fun TopBarIcon(icon: TopIcon, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val s = minOf(w, h)
        val stroke = Stroke(width = 1.9.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        when (icon) {
            TopIcon.Search -> {
                drawCircle(Color.White, radius = s * 0.2f, center = Offset(w * 0.45f, h * 0.43f), style = stroke)
                drawLine(Color.White, Offset(w * 0.6f, h * 0.58f), Offset(w * 0.78f, h * 0.76f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            TopIcon.Bell -> {
                val path = Path().apply {
                    moveTo(w * 0.32f, h * 0.58f)
                    cubicTo(w * 0.34f, h * 0.36f, w * 0.38f, h * 0.25f, w * 0.5f, h * 0.25f)
                    cubicTo(w * 0.62f, h * 0.25f, w * 0.66f, h * 0.36f, w * 0.68f, h * 0.58f)
                    lineTo(w * 0.76f, h * 0.7f)
                    lineTo(w * 0.24f, h * 0.7f)
                    close()
                }
                drawPath(path, Color.White, style = stroke)
                drawLine(Color.White, Offset(w * 0.44f, h * 0.8f), Offset(w * 0.56f, h * 0.8f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            TopIcon.Message -> {
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(w * 0.22f, h * 0.28f),
                    size = Size(w * 0.56f, h * 0.37f),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                    style = stroke
                )
                val path = Path().apply {
                    moveTo(w * 0.4f, h * 0.65f)
                    lineTo(w * 0.32f, h * 0.78f)
                    lineTo(w * 0.52f, h * 0.65f)
                }
                drawPath(path, Color.White, style = stroke)
                drawLine(Color.White, Offset(w * 0.34f, h * 0.42f), Offset(w * 0.66f, h * 0.42f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(Color.White, Offset(w * 0.34f, h * 0.52f), Offset(w * 0.58f, h * 0.52f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
        }
    }
}
