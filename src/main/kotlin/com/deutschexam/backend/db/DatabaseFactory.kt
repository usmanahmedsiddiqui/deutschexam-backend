package com.deutschexam.backend.db

import com.deutschexam.backend.db.tables.LevelCatalogTable
import com.deutschexam.backend.db.tables.LevelsTable
import com.deutschexam.backend.db.tables.UserProductsTable
import com.deutschexam.backend.db.tables.UsersTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init(config: ApplicationConfig): Database {
        val jdbcUrl = config.property("database.url").getString()
        val user = config.property("database.user").getString()
        val password = config.property("database.password").getString()
        val poolSize = config.property("database.pool_size").getString().toInt()

        val hikariConfig = HikariConfig().apply {
            this.jdbcUrl = jdbcUrl
            this.username = user
            this.password = password
            this.maximumPoolSize = poolSize
            this.isAutoCommit = false
            this.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }

        val database = Database.connect(HikariDataSource(hikariConfig))

        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(
                UsersTable,
                UserProductsTable,
                LevelsTable,
                LevelCatalogTable,
            )
            seedLevels()
        }

        return database
    }

    private data class CatalogEntry(val type: String, val isFree: Boolean)
    private data class LevelSeed(
        val id: String,
        val name: String,
        val description: String,
        val shortDescription: String,
        val catalog: List<CatalogEntry>
    )

    private val defaultCatalog = listOf(
        CatalogEntry("vocabulary", true),
        CatalogEntry("grammar", false),
        CatalogEntry("sentence_bank", true),
        CatalogEntry("dialogue", true),
        CatalogEntry("writing_practice", true),
        CatalogEntry("speaking_practice", true),
    )

    private val levelsToSeed = listOf(
        LevelSeed("a1", "A1", "Basic German language certificate covering elementary language use.", "Elementary language use", defaultCatalog),
        LevelSeed("a2", "A2", "Elementary German language certificate for everyday communication.", "Everyday communication", defaultCatalog),
        LevelSeed("b1", "B1", "Intermediate German language certificate required for German citizenship and residence permit.", "Independent language use", defaultCatalog),
    )

    private fun seedLevels() {
        val existing = LevelsTable.selectAll().map { it[LevelsTable.id] }.toSet()
        if (existing.isNotEmpty()) return

        for (level in levelsToSeed) {
            LevelsTable.insert {
                it[id] = level.id
                it[name] = level.name
                it[description] = level.description
                it[shortDescription] = level.shortDescription
            }
            for (entry in level.catalog) {
                LevelCatalogTable.insert {
                    it[levelId] = level.id
                    it[type] = entry.type
                    it[isFree] = entry.isFree
                }
            }
        }
    }
}
