package com.dogon.vpn.stats

import androidx.room.Entity
import androidx.room.PrimaryKey

/** One sampled delta of traffic, recorded roughly every minute while connected. */
@Entity(tableName = "traffic_entries")
data class TrafficEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampMs: Long,
    val dayKey: String,     // "yyyy-MM-dd" — used for daily aggregation
    val monthKey: String,   // "yyyy-MM" — used for monthly aggregation
    val rxBytesDelta: Long,
    val txBytesDelta: Long
)

data class UsageBucket(
    val label: String,
    val rxBytes: Long,
    val txBytes: Long
) {
    val totalBytes get() = rxBytes + txBytes
}
