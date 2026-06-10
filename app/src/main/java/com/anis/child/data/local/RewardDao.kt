package com.anis.child.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RewardDao {
    @Insert
    suspend fun insert(reward: RewardEntity): Long

    @Update
    suspend fun update(reward: RewardEntity)

    @Query("SELECT * FROM rewards ORDER BY earnedAt DESC")
    fun getAllRewards(): Flow<List<RewardEntity>>

    @Query("SELECT SUM(pointCost) FROM rewards WHERE state = 'consumed'")
    suspend fun getTotalPointsSpent(): Int

    @Query("SELECT SUM(pointCost) FROM rewards WHERE state = 'earned'")
    suspend fun getTotalPointsEarned(): Int?

    @Query("SELECT * FROM rewards WHERE id = :id")
    suspend fun getById(id: Long): RewardEntity?

    @Query("UPDATE rewards SET state = :state WHERE id = :id")
    suspend fun updateState(id: Long, state: String)
}
