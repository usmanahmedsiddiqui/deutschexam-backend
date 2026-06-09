package com.deutschexam.backend.exams.repository

import com.deutschexam.backend.db.tables.ExamsTable
import com.deutschexam.backend.exams.model.ExamSummaryDto
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class ExamRepository(private val db: Database) {

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

    fun findByDetailId(examDetailId: String): List<ExamSummaryDto> = transaction(db) {
        ExamsTable.selectAll()
            .where { ExamsTable.examDetailId eq examDetailId }
            .map { row ->
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
