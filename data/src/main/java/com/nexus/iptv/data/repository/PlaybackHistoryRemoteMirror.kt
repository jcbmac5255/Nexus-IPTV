package com.nexus.iptv.data.repository

import com.nexus.iptv.domain.model.ContentType
import com.nexus.iptv.domain.model.PlaybackHistory
import com.nexus.iptv.domain.model.PlaybackWatchedStatus
import com.nexus.iptv.domain.model.ProviderType
import com.nexus.iptv.data.local.dao.MovieDao
import com.nexus.iptv.domain.model.RemoteWatchedStatus
import com.nexus.iptv.domain.repository.ProviderRepository
import com.nexus.iptv.domain.repository.RemoteHistoryRepository
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Mirrors local MOVIE playback history to the PocketBase user_history collection so
 * resume positions survive uninstall / sign-out for Xtream-backed providers.
 *
 * v1 scope: movies only (episode external-id resolution is a future enhancement).
 * Position updates during playback are debounced per movie so the server isn't
 * hammered every second of playback; completion and removal fire immediately.
 */
@Singleton
class PlaybackHistoryRemoteMirror @Inject constructor(
    private val providerRepository: ProviderRepository,
    // Use the DAO directly: MovieRepositoryImpl already depends on
    // PlaybackHistoryRepository for watch-progress lookups, so injecting the
    // repo here would close a Hilt dependency cycle.
    private val movieDao: MovieDao,
    private val remoteHistoryRepository: RemoteHistoryRepository,
    private val scope: CoroutineScope
) {

    private val lastPositionPushAt = ConcurrentHashMap<String, Long>()
    private val pushMutex = Mutex()

    fun pushHistory(history: PlaybackHistory, force: Boolean = false) {
        scope.launch { pushInternal(history, force) }
    }

    fun pushRemoval(providerId: Long, contentId: Long, contentType: ContentType) {
        scope.launch { removeInternal(providerId, contentId, contentType) }
    }

    private suspend fun pushInternal(history: PlaybackHistory, force: Boolean) {
        if (history.contentType != ContentType.MOVIE) return

        val provider = providerRepository.getProvider(history.providerId) ?: return
        if (provider.type != ProviderType.XTREAM_CODES) return
        val username = provider.username.trim().lowercase()
        if (username.isEmpty()) return

        val externalId = resolveExternalId(history) ?: return
        val key = debounceKey(username, history.contentType, externalId)

        // Always allow forced pushes (completion, flush-on-exit, etc). For mid-playback
        // position drips, throttle to one push per 30 seconds per movie so we're not
        // hitting PocketBase on every second of playback.
        if (!force) {
            val now = System.currentTimeMillis()
            val last = lastPositionPushAt[key] ?: 0L
            if (now - last < POSITION_PUSH_INTERVAL_MS) return
            lastPositionPushAt[key] = now
        }

        val status = when (history.watchedStatus) {
            PlaybackWatchedStatus.IN_PROGRESS -> RemoteWatchedStatus.IN_PROGRESS
            PlaybackWatchedStatus.COMPLETED_AUTO,
            PlaybackWatchedStatus.COMPLETED_MANUAL -> RemoteWatchedStatus.COMPLETED
        }
        pushMutex.withLock {
            remoteHistoryRepository.upsertEntry(
                username = username,
                contentType = history.contentType,
                externalId = externalId,
                positionMs = history.resumePositionMs,
                durationMs = history.totalDurationMs,
                watchedStatus = status
            )
        }
    }

    private suspend fun removeInternal(providerId: Long, contentId: Long, contentType: ContentType) {
        if (contentType != ContentType.MOVIE) return
        val provider = providerRepository.getProvider(providerId) ?: return
        if (provider.type != ProviderType.XTREAM_CODES) return
        val username = provider.username.trim().lowercase()
        if (username.isEmpty()) return

        val movie = movieDao.getById(contentId) ?: return
        val externalId = movie.streamId.takeIf { it > 0L }?.toString() ?: return
        lastPositionPushAt.remove(debounceKey(username, contentType, externalId))
        remoteHistoryRepository.deleteEntry(username, contentType, externalId)
    }

    private suspend fun resolveExternalId(history: PlaybackHistory): String? {
        return when (history.contentType) {
            ContentType.MOVIE -> movieDao.getById(history.contentId)?.streamId
                ?.takeIf { it > 0L }?.toString()
            else -> null
        }
    }

    private fun debounceKey(username: String, type: ContentType, externalId: String): String =
        "$username|${type.name}|$externalId"

    private companion object {
        const val POSITION_PUSH_INTERVAL_MS = 30_000L
    }
}
