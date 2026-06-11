package com.deutschexam.backend.db.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ProductSeed(
    val id: String,
    @SerialName("level_id") val levelId: String,
    @SerialName("price_cents") val priceCents: Int,
    @SerialName("discounted_price_cents") val discountedPriceCents: Int? = null,
    val currency: String,
)