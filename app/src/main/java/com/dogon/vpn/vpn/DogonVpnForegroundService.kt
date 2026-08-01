package com.dogon.vpn.vpn

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import androidx.core.app.ServiceCompat
import com.dogon.vpn.data.SettingsStore.killSwitchEnabled
import com.dogon.vpn.stats.TrafficRepository
import com.dogon.vpn.util.Format
import com.wireguard.android.backend.Tunnel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Foreground service that keeps a single, continuously-updated notification alive
 * while the tunnel is up, ticks the on-screen duration/speed, and periodically
 * writes traffic deltas to Room for the stats screen.
 */
class DogonVpnForegroundService : Service() {

    private val scope = CoroutineScope(SupervisorJob())
    private var tickJob: Job? = null
    private lateinit var trafficRepo: TrafficRepository

    private var connectedSinceMs = 0L
    private var lastTickRx = 0L
    private var lastTickTx = 0L
    private var lastTickAtMs = 0L
    private var userRequestedDown = false

    override fun onCreate() {
        super.onCreate()
        VpnNotifier.ensureChannel(this)
        TunnelManager.init(this)
        trafficRepo = TrafficRepository(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> doConnect()
            ACTION_DISCONNECT -> doDisconnect()
            ACTION_TOGGLE -> if (TunnelManager.isConnected()) doDisconnect() else doConnect()
            else -> if (intent?.getBooleanExtra(EXTRA_AUTO_CONNECT, false) == true) doConnect()
        }
        // Start in foreground immediately with whatever state we have so Android
        // doesn't kill us mid-toggle.
        startForegroundWithCurrentState()
        return START_STICKY
    }

    private fun doConnect() {
        userRequestedDown = false
        trafficRepo.resetBaseline()
        val result = TunnelManager.connect(this)
        if (result.isSuccess) {
            connectedSinceMs = System.currentTimeMillis()
            startTicking()
        }
    }

    private fun doDisconnect() {
        userRequestedDown = true
        TunnelManager.disconnect(this)
        tickJob?.cancel()
        startForegroundWithCurrentState()
    }

    private fun startTicking() {
        tickJob?.cancel()
        lastTickAtMs = System.currentTimeMillis()
        val stats = TunnelManager.statistics()
        lastTickRx = stats?.totalRx() ?: 0L
        lastTickTx = stats?.totalTx() ?: 0L

        tickJob = scope.launch {
            var secondsSinceSample = 0
            while (true) {
                delay(1000)

                if (TunnelManager.state.value != Tunnel.State.UP) {
                    handleUnexpectedDrop()
                    continue
                }

                val now = System.currentTimeMillis()
                val current = TunnelManager.statistics()
                val rx = current?.totalRx() ?: lastTickRx
                val tx = current?.totalTx() ?: lastTickTx
                val elapsedSec = ((now - lastTickAtMs).coerceAtLeast(1)) / 1000.0
                val downSpeed = ((rx - lastTickRx) / elapsedSec).toLong().coerceAtLeast(0)
                val upSpeed = ((tx - lastTickTx) / elapsedSec).toLong().coerceAtLeast(0)
                lastTickRx = rx; lastTickTx = tx; lastTickAtMs = now

                postNotification(
                    connected = true,
                    durationSec = (now - connectedSinceMs) / 1000,
                    downSpeed = downSpeed,
                    upSpeed = upSpeed
                )

                secondsSinceSample++
                if (secondsSinceSample >= 60) {
                    secondsSinceSample = 0
                    scope.launch { trafficRepo.recordSample(rx, tx) }
                }
            }
        }
    }

    /** Tunnel dropped without the user asking for it — this is where Kill Switch matters. */
    private suspend fun handleUnexpectedDrop() {
        if (userRequestedDown) {
            tickJob?.cancel()
            return
        }
        if (killSwitchEnabled) {
            // NOTE: a byte-for-byte "no packet leaks" kill switch needs the system-level
            // Always-on VPN + "Block connections without VPN" toggle (Settings ->
            // Network -> VPN -> gear icon), because that's enforced by the OS itself
            // before our process even gets to run. We surface a shortcut to that screen
            // from the Settings tab. Here we do the best an app-level kill switch can:
            // keep retrying the handshake fast, and keep the notification honest about
            // the fact that traffic may not be protected right now.
            postNotification(connected = false, durationSec = 0, downSpeed = 0, upSpeed = 0, reconnecting = true)
            delay(1500)
            TunnelManager.connect(this)
        } else {
            tickJob?.cancel()
            startForegroundWithCurrentState()
        }
    }

    private fun startForegroundWithCurrentState() {
        val connected = TunnelManager.isConnected()
        val durationSec = if (connected && connectedSinceMs > 0)
            (System.currentTimeMillis() - connectedSinceMs) / 1000 else 0
        postNotification(connected, durationSec, 0, 0, startForeground = true)
        if (!connected) stopSelf()
    }

    private fun postNotification(
        connected: Boolean,
        durationSec: Long,
        downSpeed: Long,
        upSpeed: Long,
        reconnecting: Boolean = false,
        startForeground: Boolean = false
    ) {
        val toggleIntent = Intent(this, DogonVpnForegroundService::class.java).apply {
            action = ACTION_TOGGLE
        }
        val togglePending = PendingIntent.getService(
            this, 0, toggleIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = VpnNotifier.build(
            context = this,
            connected = connected,
            durationText = if (reconnecting) "yeniden bağlanıyor…" else Format.duration(durationSec),
            downSpeedText = Format.speed(downSpeed),
            upSpeedText = Format.speed(upSpeed),
            toggleAction = togglePending
        )
        if (startForeground) {
            ServiceCompat.startForeground(
                this, VpnNotifier.NOTIFICATION_ID, notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            VpnNotifier.notify(this, notification)
        }
    }

    override fun onDestroy() {
        tickJob?.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    companion object {
        const val ACTION_CONNECT = "com.dogon.vpn.action.CONNECT"
        const val ACTION_DISCONNECT = "com.dogon.vpn.action.DISCONNECT"
        const val ACTION_TOGGLE = "com.dogon.vpn.action.TOGGLE"
        const val EXTRA_AUTO_CONNECT = "auto_connect"

        fun start(context: Context, connect: Boolean) {
            val intent = Intent(context, DogonVpnForegroundService::class.java)
            if (connect) intent.putExtra(EXTRA_AUTO_CONNECT, true)
            context.startForegroundService(intent)
        }

        fun toggle(context: Context) {
            val intent = Intent(context, DogonVpnForegroundService::class.java).apply {
                action = ACTION_TOGGLE
            }
            context.startForegroundService(intent)
        }
    }
}

/** Statistics exposes per-peer rx/tx (there's usually just one peer here) — sum across all. */
private fun com.wireguard.android.backend.Statistics.totalRx(): Long =
    peers().sumOf { peer(it)?.rxBytes ?: 0L }

private fun com.wireguard.android.backend.Statistics.totalTx(): Long =
    peers().sumOf { peer(it)?.txBytes ?: 0L }
