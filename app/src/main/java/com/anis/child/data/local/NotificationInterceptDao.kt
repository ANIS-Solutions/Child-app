package com.anis.child.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationInterceptDao {
    @Query("SELECT * FROM notification_intercepts ORDER BY timestamp DESC")
    fun getAll(): Flow<List<NotificationInterceptEntity>>

    @Query("SELECT * FROM notification_intercepts WHERE isRead = 0 ORDER BY timestamp DESC")
    fun getUnread(): Flow<List<NotificationInterceptEntity>>

    @Query("SELECT COUNT(*) FROM notification_intercepts WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Insert
    suspend fun insert(notification: NotificationInterceptEntity): Long

    @Query("UPDATE notification_intercepts SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE notification_intercepts SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("UPDATE notification_intercepts SET isRemoved = 1 WHERE id = :id")
    suspend fun markAsRemoved(id: Long)

    @Query("DELETE FROM notification_intercepts WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM notification_intercepts")
    suspend fun clearAll()
}
