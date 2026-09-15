package com.thirdhub.downloader.service

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import com.thirdhub.downloader.data.model.DownloadStatus
import com.thirdhub.downloader.data.model.DownloadTask
import com.thirdhub.downloader.data.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class DownloadService(private val context: Context) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val _downloadTasks = MutableStateFlow<Map<String, DownloadTask>>(emptyMap())
    val downloadTasks: StateFlow<Map<String, DownloadTask>> = _downloadTasks

    private val downloadDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)

    suspend fun downloadProduct(product: Product): Flow<DownloadTask> = kotlinx.coroutines.flow.flow {
        val task = DownloadTask(product = product, status = DownloadStatus.DOWNLOADING)
        updateTask(task)

        try {
            val request = Request.Builder()
                .url(product.downloadUrl)
                .build()

            val response = client.newCall(request).execute()
            val body = response.body ?: throw Exception("响应为空")

            if (!response.isSuccessful) {
                throw Exception("下载失败: HTTP ${response.code}")
            }

            val totalBytes = body.contentLength()
            val input = body.byteStream()
            val outputFile = File(downloadDir, "${product.id}-${product.version}.apk")
            val output = FileOutputStream(outputFile)

            val buffer = ByteArray(8192)
            var downloaded = 0L
            var read: Int

            while (input.read(buffer).also { read = it } != -1) {
                output.write(buffer, 0, read)
                downloaded += read
                val progress = if (totalBytes > 0) {
                    ((downloaded * 100) / totalBytes).toInt()
                } else 0
                task.progress = progress
                updateTask(task)
                emit(task)
            }

            output.flush()
            output.close()
            input.close()

            task.status = DownloadStatus.COMPLETED
            task.filePath = outputFile.absolutePath
            updateTask(task)
            emit(task)

        } catch (e: Exception) {
            task.status = DownloadStatus.FAILED
            task.error = e.message ?: "下载失败"
            updateTask(task)
            emit(task)
        }
    }.flowOn(Dispatchers.IO)

    fun installApk(product: Product) {
        val file = File(downloadDir, "${product.id}-${product.version}.apk")
        if (!file.exists()) return

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun canInstallPackage(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else true
    }

    fun getInstallPermissionIntent(): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(
                android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            )
        } else Intent()
    }

    private fun updateTask(task: DownloadTask) {
        val current = _downloadTasks.value.toMutableMap()
        current[task.product.id] = task
        _downloadTasks.value = current
    }

    fun getTask(productId: String): DownloadTask? {
        return _downloadTasks.value[productId]
    }

    fun clearCompletedTasks() {
        val current = _downloadTasks.value.toMutableMap()
        val iterator = current.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.value.status == DownloadStatus.COMPLETED) {
                iterator.remove()
            }
        }
        _downloadTasks.value = current
    }
}
