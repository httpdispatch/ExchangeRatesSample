package com.example.exchangerates.data.local

import androidx.datastore.core.DataStore
import com.example.exchangerates.domain.entities.CurrencyInfo
import com.example.exchangerates.domain.entities.CurrencyInfo.Added
import com.example.exchangerates.domain.entities.CurrencyInfo.NotFound
import kotlinx.datetime.Instant

class CurrenciesInfoLocalSource(
    private val dataStore: DataStore<CurrenciesInfoDb>,
) {
    val state = dataStore.data

    suspend fun update(reducer: (CurrenciesInfoDb) -> CurrenciesInfoDb) =
        dataStore.updateData(reducer)

    suspend fun addCurrency(currency: String) {
        update { data ->
           with(data) {
                if (currencies.any { it.currency == currency }) return@update this
                copy(currencies = currencies + Added(currency))
            }
        }
    }

    suspend fun removeCurrency(currency: String) {
        update { data ->
            with(data) {
                copy(currencies = currencies.filter { it.currency != currency })
            }
        }
    }

    suspend fun updateCurrencyRates(
        updatedAt: Instant,
        rates: Map<String, Double>,
    ) {
        update { data ->
            with(data) {
                copy(
                    lastUpdated = updatedAt,
                    lastAttempted = updatedAt,
                    currencies = currencies.map { info ->
                        rates[info.currency]
                            ?.let { rate ->
                                CurrencyInfo.Rate(
                                    currency = info.currency,
                                    value = rate
                                )
                            }
                            ?: NotFound(currency = info.currency)
                    }
                )
            }
        }
    }

    suspend fun setUpdateError(
        attemptedAt: Instant,
    ) {
        update { data ->
            data.copy(lastAttempted = attemptedAt)
        }
    }
}
