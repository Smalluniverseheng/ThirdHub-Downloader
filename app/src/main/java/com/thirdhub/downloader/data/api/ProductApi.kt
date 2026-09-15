package com.thirdhub.downloader.data.api

import com.thirdhub.downloader.data.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ProductApi {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val manifestUrl = "https://thirdhub.pages.dev/app-versions.json"

    suspend fun getProducts(): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(manifestUrl)
                .get()
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val json = JSONObject(responseBody)
                val products = mutableListOf<Product>()

                // Android 客户端
                json.optJSONObject("clients")?.optJSONObject("android")?.let { android ->
                    products.add(Product(
                        id = "thirdhub-android",
                        name = "ThirdHub Android",
                        description = "原生安卓客户端（轻壳版）",
                        iconUrl = "https://thirdhub.pages.dev/icons/android.png",
                        downloadUrl = absolutize(android.optString("url")),
                        version = android.optString("version"),
                        size = formatSize(android.optLong("size", 0)),
                        type = "apk",
                        platform = "android",
                        minSupport = android.optString("minSupport", "Android 7.0+"),
                        changelog = android.optString("changelog", "")
                    ))
                }

                // Flutter 完全体
                json.optJSONObject("clients")?.optJSONObject("flutter")?.let { flutter ->
                    products.add(Product(
                        id = "thirdhub-flutter",
                        name = "ThirdHub 完全体",
                        description = "Flutter 完全体客户端（全功能）",
                        iconUrl = "https://thirdhub.pages.dev/icons/flutter.png",
                        downloadUrl = absolutize(flutter.optString("url")),
                        version = flutter.optString("version"),
                        size = formatSize(flutter.optLong("size", 0)),
                        type = "apk",
                        platform = "android",
                        minSupport = flutter.optString("minSupport", "Android 7.0+"),
                        changelog = flutter.optString("changelog", "")
                    ))
                }

                // Windows 客户端
                json.optJSONObject("clients")?.optJSONObject("windows")?.let { windows ->
                    products.add(Product(
                        id = "thirdhub-windows",
                        name = "ThirdHub Windows",
                        description = "Windows 桌面客户端",
                        iconUrl = "https://thirdhub.pages.dev/icons/windows.png",
                        downloadUrl = absolutize(windows.optString("url")),
                        version = windows.optString("version"),
                        size = formatSize(windows.optLong("size", 0)),
                        type = "exe",
                        platform = "windows",
                        minSupport = windows.optString("minSupport", "Windows 10+"),
                        changelog = windows.optString("changelog", "")
                    ))
                }

                // OmniHub（只看漫画）
                json.optJSONObject("clients")?.optJSONObject("omnihub")?.let { omnihub ->
                    products.add(Product(
                        id = "omnihub",
                        name = "OmniHub 只看漫画",
                        description = "第二代漫画稳定版 · 只看漫画",
                        iconUrl = "https://thirdhub.pages.dev/icons/omnihub.png",
                        downloadUrl = omnihub.optString("url"),
                        version = omnihub.optString("version"),
                        size = formatSize(omnihub.optLong("size", 0)),
                        type = "apk",
                        platform = "android",
                        minSupport = omnihub.optString("minSupport", "Android 7.0+"),
                        changelog = omnihub.optString("changelog", "")
                    ))
                }

                // PWA 网页版
                json.optString("web").let { webVersion ->
                    products.add(Product(
                        id = "thirdhub-pwa",
                        name = "ThirdHub PWA",
                        description = "网页版（添加到主屏幕即可使用）",
                        iconUrl = "https://thirdhub.pages.dev/icons/pwa.png",
                        downloadUrl = "https://thirdhub.pages.dev",
                        version = webVersion,
                        size = "在线使用",
                        type = "pwa",
                        platform = "web",
                        minSupport = "任意现代浏览器",
                        changelog = ""
                    ))
                }

                Result.success(products)
            } else {
                Result.failure(Exception("获取产品列表失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun absolutize(url: String): String {
        return if (url.startsWith("http://") || url.startsWith("https://")) {
            url
        } else {
            "https://thirdhub.pages.dev/" + url.trimStart('/')
        }
    }

    private fun formatSize(bytes: Long): String {
        if (bytes <= 0) return "未知"
        val mb = bytes / (1024.0 * 1024.0)
        return if (mb >= 1024) {
            String.format("%.1f GB", mb / 1024.0)
        } else {
            String.format("%.1f MB", mb)
        }
    }
}
