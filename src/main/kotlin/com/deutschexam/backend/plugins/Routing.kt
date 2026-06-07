package com.deutschexam.backend.plugins

import com.deutschexam.backend.auth.repository.UserRepository
import com.deutschexam.backend.auth.routes.authRoutes
import com.deutschexam.backend.auth.service.AuthService
import com.deutschexam.backend.levels.repository.LevelRepository
import com.deutschexam.backend.levels.routes.levelRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database

fun Application.configureRouting(db: Database, googleClientId: String) {
    val userRepo = UserRepository(db)
    val authService = AuthService(userRepo, googleClientId)
    val levelRepo = LevelRepository(db)

    routing {
        authRoutes(authService)
        levelRoutes(levelRepo)
    }
}
