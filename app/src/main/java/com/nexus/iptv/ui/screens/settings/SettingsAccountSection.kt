package com.nexus.iptv.ui.screens.settings

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nexus.iptv.R
import com.nexus.iptv.domain.model.Provider
import com.nexus.iptv.domain.model.ProviderType
import com.nexus.iptv.ui.time.LocalAppTimeFormat
import com.nexus.iptv.ui.time.createDateTimeFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Renders an "Account" subsection at the top of the Provider settings page when the
 * active provider is an xtream login. Surfaces the per-customer subscription info that
 * the xtream `user_info` payload exposes — expiration date, max connections, allowed
 * output formats — so customers can self-serve "what do I have?" questions without
 * needing to contact support.
 */
internal fun LazyListScope.accountInfoSection(
    uiState: SettingsUiState,
    context: Context
) {
    val active = uiState.providers.firstOrNull { it.id == uiState.activeProviderId }
        ?: return
    if (active.type != ProviderType.XTREAM_CODES) return

    item {
        SettingsSectionHeader(
            title = stringResource(R.string.settings_account_title),
            subtitle = stringResource(R.string.settings_account_subtitle)
        )
        AccountRows(provider = active, context = context)
    }
}

@Composable
private fun AccountRows(provider: Provider, context: Context) {
    val appTimeFormat = LocalAppTimeFormat.current
    val lastSyncedFormat = remember(appTimeFormat) { appTimeFormat.createDateTimeFormat() }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    ) {
        if (provider.username.isNotBlank()) {
            SettingsRow(
                label = stringResource(R.string.settings_account_username),
                value = provider.username
            )
        }
        SettingsRow(
            label = stringResource(R.string.settings_account_expiration),
            value = formatExpiration(provider.expirationDate, context)
        )
        SettingsRow(
            label = stringResource(R.string.settings_account_max_connections),
            value = provider.maxConnections.toString()
        )
        SettingsRow(
            label = stringResource(R.string.settings_account_output_formats),
            value = if (provider.allowedOutputFormats.isEmpty()) {
                stringResource(R.string.settings_account_output_formats_unknown)
            } else {
                provider.allowedOutputFormats.joinToString(", ")
            }
        )
        SettingsRow(
            label = stringResource(R.string.settings_account_last_synced),
            value = if (provider.lastSyncedAt <= 0L) {
                stringResource(R.string.settings_account_last_synced_never)
            } else {
                lastSyncedFormat.format(Date(provider.lastSyncedAt))
            }
        )
    }
}

private fun formatExpiration(expirationDate: Long?, context: Context): String {
    if (expirationDate == null || expirationDate <= 0L) {
        return context.getString(R.string.settings_account_expiration_none)
    }
    val dateLabel = expirationFormat.format(Date(expirationDate))
    val daysRemaining = ((expirationDate - System.currentTimeMillis()) / DAY_MS).toInt()
    return when {
        daysRemaining < 0 -> "$dateLabel (expired)"
        daysRemaining == 0 -> "$dateLabel (today)"
        daysRemaining == 1 -> "$dateLabel (1 day left)"
        else -> "$dateLabel ($daysRemaining days left)"
    }
}

private const val DAY_MS = 24L * 60L * 60L * 1000L
private val expirationFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
