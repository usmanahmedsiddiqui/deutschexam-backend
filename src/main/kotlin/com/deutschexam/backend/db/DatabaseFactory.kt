package com.deutschexam.backend.db

import com.deutschexam.backend.config.AppConfig
import com.deutschexam.backend.db.seed.DataSeeder
import com.deutschexam.backend.db.tables.BugsTable
import com.deutschexam.backend.db.tables.ExamDetailsTable
import com.deutschexam.backend.db.tables.ExamsTable
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
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init(config: AppConfig): Database {
        val hikariConfig = HikariConfig().apply {
            this.jdbcUrl = config.databaseUrl
            this.username = config.databaseUser
            this.password = config.databasePassword
            this.maximumPoolSize = config.databasePoolSize
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
                ExamDetailsTable,
                ExamsTable,
                BugsTable,
            )
            DataSeeder.seedAll()
        }

        return database
    }
}
