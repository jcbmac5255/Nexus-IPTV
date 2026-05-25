package com.nexus.iptv.ui.screens.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.getValue
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.nexus.iptv.R
import com.nexus.iptv.ui.components.extractProgressFraction
import com.nexus.iptv.ui.theme.OnSurface
import com.nexus.iptv.ui.theme.OnSurfaceDim
import com.nexus.iptv.ui.theme.Primary

@Composable
internal fun SyncingOverlay(
    isSyncing: Boolean,
    providerName: String? = null,
    progress: String? = null,
    busProgress: com.nexus.iptv.domain.sync.SyncProgress? = null
) {
    if (!isSyncing) return

    BackHandler(enabled = true) {}

    val overlayFocusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { overlayFocusRequester.requestFocus() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(enabled = true, onClick = {})
            .focusRequester(overlayFocusRequester)
            .focusable()
            .onKeyEvent { true },
        contentAlignment = Alignment.Center
    ) {
        val busFraction = busProgress?.takeIf { it.total > 0 }?.let { p ->
            (p.current.toFloat() / p.total.toFloat()).coerceIn(0f, 1f)
        }
        val fraction = busFraction ?: progress?.let { extractProgressFraction(it) }
        val animatedFraction by animateFloatAsState(
            targetValue = fraction ?: 0f,
            animationSpec = tween(durationMillis = 400),
            label = "syncFraction"
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.widthIn(min = 360.dp, max = 520.dp)
        ) {
            CircularProgressIndicator(color = Primary)
            Text(
                text = stringResource(R.string.settings_syncing_title),
                style = MaterialTheme.typography.titleMedium,
                color = OnSurface
            )
            Text(
                text = providerName ?: stringResource(R.string.settings_syncing_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceDim
            )
            val message = formatBusProgressMessage(busProgress) ?: progress
            if (message != null) {
                if (fraction != null) {
                    LinearProgressIndicator(
                        progress = { animatedFraction },
                        color = Primary,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    LinearProgressIndicator(
                        color = Primary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceDim
                )
            }
        }
    }
}

private fun formatBusProgressMessage(progress: com.nexus.iptv.domain.sync.SyncProgress?): String? {
    if (progress == null) return null
    val sectionLabel = when (progress.section) {
        com.nexus.iptv.domain.sync.Section.LIVE -> "Live TV"
        com.nexus.iptv.domain.sync.Section.VOD -> "Movies"
        com.nexus.iptv.domain.sync.Section.SERIES -> "Series"
        com.nexus.iptv.domain.sync.Section.EPG -> "EPG"
    }
    val countSuffix = when {
        progress.total > 0 && progress.currentLabel.isNotBlank() ->
            " ${progress.current} / ${progress.total} (${progress.currentLabel})"
        progress.total > 0 -> " ${progress.current} / ${progress.total}"
        progress.itemsIndexed > 0 -> " ${progress.itemsIndexed} items"
        else -> ""
    }
    return "Syncing $sectionLabel...$countSuffix"
}

