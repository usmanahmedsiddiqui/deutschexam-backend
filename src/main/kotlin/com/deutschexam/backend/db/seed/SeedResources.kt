package com.deutschexam.backend.db.seed

import kotlinx.serialization.json.Json

/** Lenient JSON reader for seed files: exam/detail files carry many more fields than we map. */
internal val seedJson = Json { ignoreUnknownKeys = true }

/** Reads a classpath resource (e.g. "seed/levels.json") as text, or null if missing. */
internal fun loadSeedResource(path: String): String? =
    Thread.currentThread().contextClassLoader.getResourceAsStream(path)?.reader()?.readText()

/** Decodes a JSON-array seed file into a typed list, or empty if the file is missing. */
internal inline fun <reified T> loadSeedList(path: String): List<T> {
    val text = loadSeedResource(path) ?: return emptyList()
    return seedJson.decodeFromString(text)
}
