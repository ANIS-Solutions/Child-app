package com.anis.child.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "content_filter_rules")
data class ContentFilterRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pattern: String = "",
    val type: String = "keyword",
    val isBlocked: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
