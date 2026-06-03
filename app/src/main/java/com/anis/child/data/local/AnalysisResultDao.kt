package com.anis.child.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalysisResultDao {
    @Insert
    suspend fun insert(result: AnalysisResultEntity): Long

    @Insert
    suspend fun insertBatch(results: List<AnalysisResultEntity>)

    @Query("SELECT * FROM analysis_results WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getResultsForSession(sessionId: Long): Flow<List<AnalysisResultEntity>>

    @Query("SELECT * FROM analysis_results WHERE sessionId = :sessionId AND decision = 'BLOCKED'")
    suspend fun getBlockedResultsForSession(sessionId: Long): List<AnalysisResultEntity>

    @Query("SELECT COUNT(*) FROM analysis_results WHERE sessionId = :sessionId")
    suspend fun getResultCountForSession(sessionId: Long): Int
}
