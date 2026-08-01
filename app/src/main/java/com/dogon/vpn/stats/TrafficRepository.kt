package com.dogon.vpn.stats

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Records rx/tx deltas from the WireGuard backend and exposes daily/monthly
 * aggregates for the stats screen chart.
 */
class TrafficRepository(context: Context) {
    private val dao = TrafficDatabase.get(context).trafficDao()
    private val dayFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val monthFmt = SimpleDateFormat("yyyy-MM", Locale.US)

    private var lastRx = -1L
    private var lastTx = -1L

    /** Call periodically (e.g. every 60s) with the cumulative counters from the backend. */
    suspend fun recordSample(cumulativeRx: Long, cumulativeTx: Long) {
        if (lastRx < 0) {
            // First sample after (re)connect — establish baseline, no delta yet.
            lastRx = cumulativeRx
            lastTx = cumulativeTx
            return
        }
        val rxDelta = (cumulativeRx - lastRx).coerceAtLeast(0)
        val txDelta = (cumulativeTx - lastTx).coerceAtLeast(0)
        lastRx = cumulativeRx
        lastTx = cumulativeTx
        if (rxDelta == 0L && txDelta == 0L) return

        val now = Date()
        dao.insert(
            TrafficEntry(
                timestampMs = now.time,
                dayKey = dayFmt.format(now),
                monthKey = monthFmt.format(now),
                rxBytesDelta = rxDelta,
                txBytesDelta = txDelta
            )
        )
    }

    /** Reset baseline — call on fresh connect so a reused GoBackend counter doesn't skew deltas. */
    fun resetBaseline() {
        lastRx = -1L
        lastTx = -1L
    }

    suspend fun dailyUsage(days: Int = 30): List<UsageBucket> =
        dao.dailyUsage(days).map { UsageBucket(it.label, it.rxBytes, it.txBytes) }

    suspend fun monthlyUsage(months: Int = 12): List<UsageBucket> =
        dao.monthlyUsage(months).map { UsageBucket(it.label, it.rxBytes, it.txBytes) }
}
