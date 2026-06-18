package com.anis.child.data.repository

import com.anis.child.data.PreferenceManager
import com.anis.child.data.RewardUpdateRequest
import com.anis.child.data.local.RewardDao
import com.anis.child.data.local.RewardEntity
import com.anis.child.network.ApiService
import com.anis.child.network.ApiResult
import com.anis.child.network.safeApiCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RewardRepository @Inject constructor(
    private val rewardDao: RewardDao,
    private val apiService: ApiService,
    private val preferenceManager: PreferenceManager
) {
    fun getAllRewards(): Flow<List<RewardEntity>> = rewardDao.getAllRewards()

    suspend fun getBalance(): Int {
        val earned = rewardDao.getTotalPointsEarned() ?: 0
        val spent = rewardDao.getTotalPointsSpent()
        return earned - spent
    }

    suspend fun claimReward(id: Long): Boolean {
        val reward = rewardDao.getById(id) ?: return false

        if (reward.remoteId.isEmpty()) {
            rewardDao.updateState(id, "active")
            return true
        }

        return when (safeApiCall { apiService.redeemReward(reward.remoteId) }) {
            is ApiResult.Success -> {
                rewardDao.updateState(id, "active")
                true
            }
            is ApiResult.Error -> false
        }
    }

    suspend fun syncFromApi(): Boolean {
        val childId = preferenceManager.childId ?: return false
        return when (val result = safeApiCall { apiService.getChildRewards(childId) }) {
            is ApiResult.Success -> {
                val rewards = result.data.data ?: emptyList()
                for (reward in rewards) {
                    val existing = rewardDao.getByRemoteId(reward.id)
                    if (existing != null) {
                        if (existing.title != reward.title ||
                            existing.description != reward.description ||
                            existing.pointCost != reward.pointCost ||
                            existing.type != reward.type ||
                            existing.state != reward.state
                        ) {
                            rewardDao.update(existing.copy(
                                title = reward.title,
                                description = reward.description,
                                pointCost = reward.pointCost,
                                type = reward.type,
                                state = reward.state
                            ))
                        }
                    } else {
                        rewardDao.insert(RewardEntity(
                            remoteId = reward.id,
                            title = reward.title,
                            description = reward.description,
                            pointCost = reward.pointCost,
                            type = reward.type,
                            state = reward.state
                        ))
                    }
                }
                true
            }
            is ApiResult.Error -> false
        }
    }

    suspend fun seedSampleDataIfEmpty() {
        val rewardList = rewardDao.getAllRewards().first()

        if (rewardList.isEmpty()) {
            val synced = syncFromApi()
            if (synced) return

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
