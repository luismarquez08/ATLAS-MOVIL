package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "interactions")
data class InteractionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val command: String,
    val response: String,
    val category: String, // "AI", "WEATHER", "NEWS", "SPOTIFY", "YOUTUBE", "SYSTEM", "MOTIVATION"
    val isFavorite: Boolean = false
)
