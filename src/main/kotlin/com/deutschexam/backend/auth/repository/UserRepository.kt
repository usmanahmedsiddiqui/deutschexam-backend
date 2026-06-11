package com.deutschexam.backend.auth.repository

import com.deutschexam.backend.auth.model.UserRecord
import com.deutschexam.backend.db.tables.UsersTable
import com.deutschexam.backend.products.repository.UserProductRepository
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.UUID

class UserRepository(
    private val db: Database,
    private val userProductRepo: UserProductRepository,
) {

    fun findById(id: String): UserRecord? = transaction(db) {
        UsersTable.selectAll()
            .where { UsersTable.id eq UUID.fromString(id) }
            .firstOrNull()
            ?.toUserRecord()
    }

    fun findOrCreate(googleId: String, email: String, name: String, profilePicture: String?): UserRecord = transaction(db) {
        val existing = UsersTable.selectAll()
            .where { UsersTable.googleId eq googleId }
            .firstOrNull()

        val userId = if (existing != null) {
            UsersTable.update({ UsersTable.id eq existing[UsersTable.id] }) {
                it[UsersTable.name] = name
                it[UsersTable.profilePicture] = profilePicture
            }
            existing[UsersTable.id]
        } else {
            UsersTable.insert {
                it[UsersTable.googleId] = googleId
                it[UsersTable.name] = name
                it[UsersTable.email] = email
                it[UsersTable.profilePicture] = profilePicture
            }[UsersTable.id]
        }

        UsersTable.selectAll()
            .where { UsersTable.id eq userId }
            .first()
            .toUserRecord()
    }

    private fun ResultRow.toUserRecord(): UserRecord {
        val userId = this[UsersTable.id].toString()
        return UserRecord(
            id = userId,
            name = this[UsersTable.name],
            email = this[UsersTable.email],
            profilePicture = this[UsersTable.profilePicture],
            ownedProductIds = userProductRepo.getOwnedProductIds(userId),
        )
    }
}
