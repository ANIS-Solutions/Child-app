package com.anis.child.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionSyncDao {
    @Insert
    suspend fun insert(sync: SessionSyncEntity)

    @Query("SELECT * FROM session_syncs ORDER BY syncTimestamp DESC")
    fun getAllSyncs(): Flow<List<SessionSyncEntity>>

    @Query("SELECT * FROM session_syncs ORDER BY syncTimestamp DESC LIMIT 1")
    suspend fun getLatestSync(): SessionSyncEntity?

    @Query("SELECT syncedSessionIds FROM session_syncs")
    suspend fun getAllSyncedSessionIdsList(): List<String>
}
