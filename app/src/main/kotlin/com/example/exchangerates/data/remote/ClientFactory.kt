package com.example.exchangerates.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createClient(engine: HttpClientEngine) =
    HttpClient(engine = engine) {
        expectSuccess = false
        install(ContentNegotiation) {
            json(
                Json {
                    coerceInputValues = true
                    ignoreUnknownKeys = true
                    explicitNulls = false
                },
            )
        }
    }
