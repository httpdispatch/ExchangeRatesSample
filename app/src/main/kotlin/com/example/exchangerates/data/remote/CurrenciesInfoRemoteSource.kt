package com.example.exchangerates.data.remote

import arrow.core.raise.Raise
import com.example.exchangerates.common.error.LoadError
import com.example.exchangerates.common.network.ktor.eitherGet
import io.ktor.client.HttpClient

internal const val API_ENDPOINT = "https://some.api.url/api"
internal const val BASE_CURRENCY_PARAM = "base"
internal const val CURRENCIES_PARAM = "currencies"
internal const val RATES_PATH = "rates"
internal const val CURRENCIES_PATH = "currencies"

class CurrenciesInfoRemoteSource(
    private val client: HttpClient,
) {
    suspend fun Raise<LoadError>.loadRates(
        baseCurrency: String,
        currencies: List<String>,
    ): CurrenciesInfoDto = client.eitherGet<CurrenciesInfoDto>(API_ENDPOINT) {
        url {
            encodedPathSegments += RATES_PATH
            parameters.append(BASE_CURRENCY_PARAM, baseCurrency)
            parameters.append(CURRENCIES_PARAM, currencies.joinToString(","))
        }
    }.bind()

    suspend fun Raise<LoadError>.loadCurrencies(): CurrenciesDto =
        client.eitherGet<CurrenciesDto>(API_ENDPOINT) {
            url {
                encodedPathSegments += CURRENCIES_PATH
            }
        }.bind()
}
