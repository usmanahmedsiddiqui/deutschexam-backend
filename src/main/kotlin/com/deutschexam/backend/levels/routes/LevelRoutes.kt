package com.deutschexam.backend.levels.routes

import com.deutschexam.backend.levels.model.LevelsResponseDto
import com.deutschexam.backend.levels.repository.LevelRepository
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.levelRoutes(levelRepo: LevelRepository) {
    route("/levels") {
        get {
            val levels = levelRepo.getAllLevels()
            call.respond(HttpStatusCode.OK, LevelsResponseDto(levels))
        }
    }
}
