package com.deutschexam.backend.exams.repository

import com.deutschexam.backend.db.tables.ExamsTable
import com.deutschexam.backend.exams.model.ExamDto
import com.deutschexam.backend.levels.repository.LevelRepository
import com.deutschexam.backend.providers.repository.ProviderRepository
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
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

    fun findByProviderAndLevel(providerId: String, levelId: String): List<ExamDto> = transaction(db) {
        ExamsTable.selectAll()
            .where { ExamsTable.providerId eq providerId }
            .andWhere { ExamsTable.levelId eq levelId }
            .mapNotNull { toDto(it) }
    }

    fun findById(id: String): ExamDto? = transaction(db) {
        ExamsTable.selectAll()
            .where { ExamsTable.id eq id }
            .firstOrNull()
            ?.let { toDto(it) }
    }

    private fun toDto(row: ResultRow): ExamDto? {
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
