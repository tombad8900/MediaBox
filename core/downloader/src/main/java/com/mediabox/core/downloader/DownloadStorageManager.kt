package com.mediabox.core.downloader
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File

class DownloadStorageManager(private val context: Context) {
    companion object { const val APP_DOWNLOAD_SUBFOLDER = "MediaBox" }
    fun getMediaBoxDownloadDirectory(): File {
        val appFolder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), APP_DOWNLOAD_SUBFOLDER)
        if (!appFolder.exists()) appFolder.mkdirs()
        return appFolder
    }
    fun createMediaDownloadUri(fileName: String, mimeType: String): android.net.Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/$APP_DOWNLOAD_SUBFOLDER")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Downloads.EXTERNAL_CONTENT_URI else MediaStore.Files.getContentUri("external")
        return context.contentResolver.insert(collection, contentValues)
    }
}
