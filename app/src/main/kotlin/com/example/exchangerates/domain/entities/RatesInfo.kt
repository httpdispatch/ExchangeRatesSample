package com.example.exchangerates.domain.entities

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RatesInfo(
    @SerialName("lastUpdated")
    val lastUpdated: Instant,
    @SerialName("lastAttempted")
    val lastAttempted: Instant,
    @SerialName("baseCurrency")
    val baseCurrency: CurrencyCode,
    @SerialName("currencies")
    val currencies: List<CurrencyInfo>,
)

typealias CurrencyCode = String
typealias CurrencyRate = Double

@Serializable
sealed class CurrencyInfo {
    abstract val currency: CurrencyCode

    @Serializable
    data class Added(
        @SerialName("currency")
        override val currency: CurrencyCode,
    ) : CurrencyInfo()

    @Serializable
    data class NotFound(
        @SerialName("currency")
        override val currency: CurrencyCode,
    ) : CurrencyInfo()

    @Serializable
    data class Rate(
        @SerialName("currency")
        override val currency: CurrencyCode,
        @SerialName("value")
        val value: CurrencyRate,
    ) : CurrencyInfo()
}
