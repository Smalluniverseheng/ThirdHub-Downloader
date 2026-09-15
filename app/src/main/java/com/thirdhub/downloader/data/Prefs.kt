package com.thirdhub.downloader.data

import android.content.Context
import android.content.SharedPreferences

object Prefs {
    private lateinit var sp: SharedPreferences

    fun init(ctx: Context) {
        sp = ctx.getSharedPreferences("thirdhub_downloader", Context.MODE_PRIVATE)
    }

    var accessToken: String
        get() = sp.getString("access_token", "") ?: ""
        set(v) = sp.edit().putString("access_token", v).apply()

    var refreshToken: String
        get() = sp.getString("refresh_token", "") ?: ""
        set(v) = sp.edit().putString("refresh_token", v).apply()

    var userId: String
        get() = sp.getString("user_id", "") ?: ""
        set(v) = sp.edit().putString("user_id", v).apply()

    var userEmail: String
        get() = sp.getString("user_email", "") ?: ""
        set(v) = sp.edit().putString("user_email", v).apply()

    var userNickname: String
        get() = sp.getString("user_nickname", "") ?: ""
        set(v) = sp.edit().putString("user_nickname", v).apply()

    var userAvatar: String
        get() = sp.getString("user_avatar", "") ?: ""
        set(v) = sp.edit().putString("user_avatar", v).apply()

    fun clearAuth() {
        sp.edit()
            .remove("access_token")
            .remove("refresh_token")
            .remove("user_id")
            .remove("user_email")
            .remove("user_nickname")
            .remove("user_avatar")
            .apply()
    }
}
