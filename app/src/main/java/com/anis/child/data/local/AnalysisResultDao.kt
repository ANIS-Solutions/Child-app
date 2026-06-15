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

    @Query("SELECT * FROM analysis_results WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getResultsForSessionOnce(sessionId: Long): List<AnalysisResultEntity>

    @Query("SELECT * FROM analysis_results WHERE sessionId = :sessionId ORDER BY timestamp DESC")
    fun getResultsForSessionDesc(sessionId: Long): Flow<List<AnalysisResultEntity>>

    @Query("SELECT COUNT(*) FROM analysis_results WHERE sessionId = :sessionId")
    suspend fun getResultCountForSession(sessionId: Long): Int

    @Query("SELECT COUNT(*) FROM analysis_results WHERE sessionId = :sessionId AND decision = 'BLOCKED'")
    suspend fun getBlockedCountForSession(sessionId: Long): Int

    @Query("SELECT COUNT(*) FROM analysis_results WHERE sessionId = :sessionId AND decision = 'SAFE'")
    suspend fun getSafeCountForSession(sessionId: Long): Int

    @Query("SELECT * FROM analysis_results WHERE id IN (:ids)")
    suspend fun getResultsByIds(ids: List<Long>): List<AnalysisResultEntity>

    @Query("SELECT * FROM analysis_results WHERE sessionId IN (:sessionIds) AND embedding IS NOT NULL ORDER BY timestamp ASC LIMIT :limit")
    suspend fun getResultsWithEmbeddingsForSessions(sessionIds: List<Long>, limit: Int): List<AnalysisResultEntity>

    @Query("SELECT * FROM analysis_results WHERE sessionId IN (:sessionIds) AND embedding IS NOT NULL ORDER BY timestamp ASC")
    suspend fun getAllResultsWithEmbeddingsForSessions(sessionIds: List<Long>): List<AnalysisResultEntity>

    @Query("UPDATE analysis_results SET imagePath = :newPath WHERE id = :resultId")
    suspend fun updateImagePath(resultId: Long, newPath: String)

    @Query("UPDATE analysis_results SET imagePath = :newPath WHERE id IN (:resultIds)")
    suspend fun updateImagePaths(resultIds: List<Long>, newPath: String)

    @Query("DELETE FROM analysis_results WHERE sessionId = :sessionId")
    suspend fun deleteResultsForSession(sessionId: Long)
}
