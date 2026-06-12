package com.deutschexam.backend.providers.model

import com.deutschexam.backend.levels.model.LevelDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProviderDto(
    val id: String,
    val name: String,
    @SerialName("full_name") val fullName: String,
    val description: String,
    val logo: String,
    val website: String,
    val levels: List<LevelDto>
)

@Serializable
data class ProvidersResponseDto(
    val providers: List<ProviderDto>
)
