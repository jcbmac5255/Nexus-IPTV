package com.nexus.iptv.ui.screens.nexus

import android.util.Log
import com.nexus.iptv.data.local.dao.MovieDao
import com.nexus.iptv.domain.model.ContentType
import com.nexus.iptv.domain.model.PlaybackHistory
import com.nexus.iptv.domain.model.PlaybackWatchedStatus
import com.nexus.iptv.domain.model.RemoteWatchedStatus
import com.nexus.iptv.domain.model.Result
import com.nexus.iptv.domain.repository.PlaybackHistoryRepository
import com.nexus.iptv.domain.repository.RemoteHistoryRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pulls server-side watch history (resume positions, completion status) from PocketBase
 * after a Nexus sign-in and merges it into the local playback_history table. v1 covers
 * MOVIE entries only — episode resume sync is a follow-up.
 *
 * If the local table already has an entry for the same content, the remote position
 * wins only when it's strictly larger OR newer — avoids regressing fresh local progress
 * with a stale server copy.
 */
@Singleton
class NexusHistoryRestorer @Inject constructor(
    private val remoteHistoryRepository: RemoteHistoryRepository,
    private val playbackHistoryRepository: PlaybackHistoryRepository,
    private val movieDao: MovieDao
) {

    suspend fun restoreFor(providerId: Long, username: String): Int {
        val normalized = username.trim().lowercase()
        if (normalized.isEmpty()) return 0

        val entries = when (val result = remoteHistoryRepository.fetchHistory(normalized)) {
            is Result.Success -> result.data
            is Result.Error -> {
                Log.w(TAG, "Skipping history restore: ${result.message}")
                return 0
            }
            Result.Loading -> return 0
        }

        var restored = 0
        for (remote in entries) {
            if (remote.contentType != ContentType.MOVIE) continue
            val streamId = remote.externalId.toLongOrNull() ?: continue
            val movie = movieDao.getByStreamId(providerId, streamId) ?: continue

            val existing = playbackHistoryRepository.getPlaybackHistory(
                contentId = movie.id,
                contentType = ContentType.MOVIE,
                providerId = providerId
            )
            // Only overwrite local progress with the remote copy when the remote is
            // newer; we don't want to clobber fresh local progress with a stale server
            // entry if the user installed v1 of the sync after already watching things.
            if (existing != null && existing.lastWatchedAt >= remote.lastWatchedAt) continue

            val mergedHistory = PlaybackHistory(
                id = existing?.id ?: 0L,
                contentId = movie.id,
                contentType = ContentType.MOVIE,
                providerId = providerId,
                title = movie.name,
                posterUrl = movie.posterUrl,
                streamUrl = existing?.streamUrl ?: "",
                resumePositionMs = remote.positionMs,
                totalDurationMs = remote.durationMs.takeIf { it > 0L } ?: existing?.totalDurationMs ?: 0L,
                lastWatchedAt = remote.lastWatchedAt,
                watchCount = existing?.watchCount ?: 1,
                watchedStatus = when (remote.watchedStatus) {
                    RemoteWatchedStatus.IN_PROGRESS -> PlaybackWatchedStatus.IN_PROGRESS
                    RemoteWatchedStatus.COMPLETED -> PlaybackWatchedStatus.COMPLETED_AUTO
                }
            )

            val writeResult = playbackHistoryRepository.recordPlayback(mergedHistory)
            if (writeResult is Result.Success) restored++
        }
        if (restored > 0) Log.i(TAG, "Restored $restored history entry(ies) for $normalized")
        return restored
    }

    private companion object {
        const val TAG = "NexusHistoryRestorer"
    }
}
