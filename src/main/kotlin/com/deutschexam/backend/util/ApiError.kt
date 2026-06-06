package com.deutschexam.backend.util

import io.ktor.http.*

sealed class ApiException(
    val code: String,
    message: String,
    val statusCode: HttpStatusCode,
) : Exception(message)

class AuthException(message: String = "Unauthorized") :
    ApiException("UNAUTHORIZED", message, HttpStatusCode.Unauthorized)

class ForbiddenException(message: String = "Forbidden") :
    ApiException("FORBIDDEN", message, HttpStatusCode.Forbidden)

class NotFoundException(message: String = "Not found") :
    ApiException("NOT_FOUND", message, HttpStatusCode.NotFound)

class ConflictException(message: String) :
    ApiException("CONFLICT", message, HttpStatusCode.Conflict)

class ValidationException(val field: String, message: String) :
    ApiException("VALIDATION_ERROR", message, HttpStatusCode.UnprocessableEntity)
