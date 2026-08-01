package com.dogon.vpn.stats

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TrafficEntry::class], version = 1, exportSchema = false)
abstract class TrafficDatabase : RoomDatabase() {
    abstract fun trafficDao(): TrafficDao

    companion object {
        @Volatile private var instance: TrafficDatabase? = null

        fun get(context: Context): TrafficDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    TrafficDatabase::class.java,
                    "dogon_traffic.db"
                ).build().also { instance = it }
            }
    }
}
