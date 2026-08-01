package com.dogon.vpn.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartService
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.dogon.vpn.MainActivity
import com.dogon.vpn.R
import com.dogon.vpn.vpn.DogonVpnForegroundService
import com.dogon.vpn.vpn.TunnelManager

/** The single-Color ColorProvider overload isn't stable across Glance versions — always
 *  pass both day and night explicitly (same color for both, DogonVPN is dark-only). */
private fun solidColor(color: Color) = ColorProvider(day = color, night = color)

/** 1x1 home-screen widget: tap the icon to toggle, tap the label to open the app. */
class DogonWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: android.content.Context, id: GlanceId) {
        TunnelManager.refreshState(context)
        val connected = TunnelManager.isConnected()

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(solidColor(Color(0xFF141414)))
                    .cornerRadius(20.dp)
                    .padding(8.dp)
                    .clickable(actionStartService<DogonVpnForegroundService>()),
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Image(
                    provider = ImageProvider(R.drawable.logo_mono),
                    contentDescription = "Toggle",
                    modifier = GlanceModifier
                        .background(
                            solidColor(if (connected) Color(0xFFA78BFA) else Color(0xFF2A2A2A))
                        )
                        .cornerRadius(999.dp)
                        .padding(10.dp)
                )
                Text(
                    text = if (connected) "Bağlı" else "Kapalı",
                    style = TextStyle(
                        color = solidColor(Color.White),
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = GlanceModifier
                        .padding(top = 4.dp)
                        .clickable(actionStartActivity<MainActivity>())
                )
            }
        }
    }
}
