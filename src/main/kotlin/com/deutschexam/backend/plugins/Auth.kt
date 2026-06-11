package com.deutschexam.backend.plugins

import com.deutschexam.backend.util.JwtConfig
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.http.*
import io.ktor.server.request.*
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
                val hasAuthHeader = call.request.header(HttpHeaders.Authorization) != null
                val (code, message) = if (hasAuthHeader) {
                    "TOKEN_INVALID" to "Access token is invalid or expired."
                } else {
                    "TOKEN_MISSING" to "Authentication is required to access this resource."
                }
                call.respond(HttpStatusCode.Unauthorized, ApiErrorResponse(code = code, message = message))
            }
        }
    }
}
