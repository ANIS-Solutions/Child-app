package com.anis.child.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ContentFilterRuleDao {
    @Query("SELECT * FROM content_filter_rules ORDER BY createdAt DESC")
    fun getAllRules(): Flow<List<ContentFilterRuleEntity>>

    @Query("SELECT * FROM content_filter_rules WHERE isBlocked = 1")
    suspend fun getActiveRules(): List<ContentFilterRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: ContentFilterRuleEntity): Long

    @Query("UPDATE content_filter_rules SET isBlocked = :isBlocked WHERE id = :id")
    suspend fun setRuleEnabled(id: Long, isBlocked: Boolean)

    @Delete
    suspend fun delete(rule: ContentFilterRuleEntity)

    @Query("DELETE FROM content_filter_rules")
    suspend fun clearAll()
}
