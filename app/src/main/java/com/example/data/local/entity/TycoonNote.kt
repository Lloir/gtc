package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tycoon_notes")
data class TycoonNote(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val category: String = "GENERAL", // "BASE_COORDS", "ALLIANCE", "TRADE_DEAL", "MINING"
    val timestamp: Long = System.currentTimeMillis()
)
