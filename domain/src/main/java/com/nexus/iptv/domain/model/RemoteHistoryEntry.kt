package com.nexus.iptv.domain.model

data class RemoteHistoryEntry(
    val recordId: String,
    val username: String,
    val contentType: ContentType,
    val externalId: String,
    val positionMs: Long,
    val durationMs: Long,
    val watchedStatus: RemoteWatchedStatus,
    val lastWatchedAt: Long
)

enum class RemoteWatchedStatus {
    IN_PROGRESS,
    COMPLETED
}
