package com.deutschexam.backend.exams.model

import com.deutschexam.backend.levels.model.LevelDto
import com.deutschexam.backend.providers.model.ProviderDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExamDetailSummaryDto(
    val id: String,
    val name: String,
    val provider: ProviderDto,
    val level: LevelDto,
    @SerialName("total_points") val totalPoints: Double,
    @SerialName("total_minutes") val totalMinutes: Int,
)

@Serializable
data class ExamSummaryDto(
    val id: String,
    val name: String,
    @SerialName("exam_detail_id") val examDetailId: String,
    @SerialName("is_free") val isFree: Boolean,
    val provider: ProviderDto,
    val level: LevelDto,
    @SerialName("total_points") val totalPoints: Double,
    @SerialName("total_minutes") val totalMinutes: Int,
)
