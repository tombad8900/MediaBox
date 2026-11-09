package com.mediabox.core.extractor
import com.mediabox.core.model.MediaInfo
import com.mediabox.core.model.PlatformType

class CompositeExtractor(private val extractors: List<MediaExtractor> = emptyList()) {
    suspend fun extract(url: String): Result<MediaInfo> {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) return Result.failure(IllegalArgumentException("الرابط فارغ"))
        val platform = PlatformType.detectFromUrl(trimmed)
        val matching = extractors.firstOrNull { it.canHandle(trimmed) }
        return matching?.extract(trimmed) ?: Result.failure(NoSuchElementException("لم يتم العثور على محرك منصة: ${platform.displayName}"))
    }
}
