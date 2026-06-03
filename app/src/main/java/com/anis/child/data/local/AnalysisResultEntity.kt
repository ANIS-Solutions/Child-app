package com.anis.child.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "analysis_results",
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class AnalysisResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val timestamp: Long,
    val analysisResult: String = "Safe",
    val decision: String = "SAFE",
    val ocrTimeMs: Long = 0,
    val onnxTimeMs: Long = 0,
    val preprocessTimeMs: Long = 0,
    val threatDetails: String? = null,
    val imagePath: String? = null
)
