package com.dogon.vpn.vpn

import android.content.Context
import com.wireguard.android.backend.Backend
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Statistics
import com.wireguard.android.backend.Tunnel
import com.dogon.vpn.data.ConfigStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Thin wrapper around the WireGuard GoBackend so the rest of the app never talks
 * to com.wireguard.* directly. Single-tunnel by design.
 */
object TunnelManager {

    private var backend: Backend? = null
    private lateinit var tunnel: DogonTunnel

    private val _state = MutableStateFlow(Tunnel.State.DOWN)
    val state: StateFlow<Tunnel.State> = _state

    fun init(context: Context) {
        if (backend != null) return
        backend = GoBackend(context.applicationContext)
        tunnel = DogonTunnel { newState -> _state.value = newState }
        // Pick up real state in case the tunnel was already running (e.g. process was
        // killed and relaunched while VPN stayed connected system-side).
        runCatching { _state.value = backend!!.getState(tunnel) }
    }

    fun refreshState(context: Context) {
        init(context)
        runCatching { _state.value = backend!!.getState(tunnel) }
    }

    fun isConnected(): Boolean = _state.value == Tunnel.State.UP

    fun connect(context: Context): Result<Unit> {
        init(context)
        val config = ConfigStore.parsedConfig(context)
            ?: return Result.failure(IllegalStateException("Kayıtlı bağlantı bilgisi yok"))
        return runCatching {
            backend!!.setState(tunnel, Tunnel.State.UP, config)
        }
    }

    fun disconnect(context: Context): Result<Unit> {
        init(context)
        return runCatching {
            backend!!.setState(tunnel, Tunnel.State.DOWN, null)
        }
    }

    fun statistics(): Statistics? = runCatching { backend?.getStatistics(tunnel) }.getOrNull()
}
