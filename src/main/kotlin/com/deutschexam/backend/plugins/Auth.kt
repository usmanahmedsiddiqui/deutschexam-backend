package com.deutschexam.backend.plugins

import com.deutschexam.backend.util.JwtConfig
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.http.*
import io.ktor.server.response.*

data class UserPrincipal(val userId: String, val email: String) : Principal

fun Application.configureAuth() {
    install(Authentication) {
        jwt("jwt-auth") {
            verifier(JwtConfig.verifier)
            validate { credential ->
                val userId = credential.payload.subject ?: return@validate null
                val email = credential.payload.getClaim(JwtConfig.CLAIM_EMAIL).asString() ?: return@validate null
                UserPrincipal(userId, email)
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, ApiErrorResponse(code = "UNAUTHORIZED", message = "Token invalid or expired."))
            }
        }
    }
}
