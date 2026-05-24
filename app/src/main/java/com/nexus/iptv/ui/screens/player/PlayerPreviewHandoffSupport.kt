package com.nexus.iptv.ui.screens.player

import com.nexus.iptv.player.PlaybackState
import com.nexus.iptv.player.PlayerStats

internal fun shouldRenewAdoptedPreviewOnFullscreen(
    playbackState: PlaybackState,
    playerStats: PlayerStats
): Boolean = playbackState != PlaybackState.READY || playerStats.ttffMs <= 0L
