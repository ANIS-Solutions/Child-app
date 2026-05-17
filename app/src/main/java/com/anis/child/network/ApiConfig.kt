package com.anis.child.network

object ApiConfig {
    const val BASE_URL = "https://api.anis.solutions/api/v1/"
    const val CONNECT_TIMEOUT_SECONDS = 30L
    const val READ_TIMEOUT_SECONDS = 30L
    const val WRITE_TIMEOUT_SECONDS = 30L

    object Endpoints {
        const val PAIR_DEVICE = "children/pair"
        const val SEND_TELEMETRY = "locations/telemetry/"
        const val REGISTER_FCM_TOKEN = "children/fcm-token"
    }
}
