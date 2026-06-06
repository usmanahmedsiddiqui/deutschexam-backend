package com.deutschexam.backend.plugins

import com.deutschexam.backend.util.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable

@Serializable
data class ApiErrorResponse(
    val code: String,
    val message: String,
    val details: List<FieldError>? = null,
)

@Serializable
data class FieldError(
    val field: String,
    val message: String,
)

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<ValidationException> { call, cause ->
            call.respond(
                cause.statusCode,
                ApiErrorResponse(
                    code = cause.code,
                    message = cause.message ?: "Validation failed.",
                    details = listOf(FieldError(cause.field, cause.message ?: "")),
                )
            )
        }
        exception<ApiException> { call, cause ->
            call.respond(
                cause.statusCode,
                ApiErrorResponse(code = cause.code, message = cause.message ?: "An error occurred.")
            )
        }
        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ApiErrorResponse(code = "BAD_REQUEST", message = cause.message ?: "Bad request.")
            )
        }
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiErrorResponse(code = "INTERNAL_ERROR", message = "An unexpected error occurred.")
            )
        }
    }
}
