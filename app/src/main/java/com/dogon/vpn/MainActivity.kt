package com.dogon.vpn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dogon.vpn.data.ConfigStore
import com.dogon.vpn.ui.screens.HomeScreen
import com.dogon.vpn.ui.screens.SettingsScreen
import com.dogon.vpn.ui.screens.SetupScreen
import com.dogon.vpn.ui.screens.StatsScreen
import com.dogon.vpn.ui.theme.AccentLive
import com.dogon.vpn.ui.theme.BgBase
import com.dogon.vpn.ui.theme.BgCard
import com.dogon.vpn.vpn.TunnelManager

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            com.dogon.vpn.ui.theme.DogonVPNTheme {
                DogonApp()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Detect "app was killed from recents but VPN is still up" and reflect it in the UI.
        TunnelManager.refreshState(this)
    }
}

@Composable
private fun DogonApp() {
    val context = LocalContext.current
    val navController = rememberNavController()
    var hasConfig by remember { mutableStateOf(ConfigStore.hasConfig(context)) }

    if (!hasConfig) {
        SetupScreen(onConfigSaved = { hasConfig = true })
        return
    }

    Scaffold(
        containerColor = BgBase,
        bottomBar = { BottomBar(navController) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {
            composable("home") { HomeScreen() }
            composable("stats") { StatsScreen() }
            composable("settings") {
                SettingsScreen(
                    onConfigCleared = { hasConfig = false },
                    onOpenSplitTunneling = { navController.navigate("split_tunneling") },
                    onOpenWifiExceptions = { navController.navigate("wifi_exceptions") }
                )
            }
            composable("split_tunneling") {
                com.dogon.vpn.ui.screens.AppPickerScreen(onBack = { navController.popBackStack() })
            }
            composable("wifi_exceptions") {
                com.dogon.vpn.ui.screens.WifiExceptionScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
private fun BottomBar(navController: androidx.navigation.NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val current = backStackEntry?.destination?.route ?: "home"

    NavigationBar(containerColor = BgCard) {
        NavigationBarItem(
            selected = current == "home",
            onClick = { navController.navigate("home") { launchSingleTop = true } },
            icon = { Icon(painterResource(R.drawable.ic_home), contentDescription = "Ana Sayfa", tint = if (current == "home") AccentLive else Color.White) },
            label = { Text("Ana Sayfa") }
        )
        NavigationBarItem(
            selected = current == "stats",
            onClick = { navController.navigate("stats") { launchSingleTop = true } },
            icon = { Icon(painterResource(R.drawable.ic_stats), contentDescription = "İstatistik", tint = if (current == "stats") AccentLive else Color.White) },
            label = { Text("İstatistik") }
        )
        NavigationBarItem(
            selected = current == "settings",
            onClick = { navController.navigate("settings") { launchSingleTop = true } },
            icon = { Icon(painterResource(R.drawable.ic_settings), contentDescription = "Ayarlar", tint = if (current == "settings") AccentLive else Color.White) },
            label = { Text("Ayarlar") }
        )
    }
}
