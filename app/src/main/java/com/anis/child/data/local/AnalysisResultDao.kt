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
    suspend fun insertResults(results: List<AnalysisResultEntity>)

    @Query("SELECT * FROM analysis_results WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getResultsForSession(sessionId: Long): Flow<List<AnalysisResultEntity>>

    @Query("SELECT * FROM analysis_results WHERE sessionId = :sessionId ORDER BY timestamp DESC")
    fun getResultsForSessionDesc(sessionId: Long): Flow<List<AnalysisResultEntity>>

    @Query("SELECT COUNT(*) FROM analysis_results WHERE sessionId = :sessionId")
    suspend fun getResultCountForSession(sessionId: Long): Int

    @Query("SELECT COUNT(*) FROM analysis_results WHERE sessionId = :sessionId AND decision = 'BLOCKED'")
    suspend fun getBlockedCountForSession(sessionId: Long): Int

    @Query("SELECT COUNT(*) FROM analysis_results WHERE sessionId = :sessionId AND decision = 'SAFE'")
    suspend fun getSafeCountForSession(sessionId: Long): Int

    @Query("DELETE FROM analysis_results WHERE sessionId = :sessionId")
    suspend fun deleteResultsForSession(sessionId: Long)
}
