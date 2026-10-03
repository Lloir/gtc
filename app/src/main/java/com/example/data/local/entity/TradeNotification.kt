package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trade_notifications")
data class TradeNotification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val alertType: String, // "PRICE_SPIKE", "PRICE_DROP", "CONTRACT_OFFER", "FLEET_ARRIVAL", "MARKET_OPPORTUNITY"
    val isRead: Boolean = false,
    val relatedCommodityId: String? = null,
    val currentPrice: Double? = null
)
