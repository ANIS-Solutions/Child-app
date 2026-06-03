package com.anis.child.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val remoteId: String = "",
    val title: String,
    val description: String = "",
    val dueDate: Long? = null,
    val rewardValue: Int = 0,
    val status: String = "pending",
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
