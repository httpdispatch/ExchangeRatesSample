package com.example.exchangerates.data

import arrow.core.raise.Raise
import arrow.core.raise.recover
import com.example.exchangerates.data.local.CurrenciesInfoLocalSource
import com.example.exchangerates.data.remote.CurrenciesInfoRemoteSource
import com.example.exchangerates.domain.entities.CurrencyCode
import com.example.exchangerates.domain.entities.CurrencyError
import com.example.exchangerates.domain.entities.RatesInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class CurrenciesRepository(
    val localSource: CurrenciesInfoLocalSource,
    val remoteSource: CurrenciesInfoRemoteSource,
    val currentTime: () -> Instant = { Clock.System.now() },
) {
    val state: Flow<RatesInfo> =
        localSource.state

    suspend fun Raise<CurrencyError>.loadRates() {
        val localState = state.first()
        val dto = recover(
            block = {
                remoteSource.run {
                    loadRates(
                        baseCurrency = localState.baseCurrency,
                        currencies = localState.currencies.map { it.currency }
                    )
                }
            },
            recover = { error ->
                localSource.setUpdateError(attemptedAt = currentTime())
                raise(error)
            })
        localSource.updateCurrencyRates(
            updatedAt = currentTime(),
            rates = dto.rates,
        )
    }

    suspend fun Raise<CurrencyError>.loadCurrencies(): List<CurrencyCode> =
        remoteSource.run { loadCurrencies() }

    suspend fun addCurrency(currency: CurrencyCode) {
        localSource.addCurrency(currency)
    }

    suspend fun removeCurrency(currency: CurrencyCode) {
        localSource.removeCurrency(currency)
    }
}
