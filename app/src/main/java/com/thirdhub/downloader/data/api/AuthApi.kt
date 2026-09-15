package com.thirdhub.downloader.data.api

import com.thirdhub.downloader.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AuthApi {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val baseUrl = "https://mxvxlgjzeboktufumxbp.supabase.co"
    private val anonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im14dnhsZ2p6ZWJva3R1ZnVteGJwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODQzODM5OTcsImV4cCI6MjA5OTU5OTl9.4bq3dzkf-qtBaNkd1elL83KEkGgKJA2MYh5Q4qVSVgM"

    suspend fun login(email: String, password: String): Result<Pair<String, User>> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("email", email)
                put("password", password)
            }

            val body = json.toString()
                .toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("$baseUrl/auth/v1/token?grant_type=password")
                .post(body)
                .addHeader("apikey", anonKey)
                .addHeader("Content-Type", "application/json")
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val jsonResponse = JSONObject(responseBody)
                val accessToken = jsonResponse.getString("access_token")
                val userJson = jsonResponse.getJSONObject("user")

                val user = User(
                    id = userJson.getString("id"),
                    email = userJson.optString("email", ""),
                    nickname = userJson.optJSONObject("user_metadata")?.optString("nickname", "") ?: "",
                    avatarUrl = userJson.optJSONObject("user_metadata")?.optString("avatar_url", null)
                )

                Result.success(accessToken to user)
            } else {
                val errorJson = JSONObject(responseBody)
                val errorMsg = errorJson.optString("error_description", "登录失败")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProfile(accessToken: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/auth/v1/user")
                .get()
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $accessToken")
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val jsonResponse = JSONObject(responseBody)
                val user = User(
                    id = jsonResponse.getString("id"),
                    email = jsonResponse.optString("email", ""),
                    nickname = jsonResponse.optJSONObject("user_metadata")?.optString("nickname", "") ?: "",
                    avatarUrl = jsonResponse.optJSONObject("user_metadata")?.optString("avatar_url", null)
                )
                Result.success(user)
            } else {
                Result.failure(Exception("获取用户信息失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
