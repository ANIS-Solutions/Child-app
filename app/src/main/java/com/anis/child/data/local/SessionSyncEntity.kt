package com.anis.child.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session_syncs")
data class SessionSyncEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val syncTimestamp: Long,
    val childId: String,
    val sessionCount: Int,
    val imageCount: Int,
    val embeddingCount: Int,
    val syncedSessionIds: String,
    val selectedImagePaths: String,
    val serializedEmbeddings: String,
    val imageHighlightsJson: String
)
