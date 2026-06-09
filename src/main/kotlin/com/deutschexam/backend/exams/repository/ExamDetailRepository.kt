package com.deutschexam.backend.exams.repository

import com.deutschexam.backend.db.tables.ExamDetailsTable
import com.deutschexam.backend.exams.model.ExamDetailSummaryDto
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class ExamDetailRepository(private val db: Database) {

    fun findAll(): List<ExamDetailSummaryDto> = transaction(db) {
        ExamDetailsTable.selectAll().map { row ->
            ExamDetailSummaryDto(
                id = row[ExamDetailsTable.id],
                name = row[ExamDetailsTable.name],
                providerId = row[ExamDetailsTable.providerId],
                levelId = row[ExamDetailsTable.levelId],
                totalPoints = row[ExamDetailsTable.totalPoints],
                totalMinutes = row[ExamDetailsTable.totalMinutes],
            )
        }
    }

    fun findByProviderAndLevel(providerId: String, levelId: String): JsonElement? = transaction(db) {
        ExamDetailsTable.selectAll()
            .where { ExamDetailsTable.providerId eq providerId }
            .andWhere { ExamDetailsTable.levelId eq levelId }
            .firstOrNull()
            ?.let { Json.parseToJsonElement(it[ExamDetailsTable.data]) }
    }

    fun findById(id: String): JsonElement? = transaction(db) {
        ExamDetailsTable.selectAll()
            .where { ExamDetailsTable.id eq id }
            .firstOrNull()
            ?.let { Json.parseToJsonElement(it[ExamDetailsTable.data]) }
    }
}
