package com.example.exchangerates.data.local

import androidx.datastore.core.okio.OkioSerializer
import com.example.exchangerates.domain.entities.RatesInfo
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.BufferedSink
import okio.BufferedSource

object CurrenciesInfoDbSerializer : OkioSerializer<RatesInfo> {
    override val defaultValue = RatesInfo(
        lastUpdated = Instant.DISTANT_PAST,
        lastAttempted = Instant.DISTANT_PAST,
        baseCurrency = "usd",
        currencies = emptyList(),
    )

    override suspend fun readFrom(source: BufferedSource): RatesInfo =
        source
            .readUtf8()
            .let(Json::decodeFromString)

    override suspend fun writeTo(
        item: RatesInfo,
        sink: BufferedSink,
    ) {
        Json
            .encodeToString(item)
            .encodeToByteArray()
            .let(sink::write)
    }
}
