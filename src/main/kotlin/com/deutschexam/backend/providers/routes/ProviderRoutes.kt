package com.deutschexam.backend.providers.routes

import com.deutschexam.backend.providers.model.ProvidersResponseDto
import com.deutschexam.backend.providers.repository.ProviderRepository
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.providerRoutes(providerRepo: ProviderRepository) {
    route("/providers") {
        get {
            val providers = providerRepo.getAllProviders()
            call.respond(HttpStatusCode.OK, ProvidersResponseDto(providers))
        }
    }
}
