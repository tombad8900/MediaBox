package com.mediabox.core.extractor
import com.mediabox.core.model.MediaInfo
import com.mediabox.core.model.PlatformType

interface MediaExtractor {
    val supportedPlatform: PlatformType
    fun canHandle(url: String): Boolean
    suspend fun extract(url: String): Result<MediaInfo>
}
