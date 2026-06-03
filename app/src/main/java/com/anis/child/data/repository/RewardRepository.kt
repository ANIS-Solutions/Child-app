package com.anis.child.data.repository

import com.anis.child.data.local.RewardDao
import com.anis.child.data.local.RewardEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RewardRepository @Inject constructor(
    private val rewardDao: RewardDao
) {
    fun getAllRewards(): Flow<List<RewardEntity>> = rewardDao.getAllRewards()

    suspend fun getBalance(): Int {
        val earned = rewardDao.getTotalPointsEarned() ?: 0
        val spent = rewardDao.getTotalPointsSpent()
        return earned - spent
    }

    suspend fun claimReward(id: Long) {
        rewardDao.updateState(id, "active")
    }

    suspend fun seedSampleDataIfEmpty() {
        val rewards = rewardDao.getAllRewards()
        var rewardList = emptyList<RewardEntity>()
        rewards.collect { rewardList = it; return@collect }

        if (rewardList.isEmpty()) {
            rewardDao.insert(RewardEntity(
                title = "30 min Extra Screen Time",
                description = "Redeem for 30 additional minutes of screen time",
                pointCost = 50,
                type = "screen_time",
                state = "earned"
            ))
            rewardDao.insert(RewardEntity(
                title = "Choose Weekend Movie",
                description = "Pick a movie for family movie night",
                pointCost = 100,
                type = "custom",
                state = "earned"
            ))
            rewardDao.insert(RewardEntity(
                title = "1 Hour Gaming Time",
                description = "Unlock 1 hour of gaming on weekends",
                pointCost = 75,
                type = "privilege",
                state = "earned"
            ))
        }
    }
}
