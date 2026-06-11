package com.deutschexam.backend.db.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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