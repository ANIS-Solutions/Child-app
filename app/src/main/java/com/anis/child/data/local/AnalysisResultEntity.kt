package com.anis.child.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "analysis_results")
data class AnalysisResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val timestamp: Long,
    @ColumnInfo(defaultValue = "Safe")
    val analysisResult: String = "Safe",
    @ColumnInfo(defaultValue = "SAFE")
    val decision: String = "SAFE",
    val threatDetails: String? = null,
    val imagePath: String? = null
)
