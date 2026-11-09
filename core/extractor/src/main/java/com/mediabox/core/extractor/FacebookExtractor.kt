package com.mediabox.core.extractor

import com.mediabox.core.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.regex.Pattern

class FacebookExtractor : MediaExtractor {
    override val supportedPlatform = PlatformType.FACEBOOK

    override fun canHandle(url: String): Boolean {
        val lower = url.lowercase()
        return lower.contains("facebook.com") || lower.contains("fb.watch")
    }

    override suspend fun extract(url: String): Result<MediaInfo> = withContext(Dispatchers.IO) {
        try {
            val encodedUrl = URLEncoder.encode(url, "UTF-8")
            val pluginUrl = "https://www.facebook.com/plugins/video.php?href=$encodedUrl&show_text=0"
            
            val connection = URL(pluginUrl).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            connection.setRequestProperty("Accept-Language", "ar,en;q=0.9")

            val html = connection.inputStream.bufferedReader().readText()
            val sdMatcher = Pattern.compile("""(?:sd_src|"playable_url"):\s*"([^"]+)"""").matcher(html)
            val hdMatcher = Pattern.compile("""(?:hd_src|"playable_url_quality_hd"):\s*"([^"]+)"""").matcher(html)

            val options = mutableListOf<StreamOption>()
            if (hdMatcher.find()) {
                val rawHdUrl = hdMatcher.group(1)
                val hdUrl = rawHdUrl.replace("""\/""", "/")
                options.add(StreamOption(id = "fb_hd", qualityLabel = "جودة عالية HD", format = "mp4", downloadUrl = hdUrl, mediaType = MediaType.VIDEO))
            }
            if (sdMatcher.find()) {
                val rawSdUrl = sdMatcher.group(1)
                val sdUrl = rawSdUrl.replace("""\/""", "/")
                options.add(StreamOption(id = "fb_sd", qualityLabel = "جودة قياسية SD", format = "mp4", downloadUrl = sdUrl, mediaType = MediaType.VIDEO))
            }

            if (options.isNotEmpty()) {
                Result.success(MediaInfo(originalUrl = url, title = "فيديو Facebook", author = "Facebook Video", platform = PlatformType.FACEBOOK, options = options))
            } else {
                Result.failure(Exception("لم نتمكن من استخراج رابط الفيديو المباشر من Facebook، تأكد أن الفيديو عام"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("خطأ في الاتصال بـ Facebook: ${e.localizedMessage}"))
        }
    }
}
