package com.dogon.vpn.data

import android.content.Context
import com.wireguard.config.Config
import com.dogon.vpn.data.SettingsStore.excludedApps
import java.io.ByteArrayInputStream

/**
 * Stores the single WireGuard tunnel configuration (raw .conf text) the user provided
 * via QR scan / gallery QR / pasted text. DogonVPN intentionally supports one active
 * config at a time — no naming / multi-profile UI, per product spec.
 */
object ConfigStore {
    private const val KEY_RAW_CONFIG = "wg_raw_config"

    fun save(context: Context, rawConfigText: String) {
        SecurePrefs.get(context).edit().putString(KEY_RAW_CONFIG, rawConfigText).apply()
    }

    fun rawConfig(context: Context): String? =
        SecurePrefs.get(context).getString(KEY_RAW_CONFIG, null)

    fun hasConfig(context: Context): Boolean = rawConfig(context) != null

    fun clear(context: Context) {
        SecurePrefs.get(context).edit().remove(KEY_RAW_CONFIG).apply()
    }

    /**
     * Parses the stored raw text into a WireGuard [Config], merging in the current
     * split-tunneling app exclusion list (Settings > Split Tunneling) as an
     * `ExcludedApplications` line in the [Interface] section — this is the extension
     * key the WireGuard Android backend understands for per-app tunnel bypass.
     */
    fun parsedConfig(context: Context): Config? {
        val raw = rawConfig(context) ?: return null
        val merged = withExcludedApplications(raw, context.excludedApps)
        return runCatching {
            Config.parse(ByteArrayInputStream(merged.toByteArray()))
        }.getOrNull()
    }

    private fun withExcludedApplications(rawConfig: String, excludedApps: Set<String>): String {
        val withoutOldLine = rawConfig.lineSequence()
            .filterNot { it.trim().startsWith("ExcludedApplications", ignoreCase = true) }
            .toMutableList()

        if (excludedApps.isEmpty()) return withoutOldLine.joinToString("\n")

        val interfaceIndex = withoutOldLine.indexOfFirst { it.trim().equals("[Interface]", ignoreCase = true) }
        if (interfaceIndex == -1) return withoutOldLine.joinToString("\n")

        withoutOldLine.add(interfaceIndex + 1, "ExcludedApplications = ${excludedApps.joinToString(",")}")
        return withoutOldLine.joinToString("\n")
    }
}

