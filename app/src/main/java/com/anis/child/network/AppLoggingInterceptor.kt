package com.anis.child.network

import com.anis.child.data.LogManager
import com.anis.child.data.LogType
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLoggingInterceptor @Inject constructor(
    private val logManager: LogManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startMs = System.currentTimeMillis()

        val requestBody = request.body
        val requestBodyStr = if (requestBody != null && requestBody.contentLength() != 0L) {
            val buffer = Buffer()
            requestBody.writeTo(buffer)
            buffer.readUtf8()
        } else null

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

        val responseBody = response.body
        val responseBodyStr = if (responseBody != null && responseBody.contentLength() != 0L) {
            val source = responseBody.source()
            source.request(Long.MAX_VALUE)
            val buffer = source.buffer.clone()
            buffer.readUtf8()
        } else null

        val sb = StringBuilder()
        sb.append("$method $path → $code (${elapsed}ms)")
        if (requestBodyStr != null) {
            sb.append(" | REQ: ${requestBodyStr.take(200)}")
        }
        if (responseBodyStr != null) {
            sb.append(" | RES: ${responseBodyStr.take(200)}")
        }

        logManager.log(
            message = sb.toString(),
            type = if (code in 200..299) LogType.SUCCESS else LogType.ERROR
        )

        return response
    }
}
