package com.dogon.vpn.ui.screens

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.dogon.vpn.data.SettingsStore.excludedApps
import com.dogon.vpn.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private data class InstalledApp(
    val packageName: String,
    val label: String,
    val icon: Drawable?
)

@Composable
fun AppPickerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var apps by remember { mutableStateOf<List<InstalledApp>>(emptyList()) }
    var excluded by remember { mutableStateOf(context.excludedApps) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        apps = withContext(Dispatchers.Default) { loadInstalledApps(context) }
        loading = false
    }

    fun toggle(pkg: String) {
        excluded = if (pkg in excluded) excluded - pkg else excluded + pkg
        context.excludedApps = excluded
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) { Text("‹ Geri", color = AccentLive) }
        }
        Column(Modifier.padding(horizontal = 20.dp)) {
            Text("Split Tunneling", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
            Spacer(Modifier.height(4.dp))
            Text("Seçilen uygulamalar VPN tünelinin dışında kalır", color = TextSecondary, fontSize = 13.sp)
        }
        Spacer(Modifier.height(16.dp))

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentLive)
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp)) {
                items(apps, key = { it.packageName }) { app ->
                    val isExcluded = app.packageName in excluded
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(BgCard)
                            .clickable { toggle(app.packageName) }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val bitmap = remember(app.packageName) {
                            app.icon?.toBitmap(width = 80, height = 80)?.asImageBitmap()
                        }
                        if (bitmap != null) {
                            Image(bitmap = bitmap, contentDescription = app.label, modifier = Modifier.size(32.dp))
                        } else {
                            Box(Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(BgCardAlt))
                        }
                        Spacer(Modifier.width(14.dp))
                        Text(app.label, color = TextPrimary, fontSize = 14.sp, modifier = Modifier.weight(1f))
                        Switch(
                            checked = isExcluded,
                            onCheckedChange = { toggle(app.packageName) },
                            colors = SwitchDefaults.colors(checkedThumbColor = AccentLive)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

private fun loadInstalledApps(context: android.content.Context): List<InstalledApp> {
    val pm = context.packageManager
    val launcherIntent = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
        addCategory(android.content.Intent.CATEGORY_LAUNCHER)
    }
    val resolveInfos = pm.queryIntentActivities(launcherIntent, 0)
    return resolveInfos
        .mapNotNull { it.activityInfo?.applicationInfo }
        .distinctBy { it.packageName }
        .filterNot { it.packageName == context.packageName } // don't let DogonVPN exclude itself
        .map { ai: ApplicationInfo ->
            InstalledApp(
                packageName = ai.packageName,
                label = pm.getApplicationLabel(ai).toString(),
                icon = runCatching { pm.getApplicationIcon(ai) }.getOrNull()
            )
        }
        .sortedBy { it.label.lowercase() }
}
