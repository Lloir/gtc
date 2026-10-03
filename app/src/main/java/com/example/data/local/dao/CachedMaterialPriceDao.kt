package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.CachedMaterialPrice
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedMaterialPriceDao {
    @Query("SELECT * FROM cached_material_prices ORDER BY matName ASC")
    fun getAllCachedPrices(): Flow<List<CachedMaterialPrice>>

    @Query("SELECT * FROM cached_material_prices ORDER BY matName ASC")
    suspend fun getCachedPricesSnapshot(): List<CachedMaterialPrice>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrices(prices: List<CachedMaterialPrice>)

    @Query("DELETE FROM cached_material_prices")
    suspend fun clearCache()
}
