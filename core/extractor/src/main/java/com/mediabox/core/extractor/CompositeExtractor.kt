package com.mediabox.core.extractor

import com.mediabox.core.model.MediaInfo
import com.mediabox.core.model.PlatformType

class CompositeExtractor(
    private val extractors: List<MediaExtractor> = listOf(
        FacebookExtractor(),
        YouTubeExtractor(),
        TikTokExtractor(),
        TelegramExtractor()
    )
) {
    suspend fun extract(url: String): Result<MediaInfo> {
        val trimmedUrl = url.trim()
        if (trimmedUrl.isEmpty()) {
            return Result.failure(IllegalArgumentException("الرابط المكتوب فارغ"))
        }

        val platform = PlatformType.detectFromUrl(trimmedUrl)
        val matchingExtractor = extractors.firstOrNull { it.canHandle(trimmedUrl) }

        return if (matchingExtractor != null) {
            matchingExtractor.extract(trimmedUrl)
        } else {
            Result.failure(NoSuchElementException("لم يتم العثور على محرك متوافق مع: ${platform.displayName}"))
        }
    }
}
