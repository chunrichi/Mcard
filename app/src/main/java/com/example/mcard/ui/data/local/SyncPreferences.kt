package com.example.mcard.ui.data.local

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject

class SyncPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    private val timestampsJson: JSONObject
        get() {
            val json = prefs.getString(KEY_SYNC_TIMESTAMPS, "{}") ?: "{}"
            return JSONObject(json)
        }

    fun getLastSyncTimestamp(sourceId: String): Long {
        return timestampsJson.optLong(sourceId, 0L)
    }

    fun setLastSyncTimestamp(sourceId: String, timestamp: Long) {
        val json = timestampsJson
        json.put(sourceId, timestamp)
        prefs.edit().putString(KEY_SYNC_TIMESTAMPS, json.toString()).apply()
    }

    fun clearSourceTimestamp(sourceId: String) {
        val json = timestampsJson
        json.remove(sourceId)
        prefs.edit().putString(KEY_SYNC_TIMESTAMPS, json.toString()).apply()
    }

    var lastMessageCount: Int
        get() = prefs.getInt(KEY_MESSAGE_COUNT, 0)
        set(value) = prefs.edit().putInt(KEY_MESSAGE_COUNT, value).apply()

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "mcard_sync"
        private const val KEY_SYNC_TIMESTAMPS = "sync_timestamps"
        private const val KEY_MESSAGE_COUNT = "message_count"
    }
}
