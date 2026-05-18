package com.nexus.iptv.domain.repository

import com.nexus.iptv.domain.model.ContentType
import com.nexus.iptv.domain.model.RemoteHistoryEntry
import com.nexus.iptv.domain.model.RemoteWatchedStatus
import com.nexus.iptv.domain.model.Result

interface RemoteHistoryRepository {
    suspend fun fetchHistory(username: String): Result<List<RemoteHistoryEntry>>
    suspend fun upsertEntry(
        username: String,
        contentType: ContentType,
        externalId: String,
        positionMs: Long,
        durationMs: Long,
        watchedStatus: RemoteWatchedStatus
    ): Result<Unit>
    suspend fun deleteEntry(username: String, contentType: ContentType, externalId: String): Result<Unit>
}
