package com.example.exchangerates.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CurrenciesInfoDto(
    @SerialName("base")
    val base: String,
    @SerialName("rates")
    val rates: Map<String, Double>,
)
