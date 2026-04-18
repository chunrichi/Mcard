package com.example.mcard.ui.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.mcard.ui.data.model.AuthType
import com.example.mcard.ui.data.model.Source
import org.json.JSONArray
import org.json.JSONObject

class SourcesPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    fun getSources(): List<Source> {
        val json = prefs.getString(KEY_SOURCES, "[]") ?: "[]"
        return parseSources(JSONArray(json))
    }

    fun saveSources(sources: List<Source>) {
        val jsonArray = JSONArray()
        sources.forEach { source ->
            val obj = JSONObject().apply {
                put("id", source.id)
                put("name", source.name)
                put("url", source.url)
                put("isEnabled", source.isEnabled)
                put("authType", source.authType.name)
                put("authKey", source.authKey)
                put("authValue", source.authValue)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_SOURCES, jsonArray.toString()).apply()
    }

    fun addSource(source: Source) {
        val sources = getSources().toMutableList()
        sources.add(source)
        saveSources(sources)
    }

    fun updateSource(source: Source) {
        val sources = getSources().toMutableList()
        val index = sources.indexOfFirst { it.id == source.id }
        if (index >= 0) {
            sources[index] = source
            saveSources(sources)
        }
    }

    fun deleteSource(id: String) {
        val sources = getSources().filter { it.id != id }
        saveSources(sources)
    }

    fun clearAll() {
        prefs.edit().remove(KEY_SOURCES).apply()
    }

    private fun parseSources(jsonArray: JSONArray): List<Source> {
        val sources = mutableListOf<Source>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            sources.add(
                Source(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    url = obj.optString("url", ""),
                    isEnabled = obj.optBoolean("isEnabled", true),
                    authType = try { AuthType.valueOf(obj.optString("authType", "NONE")) } catch (e: Exception) { AuthType.NONE },
                    authKey = obj.optString("authKey", ""),
                    authValue = obj.optString("authValue", "")
                )
            )
        }
        return sources
    }

    companion object {
        private const val PREFS_NAME = "mcard_sources"
        private const val KEY_SOURCES = "sources"
    }
}
