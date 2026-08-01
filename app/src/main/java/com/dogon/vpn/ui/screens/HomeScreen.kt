package com.dogon.vpn.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dogon.vpn.ui.components.ConnectButton
import com.dogon.vpn.ui.components.ConnectVisualState
import com.dogon.vpn.ui.theme.*
import com.dogon.vpn.util.Format
import com.dogon.vpn.vpn.DogonVpnForegroundService
import com.dogon.vpn.vpn.TunnelManager
import com.wireguard.android.backend.Tunnel
import kotlinx.coroutines.delay

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val state by TunnelManager.state.collectAsState()
    var connectedSince by remember { mutableStateOf(0L) }
    var elapsedSec by remember { mutableStateOf(0L) }
    var downSpeed by remember { mutableStateOf(0L) }
    var upSpeed by remember { mutableStateOf(0L) }

    // Re-sync with the real tunnel state whenever this screen appears — this is the
    // "I force-closed the app but VPN is still up" case the spec called out.
    LaunchedEffect(Unit) {
        TunnelManager.refreshState(context)
        if (TunnelManager.isConnected() && connectedSince == 0L) {
            connectedSince = System.currentTimeMillis()
        }
    }

    LaunchedEffect(state) {
        if (state == Tunnel.State.UP) {
            if (connectedSince == 0L) connectedSince = System.currentTimeMillis()
            var lastRx = -1L
            var lastTx = -1L
            while (TunnelManager.state.value == Tunnel.State.UP) {
                delay(1000)
                elapsedSec = (System.currentTimeMillis() - connectedSince) / 1000
                val stats = TunnelManager.statistics()
                if (stats != null) {
                    val rx = runCatching { stats.peers().sumOf { p -> stats.peer(p)?.rxBytes ?: 0L } }.getOrDefault(0L)
                    val tx = runCatching { stats.peers().sumOf { p -> stats.peer(p)?.txBytes ?: 0L } }.getOrDefault(0L)
                    if (lastRx >= 0) {
                        downSpeed = (rx - lastRx).coerceAtLeast(0)
                        upSpeed = (tx - lastTx).coerceAtLeast(0)
                    }
                    lastRx = rx; lastTx = tx
                }
            }
        } else {
            connectedSince = 0L
            elapsedSec = 0L
            downSpeed = 0L
            upSpeed = 0L
        }
    }

    val visualState = when (state) {
        Tunnel.State.UP -> ConnectVisualState.CONNECTED
        Tunnel.State.TOGGLE -> ConnectVisualState.CONNECTING
        else -> ConnectVisualState.IDLE
    }

    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            DogonVpnForegroundService.toggle(context)
        }
    }

    fun requestToggle() {
        if (TunnelManager.isConnected()) {
            DogonVpnForegroundService.toggle(context)
            return
        }
        val consentIntent = VpnService.prepare(context)
        if (consentIntent != null) {
            vpnPermissionLauncher.launch(consentIntent)
        } else {
            DogonVpnForegroundService.toggle(context)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            when (visualState) {
                ConnectVisualState.CONNECTED -> "Bağlandı"
                ConnectVisualState.CONNECTING -> "Bağlanıyor…"
                ConnectVisualState.IDLE -> "Bağlı Değil"
            },
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
        if (visualState == ConnectVisualState.CONNECTED) {
            Spacer(Modifier.height(4.dp))
            Text(Format.duration(elapsedSec), color = TextSecondary, fontSize = 14.sp)
        }

        Spacer(Modifier.height(36.dp))

        ConnectButton(state = visualState) {
            requestToggle()
        }

        Spacer(Modifier.height(40.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SpeedCard("İndirme", Format.speed(downSpeed), ArrowDown, Modifier.weight(1f))
            SpeedCard("Yükleme", Format.speed(upSpeed), ArrowUp, Modifier.weight(1f))
        }
    }
}

@Composable
private fun SpeedCard(
    label: String,
    value: String,
    accent: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .padding(16.dp)
    ) {
        Text(label, color = TextTertiary, fontSize = 12.sp)
        Spacer(Modifier.height(4.dp))
        Text(value, color = accent, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}
