package com.mediabox.core.extractor

import com.mediabox.core.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import java.io.BufferedReader
import java.io.InputStreamReader

class YtDlpExtractor : MediaExtractor {
    override val supportedPlatform = PlatformType.GENERIC

    override fun canHandle(url: String): Boolean = true

    override suspend fun extract(url: String): Result<MediaInfo> = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec(arrayOf("yt-dlp", "--dump-json", "--no-warnings", url.trim()))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val jsonText = reader.readText()
            process.waitFor()

            if (jsonText.isNotBlank()) {
                val json = Json { ignoreUnknownKeys = true }
                val root = json.parseToJsonElement(jsonText).jsonObject

                val title = root["title"]?.jsonPrimitive?.content ?: "فيديو MediaBox"
                val uploader = root["uploader"]?.jsonPrimitive?.content
                    ?: root["channel"]?.jsonPrimitive?.content
                    ?: "MediaBox Creator"
                val thumbnail = root["thumbnail"]?.jsonPrimitive?.content ?: ""
                val directUrl = root["url"]?.jsonPrimitive?.content ?: ""

                val options = mutableListOf<StreamOption>()
                val formatsArr = root["formats"]?.jsonArray

                if (formatsArr != null && formatsArr.isNotEmpty()) {
                    for (element in formatsArr.reversed()) {
                        val fObj = element.jsonObject
                        val fUrl = fObj["url"]?.jsonPrimitive?.content ?: continue
                        val vcodec = fObj["vcodec"]?.jsonPrimitive?.content ?: "none"
                        val formatNote = fObj["format_note"]?.jsonPrimitive?.content
                            ?: fObj["resolution"]?.jsonPrimitive?.content
                            ?: "جودة فيديو"
                        val ext = fObj["ext"]?.jsonPrimitive?.content ?: "mp4"

                        if (vcodec != "none") {
                            options.add(
                                StreamOption(
                                    id = "opt_${options.size}",
                                    qualityLabel = formatNote,
                                    format = ext,
                                    downloadUrl = fUrl,
                                    mediaType = MediaType.VIDEO
                                )
                            )
                        }
                        if (options.size >= 4) break
                    }
                }

                if (options.isEmpty() && directUrl.isNotEmpty()) {
                    options.add(
                        StreamOption(
                            id = "direct_opt",
                            qualityLabel = "فيديو HD مباشر",
                            format = "mp4",
                            downloadUrl = directUrl,
                            mediaType = MediaType.VIDEO
                        )
                    )
                }

                if (options.isNotEmpty()) {
                    val platform = PlatformType.detectFromUrl(url)
                    return@withContext Result.success(
                        MediaInfo(
                            originalUrl = url,
                            title = title,
                            author = uploader,
                            thumbnailUrl = thumbnail,
                            platform = platform,
                            options = options
                        )
                    )
                }
            }
            Result.failure(Exception("تعذر تحليل الفيديو بواسطة المحرك الشامل"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
