package com.dogon.vpn.stats

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TrafficDao {
    @Insert
    suspend fun insert(entry: TrafficEntry)

    @Query("""
        SELECT dayKey AS label, SUM(rxBytesDelta) AS rxBytes, SUM(txBytesDelta) AS txBytes
        FROM traffic_entries
        GROUP BY dayKey
        ORDER BY dayKey DESC
        LIMIT :days
    """)
    suspend fun dailyUsage(days: Int): List<UsageBucketRow>

    @Query("""
        SELECT monthKey AS label, SUM(rxBytesDelta) AS rxBytes, SUM(txBytesDelta) AS txBytes
        FROM traffic_entries
        GROUP BY monthKey
        ORDER BY monthKey DESC
        LIMIT :months
    """)
    suspend fun monthlyUsage(months: Int): List<UsageBucketRow>
}

data class UsageBucketRow(
    val label: String,
    val rxBytes: Long,
    val txBytes: Long
)
