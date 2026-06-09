package com.deutschexam.backend.exams.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExamDetailSummaryDto(
    val id: String,
    val name: String,
    @SerialName("provider_id") val providerId: String,
    @SerialName("level_id") val levelId: String,
    @SerialName("total_points") val totalPoints: Double,
    @SerialName("total_minutes") val totalMinutes: Int,
)

@Serializable
data class ExamSummaryDto(
    val id: String,
    val name: String,
    @SerialName("exam_detail_id") val examDetailId: String,
    @SerialName("is_free") val isFree: Boolean,
    @SerialName("total_points") val totalPoints: Double,
    @SerialName("total_minutes") val totalMinutes: Int,
)
