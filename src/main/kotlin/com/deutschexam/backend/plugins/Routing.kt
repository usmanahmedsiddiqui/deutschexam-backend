package com.deutschexam.backend.plugins

import com.deutschexam.backend.auth.repository.UserRepository
import com.deutschexam.backend.auth.routes.authRoutes
import com.deutschexam.backend.auth.service.AuthService
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database

fun Application.configureRouting(db: Database, googleClientId: String) {
    val userRepo = UserRepository(db)
    val authService = AuthService(userRepo, googleClientId)

    routing {
        authRoutes(authService)
    }
}
