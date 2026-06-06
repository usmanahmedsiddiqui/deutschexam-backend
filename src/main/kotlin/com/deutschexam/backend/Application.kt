package com.deutschexam.backend

import com.deutschexam.backend.db.DatabaseFactory
import com.deutschexam.backend.plugins.*
import com.deutschexam.backend.util.JwtConfig
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    val config = environment.config

    JwtConfig.init(config)

    val db = DatabaseFactory.init(config)
    val googleClientId = config.property("google.client_id").getString()

    configureAuth()
    configureSerialization()
    configureCORS()
    configureStatusPages()
    configureRouting(db, googleClientId)
}
