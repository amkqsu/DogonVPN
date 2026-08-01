package com.dogon.vpn.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.dogon.vpn.R
import com.dogon.vpn.data.SettingsStore.excludedWifiSsids
import com.dogon.vpn.ui.theme.*

@Composable
fun WifiExceptionScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var ssids by remember { mutableStateOf(context.excludedWifiSsids) }
    var newSsid by remember { mutableStateOf("") }
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasLocationPermission = granted }

    fun add(ssid: String) {
        val trimmed = ssid.trim()
        if (trimmed.isEmpty()) return
        ssids = ssids + trimmed
        context.excludedWifiSsids = ssids
        newSsid = ""
    }

    fun remove(ssid: String) {
        ssids = ssids - ssid
        context.excludedWifiSsids = ssids
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
            .padding(20.dp)
    ) {
        TextButton(onClick = onBack) { Text("‹ Geri", color = AccentLive) }
        Spacer(Modifier.height(4.dp))
        Text("Wi-Fi İstisnaları", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
        Spacer(Modifier.height(4.dp))
        Text(
            "Bu ağlardayken \"Otomatik Bağlan\" devreye girmez",
            color = TextSecondary, fontSize = 13.sp
        )
        Spacer(Modifier.height(20.dp))

        // Quick-add current network
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(BgCard)
                .clickable {
                    if (!hasLocationPermission) {
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    } else {
                        currentSsid(context)?.let(::add)
                    }
                }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painter = painterResource(R.drawable.ic_wifi), contentDescription = null, tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text(
                if (hasLocationPermission) "Şu anki ağı ekle" else "Ağ adını okumak için konum izni ver",
                color = TextPrimary, fontSize = 14.sp
            )
        }

        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newSsid,
                onValueChange = { newSsid = it },
                modifier = Modifier.weight(1f),
                label = { Text("Ağ adı (SSID)") },
                singleLine = true
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = { add(newSsid) }) { Text("Ekle") }
        }

        Spacer(Modifier.height(20.dp))

        LazyColumn {
            items(ssids.toList(), key = { it }) { ssid ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BgCard)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(ssid, color = TextPrimary, fontSize = 14.sp)
                    TextButton(onClick = { remove(ssid) }) { Text("Kaldır", color = AccentWarn) }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

private fun currentSsid(context: android.content.Context): String? {
    return runCatching {
        val wifiManager = context.applicationContext
            .getSystemService(android.content.Context.WIFI_SERVICE) as WifiManager
        val raw = wifiManager.connectionInfo?.ssid ?: return null
        raw.trim('"').takeIf { it.isNotBlank() && it != "<unknown ssid>" }
    }.getOrNull()
}
