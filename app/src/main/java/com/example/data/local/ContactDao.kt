package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY name ASC")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts")
    suspend fun getAllContactsList(): List<ContactEntity>

    @Query("SELECT * FROM contacts WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun findByName(name: String): ContactEntity?

    @Query("SELECT * FROM contacts WHERE LOWER(name) LIKE '%' || LOWER(:query) || '%' LIMIT 1")
    suspend fun searchByName(query: String): ContactEntity?

    @Query("SELECT * FROM contacts WHERE LOWER(relationship) LIKE '%' || LOWER(:query) || '%' LIMIT 1")
    suspend fun searchByRelationship(query: String): ContactEntity?

    @Query("SELECT * FROM contacts WHERE LOWER(aliases) LIKE '%' || LOWER(:query) || '%' LIMIT 1")
    suspend fun searchByAlias(query: String): ContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity): Long

    @Query("DELETE FROM contacts WHERE id = :id")
    suspend fun deleteContact(id: Long)

    @Query("SELECT COUNT(*) FROM contacts")
    suspend fun count(): Int
}
