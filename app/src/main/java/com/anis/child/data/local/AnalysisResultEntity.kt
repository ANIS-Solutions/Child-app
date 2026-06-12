package com.anis.child.data.local

import androidx.room.ColumnInfo
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
    indices = [Index(value = ["sessionId"])]
)
data class AnalysisResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val timestamp: Long,
    @ColumnInfo(defaultValue = "Safe")
    val analysisResult: String = "Safe",
    @ColumnInfo(defaultValue = "SAFE")
    val decision: String = "SAFE",
    @ColumnInfo(defaultValue = "0")
    val ocrTimeMs: Double = 0.0,
    @ColumnInfo(defaultValue = "0")
    val onnxTimeMs: Double = 0.0,
    @ColumnInfo(defaultValue = "0")
    val preprocessTimeMs: Double = 0.0,
    val threatDetails: String? = null,
    val imagePath: String? = null,
    val embedding: String? = null
)
