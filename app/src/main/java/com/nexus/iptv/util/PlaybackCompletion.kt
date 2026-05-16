package com.nexus.iptv.util

import com.nexus.iptv.domain.util.isPlaybackComplete as domainIsPlaybackComplete

fun isPlaybackComplete(
    progressMs: Long,
    totalDurationMs: Long,
    threshold: Float = com.nexus.iptv.domain.util.DEFAULT_PLAYBACK_COMPLETION_THRESHOLD
): Boolean = domainIsPlaybackComplete(progressMs, totalDurationMs, threshold)
