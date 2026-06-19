package com.anis.child.ui.screen.ai

import com.anis.child.data.local.AnalysisResultEntity

data class VoteAllState(
    val status: VoteStatus = VoteStatus.Idle,
    val selectedResults: List<AnalysisResultEntity> = emptyList(),
    val allEmbeddings: List<AnalysisResultEntity> = emptyList(),
    val selectedSessionIds: List<Long> = emptyList(),
    val error: String? = null
)

enum class VoteStatus { Idle, Voting, Previewing, Sending, Sent }
