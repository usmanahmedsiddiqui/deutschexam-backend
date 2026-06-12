package com.deutschexam.backend.levels.repository

import com.deutschexam.backend.db.tables.LevelCatalogTable
import com.deutschexam.backend.db.tables.LevelsTable
import com.deutschexam.backend.levels.model.CatalogItemDto
import com.deutschexam.backend.levels.model.LevelDto
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class LevelRepository(private val db: Database) {

    fun getAllLevels(): List<LevelDto> = transaction(db) {
        val catalogByLevel = LevelCatalogTable
            .selectAll()
            .groupBy { it[LevelCatalogTable.levelId] }
            .mapValues { (_, rows) ->
                rows.map { CatalogItemDto(it[LevelCatalogTable.type], it[LevelCatalogTable.isFree]) }
            }

        LevelsTable.selectAll().map { row ->
            val levelId = row[LevelsTable.id]
            LevelDto(
                id = levelId,
                name = row[LevelsTable.name],
                description = row[LevelsTable.description],
                shortDescription = row[LevelsTable.shortDescription],
                catalog = catalogByLevel[levelId] ?: emptyList()
            )
        }
    }

    fun findById(id: String): LevelDto? = getAllLevels().firstOrNull { it.id == id }
}
