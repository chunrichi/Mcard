package com.example.mcard.ui.data.network

import com.example.mcard.ui.data.model.AuthType
import com.example.mcard.ui.data.model.Message
import com.example.mcard.ui.data.model.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class ApiService(
    private val baseUrl: String = "http://10.8.0.1:8000"
) {
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun fetchMessagesFromSource(source: Source, since: Long? = null): Result<List<Message>> {
        return withContext(Dispatchers.IO) {
            try {
                var url = source.url.trimEnd('/')
                if (since != null) {
                    url += if (url.contains("?")) "&since=$since" else "?since=$since"
                }

                android.util.Log.d("ApiService", "Fetching from: $url")

                val requestBuilder = Request.Builder().url(url).get()

                // Add auth header based on authType
                when (source.authType) {
                    AuthType.NONE -> {}
                    AuthType.API_KEY, AuthType.CUSTOM_HEADER -> {
                        if (source.authKey.isNotBlank()) {
                            requestBuilder.addHeader(source.authKey, source.authValue)
                        }
                    }
                    AuthType.BEARER_TOKEN -> {
                        requestBuilder.addHeader("Authorization", "Bearer ${source.authValue}")
                    }
                    AuthType.BASIC_AUTH -> {
                        val credentials = "${source.authKey}:${source.authValue}"
                        val encoded = android.util.Base64.encodeToString(
                            credentials.toByteArray(),
                            android.util.Base64.NO_WRAP
                        )
                        requestBuilder.addHeader("Authorization", "Basic $encoded")
                    }
                }

                val response = client.newCall(requestBuilder.build()).execute()
                val body = response.body?.string()

                if (response.isSuccessful && body != null) {
                    val dataArray = try {
                        val json = JSONObject(body)
                        if (json.has("data")) json.getJSONArray("data") else JSONArray(body)
                    } catch (e: Exception) {
                        JSONArray(body)
                    }
                    val messages = parseMessages(dataArray)
                    Result.success(messages)
                } else {
                    Result.failure(IOException("HTTP ${response.code}: ${response.message}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getSources(): Result<List<Source>> {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("$baseUrl/api/sources")
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string()

                if (response.isSuccessful && body != null) {
                    val json = JSONObject(body)
                    val success = json.optBoolean("success", false)
                    if (success) {
                        val dataArray = json.getJSONArray("data")
                        val sources = parseSources(dataArray)
                        Result.success(sources)
                    } else {
                        Result.failure(IOException(json.optString("message", "Unknown error")))
                    }
                } else {
                    Result.failure(IOException("HTTP ${response.code}: ${response.message}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun addSource(
        name: String,
        url: String,
        authType: AuthType,
        authKey: String,
        authValue: String
    ): Result<Source> {
        return withContext(Dispatchers.IO) {
            try {
                val json = JSONObject().apply {
                    put("name", name)
                    put("url", url)
                    put("authType", authType.name)
                    put("authKey", authKey)
                    put("authValue", authValue)
                }

                val requestBody = json.toString().toRequestBody(jsonMediaType)

                val request = Request.Builder()
                    .url("$baseUrl/api/sources")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string()

                if (response.isSuccessful && body != null) {
                    val jsonResponse = JSONObject(body)
                    val success = jsonResponse.optBoolean("success", false)
                    if (success) {
                        val data = jsonResponse.getJSONObject("data")
                        val sources = parseSources(JSONArray().put(data))
                        Result.success(sources.first())
                    } else {
                        Result.failure(IOException(jsonResponse.optString("message", "Unknown error")))
                    }
                } else {
                    Result.failure(IOException("HTTP ${response.code}: ${response.message}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteSource(id: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("$baseUrl/api/sources/$id")
                    .delete()
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string()

                if (response.isSuccessful && body != null) {
                    val json = JSONObject(body)
                    Result.success(json.optBoolean("success", false))
                } else {
                    Result.failure(IOException("HTTP ${response.code}: ${response.message}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun updateSource(source: Source): Result<Source> {
        return withContext(Dispatchers.IO) {
            try {
                val json = JSONObject().apply {
                    put("name", source.name)
                    put("url", source.url)
                    put("isEnabled", source.isEnabled)
                    put("authType", source.authType.name)
                    put("authKey", source.authKey)
                    put("authValue", source.authValue)
                }

                val requestBody = json.toString().toRequestBody(jsonMediaType)

                val request = Request.Builder()
                    .url("$baseUrl/api/sources/${source.id}")
                    .put(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string()

                if (response.isSuccessful && body != null) {
                    val jsonResponse = JSONObject(body)
                    val success = jsonResponse.optBoolean("success", false)
                    if (success) {
                        val data = jsonResponse.getJSONObject("data")
                        val sources = parseSources(JSONArray().put(data))
                        Result.success(sources.first())
                    } else {
                        Result.failure(IOException(jsonResponse.optString("message", "Unknown error")))
                    }
                } else {
                    Result.failure(IOException("HTTP ${response.code}: ${response.message}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
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
                    source = obj.optString("source", "")
                )
            )
        }
        return messages
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
}
