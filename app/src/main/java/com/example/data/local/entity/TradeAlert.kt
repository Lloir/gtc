package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trade_alerts")
data class TradeAlert(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val commodityId: String,
    val commodityName: String,
    val condition: String, // "ABOVE" or "BELOW"
    val targetPrice: Double,
    val unit: String = "Cr/ton",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val lastTriggeredAt: Long? = null,
    val notes: String = ""
)
