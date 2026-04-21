package com.example.mcard.ui.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.mcard.ui.data.model.Message
import org.json.JSONArray
import org.json.JSONObject

class MessagesPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    fun getMessages(): List<Message> {
        val json = prefs.getString(KEY_MESSAGES, "[]") ?: "[]"
        return parseMessages(JSONArray(json))
    }

    fun saveMessages(messages: List<Message>) {
        val jsonArray = JSONArray()
        messages.forEach { message ->
            val obj = JSONObject().apply {
                put("id", message.id)
                put("title", message.title)
                put("preview", message.preview)
                put("content", message.content)
                put("timestamp", message.timestamp)
                put("source", message.source)
                message.url?.let { put("url", it) }
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_MESSAGES, jsonArray.toString()).apply()
    }

    fun clearAll() {
        prefs.edit().remove(KEY_MESSAGES).apply()
    }

    fun clearMessagesOnly() {
        prefs.edit().remove(KEY_MESSAGES).apply()
    }

    fun clearMessagesKeepingFavorites() {
        val favoriteKeys = getFavoriteMessageIds()
        val allMessages = getMessages()
        val favoritedMessages = allMessages.filter { favoriteKeys.contains("${it.source}_${it.id}") }
        saveMessages(favoritedMessages)
    }

    fun toggleFavorite(messageKey: String) {
        val favoriteSet = getFavoriteMessageIds().toMutableSet()
        if (favoriteSet.contains(messageKey)) {
            favoriteSet.remove(messageKey)
        } else {
            favoriteSet.add(messageKey)
        }
        prefs.edit().putStringSet(KEY_FAVORITES, favoriteSet).apply()
    }

    fun isMessageFavorited(messageKey: String): Boolean {
        return getFavoriteMessageIds().contains(messageKey)
    }

    fun getFavoriteMessageIds(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    }

    fun deleteMessage(messageKey: String) {
        val currentMessages = getMessages().toMutableList()
        val updatedMessages = currentMessages.filter { "${it.source}_${it.id}" != messageKey }
        saveMessages(updatedMessages)
        removeFavorite(messageKey)
    }

    fun removeFavorite(messageKey: String) {
        val favoriteSet = getFavoriteMessageIds().toMutableSet()
        favoriteSet.remove(messageKey)
        prefs.edit().putStringSet(KEY_FAVORITES, favoriteSet).apply()
    }

    fun markAsRead(sourceId: String) {
        val readSet = getReadMessageIds().toMutableSet()
        readSet.add(sourceId)
        prefs.edit().putStringSet(KEY_READ_MESSAGES, readSet).apply()
    }

    fun isMessageRead(sourceId: String): Boolean {
        return getReadMessageIds().contains(sourceId)
    }

    fun getReadMessageIds(): Set<String> {
        return prefs.getStringSet(KEY_READ_MESSAGES, emptySet()) ?: emptySet()
    }

    private fun parseMessages(jsonArray: JSONArray): List<Message> {
        val messages = mutableListOf<Message>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            messages.add(
                Message(
                    id = obj.optString("id", ""),
                    title = obj.optString("title", ""),
                    preview = obj.optString("preview", ""),
                    content = obj.optString("content", ""),
                    timestamp = obj.optLong("timestamp", 0L),
                    source = obj.optString("source", ""),
                    url = if (obj.has("url")) obj.getString("url").takeIf { it.isNotEmpty() } else null
                )
            )
        }
        return messages
    }

    companion object {
        private const val PREFS_NAME = "mcard_messages"
        private const val KEY_MESSAGES = "messages"
        private const val KEY_READ_MESSAGES = "read_messages"
        private const val KEY_FAVORITES = "favorites"
    }
}
