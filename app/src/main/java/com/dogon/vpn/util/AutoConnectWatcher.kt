package com.dogon.vpn.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import com.dogon.vpn.data.ConfigStore
import com.dogon.vpn.data.SettingsStore.autoConnectEnabled
import com.dogon.vpn.data.SettingsStore.excludedWifiSsids
import com.dogon.vpn.vpn.DogonVpnForegroundService
import com.dogon.vpn.vpn.TunnelManager

/**
 * Registers a ConnectivityManager network callback for the lifetime of the process.
 * When "Otomatik Bağlan" is on and a usable network appears, it starts the tunnel —
 * unless it's Wi-Fi and the SSID is in the user's exception list.
 *
 * Reading the current Wi-Fi SSID requires ACCESS_FINE_LOCATION at runtime (Android
 * platform restriction, not something we can avoid). If that permission hasn't been
 * granted yet, we can't tell SSIDs apart, so we conservatively skip auto-connect on
 * Wi-Fi and only act on non-Wi-Fi networks (e.g. mobile data) until the user grants it
 * from the Wi-Fi İstisnaları screen.
 */
object AutoConnectWatcher {
    private var registered = false

    fun start(context: Context) {
        if (registered) return
        val appContext = context.applicationContext
        val cm = appContext.getSystemService(ConnectivityManager::class.java) ?: return

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                maybeAutoConnect(appContext, cm, network)
            }
        }

        runCatching { cm.registerDefaultNetworkCallback(callback) }
        registered = true
    }

    private fun maybeAutoConnect(context: Context, cm: ConnectivityManager, network: Network) {
        if (!context.autoConnectEnabled) return
        if (!ConfigStore.hasConfig(context)) return
        if (TunnelManager.isConnected()) return

        val capabilities = cm.getNetworkCapabilities(network) ?: return
        val isWifi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)

        if (isWifi) {
            val ssid = currentSsidOrNull(context)
            if (ssid != null && ssid in context.excludedWifiSsids) return
        }

        DogonVpnForegroundService.start(context, connect = true)
    }

    private fun currentSsidOrNull(context: Context): String? = runCatching {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiManager.connectionInfo?.ssid?.trim('"')?.takeIf { it.isNotBlank() && it != "<unknown ssid>" }
    }.getOrNull()
}
