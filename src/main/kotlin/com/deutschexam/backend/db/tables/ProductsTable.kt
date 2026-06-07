package com.deutschexam.backend.db.tables

import org.jetbrains.exposed.sql.Table

object ProductsTable : Table("products") {
    val id = varchar("id", 20)
    val levelId = varchar("level_id", 10).references(LevelsTable.id)
    val priceCents = integer("price_cents")
    val discountedPriceCents = integer("discounted_price_cents").nullable()
    val currency = varchar("currency", 10)

    override val primaryKey = PrimaryKey(id)
}
