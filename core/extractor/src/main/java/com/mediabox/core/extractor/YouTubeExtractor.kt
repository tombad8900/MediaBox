package com.mediabox.core.extractor

import com.mediabox.core.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class YouTubeExtractor : MediaExtractor {
    override val supportedPlatform = PlatformType.YOUTUBE

    override fun canHandle(url: String): Boolean {
        val lower = url.lowercase()
        return lower.contains("youtube.com") || lower.contains("youtu.be")
    }

    override suspend fun extract(url: String): Result<MediaInfo> = withContext(Dispatchers.IO) {
        try {
            val options = listOf(
                StreamOption(id = "yt_best", qualityLabel = "أعلى جودة متاحة (Auto)", format = "mp4", downloadUrl = url, mediaType = MediaType.VIDEO),
                StreamOption(id = "yt_audio", qualityLabel = "صوت فقط (MP3)", format = "mp3", downloadUrl = url, mediaType = MediaType.AUDIO)
            )
            Result.success(MediaInfo(originalUrl = url, title = "مطع YouTube", author = "YouTube", platform = PlatformType.YOUTUBE, options = options))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
