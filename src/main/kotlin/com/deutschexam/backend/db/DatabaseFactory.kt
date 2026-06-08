package com.deutschexam.backend.db

import com.deutschexam.backend.db.tables.LevelCatalogTable
import com.deutschexam.backend.db.tables.LevelsTable
import com.deutschexam.backend.db.tables.ProviderLevelsTable
import com.deutschexam.backend.db.tables.ProvidersTable
import com.deutschexam.backend.db.tables.ProductsTable
import com.deutschexam.backend.db.tables.RefreshTokensTable
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
                ProvidersTable,
                ProviderLevelsTable,
                ProductsTable,
                RefreshTokensTable,
            )
            seedLevels()
            seedProviders()
            seedProducts()
        }

        return database
    }

    // ── Levels seed ──────────────────────────────────────────────────────────

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

    // ── Providers seed ───────────────────────────────────────────────────────

    private data class ProviderSeed(
        val id: String,
        val name: String,
        val fullName: String,
        val description: String,
        val logo: String,
        val website: String,
        val levelIds: List<String>
    )

    private val providersToSeed = listOf(
        ProviderSeed(
            id = "telc",
            name = "Telc",
            fullName = "Telc GmbH (The European Language Certificates)",
            description = "Telc language tests are recognized internationally and test German language skills at various CEFR levels.",
            logo = "assets/logos/telc.png",
            website = "https://www.telc.net",
            levelIds = listOf("a1", "a2", "b1")
        ),
        ProviderSeed(
            id = "goethe",
            name = "Goethe-Institut",
            fullName = "Goethe-Institut e.V.",
            description = "The Goethe-Institut is the Federal Republic of Germany's cultural institution, offering standardized German language exams worldwide.",
            logo = "assets/logos/goethe.png",
            website = "https://www.goethe.de",
            levelIds = listOf("a1", "a2", "b1")
        ),
    )

    private fun seedProviders() {
        val existing = ProvidersTable.selectAll().map { it[ProvidersTable.id] }.toSet()
        if (existing.isNotEmpty()) return

        for (provider in providersToSeed) {
            ProvidersTable.insert {
                it[id] = provider.id
                it[name] = provider.name
                it[fullName] = provider.fullName
                it[description] = provider.description
                it[logo] = provider.logo
                it[website] = provider.website
            }
            for (levelId in provider.levelIds) {
                ProviderLevelsTable.insert {
                    it[providerId] = provider.id
                    it[ProviderLevelsTable.levelId] = levelId
                }
            }
        }
    }

    // ── Products seed ────────────────────────────────────────────────────────

    private data class ProductSeed(
        val id: String,
        val levelId: String,
        val priceCents: Int,
        val discountedPriceCents: Int?,
        val currency: String
    )

    private val productsToSeed = listOf(
        ProductSeed("p_a1", "a1", 1999, null, "EUR"),
        ProductSeed("p_a2", "a2", 1999, 1499, "EUR"),
        ProductSeed("p_b1", "b1", 1999, null, "EUR"),
    )

    private fun seedProducts() {
        val existing = ProductsTable.selectAll().map { it[ProductsTable.id] }.toSet()
        if (existing.isNotEmpty()) return

        for (product in productsToSeed) {
            ProductsTable.insert {
                it[id] = product.id
                it[levelId] = product.levelId
                it[priceCents] = product.priceCents
                it[discountedPriceCents] = product.discountedPriceCents
                it[currency] = product.currency
            }
        }
    }
}
