package com.deutschexam.backend.auth.model

import kotlinx.serialization.Serializable

@Serializable
data class GoogleAuthRequest(val idToken: String)

@Serializable
data class AuthResponseDto(
    val token: String,
    val name: String,
    val email: String,
    val pictureUrl: String? = null,
    val ownedProductIds: List<String> = emptyList(),
)

data class UserRecord(
    val id: String,
    val name: String,
    val email: String,
    val pictureUrl: String?,
    val ownedProductIds: List<String>,
)
