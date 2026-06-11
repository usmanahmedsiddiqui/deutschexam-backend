package com.deutschexam.backend.db.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ExamDetailSeed(
    val id: String,
    val name: String,
    @SerialName("provider_id") val providerId: String,
    @SerialName("level_id") val levelId: String,
    @SerialName("total_points") val totalPoints: Double,
    @SerialName("total_minutes") val totalMinutes: Int,
)