package com.deutschexam.backend.db.tables

import org.jetbrains.exposed.sql.Table

object LevelsTable : Table("levels") {
    val id = varchar("id", 10)
    val name = varchar("name", 10)
    val description = text("description")
    val shortDescription = varchar("short_description", 255)

    override val primaryKey = PrimaryKey(id)
}

object LevelCatalogTable : Table("level_catalog") {
    val id = uuid("id").autoGenerate()
    val levelId = varchar("level_id", 10).references(LevelsTable.id)
    val type = varchar("type", 50)
    val isFree = bool("is_free")

    override val primaryKey = PrimaryKey(id)
}
