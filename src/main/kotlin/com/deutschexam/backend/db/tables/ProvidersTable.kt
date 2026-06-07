package com.deutschexam.backend.db.tables

import org.jetbrains.exposed.sql.Table

object ProvidersTable : Table("providers") {
    val id = varchar("id", 50)
    val name = varchar("name", 100)
    val fullName = varchar("full_name", 255)
    val description = text("description")
    val logo = varchar("logo", 255)
    val website = varchar("website", 255)

    override val primaryKey = PrimaryKey(id)
}

object ProviderLevelsTable : Table("provider_levels") {
    val providerId = varchar("provider_id", 50).references(ProvidersTable.id)
    val levelId = varchar("level_id", 10).references(LevelsTable.id)

    override val primaryKey = PrimaryKey(providerId, levelId)
}
