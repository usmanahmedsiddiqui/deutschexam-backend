package com.deutschexam.backend.auth.model

import kotlinx.serialization.Serializable

@Serializable
data class GoogleAuthRequest(val idToken: String)

@Serializable
data class LoginResponseDto(
    val token: String?,
    val name: String?,
    val email: String?,
    val phoneNumber: String?,
    val profilePicture: String?,
    val ownedProductIds: List<String> = emptyList(),
)

data class UserRecord(
    val id: String,
    val name: String,
    val email: String,
    val profilePicture: String?,
    val ownedProductIds: List<String>,
)
