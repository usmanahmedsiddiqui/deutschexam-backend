package com.deutschexam.backend.db.tables

import org.jetbrains.exposed.sql.Table

object ExamDetailsTable : Table("exam_details") {
    val id = varchar("id", 50)
    val providerId = varchar("provider_id", 50).references(ProvidersTable.id)
    val levelId = varchar("level_id", 10).references(LevelsTable.id)
    val name = varchar("name", 255)
    val totalPoints = double("total_points")
    val totalMinutes = integer("total_minutes")
    val data = text("data")

    override val primaryKey = PrimaryKey(id)
}
