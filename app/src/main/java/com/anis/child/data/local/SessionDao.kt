package com.anis.child.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert
    suspend fun insert(session: SessionEntity): Long

    @Update
    suspend fun update(session: SessionEntity)

    @Query("SELECT * FROM sessions ORDER BY id DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): SessionEntity?

    @Query("SELECT * FROM sessions WHERE status = :status ORDER BY id DESC LIMIT 1")
    suspend fun getLatestSessionByStatus(status: String): SessionEntity?

    @Query("UPDATE sessions SET status = :status, endTime = :endTime WHERE id = :id")
    suspend fun endSession(id: Long, status: String, endTime: Long)
}
