package com.anis.child.network

object ApiConfig {
    const val BASE_URL = "https://api.anis.solutions/api/v1/"
    const val CONNECT_TIMEOUT_SECONDS = 30L
    const val READ_TIMEOUT_SECONDS = 30L
    const val WRITE_TIMEOUT_SECONDS = 30L

    object Endpoints {
        const val PAIR_DEVICE = "children/pair"
        const val SEND_TELEMETRY = "locations/telemetry/{childId}"
        const val REGISTER_FCM_TOKEN = "children/fcm-token"
        const val ADD_BULK_APPS = "apps/add-bulk"
        const val CHILDREN_ME = "children/me"
        const val QUESTS = "children/{childId}/quests"
        const val REWARDS = "children/{childId}/rewards"
        const val UPDATE_QUEST = "children/{childId}/quests/{questId}"
        const val UPDATE_REWARD = "children/{childId}/rewards/{rewardId}"
        const val CHILD_SESSION = "child-session"
    }
}
