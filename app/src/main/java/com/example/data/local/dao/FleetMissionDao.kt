package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.FleetMission
import kotlinx.coroutines.flow.Flow

@Dao
interface FleetMissionDao {
    @Query("SELECT * FROM fleet_missions ORDER BY departureTime DESC")
    fun getAllMissions(): Flow<List<FleetMission>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMission(mission: FleetMission): Long

    @Update
    suspend fun updateMission(mission: FleetMission)

    @Query("UPDATE fleet_missions SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("DELETE FROM fleet_missions WHERE id = :id")
    suspend fun deleteMission(id: Long)
}
