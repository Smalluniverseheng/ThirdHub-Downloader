package com.thirdhub.downloader.data.repository

import com.thirdhub.downloader.data.Prefs
import com.thirdhub.downloader.data.api.AuthApi
import com.thirdhub.downloader.data.model.User

class AuthRepository {
    private val api = AuthApi()

    suspend fun login(email: String, password: String): Result<User> {
        val result = api.login(email, password)
        return result.mapCatching { (token, user) ->
            Prefs.accessToken = token
            Prefs.userId = user.id
            Prefs.userEmail = user.email
            Prefs.userNickname = user.nickname
            Prefs.userAvatar = user.avatarUrl ?: ""
            user
        }
    }

    suspend fun getProfile(): Result<User> {
        val token = Prefs.accessToken
        if (token.isEmpty()) return Result.failure(Exception("未登录"))

        return api.getProfile(token).mapCatching { user ->
            Prefs.userNickname = user.nickname
            Prefs.userAvatar = user.avatarUrl ?: ""
            user
        }
    }

    fun logout() {
        Prefs.clearAuth()
    }

    fun isLoggedIn(): Boolean = Prefs.accessToken.isNotEmpty()

    fun getCurrentUser(): User? {
        return if (isLoggedIn()) {
            User(
                id = Prefs.userId,
                email = Prefs.userEmail,
                nickname = Prefs.userNickname,
                avatarUrl = Prefs.userAvatar.ifEmpty { null }
            )
        } else null
    }
}
