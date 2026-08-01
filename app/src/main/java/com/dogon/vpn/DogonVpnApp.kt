package com.dogon.vpn

import android.app.Application
import com.dogon.vpn.util.AutoConnectWatcher
import com.dogon.vpn.vpn.TunnelManager
import com.dogon.vpn.vpn.VpnNotifier

class DogonVpnApp : Application() {
    override fun onCreate() {
        super.onCreate()
        VpnNotifier.ensureChannel(this)
        TunnelManager.init(this)
        AutoConnectWatcher.start(this)
    }
}
