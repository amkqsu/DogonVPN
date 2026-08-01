package com.dogon.vpn.tile

import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.dogon.vpn.R
import com.dogon.vpn.data.ConfigStore
import com.dogon.vpn.vpn.DogonVpnForegroundService
import com.dogon.vpn.vpn.TunnelManager

/** Quick Settings ("flash panel") toggle — one tap to connect/disconnect. */
class DogonTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        TunnelManager.refreshState(this)
        refresh()
    }

    override fun onClick() {
        super.onClick()
        if (!ConfigStore.hasConfig(this)) return
        DogonVpnForegroundService.toggle(this)
        // Optimistic flip; the service will correct this shortly if the real state differs.
        qsTile?.state = if (TunnelManager.isConnected()) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE
        qsTile?.updateTile()
    }

    private fun refresh() {
        val tile = qsTile ?: return
        val connected = TunnelManager.isConnected()
        tile.state = if (connected) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = "DogonVPN"
        tile.icon = Icon.createWithResource(this, R.drawable.logo_mono)
        tile.updateTile()
    }
}
