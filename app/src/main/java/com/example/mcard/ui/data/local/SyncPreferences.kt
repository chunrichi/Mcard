package com.example.mcard.ui.data.local

import android.content.Context
import android.content.SharedPreferences

class SyncPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    var lastSyncTimestamp: Long
        get() = prefs.getLong(KEY_LAST_SYNC, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_SYNC, value).apply()

    var lastMessageCount: Int
        get() = prefs.getInt(KEY_MESSAGE_COUNT, 0)
        set(value) = prefs.edit().putInt(KEY_MESSAGE_COUNT, value).apply()

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "mcard_sync"
        private const val KEY_LAST_SYNC = "last_sync_timestamp"
        private const val KEY_MESSAGE_COUNT = "message_count"
    }
}
