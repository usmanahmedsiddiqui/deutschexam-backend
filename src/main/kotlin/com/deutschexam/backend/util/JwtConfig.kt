package com.deutschexam.backend.util

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.deutschexam.backend.config.AppConfig
import java.util.*

object JwtConfig {
    private lateinit var algorithm: Algorithm
    lateinit var verifier: JWTVerifier
    lateinit var issuer: String
    private var expiryMs: Long = 24 * 60 * 60 * 1000L

    const val CLAIM_EMAIL = "email"

    fun init(config: AppConfig) {
        issuer = config.jwtIssuer
        expiryMs = config.jwtExpiryHours * 60 * 60 * 1000L

        algorithm = Algorithm.HMAC256(config.jwtSecret)
        verifier = JWT.require(algorithm)
            .withIssuer(issuer)
            .build()
    }

    fun generateToken(userId: String, email: String): String =
        JWT.create()
            .withIssuer(issuer)
            .withSubject(userId)
            .withClaim(CLAIM_EMAIL, email)
            .withIssuedAt(Date())
            .withExpiresAt(Date(System.currentTimeMillis() + expiryMs))
            .sign(algorithm)

    fun tokenExpiresAt(): Long = (System.currentTimeMillis() + expiryMs) / 1000
}
