package com.deutschexam.backend.auth.routes

import com.deutschexam.backend.auth.model.GoogleAuthRequest
import com.deutschexam.backend.auth.service.AuthService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(authService: AuthService) {
    route("/auth") {
        post("/google") {
            val req = call.receive<GoogleAuthRequest>()
            val response = authService.googleSignIn(req)
            call.respond(HttpStatusCode.OK, response)
        }
    }
}
