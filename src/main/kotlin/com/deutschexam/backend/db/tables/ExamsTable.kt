package com.deutschexam.backend.db.tables

import org.jetbrains.exposed.sql.Table

object ExamsTable : Table("exams") {
    val id = varchar("id", 50)
    val examDetailId = varchar("exam_detail_id", 50).references(ExamDetailsTable.id)
    val levelId = varchar("level_id", 10).references(LevelsTable.id)
    val providerId = varchar("provider_id", 50).references(ProvidersTable.id)
    val name = varchar("name", 255)
    val isFree = bool("is_free").default(false)
    val totalPoints = double("total_points")
    val totalMinutes = integer("total_minutes")
    val data = text("data")

    override val primaryKey = PrimaryKey(id)
}
