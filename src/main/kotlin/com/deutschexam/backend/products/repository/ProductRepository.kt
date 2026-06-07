package com.deutschexam.backend.products.repository

import com.deutschexam.backend.auth.repository.UserRepository
import com.deutschexam.backend.db.tables.LevelCatalogTable
import com.deutschexam.backend.db.tables.LevelsTable
import com.deutschexam.backend.db.tables.ProductsTable
import com.deutschexam.backend.levels.model.CatalogItemDto
import com.deutschexam.backend.levels.model.LevelDto
import com.deutschexam.backend.products.model.BuyProductResponseDto
import com.deutschexam.backend.products.model.ProductDto
import com.deutschexam.backend.products.model.ProductsResponseDto
import com.deutschexam.backend.util.NotFoundException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class ProductRepository(private val db: Database, private val userRepo: UserRepository) {

    fun getAllProducts(): ProductsResponseDto {
        val products = fetchProducts()
        return ProductsResponseDto(products)
    }

    fun buyProduct(productId: String, userId: String): BuyProductResponseDto {
        val product = fetchProducts().find { it.id == productId }
            ?: throw NotFoundException("Product not found.")

        userRepo.addOwnedProduct(userId, productId)

        return BuyProductResponseDto(product)
    }

    private fun fetchProducts(): List<ProductDto> = transaction(db) {
        val catalogByLevel = LevelCatalogTable
            .selectAll()
            .groupBy { it[LevelCatalogTable.levelId] }
            .mapValues { (_, rows) ->
                rows.map { CatalogItemDto(it[LevelCatalogTable.type], it[LevelCatalogTable.isFree]) }
            }

        Join(
            table = ProductsTable,
            otherTable = LevelsTable,
            joinType = JoinType.INNER,
            onColumn = ProductsTable.levelId,
            otherColumn = LevelsTable.id
        ).selectAll().map { row ->
            val levelId = row[LevelsTable.id]
            ProductDto(
                id = row[ProductsTable.id],
                level = LevelDto(
                    id = levelId,
                    name = row[LevelsTable.name],
                    description = row[LevelsTable.description],
                    shortDescription = row[LevelsTable.shortDescription],
                    catalog = catalogByLevel[levelId] ?: emptyList()
                ),
                priceCents = row[ProductsTable.priceCents],
                discountedPriceCents = row[ProductsTable.discountedPriceCents],
                currency = row[ProductsTable.currency]
            )
        }
    }
}
