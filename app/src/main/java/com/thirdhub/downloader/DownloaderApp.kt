package com.thirdhub.downloader

import android.app.Application
import com.thirdhub.downloader.data.Prefs

class DownloaderApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Prefs.init(this)
    }
}
