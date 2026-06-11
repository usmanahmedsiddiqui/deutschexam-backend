package com.deutschexam.backend.db.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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