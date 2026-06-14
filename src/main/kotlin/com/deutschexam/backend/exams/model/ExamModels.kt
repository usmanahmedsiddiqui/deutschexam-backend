package com.deutschexam.backend.exams.model

import com.deutschexam.backend.levels.model.LevelDto
import com.deutschexam.backend.providers.model.ProviderDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ExamDetailDto(
    val id: String,
    val name: String,
    val provider: ProviderDto,
    val level: LevelDto,
    @SerialName("total_points") val totalPoints: Double,
    @SerialName("total_minutes") val totalMinutes: Int,
    @SerialName("passing_criteria") val passingCriteria: JsonElement,
    val grading: JsonElement,
    val sections: JsonElement,
)

@Serializable
data class ExamSectionSummaryDto(
    val id: String,
    val name: String,
    @SerialName("section_minutes") val sectionMinutes: Int,
    @SerialName("section_points") val sectionPoints: Double,
)

@Serializable
data class ExamSummaryDto(
    val id: String,
    val name: String,
    @SerialName("is_free") val isFree: Boolean,
    @SerialName("product_id") val productId: String?,
    val provider: ProviderDto,
    val level: LevelDto,
    @SerialName("total_points") val totalPoints: Double,
    @SerialName("total_minutes") val totalMinutes: Int,
    val sections: List<ExamSectionSummaryDto>,
)

@Serializable
data class ExamDto(
    val id: String,
    val name: String,
    @SerialName("exam_detail_id") val examDetailId: String,
    @SerialName("is_free") val isFree: Boolean,
    @SerialName("product_id") val productId: String?,
    val provider: ProviderDto,
    val level: LevelDto,
    @SerialName("total_points") val totalPoints: Double,
    @SerialName("total_minutes") val totalMinutes: Int,
    @SerialName("passing_criteria") val passingCriteria: JsonElement,
    val grading: JsonElement,
    val sections: JsonElement,
)
