package com.deutschexam.backend.db.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CatalogEntrySeed(
    val type: String,
    @SerialName("is_free") val isFree: Boolean,
)