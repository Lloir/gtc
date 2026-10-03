package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.TradeAlert
import kotlinx.coroutines.flow.Flow

@Dao
interface TradeAlertDao {
    @Query("SELECT * FROM trade_alerts ORDER BY createdAt DESC")
    fun getAllAlerts(): Flow<List<TradeAlert>>

    @Query("SELECT * FROM trade_alerts WHERE isActive = 1")
    suspend fun getActiveAlerts(): List<TradeAlert>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: TradeAlert): Long

    @Update
    suspend fun updateAlert(alert: TradeAlert)

    @Delete
    suspend fun deleteAlert(alert: TradeAlert)

    @Query("DELETE FROM trade_alerts WHERE id = :id")
    suspend fun deleteAlertById(id: Long)

    @Query("UPDATE trade_alerts SET isActive = :isActive WHERE id = :id")
    suspend fun toggleAlertStatus(id: Long, isActive: Boolean)

    @Query("UPDATE trade_alerts SET lastTriggeredAt = :timestamp WHERE id = :id")
    suspend fun updateLastTriggered(id: Long, timestamp: Long)
}
