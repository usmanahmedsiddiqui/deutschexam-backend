package com.deutschexam.backend

import com.deutschexam.backend.auth.service.ConsoleEmailService
import com.deutschexam.backend.auth.service.OtpService
import com.deutschexam.backend.auth.service.SmtpEmailService
import com.deutschexam.backend.db.DatabaseFactory
import com.deutschexam.backend.plugins.*
import com.deutschexam.backend.util.JwtConfig
import io.ktor.server.application.*
import io.ktor.server.netty.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.Database
import kotlin.time.Duration.Companion.hours

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    val config = environment.config

    JwtConfig.init(config)

    val db = DatabaseFactory.init(config)

    val emailService = buildEmailService(config, db)

    configureAuth()
    configureSerialization()
    configureCORS()
    configureStatusPages()
    configureRouting(db, emailService)

    scheduleOtpCleanup(db)
}

private fun Application.buildEmailService(
    config: io.ktor.server.config.ApplicationConfig,
    db: Database,
): com.deutschexam.backend.auth.service.EmailService {
    val host = config.property("email.host").getString()
    return if (host == "localhost" || host == "mailhog") {
        // Dev mode: print OTPs to console so you can see them without a real email
        ConsoleEmailService()
    } else {
        SmtpEmailService(config)
    }
}

private fun Application.scheduleOtpCleanup(db: Database) {
    val otpService = OtpService(db)
    launch {
        while (true) {
            delay(1.hours)
            try {
                otpService.cleanupExpired()
            } catch (e: Exception) {
                log.warn("OTP cleanup failed: ${e.message}")
            }
        }
    }
}
