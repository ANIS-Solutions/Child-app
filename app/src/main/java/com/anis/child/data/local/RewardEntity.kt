package com.anis.child.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rewards")
data class RewardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val remoteId: String = "",
    val type: String = "screen_time",
    val title: String = "",
    val description: String = "",
    val pointCost: Int = 0,
    val state: String = "earned",
    val earnedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long? = null,
    val consumedAt: Long? = null
)
