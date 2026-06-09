package com.deutschexam.backend.bugs.model

import kotlinx.serialization.Serializable

@Serializable
data class BugReportRequest(
    val title: String,
    val description: String,
)

@Serializable
data class BugReportResponse(
    val id: String,
    val title: String,
    val description: String,
)
