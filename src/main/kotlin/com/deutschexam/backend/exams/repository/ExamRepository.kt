package com.deutschexam.backend.exams.repository

import com.deutschexam.backend.db.tables.ExamsTable
import com.deutschexam.backend.exams.model.ExamDto
import com.deutschexam.backend.exams.model.ExamSectionSummaryDto
import com.deutschexam.backend.exams.model.ExamSummaryDto
import com.deutschexam.backend.levels.model.LevelDto
import com.deutschexam.backend.levels.repository.LevelRepository
import com.deutschexam.backend.providers.model.ProviderDto
import com.deutschexam.backend.providers.repository.ProviderRepository
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class ExamRepository(
    private val db: Database,
    private val providerRepo: ProviderRepository,
    private val levelRepo: LevelRepository,
) {

    fun findAll(): List<ExamSummaryDto> = transaction(db) {
        val providers = providerRepo.getAllProviders().associateBy { it.id }
        val levels = levelRepo.getAllLevels().associateBy { it.id }
        ExamsTable.selectAll().mapNotNull { row ->
            val provider = providers[row[ExamsTable.providerId]] ?: return@mapNotNull null
            val level = levels[row[ExamsTable.levelId]] ?: return@mapNotNull null
            toSummaryDto(row, provider, level)
        }
    }

    fun findByProviderAndLevel(providerId: String, levelId: String): List<ExamSummaryDto> = transaction(db) {
        val provider = providerRepo.findById(providerId) ?: return@transaction emptyList()
        val level = levelRepo.findById(levelId) ?: return@transaction emptyList()
        ExamsTable.selectAll()
            .where { ExamsTable.providerId eq providerId }
            .andWhere { ExamsTable.levelId eq levelId }
            .map { toSummaryDto(it, provider, level) }
    }

    fun findById(id: String): ExamDto? = transaction(db) {
        ExamsTable.selectAll()
            .where { ExamsTable.id eq id }
            .firstOrNull()
            ?.let { toFullDto(it) }
    }

    private fun toSummaryDto(row: ResultRow, provider: ProviderDto, level: LevelDto): ExamSummaryDto {
        val blob = Json.parseToJsonElement(row[ExamsTable.data]).jsonObject
        val sections = blob["sections"]?.jsonArray?.map { el ->
            val s = el.jsonObject
            ExamSectionSummaryDto(
                id = s["id"]!!.jsonPrimitive.content,
                name = s["name"]!!.jsonPrimitive.content,
                sectionMinutes = s["section_minutes"]!!.jsonPrimitive.content.toInt(),
                sectionPoints = s["section_points"]!!.jsonPrimitive.content.toDouble(),
            )
        } ?: emptyList()
        return ExamSummaryDto(
            id = row[ExamsTable.id],
            name = row[ExamsTable.name],
            isFree = row[ExamsTable.isFree],
            provider = provider,
            level = level,
            totalPoints = row[ExamsTable.totalPoints],
            totalMinutes = row[ExamsTable.totalMinutes],
            sections = sections,
        )
    }

    private fun toFullDto(row: ResultRow): ExamDto? {
        val provider = providerRepo.findById(row[ExamsTable.providerId]) ?: return null
        val level = levelRepo.findById(row[ExamsTable.levelId]) ?: return null
        val blob = Json.parseToJsonElement(row[ExamsTable.data]).jsonObject
        return ExamDto(
            id = row[ExamsTable.id],
            name = row[ExamsTable.name],
            examDetailId = row[ExamsTable.examDetailId],
            isFree = row[ExamsTable.isFree],
            provider = provider,
            level = level,
            totalPoints = row[ExamsTable.totalPoints],
            totalMinutes = row[ExamsTable.totalMinutes],
            passingCriteria = blob["passing_criteria"]!!,
            grading = blob["grading"]!!,
            sections = blob["sections"]!!,
        )
    }
}
