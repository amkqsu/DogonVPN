package com.dogon.vpn.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.dogon.vpn.stats.UsageBucket
import com.dogon.vpn.ui.theme.AccentLive
import com.dogon.vpn.ui.theme.ArrowUp

/** Simple animated bar chart: total (rx+tx) bytes per bucket (day or month). */
@Composable
fun UsageBarChart(
    buckets: List<UsageBucket>,
    modifier: Modifier = Modifier
) {
    val ordered = buckets.reversed() // oldest -> newest, left to right
    val maxValue = (ordered.maxOfOrNull { it.totalBytes } ?: 1L).coerceAtLeast(1L)

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(horizontal = 4.dp)
        ) {
            if (ordered.isEmpty()) return@Canvas
            val barCount = ordered.size
            val gap = 6.dp.toPx()
            val barWidth = ((size.width - gap * (barCount - 1)) / barCount).coerceAtLeast(2f)

            ordered.forEachIndexed { index, bucket ->
                val ratio = bucket.totalBytes.toFloat() / maxValue.toFloat()
                val barHeight = size.height * ratio.coerceIn(0.02f, 1f)
                val left = index * (barWidth + gap)
                val top = size.height - barHeight

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(AccentLive, ArrowUp)
                    ),
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(6f, 6f)
                )
            }
        }
    }
}
