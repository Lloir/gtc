package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fleet_missions")
data class FleetMission(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val shipName: String,
    val shipType: String, // "Scout Hauler", "Bulk Freighter", "Heavy Transport", "Titan Carrier"
    val originPlanet: String,
    val destinationExchange: String,
    val cargoManifest: String,
    val cargoTons: Int,
    val estimatedValueCr: Double,
    val departureTime: Long,
    val travelDurationSec: Int,
    val status: String // "IN_TRANSIT", "ARRIVED", "UNLOADED"
)
