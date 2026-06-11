package com.deutschexam.backend.util

import io.ktor.http.*

sealed class ApiException(
    val code: String,
    message: String,
    val statusCode: HttpStatusCode,
) : Exception(message)

class AuthException(message: String = "Unauthorized", code: String = "UNAUTHORIZED") :
    ApiException(code, message, HttpStatusCode.Unauthorized)

class ForbiddenException(message: String = "Forbidden", code: String = "FORBIDDEN") :
    ApiException(code, message, HttpStatusCode.Forbidden)

class NotFoundException(message: String = "Not found", code: String = "NOT_FOUND") :
    ApiException(code, message, HttpStatusCode.NotFound)

class ConflictException(message: String, code: String = "CONFLICT") :
    ApiException(code, message, HttpStatusCode.Conflict)

class ValidationException(message: String) :
    ApiException("VALIDATION_ERROR", message, HttpStatusCode.UnprocessableEntity)
