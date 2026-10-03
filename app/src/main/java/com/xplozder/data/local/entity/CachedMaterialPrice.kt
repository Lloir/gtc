package com.xplozder.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_material_prices")
data class CachedMaterialPrice(
    @PrimaryKey
    val matId: Int,
    val matName: String,
    val currentPriceCents: Long,
    val avgPriceCents: Long,
    val lastUpdated: Long = System.currentTimeMillis()
)
