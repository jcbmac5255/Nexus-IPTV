package com.nexus.iptv.data.sync

import com.nexus.iptv.data.local.dao.ChannelDao
import com.nexus.iptv.domain.repository.SyncMetadataRepository
import com.nexus.iptv.domain.model.ProviderType
import kotlinx.coroutines.flow.first

internal suspend fun hasUsableLiveCatalogForActivation(
    providerId: Long,
    providerType: ProviderType,
    channelDao: ChannelDao,
    syncMetadataRepository: SyncMetadataRepository
): Boolean {
    if (providerType != ProviderType.XTREAM_CODES) {
        return true
    }

    if (channelDao.getCount(providerId).first() > 0) {
        return true
    }

    val metadata = syncMetadataRepository.getMetadata(providerId)
    return (metadata?.movieCount ?: 0) > 0 || (metadata?.seriesCount ?: 0) > 0
}