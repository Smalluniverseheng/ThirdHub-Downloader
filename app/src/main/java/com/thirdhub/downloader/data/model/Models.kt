package com.thirdhub.downloader.data.model

data class User(
    val id: String,
    val email: String,
    val nickname: String,
    val avatarUrl: String?
)

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val iconUrl: String,
    val downloadUrl: String,
    val version: String,
    val size: String,
    val type: String, // "apk" or "exe" or "pwa"
    val platform: String, // "android" or "windows" or "web"
    val minSupport: String,
    val changelog: String
)

data class DownloadTask(
    val product: Product,
    var progress: Int = 0,
    var status: DownloadStatus = DownloadStatus.PENDING,
    var filePath: String? = null,
    var error: String? = null
)

enum class DownloadStatus {
    PENDING, DOWNLOADING, PAUSED, COMPLETED, FAILED, INSTALLING
}
