package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.TycoonNote
import kotlinx.coroutines.flow.Flow

@Dao
interface TycoonNoteDao {
    @Query("SELECT * FROM tycoon_notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<TycoonNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: TycoonNote): Long

    @Update
    suspend fun updateNote(note: TycoonNote)

    @Delete
    suspend fun deleteNote(note: TycoonNote)

    @Query("DELETE FROM tycoon_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)
}
