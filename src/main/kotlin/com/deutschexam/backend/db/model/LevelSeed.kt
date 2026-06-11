package com.deutschexam.backend.db.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class LevelSeed(
    val id: String,
    val name: String,
    val description: String,
    @SerialName("short_description") val shortDescription: String,
    val catalog: List<CatalogEntrySeed>,
)