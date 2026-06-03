package com.anis.child.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_intercepts")
data class NotificationInterceptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String = "",
    val appLabel: String = "",
    val title: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val isRemoved: Boolean = false
)
