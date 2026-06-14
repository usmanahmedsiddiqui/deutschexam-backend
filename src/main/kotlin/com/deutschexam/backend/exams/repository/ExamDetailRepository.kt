package com.deutschexam.backend.exams.repository

import com.deutschexam.backend.db.tables.ExamDetailsTable
import com.deutschexam.backend.exams.model.ExamDetailDto
import com.deutschexam.backend.levels.repository.LevelRepository
import com.deutschexam.backend.providers.repository.ProviderRepository
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class ExamDetailRepository(
    private val db: Database,
    private val providerRepo: ProviderRepository,
    private val levelRepo: LevelRepository,
) {

    fun findByProviderAndLevel(providerId: String, levelId: String): ExamDetailDto? = transaction(db) {
        ExamDetailsTable.selectAll()
            .where { ExamDetailsTable.providerId eq providerId }
            .andWhere { ExamDetailsTable.levelId eq levelId }
            .firstOrNull()
            ?.let { toDto(it) }
    }


    private fun toDto(row: ResultRow): ExamDetailDto? {
        val provider = providerRepo.findById(row[ExamDetailsTable.providerId]) ?: return null
        val level = levelRepo.findById(row[ExamDetailsTable.levelId]) ?: return null
        val blob = Json.parseToJsonElement(row[ExamDetailsTable.data]).jsonObject
        return ExamDetailDto(
            id = row[ExamDetailsTable.id],
            name = row[ExamDetailsTable.name],
            provider = provider,
            level = level,
            totalPoints = row[ExamDetailsTable.totalPoints],
            totalMinutes = row[ExamDetailsTable.totalMinutes],
            passingCriteria = blob["passing_criteria"]!!,
            grading = blob["grading"]!!,
            sections = blob["sections"]!!,
        )
    }
}
