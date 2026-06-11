package com.deutschexam.backend.exams.repository

import com.deutschexam.backend.db.tables.ExamsTable
import com.deutschexam.backend.exams.model.ExamSummaryDto
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

data class ExamResult(
    val isFree: Boolean,
    val levelId: String,
    val providerId: String,
    val data: JsonElement,
)

class ExamRepository(private val db: Database) {

    fun findByProviderAndLevel(providerId: String, levelId: String): List<JsonElement> = transaction(db) {
        ExamsTable.selectAll()
            .where { ExamsTable.providerId eq providerId }
            .andWhere { ExamsTable.levelId eq levelId }
            .map { Json.parseToJsonElement(it[ExamsTable.data]) }
    }

    fun findAll(): List<ExamSummaryDto> = transaction(db) {
        ExamsTable.selectAll().map { row ->
            ExamSummaryDto(
                id = row[ExamsTable.id],
                name = row[ExamsTable.name],
                examDetailId = row[ExamsTable.examDetailId],
                isFree = row[ExamsTable.isFree],
                totalPoints = row[ExamsTable.totalPoints],
                totalMinutes = row[ExamsTable.totalMinutes],
            )
        }
    }

    fun findById(id: String): ExamResult? = transaction(db) {
        ExamsTable.selectAll()
            .where { ExamsTable.id eq id }
            .firstOrNull()
            ?.let {
                ExamResult(
                    isFree = it[ExamsTable.isFree],
                    levelId = it[ExamsTable.levelId],
                    providerId = it[ExamsTable.providerId],
                    data = Json.parseToJsonElement(it[ExamsTable.data]),
                )
            }
    }
}
