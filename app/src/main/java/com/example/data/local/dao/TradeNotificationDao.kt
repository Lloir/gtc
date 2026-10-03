package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.TradeNotification
import kotlinx.coroutines.flow.Flow

@Dao
interface TradeNotificationDao {
    @Query("SELECT * FROM trade_notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<TradeNotification>>

    @Query("SELECT COUNT(*) FROM trade_notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: TradeNotification): Long

    @Query("UPDATE trade_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE trade_notifications SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM trade_notifications WHERE id = :id")
    suspend fun deleteNotification(id: Long)

    @Query("DELETE FROM trade_notifications")
    suspend fun clearAllNotifications()
}
