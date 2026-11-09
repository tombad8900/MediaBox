package com.mediabox.core.model
import kotlinx.serialization.Serializable

enum class MediaType { VIDEO, AUDIO, IMAGE }

@Serializable
data class StreamOption(val id: String, val qualityLabel: String, val format: String, val downloadUrl: String, val sizeBytes: Long = 0L, val mediaType: MediaType = MediaType.VIDEO)

@Serializable
data class MediaInfo(val originalUrl: String, val title: String, val author: String = "", val thumbnailUrl: String = "", val durationSeconds: Long = 0L, val platform: PlatformType = PlatformType.GENERIC, val options: List<StreamOption> = emptyList())
