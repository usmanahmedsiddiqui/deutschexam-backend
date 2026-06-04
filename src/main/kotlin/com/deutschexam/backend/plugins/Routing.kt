package com.deutschexam.backend.plugins

import com.deutschexam.backend.auth.repository.UserRepository
import com.deutschexam.backend.auth.routes.authRoutes
import com.deutschexam.backend.auth.service.AuthService
import com.deutschexam.backend.auth.service.EmailService
import com.deutschexam.backend.auth.service.OtpService
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database

fun Application.configureRouting(db: Database, emailService: EmailService) {
    val userRepo = UserRepository(db)
    val otpService = OtpService(db)
    val authService = AuthService(userRepo, otpService, emailService)

    routing {
        authRoutes(authService)
    }
}
