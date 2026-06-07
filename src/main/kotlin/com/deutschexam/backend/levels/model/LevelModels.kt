package com.deutschexam.backend.levels.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CatalogItemDto(
    val type: String,
    @SerialName("is_free") val isFree: Boolean
)

@Serializable
data class LevelDto(
    val id: String,
    val name: String,
    val description: String,
    @SerialName("short_description") val shortDescription: String,
    val catalog: List<CatalogItemDto>
)

@Serializable
data class LevelsResponseDto(
    val levels: List<LevelDto>
)
