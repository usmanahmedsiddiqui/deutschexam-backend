package com.deutschexam.backend.util

import io.ktor.http.*

sealed class ApiException(message: String, val statusCode: HttpStatusCode) : Exception(message)

class AuthException(message: String = "Unauthorized") : ApiException(message, HttpStatusCode.Unauthorized)
class ForbiddenException(message: String = "Forbidden") : ApiException(message, HttpStatusCode.Forbidden)
class NotFoundException(message: String = "Not found") : ApiException(message, HttpStatusCode.NotFound)
class ValidationException(val field: String, message: String) : ApiException(message, HttpStatusCode.UnprocessableEntity)
class ConflictException(message: String) : ApiException(message, HttpStatusCode.Conflict)
