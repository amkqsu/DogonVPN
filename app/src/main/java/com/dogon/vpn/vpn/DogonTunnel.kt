package com.dogon.vpn.vpn

import com.wireguard.android.backend.Tunnel

/**
 * Single tunnel object for the app's one-and-only config. DogonVPN never shows this
 * name to the user (no "server name" concept in the UI) — it's just an internal handle
 * the WireGuard backend needs.
 */
class DogonTunnel(
    private val onStateChanged: (Tunnel.State) -> Unit
) : Tunnel {
    override fun getName(): String = "dogon0"

    override fun onStateChange(newState: Tunnel.State) {
        onStateChanged(newState)
    }
}
