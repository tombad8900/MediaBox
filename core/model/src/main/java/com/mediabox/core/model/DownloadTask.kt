package com.mediabox.core.model
import kotlinx.serialization.Serializable

enum class DownloadState { PENDING, DOWNLOADING, PAUSED, COMPLETED, FAILED }

@Serializable
data class DownloadTask(val id: String, val title: String, val downloadUrl: String, val targetFileName: String, val platform: PlatformType, val qualityLabel: String, val format: String, val totalBytes: Long = 0L, val downloadedBytes: Long = 0L, val progressPercent: Int = 0, val state: DownloadState = DownloadState.PENDING, val errorMessage: String? = null, val relativeDestinationPath: String = "Download/MediaBox")
