package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.CachedMaterialPriceDao
import com.example.data.local.dao.FleetMissionDao
import com.example.data.local.dao.TradeAlertDao
import com.example.data.local.dao.TradeNotificationDao
import com.example.data.local.dao.TycoonNoteDao
import com.example.data.local.entity.CachedMaterialPrice
import com.example.data.local.entity.FleetMission
import com.example.data.local.entity.TradeAlert
import com.example.data.local.entity.TradeNotification
import com.example.data.local.entity.TycoonNote

@Database(
    entities = [
        TradeAlert::class,
        TradeNotification::class,
        FleetMission::class,
        TycoonNote::class,
        CachedMaterialPrice::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun tradeAlertDao(): TradeAlertDao
    abstract fun tradeNotificationDao(): TradeNotificationDao
    abstract fun fleetMissionDao(): FleetMissionDao
    abstract fun tycoonNoteDao(): TycoonNoteDao
    abstract fun cachedMaterialPriceDao(): CachedMaterialPriceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "galactic_tycoons.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
