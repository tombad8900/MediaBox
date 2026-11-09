package com.mediabox.core.extractor

import com.mediabox.core.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class TikTokExtractor : MediaExtractor {
    override val supportedPlatform = PlatformType.TIKTOK

    override fun canHandle(url: String): Boolean {
        return url.lowercase().contains("tiktok.com")
    }

    override suspend fun extract(url: String): Result<MediaInfo> = withContext(Dispatchers.IO) {
        try {
            val apiUrl = "https://www.tikwm.com/api/?url=" + URLEncoder.encode(url, "UTF-8")
            val connection = URL(apiUrl).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")

            val responseText = connection.inputStream.bufferedReader().readText()
            val json = Json { ignoreUnknownKeys = true }
            val root = json.parseToJsonElement(responseText).jsonObject

            if (root["code"]?.jsonPrimitive?.int == 0) {
                val data = root["data"]?.jsonObject ?: return@withContext Result.failure(Exception("استجابة غير مكتملة"))
                val title = data["title"]?.jsonPrimitive?.content ?: "فيديو TikTok"
                val author = data["author"]?.jsonObject?.get("nickname")?.jsonPrimitive?.content ?: "TikTok Creator"
                val cover = data["cover"]?.jsonPrimitive?.content ?: ""
                val videoUrl = data["hdplay"]?.jsonPrimitive?.content ?: data["play"]?.jsonPrimitive?.content ?: ""
                val audioUrl = data["music"]?.jsonPrimitive?.content ?: ""

                val options = mutableListOf<StreamOption>()
                if (videoUrl.isNotEmpty()) {
                    options.add(StreamOption(id = "hd_video", qualityLabel = "فيديو HD (بدون علامة مائية)", format = "mp4", downloadUrl = videoUrl, mediaType = MediaType.VIDEO))
                }
                if (audioUrl.isNotEmpty()) {
                    options.add(StreamOption(id = "audio_mp3", qualityLabel = "صوت MP3", format = "mp3", downloadUrl = audioUrl, mediaType = MediaType.AUDIO))
                }

                Result.success(MediaInfo(originalUrl = url, title = title, author = author, thumbnailUrl = cover, platform = PlatformType.TIKTOK, options = options))
            } else {
                Result.failure(Exception(root["msg"]?.jsonPrimitive?.content ?: "فشل التحليل"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
