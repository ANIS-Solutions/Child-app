package com.anis.child.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LogEntry(
    val timestamp: Long,
    val message: String,
    val type: LogType
)

enum class LogType {
    INFO, SUCCESS, ERROR, LOCATION, HTTP, NOTIFICATION
}

class LogManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    fun log(message: String, type: LogType = LogType.INFO) {
        val entry = JSONObject().apply {
            put("timestamp", System.currentTimeMillis())
            put("message", message)
            put("type", type.name)
        }

        val logs = getLogsJson()
        logs.put(entry)

        if (logs.length() > MAX_LOGS) {
            val trimmed = JSONArray()
            for (i in (logs.length() - MAX_LOGS) until logs.length()) {
                trimmed.put(logs.getJSONObject(i))
            }
            prefs.edit { putString(KEY_LOGS, trimmed.toString()) }
        } else {
            prefs.edit { putString(KEY_LOGS, logs.toString()) }
        }
    }

    fun getLogs(): List<LogEntry> {
        val logs = getLogsJson()
        val result = mutableListOf<LogEntry>()
        for (i in 0 until logs.length()) {
            val obj = logs.getJSONObject(i)
            result.add(
                LogEntry(
                    timestamp = obj.getLong("timestamp"),
                    message = obj.getString("message"),
                    type = LogType.valueOf(obj.getString("type"))
                )
            )
        }
        return result.reversed()
    }

    fun getLogsJson(): JSONArray {
        return try {
            JSONArray(prefs.getString(KEY_LOGS, "[]") ?: "[]")
        } catch (e: Exception) {
            JSONArray()
        }
    }

    fun formatTimestamp(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    fun clear() {
        prefs.edit { remove(KEY_LOGS) }
    }

    companion object {
        private const val PREFS_NAME = "anis_logs"
        private const val KEY_LOGS = "app_logs"
        private const val MAX_LOGS = 100
    }
}