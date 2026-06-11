package com.deutschexam.backend.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.bodylimit.*

private const val MAX_BODY_BYTES = 64 * 1024L // 64 KB

fun Application.configureBodyLimit() {
    install(RequestBodyLimit) {
        bodyLimit { MAX_BODY_BYTES }
    }
}
