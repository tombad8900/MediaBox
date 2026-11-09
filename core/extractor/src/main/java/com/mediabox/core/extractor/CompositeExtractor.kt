package com.mediabox.core.extractor

import com.mediabox.core.model.MediaInfo
import com.mediabox.core.model.PlatformType

class CompositeExtractor(
    private val extractors: List<MediaExtractor> = listOf(
        YtDlpExtractor(),
        FacebookExtractor(),
        YouTubeExtractor(),
        TikTokExtractor(),
        TelegramExtractor()
    )
) {
    suspend fun extract(url: String): Result<MediaInfo> = try {
        val trimmedUrl = url.trim()
        if (trimmedUrl.isEmpty()) {
            Result.failure(IllegalArgumentException("الرابط المكتوب فارغ"))
        } else {
            var finalRes: Result<MediaInfo>? = null
            
            val ytdlp = extractors.filterIsInstance<YtDlpExtractor>().firstOrNull()
            if (ytdlp != null) {
                val res = ytdlp.extract(trimmedUrl)
                if (res.isSuccess) {
                    finalRes = res
                }
            }

            if (finalRes == null || finalRes.isFailure) {
                val matching = extractors.firstOrNull { it !is YtDlpExtractor && it.canHandle(trimmedUrl) }
                if (matching != null) {
                    finalRes = matching.extract(trimmedUrl)
                }
            }

            finalRes ?: Result.failure(Exception("لم نتمكن من استخراج رابط الفيديو المباشر، تأكد أن الفيديو عام ورابطه صحيح"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
