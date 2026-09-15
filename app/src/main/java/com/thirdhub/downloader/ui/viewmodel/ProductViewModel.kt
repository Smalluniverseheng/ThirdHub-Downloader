package com.thirdhub.downloader.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thirdhub.downloader.data.model.DownloadTask
import com.thirdhub.downloader.data.model.Product
import com.thirdhub.downloader.data.model.User
import com.thirdhub.downloader.data.repository.AuthRepository
import com.thirdhub.downloader.data.repository.ProductRepository
import com.thirdhub.downloader.service.DownloadService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductViewModel(application: Application) : AndroidViewModel(application) {
    private val productRepository = ProductRepository()
    private val authRepository = AuthRepository()
    private val downloadService = DownloadService(application)

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _downloadTasks = MutableStateFlow<Map<String, DownloadTask>>(emptyMap())
    val downloadTasks: StateFlow<Map<String, DownloadTask>> = _downloadTasks

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    init {
        _user.value = authRepository.getCurrentUser()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            productRepository.getProducts()
                .onSuccess { list ->
                    _products.value = list
                }
                .onFailure { e ->
                    // 处理错误
                }
            _isLoading.value = false
        }
    }

    fun downloadProduct(product: Product) {
        viewModelScope.launch {
            downloadService.downloadProduct(product)
                .collect { task ->
                    val current = _downloadTasks.value.toMutableMap()
                    current[product.id] = task
                    _downloadTasks.value = current
                }
        }
    }

    fun installProduct(product: Product) {
        downloadService.installApk(product)
    }

    fun logout() {
        authRepository.logout()
    }
}
