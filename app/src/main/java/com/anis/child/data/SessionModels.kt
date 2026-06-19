package com.anis.child.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SessionHighlight(
    val resultId: Long,
    val sessionId: Long,
    val timestamp: Long,
    val embedding: List<Double>
)

@Serializable
data class SessionSyncMetadata(
    val childId: String,
    val totalSessions: Int,
    @SerialName("image-highlights") val imageHighlights: List<SessionHighlight>,
    @SerialName("list-of-embedding") val listOfEmbedding: List<List<Double>>
)
