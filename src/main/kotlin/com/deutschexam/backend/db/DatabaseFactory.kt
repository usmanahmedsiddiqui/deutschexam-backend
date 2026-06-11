package com.deutschexam.backend.db

import com.deutschexam.backend.config.AppConfig
import com.deutschexam.backend.db.seed.DataSeeder
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init(config: AppConfig): Database {
        val dataSource = HikariDataSource(HikariConfig().apply {
            this.jdbcUrl = config.databaseUrl
            this.username = config.databaseUser
            this.password = config.databasePassword
            this.maximumPoolSize = config.databasePoolSize
            this.isAutoCommit = false
            this.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        })

        Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .load()
            .migrate()

        val database = Database.connect(dataSource)

        transaction(database) {
            DataSeeder.seedAll()
        }

        return database
    }
}
