package com.nexus.iptv.domain.usecase

import com.nexus.iptv.domain.model.ContentType
import com.nexus.iptv.domain.model.ExternalSubtitle
import com.nexus.iptv.domain.repository.ExternalSubtitleRepository
import javax.inject.Inject

class SearchExternalSubtitles @Inject constructor(
    private val repository: ExternalSubtitleRepository
) {
    suspend operator fun invoke(
        contentType: ContentType,
        title: String,
        year: Int?,
        tmdbId: Long?,
        parentTmdbId: Long?,
        seasonNumber: Int?,
        episodeNumber: Int?,
        language: String
    ): Result<List<ExternalSubtitle>> = repository.search(
        contentType = contentType,
        title = title,
        year = year,
        tmdbId = tmdbId,
        parentTmdbId = parentTmdbId,
        seasonNumber = seasonNumber,
        episodeNumber = episodeNumber,
        language = language
    )
}
