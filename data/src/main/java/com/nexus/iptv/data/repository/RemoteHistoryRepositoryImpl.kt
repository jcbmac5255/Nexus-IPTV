package com.nexus.iptv.data.repository

import android.util.Log
import com.nexus.iptv.domain.model.ContentType
import com.nexus.iptv.domain.model.RemoteHistoryEntry
import com.nexus.iptv.domain.model.RemoteWatchedStatus
import com.nexus.iptv.domain.model.Result
import com.nexus.iptv.domain.repository.RemoteHistoryRepository
import java.io.IOException
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

@Singleton
class RemoteHistoryRepositoryImpl @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val json: Json
) : RemoteHistoryRepository {

    override suspend fun fetchHistory(username: String): Result<List<RemoteHistoryEntry>> =
        withContext(Dispatchers.IO) {
            val normalized = username.trim().lowercase()
            if (normalized.isEmpty()) return@withContext Result.success(emptyList())

            val filter = URLEncoder.encode("username=\"$normalized\"", "UTF-8")
            val request = Request.Builder()
                .url("$BASE_URL/api/collections/$COLLECTION/records?perPage=500&filter=$filter")
                .header("Accept", "application/json")
                .build()

            runCatching {
                okHttpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IOException("HTTP ${response.code} from PocketBase")
                    }
                    val bodyText = response.body?.string().orEmpty()
                    val items = json.parseToJsonElement(bodyText).jsonObject["items"] as? JsonArray
                        ?: return@use emptyList()
                    items.mapNotNull { parseRecord(it.jsonObject) }
                }
            }.fold(
                onSuccess = { Result.success(it) },
                onFailure = { error ->
                    Log.w(TAG, "Failed to fetch remote history", error)
                    Result.error(error.message ?: "Failed to fetch history", error)
                }
            )
        }

    override suspend fun upsertEntry(
        username: String,
        contentType: ContentType,
        externalId: String,
        positionMs: Long,
        durationMs: Long,
        watchedStatus: RemoteWatchedStatus
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val normalized = username.trim().lowercase()
        if (normalized.isEmpty() || externalId.isBlank()) return@withContext Result.success(Unit)

        val existingId = lookupRecordId(normalized, contentType, externalId)
        val payload = buildJsonObject {
            put("username", normalized)
            put("content_type", contentType.name)
            put("external_id", externalId)
            put("position_ms", positionMs)
            put("duration_ms", durationMs)
            put("watched_status", watchedStatus.name)
            put("last_watched_at", System.currentTimeMillis())
        }
        val (url, method) = if (existingId != null) {
            "$BASE_URL/api/collections/$COLLECTION/records/$existingId" to "PATCH"
        } else {
            "$BASE_URL/api/collections/$COLLECTION/records" to "POST"
        }
        val body = payload.toString().toRequestBody(JSON_MEDIA)
        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/json")
            .method(method, body)
            .build()

        runCatching {
            okHttpClient.newCall(request).execute().use { response ->
                // 400 is acceptable on POST when another concurrent write already created
                // the row (unique index collision). Position drift between racing writes
                // is acceptable for v1.
                if (!response.isSuccessful && !(method == "POST" && response.code == 400)) {
                    throw IOException("HTTP ${response.code} upserting history entry")
                }
            }
        }.fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { error ->
                Log.w(TAG, "Failed to upsert remote history entry", error)
                Result.error(error.message ?: "Failed to upsert history", error)
            }
        )
    }

    override suspend fun deleteEntry(
        username: String,
        contentType: ContentType,
        externalId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val normalized = username.trim().lowercase()
        if (normalized.isEmpty() || externalId.isBlank()) return@withContext Result.success(Unit)

        val recordId = lookupRecordId(normalized, contentType, externalId)
            ?: return@withContext Result.success(Unit)

        val request = Request.Builder()
            .url("$BASE_URL/api/collections/$COLLECTION/records/$recordId")
            .delete()
            .build()

        runCatching {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful && response.code != 404) {
                    throw IOException("HTTP ${response.code} deleting history entry")
                }
            }
        }.fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { error ->
                Log.w(TAG, "Failed to delete remote history entry", error)
                Result.error(error.message ?: "Failed to delete history", error)
            }
        )
    }

    private suspend fun lookupRecordId(
        normalizedUsername: String,
        contentType: ContentType,
        externalId: String
    ): String? = withContext(Dispatchers.IO) {
        val filter = URLEncoder.encode(
            "username=\"$normalizedUsername\" && content_type=\"${contentType.name}\" && external_id=\"$externalId\"",
            "UTF-8"
        )
        val request = Request.Builder()
            .url("$BASE_URL/api/collections/$COLLECTION/records?perPage=1&filter=$filter")
            .header("Accept", "application/json")
            .build()
        runCatching {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use null
                val bodyText = response.body?.string().orEmpty()
                val items = json.parseToJsonElement(bodyText).jsonObject["items"] as? JsonArray
                items?.firstOrNull()?.jsonObject?.get("id")?.jsonPrimitive?.contentOrNull
            }
        }.getOrNull()
    }

    private fun parseRecord(obj: JsonObject): RemoteHistoryEntry? {
        val id = obj["id"]?.jsonPrimitive?.contentOrNull ?: return null
        val username = obj["username"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() } ?: return null
        val contentTypeRaw = obj["content_type"]?.jsonPrimitive?.contentOrNull ?: return null
        val contentType = runCatching { ContentType.valueOf(contentTypeRaw.uppercase()) }.getOrNull() ?: return null
        val externalId = obj["external_id"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() } ?: return null
        val positionMs = obj["position_ms"]?.jsonPrimitive?.longOrNull ?: 0L
        val durationMs = obj["duration_ms"]?.jsonPrimitive?.longOrNull ?: 0L
        val statusRaw = obj["watched_status"]?.jsonPrimitive?.contentOrNull ?: "IN_PROGRESS"
        val status = runCatching { RemoteWatchedStatus.valueOf(statusRaw.uppercase()) }
            .getOrDefault(RemoteWatchedStatus.IN_PROGRESS)
        val lastWatchedAt = obj["last_watched_at"]?.jsonPrimitive?.longOrNull ?: 0L

        return RemoteHistoryEntry(
            recordId = id,
            username = username,
            contentType = contentType,
            externalId = externalId,
            positionMs = positionMs,
            durationMs = durationMs,
            watchedStatus = status,
            lastWatchedAt = lastWatchedAt
        )
    }

    private companion object {
        const val TAG = "RemoteHistoryRepo"
        const val BASE_URL = "https://nexus.nexgrid.cc"
        const val COLLECTION = "user_history"
        val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
    }
}
