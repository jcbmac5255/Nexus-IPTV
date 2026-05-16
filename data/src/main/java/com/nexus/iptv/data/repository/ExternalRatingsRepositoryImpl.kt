package com.nexus.iptv.data.repository

import com.nexus.iptv.domain.model.ExternalRatings
import com.nexus.iptv.domain.model.ExternalRatingsLookup
import com.nexus.iptv.domain.model.Result
import com.nexus.iptv.domain.repository.ExternalRatingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExternalRatingsRepositoryImpl @Inject constructor() : ExternalRatingsRepository {

    override suspend fun getRatings(lookup: ExternalRatingsLookup): Result<ExternalRatings> {
        return Result.success(ExternalRatings.unavailable())
    }
}