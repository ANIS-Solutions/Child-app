package com.anis.child.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quizzes")
data class QuizEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val remoteId: String = "",
    val title: String,
    val subject: String = "",
    val difficulty: String = "medium",
    val rewardPoints: Int = 0,
    val timeLimitMinutes: Int = 10,
    val dueDate: Long? = null,
    val isCompleted: Boolean = false,
    val score: Int = 0,
    val totalQuestions: Int = 0,
    val answeredCorrectly: Int = 0,
    val assignedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)
