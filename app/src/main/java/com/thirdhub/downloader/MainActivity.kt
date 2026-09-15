package com.thirdhub.downloader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.thirdhub.downloader.data.Prefs
import com.thirdhub.downloader.ui.LoginScreen
import com.thirdhub.downloader.ui.ProductListScreen
import com.thirdhub.downloader.ui.theme.ThirdHubDownloaderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ThirdHubDownloaderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var isLoggedIn by remember { mutableStateOf(Prefs.accessToken.isNotEmpty()) }

                    if (isLoggedIn) {
                        ProductListScreen(
                            onLogout = {
                                Prefs.clearAuth()
                                isLoggedIn = false
                            }
                        )
                    } else {
                        LoginScreen(
                            onLoginSuccess = { isLoggedIn = true }
                        )
                    }
                }
            }
        }
    }
}
