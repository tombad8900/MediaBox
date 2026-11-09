package com.mediabox.core.extractor

import com.mediabox.core.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern

class TelegramExtractor : MediaExtractor {
    override val supportedPlatform = PlatformType.TELEGRAM

    override fun canHandle(url: String): Boolean {
        val lower = url.lowercase()
        return lower.contains("t.me") || lower.contains("telegram.org")
    }

    override suspend fun extract(url: String): Result<MediaInfo> = withContext(Dispatchers.IO) {
        try {
            val cleanUrl = if (!url.contains("/s/")) url.replace("t.me/", "t.me/s/") else url
            val connection = URL(cleanUrl).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")

            val html = connection.inputStream.bufferedReader().readText()
            val videoMatcher = Pattern.compile("""<video[^>]+src="([^"]+)"""").matcher(html)
            
            if (videoMatcher.find()) {
                val videoUrl = videoMatcher.group(1)
                val textMatcher = Pattern.compile("""class="tgme_widget_message_text[^"]*"[^>]*>(.*?)</div>""", Pattern.DOTALL).matcher(html)
                val rawCaption = if (textMatcher.find()) textMatcher.group(1).replace(Regex("<[^>]+>"), "").trim() else "منشور Telegram"

                val options = listOf(
                    StreamOption(id = "tg_mp4", qualityLabel = "فيديو MP4 مباشر", format = "mp4", downloadUrl = videoUrl, mediaType = MediaType.VIDEO)
                )

                Result.success(MediaInfo(originalUrl = url, title = rawCaption.take(60), author = "Telegram Channel", platform = PlatformType.TELEGRAM, options = options))
            } else {
                Result.failure(Exception("لم يتم العثور على وسائط فيديو قابلة للتنزيل"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
