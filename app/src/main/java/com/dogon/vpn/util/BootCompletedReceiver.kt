package com.dogon.vpn.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dogon.vpn.data.ConfigStore
import com.dogon.vpn.data.SettingsStore.autoConnectEnabled
import com.dogon.vpn.vpn.DogonVpnForegroundService

/** If auto-connect is enabled and a config exists, resume the tunnel after reboot. */
class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        if (!context.autoConnectEnabled) return
        if (!ConfigStore.hasConfig(context)) return
        DogonVpnForegroundService.start(context, connect = true)
    }
}
