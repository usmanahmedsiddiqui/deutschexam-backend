package com.deutschexam.backend.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.callid.*
import java.util.UUID

fun Application.configureCallId() {
    install(CallId) {
        generate { UUID.randomUUID().toString() }
        replyToHeader("X-Request-Id")
    }
}
