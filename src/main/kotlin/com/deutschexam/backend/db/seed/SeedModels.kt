package com.deutschexam.backend.db.seed

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Minimal reference to a nested object that only contributes its id (provider/level in exam files). */
@Serializable
internal data class SeedRef(val id: String)

@Serializable
internal data class CatalogEntrySeed(
    val type: String,
    @SerialName("is_free") val isFree: Boolean,
)

@Serializable
internal data class LevelSeed(
    val id: String,
    val name: String,
    val description: String,
    @SerialName("short_description") val shortDescription: String,
    val catalog: List<CatalogEntrySeed>,
)

@Serializable
internal data class ProviderSeed(
    val id: String,
    val name: String,
    @SerialName("full_name") val fullName: String,
    val description: String,
    val logo: String,
    val website: String,
    @SerialName("level_ids") val levelIds: List<String>,
)

@Serializable
internal data class ProductSeed(
    val id: String,
    @SerialName("level_id") val levelId: String,
    @SerialName("price_cents") val priceCents: Int,
    @SerialName("discounted_price_cents") val discountedPriceCents: Int? = null,
    val currency: String,
)

@Serializable
internal data class ExamDetailSeed(
    val id: String,
    val name: String,
    @SerialName("provider_id") val providerId: String,
    @SerialName("level_id") val levelId: String,
    @SerialName("total_points") val totalPoints: Double,
    @SerialName("total_minutes") val totalMinutes: Int,
)

@Serializable
internal data class ExamSeed(
    val id: String,
    val name: String,
    @SerialName("is_free") val isFree: Boolean,
    @SerialName("provider_id") val providerId: String,
    @SerialName("level_id") val levelId: String,
    @SerialName("total_points") val totalPoints: Double,
    @SerialName("total_minutes") val totalMinutes: Int,
)
