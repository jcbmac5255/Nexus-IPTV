package com.nexus.iptv.domain.usecase

import com.nexus.iptv.domain.model.ContentType
import com.nexus.iptv.domain.model.PlaybackHistory
import com.nexus.iptv.domain.model.Result
import com.nexus.iptv.domain.repository.PlaybackHistoryRepository
import com.nexus.iptv.domain.util.isPlaybackComplete
import javax.inject.Inject

class MarkAsWatched @Inject constructor(
    private val playbackHistoryRepository: PlaybackHistoryRepository
) {
    suspend operator fun invoke(history: PlaybackHistory): Result<Unit> {
        val normalizedHistory = if (
            history.contentType != ContentType.LIVE &&
            isPlaybackComplete(history.resumePositionMs, history.totalDurationMs)
        ) {
            history.copy(
                resumePositionMs = history.totalDurationMs.coerceAtLeast(history.resumePositionMs),
                lastWatchedAt = System.currentTimeMillis()
            )
        } else {
            history
        }
        return playbackHistoryRepository.markAsWatched(normalizedHistory)
    }
}