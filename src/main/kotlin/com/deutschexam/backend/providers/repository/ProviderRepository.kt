package com.deutschexam.backend.providers.repository

import com.deutschexam.backend.db.tables.LevelCatalogTable
import com.deutschexam.backend.db.tables.LevelsTable
import com.deutschexam.backend.db.tables.ProviderLevelsTable
import com.deutschexam.backend.db.tables.ProvidersTable
import com.deutschexam.backend.levels.model.CatalogItemDto
import com.deutschexam.backend.levels.model.LevelDto
import com.deutschexam.backend.providers.model.ProviderDto
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class ProviderRepository(private val db: Database) {

    fun getAllProviders(): List<ProviderDto> = transaction(db) {
        val catalogByLevel = LevelCatalogTable
            .selectAll()
            .groupBy { it[LevelCatalogTable.levelId] }
            .mapValues { (_, rows) ->
                rows.map { CatalogItemDto(it[LevelCatalogTable.type], it[LevelCatalogTable.isFree]) }
            }

        val levelsByProvider = Join(
            table = ProviderLevelsTable,
            otherTable = LevelsTable,
            joinType = JoinType.INNER,
            onColumn = ProviderLevelsTable.levelId,
            otherColumn = LevelsTable.id
        ).selectAll()
            .groupBy { it[ProviderLevelsTable.providerId] }
            .mapValues { (_, rows) ->
                rows.map { row ->
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

        ProvidersTable.selectAll().map { row ->
            val providerId = row[ProvidersTable.id]
            ProviderDto(
                id = providerId,
                name = row[ProvidersTable.name],
                fullName = row[ProvidersTable.fullName],
                description = row[ProvidersTable.description],
                logo = row[ProvidersTable.logo],
                website = row[ProvidersTable.website],
                levels = levelsByProvider[providerId] ?: emptyList()
            )
        }
    }
}
