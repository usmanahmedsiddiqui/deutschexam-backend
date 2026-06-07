package com.deutschexam.backend.products.model

import com.deutschexam.backend.levels.model.LevelDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    val id: String,
    val level: LevelDto,
    @SerialName("price_cents") val priceCents: Int,
    @SerialName("discounted_price_cents") val discountedPriceCents: Int? = null,
    val currency: String
)

@Serializable
data class ProductsResponseDto(
    val products: List<ProductDto>
)

@Serializable
data class BuyProductResponseDto(
    val product: ProductDto
)
