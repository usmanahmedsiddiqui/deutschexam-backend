package com.deutschexam.backend.util

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.config.*
import java.util.*

object JwtConfig {
    private lateinit var algorithm: Algorithm
    lateinit var verifier: JWTVerifier
    lateinit var issuer: String
    private var expiryMs: Long = 24 * 60 * 60 * 1000L

    const val CLAIM_USER_ID = "sub"
    const val CLAIM_EMAIL = "email"

    fun init(config: ApplicationConfig) {
        val secret = config.property("jwt.secret").getString()
        issuer = config.property("jwt.issuer").getString()
        val expiryHours = config.property("jwt.expiry_hours").getString().toLong()
        expiryMs = expiryHours * 60 * 60 * 1000L

        algorithm = Algorithm.HMAC256(secret)
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
}
