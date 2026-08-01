package com.dogon.vpn.data

import android.content.Context
import androidx.core.content.edit

/** Plain (non-secret) app settings: kill switch, split tunneling, Wi-Fi auto-connect rules. */
object SettingsStore {
    private const val PREFS = "dogon_settings"
    private const val KEY_KILL_SWITCH = "kill_switch"
    private const val KEY_AUTO_CONNECT = "auto_connect"
    private const val KEY_EXCLUDED_APPS = "excluded_apps"      // split tunneling
    private const val KEY_EXCLUDED_WIFI = "excluded_wifi_ssids" // auto-connect exceptions

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    var Context.killSwitchEnabled: Boolean
        get() = prefs(this).getBoolean(KEY_KILL_SWITCH, false)
        set(value) = prefs(this).edit { putBoolean(KEY_KILL_SWITCH, value) }

    var Context.autoConnectEnabled: Boolean
        get() = prefs(this).getBoolean(KEY_AUTO_CONNECT, false)
        set(value) = prefs(this).edit { putBoolean(KEY_AUTO_CONNECT, value) }

    /** Package names excluded from the VPN tunnel (split tunneling). */
    var Context.excludedApps: Set<String>
        get() = prefs(this).getStringSet(KEY_EXCLUDED_APPS, emptySet()) ?: emptySet()
        set(value) = prefs(this).edit { putStringSet(KEY_EXCLUDED_APPS, value) }

    /** Wi-Fi SSIDs where auto-connect should NOT trigger. */
    var Context.excludedWifiSsids: Set<String>
        get() = prefs(this).getStringSet(KEY_EXCLUDED_WIFI, emptySet()) ?: emptySet()
        set(value) = prefs(this).edit { putStringSet(KEY_EXCLUDED_WIFI, value) }
}
