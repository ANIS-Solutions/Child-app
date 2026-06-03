package com.anis.child.ui.screen.reward

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anis.child.data.local.RewardEntity
import com.anis.child.data.repository.RewardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RewardUiState(
    val balance: Int = 0,
    val rewards: List<RewardEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class RewardViewModel @Inject constructor(
    private val rewardRepository: RewardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RewardUiState())
    val uiState: StateFlow<RewardUiState> = _uiState.asStateFlow()

    init {
        loadRewards()
    }

    private fun loadRewards() {
        viewModelScope.launch {
            rewardRepository.seedSampleDataIfEmpty()
            val balance = rewardRepository.getBalance()
            rewardRepository.getAllRewards().collect { rewards ->
                _uiState.value = RewardUiState(
                    balance = balance,
                    rewards = rewards,
                    isLoading = false
                )
            }
        }
    }

    fun claimReward(rewardId: Long) {
        viewModelScope.launch {
            rewardRepository.claimReward(rewardId)
            val balance = rewardRepository.getBalance()
            _uiState.value = _uiState.value.copy(balance = balance)
        }
    }
}
