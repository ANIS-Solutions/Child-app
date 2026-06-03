package com.anis.child.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppRestrictionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(restriction: AppRestrictionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(restrictions: List<AppRestrictionEntity>)

    @Update
    suspend fun update(restriction: AppRestrictionEntity)

    @Query("SELECT * FROM app_restrictions ORDER BY label ASC")
    fun getAllRestrictions(): Flow<List<AppRestrictionEntity>>

    @Query("SELECT * FROM app_restrictions WHERE packageName = :packageName")
    suspend fun getRestriction(packageName: String): AppRestrictionEntity?

    @Query("SELECT * FROM app_restrictions WHERE isBlocked = 1")
    suspend fun getBlockedApps(): List<AppRestrictionEntity>

    @Query("DELETE FROM app_restrictions WHERE packageName = :packageName")
    suspend fun delete(packageName: String)

    @Query("DELETE FROM app_restrictions")
    suspend fun clearAll()
}
