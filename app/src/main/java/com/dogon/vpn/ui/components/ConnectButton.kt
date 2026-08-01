package com.dogon.vpn.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.dogon.vpn.R
import com.dogon.vpn.ui.theme.AccentLive
import com.dogon.vpn.ui.theme.AccentWarn
import com.dogon.vpn.ui.theme.BgCardAlt

enum class ConnectVisualState { IDLE, CONNECTING, CONNECTED }

/** The big center button — pulses while connecting, glows solid purple while connected. */
@Composable
fun ConnectButton(
    state: ConnectVisualState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val infinite = rememberInfiniteTransition(label = "pulse")
    val pulse by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val ringColor = when (state) {
        ConnectVisualState.CONNECTED -> AccentLive
        ConnectVisualState.CONNECTING -> AccentWarn
        ConnectVisualState.IDLE -> Color(0x33FFFFFF)
    }
    val scale = if (state == ConnectVisualState.CONNECTING) pulse else 1f

    Box(
        modifier = modifier
            .size(180.dp)
            .scale(scale)
            .background(BgCardAlt, CircleShape)
            .border(2.dp, ringColor, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_power),
            contentDescription = "Connect",
            tint = if (state == ConnectVisualState.CONNECTED) AccentLive else Color.White,
            modifier = Modifier.size(56.dp)
        )
    }
}
