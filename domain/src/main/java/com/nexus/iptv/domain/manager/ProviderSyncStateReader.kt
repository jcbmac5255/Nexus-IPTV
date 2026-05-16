package com.nexus.iptv.domain.manager

import com.nexus.iptv.domain.model.SyncState
import kotlinx.coroutines.flow.Flow

interface ProviderSyncStateReader {
    fun currentSyncState(providerId: Long): SyncState
    fun observeBackgroundIndexingActive(providerId: Long): Flow<Boolean>
}
