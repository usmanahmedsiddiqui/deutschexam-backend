package com.deutschexam.backend.exams.repository

import com.deutschexam.backend.db.tables.ExamDetailsTable
import com.deutschexam.backend.db.tables.ExamsTable
import com.deutschexam.backend.exams.model.ExamSummaryDto
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class ExamRepository(private val db: Database) {

    fun findByProviderAndLevel(providerId: String, levelId: String): List<JsonElement> = transaction(db) {
        (ExamsTable innerJoin ExamDetailsTable)
            .selectAll()
            .where { ExamsTable.providerId eq providerId }
            .andWhere { ExamsTable.levelId eq levelId }
            .map { row ->
                buildJsonObject {
                    put("id", row[ExamsTable.id])
                    put("exam_detail_id", row[ExamsTable.examDetailId])
                    put("name", row[ExamsTable.name])
                    put("is_free", row[ExamsTable.isFree])
                    put("total_points", row[ExamsTable.totalPoints])
                    put("total_minutes", row[ExamsTable.totalMinutes])
                    put("exam_detail", Json.parseToJsonElement(row[ExamDetailsTable.data]))
                }
            }
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

    fun findById(id: String): JsonElement? = transaction(db) {
        ExamsTable.selectAll()
            .where { ExamsTable.id eq id }
            .firstOrNull()
            ?.let { Json.parseToJsonElement(it[ExamsTable.data]) }
    }
}
