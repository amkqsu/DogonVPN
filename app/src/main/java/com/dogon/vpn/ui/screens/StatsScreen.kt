package com.dogon.vpn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dogon.vpn.stats.TrafficRepository
import com.dogon.vpn.stats.UsageBucket
import com.dogon.vpn.ui.components.UsageBarChart
import com.dogon.vpn.ui.theme.*
import com.dogon.vpn.util.Format

private enum class StatsRange { DAILY, MONTHLY }

@Composable
fun StatsScreen() {
    val context = LocalContext.current
    val repo = remember { TrafficRepository(context) }
    var range by remember { mutableStateOf(StatsRange.DAILY) }
    var buckets by remember { mutableStateOf<List<UsageBucket>>(emptyList()) }

    LaunchedEffect(range) {
        buckets = if (range == StatsRange.DAILY) repo.dailyUsage(14) else repo.monthlyUsage(12)
    }

    val totalBytes = buckets.sumOf { it.totalBytes }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
            .padding(20.dp)
    ) {
        Text("İstatistik", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium, color = TextPrimary)
        Spacer(Modifier.height(4.dp))
        Text(
            if (range == StatsRange.DAILY) "Son 14 gün" else "Son 12 ay",
            color = TextSecondary
        )
        Spacer(Modifier.height(18.dp))

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(BgCard)
                .padding(4.dp)
        ) {
            RangeChip("Günlük", range == StatsRange.DAILY, Modifier.weight(1f)) { range = StatsRange.DAILY }
            RangeChip("Aylık", range == StatsRange.MONTHLY, Modifier.weight(1f)) { range = StatsRange.MONTHLY }
        }

        Spacer(Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(BgCard)
                .padding(16.dp)
        ) {
            Text("Toplam Kullanım", color = TextTertiary, fontSize = 12.sp)
            Text(
                Format.bytes(totalBytes),
                color = AccentLive,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(16.dp))
            if (buckets.isEmpty()) {
                Text("Henüz veri yok — bağlandığında burada birikmeye başlar.", color = TextTertiary, fontSize = 13.sp)
            } else {
                UsageBarChart(buckets = buckets)
            }
        }

        Spacer(Modifier.height(20.dp))

        buckets.take(6).forEach { bucket ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(bucket.label, color = TextSecondary, fontSize = 13.sp)
                Text(Format.bytes(bucket.totalBytes), color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun RangeChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(if (selected) AccentLive else androidx.compose.ui.graphics.Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(label, color = androidx.compose.ui.graphics.Color.White, fontSize = 13.sp)
    }
}
