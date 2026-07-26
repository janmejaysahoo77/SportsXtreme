package com.example.sportsxtreme.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val LocalSkeletonBrush = staticCompositionLocalOf {
    Brush.linearGradient(listOf(Color(0xFF25323A), Color(0xFF40515B), Color(0xFF25323A)))
}

@Composable
internal fun ProfileSkeleton() {
    CompositionLocalProvider(LocalSkeletonBrush provides rememberSkeletonShimmerBrush()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SkeletonHeroProfileCard()
            Spacer(Modifier.height(14.dp))
            SkeletonHostedSummary()
            Spacer(Modifier.height(18.dp))
            SkeletonSectionHeader(width = 168.dp)
            SkeletonStatsRow()
            SkeletonDetailSection(rowCount = 3)
            SkeletonDetailSection(rowCount = 3)
            Spacer(Modifier.height(18.dp))
            SkeletonSettingsCard()
            Spacer(Modifier.height(31.dp))
        }
    }
}

@Composable
private fun rememberSkeletonShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "profileSkeletonShimmer")
    val offset by transition.animateFloat(
        initialValue = -420f,
        targetValue = 1250f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1250),
            repeatMode = RepeatMode.Restart
        ),
        label = "profileSkeletonShimmerOffset"
    )

    return Brush.linearGradient(
        colors = listOf(
            Color(0xFF25323A).copy(alpha = 0.76f),
            Color(0xFF4B5F69).copy(alpha = 0.96f),
            Color(0xFF25323A).copy(alpha = 0.76f)
        ),
        start = Offset(offset, 0f),
        end = Offset(offset + 360f, 360f)
    )
}

@Composable
private fun SkeletonHeroProfileCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(24.dp, RoundedCornerShape(24.dp), clip = false)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF28312B), Color(0xFF061016), Color(0xFF101C22))))
            .border(1.dp, Color(0xCCFFD66B), RoundedCornerShape(24.dp))
            .padding(horizontal = 22.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SkeletonBlock(width = 178.dp, height = 12.dp, radius = 8.dp)
        Spacer(Modifier.height(15.dp))
        SkeletonBlock(size = 124.dp, shape = CircleShape)
        Spacer(Modifier.height(16.dp))
        SkeletonBlock(width = 220.dp, height = 30.dp, radius = 12.dp)
        Spacer(Modifier.height(8.dp))
        SkeletonBlock(width = 190.dp, height = 14.dp, radius = 8.dp)
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SkeletonBlock(width = 106.dp, height = 28.dp, radius = 50.dp)
            SkeletonBlock(width = 76.dp, height = 28.dp, radius = 50.dp)
        }
        Spacer(Modifier.height(17.dp))
        SkeletonBlock(width = 174.dp, height = 34.dp, radius = 50.dp)
        Spacer(Modifier.height(21.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) {
                SkeletonMetricTile(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SkeletonHostedSummary() {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF101A1F), Color(0xFF071014))))
            .border(1.dp, Color(0x5545E9FF), RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(5.dp).height(22.dp).clip(RoundedCornerShape(8.dp)).background(Brush.verticalGradient(listOf(Gold, Aqua))))
            Spacer(Modifier.width(10.dp))
            SkeletonBlock(width = 72.dp, height = 21.dp, radius = 8.dp)
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) {
                SkeletonHostedTile(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SkeletonStatsRow() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(3) {
            SkeletonStatTile(Modifier.weight(1f))
        }
    }
}

@Composable
private fun SkeletonDetailSection(rowCount: Int) {
    SkeletonSectionHeader(width = 158.dp)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        repeat(rowCount) {
            SkeletonDetailCard()
        }
    }
}

@Composable
private fun SkeletonSectionHeader(width: Dp) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(5.dp).height(24.dp).clip(RoundedCornerShape(8.dp)).background(Brush.verticalGradient(listOf(Gold, Aqua))))
        Spacer(Modifier.width(10.dp))
        SkeletonBlock(width = width, height = 20.dp, radius = 8.dp)
    }
}

@Composable
private fun SkeletonMetricTile(modifier: Modifier = Modifier) {
    Column(
        modifier
            .shadow(10.dp, RoundedCornerShape(14.dp), clip = false)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF16242B), DeepPanel)))
            .border(1.dp, Lime.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SkeletonBlock(width = 38.dp, height = 25.dp, radius = 8.dp)
        Spacer(Modifier.height(6.dp))
        SkeletonBlock(width = 46.dp, height = 10.dp, radius = 6.dp)
    }
}

@Composable
private fun SkeletonHostedTile(modifier: Modifier = Modifier) {
    Column(
        modifier
            .shadow(10.dp, RoundedCornerShape(14.dp), clip = false)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF17262C), DeepPanel)))
            .border(1.dp, Aqua.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .padding(vertical = 15.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SkeletonBlock(width = 34.dp, height = 26.dp, radius = 8.dp)
        Spacer(Modifier.height(6.dp))
        SkeletonBlock(width = 62.dp, height = 10.dp, radius = 6.dp)
    }
}

@Composable
private fun SkeletonStatTile(modifier: Modifier = Modifier) {
    Column(
        modifier
            .shadow(12.dp, RoundedCornerShape(15.dp), clip = false)
            .clip(RoundedCornerShape(15.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF142229), Card)))
            .border(1.dp, Line.copy(alpha = 0.9f), RoundedCornerShape(15.dp))
            .padding(vertical = 15.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SkeletonBlock(width = 48.dp, height = 24.dp, radius = 8.dp)
        Spacer(Modifier.height(8.dp))
        SkeletonBlock(width = 64.dp, height = 11.dp, radius = 6.dp)
    }
}

@Composable
private fun SkeletonDetailCard() {
    Row(
        Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(15.dp), clip = false)
            .clip(RoundedCornerShape(15.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF121E24), Card)))
            .border(1.dp, Line.copy(alpha = 0.9f), RoundedCornerShape(15.dp))
            .padding(17.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(5.dp).height(46.dp).clip(RoundedCornerShape(6.dp)).background(Brush.verticalGradient(listOf(Gold, Aqua))))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            SkeletonBlock(width = 112.dp, height = 13.dp, radius = 6.dp)
            Spacer(Modifier.height(8.dp))
            SkeletonBlock(widthFraction = 0.72f, height = 17.dp, radius = 8.dp)
        }
    }
}

@Composable
private fun SkeletonSettingsCard() {
    Column(
        Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(14.dp), clip = false)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF10191D), Color(0xFF080F13))))
            .border(1.dp, Color(0xFF344249), RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        repeat(3) {
            SkeletonBlock(widthFraction = if (it == 0) 0.62f else 0.48f, height = 16.dp, radius = 8.dp)
            if (it < 2) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF202C34)))
            }
        }
    }
}

@Composable
private fun SkeletonBlock(
    modifier: Modifier = Modifier,
    width: Dp? = null,
    widthFraction: Float? = null,
    height: Dp? = null,
    size: Dp? = null,
    radius: Dp = 10.dp,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(radius)
) {
    val sizedModifier = when {
        size != null -> modifier.size(size)
        widthFraction != null && height != null -> modifier.fillMaxWidth(widthFraction).height(height)
        width != null && height != null -> modifier.width(width).height(height)
        else -> modifier
    }

    Box(
        sizedModifier
            .clip(shape)
            .background(LocalSkeletonBrush.current)
    )
}
