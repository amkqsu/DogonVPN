package com.dogon.vpn.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dogon.vpn.R
import com.dogon.vpn.data.ConfigStore
import com.dogon.vpn.data.SettingsStore.autoConnectEnabled
import com.dogon.vpn.data.SettingsStore.killSwitchEnabled
import com.dogon.vpn.ui.theme.*
import com.dogon.vpn.vpn.DogonVpnForegroundService

@Composable
fun SettingsScreen(
    onConfigCleared: () -> Unit,
    onOpenSplitTunneling: () -> Unit,
    onOpenWifiExceptions: () -> Unit
) {
    val context = LocalContext.current
    var killSwitch by remember { mutableStateOf(context.killSwitchEnabled) }
    var autoConnect by remember { mutableStateOf(context.autoConnectEnabled) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
            .padding(20.dp)
    ) {
        Text("Ayarlar", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
        Spacer(Modifier.height(20.dp))

        SettingSwitchRow(
            icon = R.drawable.ic_shield,
            title = "Kill Switch",
            subtitle = "Bağlantı koparsa trafiği durdur",
            checked = killSwitch
        ) {
            killSwitch = it
            context.killSwitchEnabled = it
        }

        SettingSwitchRow(
            icon = R.drawable.ic_power,
            title = "Otomatik Bağlan",
            subtitle = "Cihaz açıldığında otomatik bağlan",
            checked = autoConnect
        ) {
            autoConnect = it
            context.autoConnectEnabled = it
        }

        SettingNavRow(
            icon = R.drawable.ic_split,
            title = "Split Tunneling",
            subtitle = "VPN'den muaf tutulacak uygulamalar"
        ) { onOpenSplitTunneling() }

        SettingNavRow(
            icon = R.drawable.ic_wifi,
            title = "Wi-Fi İstisnaları",
            subtitle = "Otomatik bağlanmayacak ağlar"
        ) { onOpenWifiExceptions() }

        Spacer(Modifier.height(8.dp))

        // A true system-level kill switch needs Android's own Always-on VPN toggle.
        SettingNavRow(
            icon = R.drawable.ic_shield,
            title = "Sistem Düzeyinde Kill Switch",
            subtitle = "Ayarlar > VPN > Her Zaman Açık"
        ) {
            runCatching {
                context.startActivity(Intent(Settings.ACTION_VPN_SETTINGS))
            }
        }

        Spacer(Modifier.weight(1f))

        TextButton(onClick = {
            ConfigStore.clear(context)
            DogonVpnForegroundService.start(context, connect = false)
            onConfigCleared()
        }) {
            Text("Bağlantıyı Kaldır", color = AccentWarn)
        }
    }
}

@Composable
private fun SettingSwitchRow(
    icon: Int,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .padding(16.dp)
            .padding(vertical = 4.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Icon(painter = painterResource(icon), contentDescription = title, tint = Color.White, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = TextTertiary, fontSize = 12.sp)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = AccentLive))
    }
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun SettingNavRow(icon: Int, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .then(androidx.compose.ui.Modifier)
            .padding(16.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        androidx.compose.material3.Surface(
            onClick = onClick,
            color = Color.Transparent,
            modifier = Modifier.weight(1f)
        ) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Icon(painter = painterResource(icon), contentDescription = title, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    Text(subtitle, color = TextTertiary, fontSize = 12.sp)
                }
            }
        }
    }
    Spacer(Modifier.height(10.dp))
}
