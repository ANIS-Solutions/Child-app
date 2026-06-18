package com.anis.child.network

import com.anis.child.data.LogManager
import com.anis.child.data.LogType
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLoggingInterceptor @Inject constructor(
    private val logManager: LogManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startMs = System.currentTimeMillis()

        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            val elapsed = System.currentTimeMillis() - startMs
            logManager.log(
                message = "${request.method} ${request.url.encodedPath} FAILED (${elapsed}ms): ${e.message}",
                type = LogType.HTTP
            )
            throw e
        }

        val elapsed = System.currentTimeMillis() - startMs
        val code = response.code
        val method = request.method
        val path = request.url.encodedPath

        logManager.log(
            message = "$method $path → $code (${elapsed}ms)",
            type = if (code in 200..299) LogType.SUCCESS else LogType.ERROR
        )

        return response
    }
}
