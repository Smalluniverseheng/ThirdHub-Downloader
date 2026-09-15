package com.thirdhub.downloader.data.repository

import com.thirdhub.downloader.data.api.ProductApi
import com.thirdhub.downloader.data.model.Product

class ProductRepository {
    private val api = ProductApi()

    suspend fun getProducts(): Result<List<Product>> {
        return api.getProducts()
    }
}
