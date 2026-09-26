package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface InteractionDao {

    @Query("SELECT * FROM interactions ORDER BY timestamp DESC")
    fun getAllInteractions(): Flow<List<InteractionEntity>>

    @Query("SELECT * FROM interactions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentInteractions(limit: Int = 20): Flow<List<InteractionEntity>>

    @Query("SELECT * FROM interactions WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavorites(): Flow<List<InteractionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInteraction(interaction: InteractionEntity): Long

    @Query("UPDATE interactions SET isFavorite = :isFav WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFav: Boolean)

    @Query("DELETE FROM interactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM interactions")
    suspend fun clearAll()
}
