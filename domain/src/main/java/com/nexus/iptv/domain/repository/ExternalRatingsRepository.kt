package com.nexus.iptv.domain.repository

import com.nexus.iptv.domain.model.ExternalRatings
import com.nexus.iptv.domain.model.ExternalRatingsLookup
import com.nexus.iptv.domain.model.Result

interface ExternalRatingsRepository {
    suspend fun getRatings(lookup: ExternalRatingsLookup): Result<ExternalRatings>
}