package com.example.exchangerates.domain.tea.rates

import arrow.core.raise.either
import com.example.exchangerates.domain.entities.CurrencyCode
import com.example.exchangerates.domain.entities.RatesInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlin.time.Duration.Companion.seconds

private val RefreshTimeout = 5.seconds

data object OnInit : Msg {
    override fun invoke(state: ExchangeRatesState) = Update(
        state = state,
        cmds = listOf(ObserveCurrencies, AutoRefreshCurrencies),
    )

    private data object ObserveCurrencies : Cmd {
        override fun RatesCmdCtx.invoke() =
            ratesInfo.map(::OnCurrenciesUpdated)

    }

    private data object AutoRefreshCurrencies : Cmd {
        override fun RatesCmdCtx.invoke() = flow {
            while (true) {
                emit(OnLoadingStarted)
                either {
                    refreshRates()
                }.fold(
                    ifLeft = { error ->
                        log { "Refresh rates failed with error: $error" }
                        emit(OnLoadingError)
                    },
                    ifRight = { emit(OnLoadingSuccess) }
                )
                delay(RefreshTimeout)
            }
        }

        data object OnLoadingStarted : Msg {
            override fun invoke(state: ExchangeRatesState) = Update(
                state.copy(
                    loadingState = LoadingState.Loading
                )
            )
        }

        data object OnLoadingError : Msg {
            override fun invoke(state: ExchangeRatesState) = Update(
                state.copy(
                    loadingState = LoadingState.Error
                )
            )
        }

        data object OnLoadingSuccess : Msg {
            override fun invoke(state: ExchangeRatesState) = Update(
                state.copy(
                    loadingState = LoadingState.Success
                )
            )
        }
    }

    private data class OnCurrenciesUpdated(
        val currencies: RatesInfo,
    ) : Msg {
        override fun invoke(state: ExchangeRatesState): Update = Update(
            state = state.copy(
                localState = LocalState.Data(currencies)
            )
        )
    }
}

data class OnCurrencyRemoved(val currency: CurrencyCode) : Msg {
    override fun invoke(state: ExchangeRatesState): Update = Update(
        state = state,
        cmds = listOf(RemoveCurrency(currency))
    )

    private data class RemoveCurrency(val currency: CurrencyCode) : Cmd {
        override fun RatesCmdCtx.invoke() = flow<Nothing> {
            removeCurrency(currency)
        }
    }
}

data object OnAddCurrencies : Msg {
    override fun invoke(state: ExchangeRatesState): Update = Update(
        state = state,
        cmds = listOf(AddCurrencies)
    )

    private data object AddCurrencies : Cmd {
        override fun RatesCmdCtx.invoke() = flow<Nothing> {
            navigateToAddCurrencies()
        }
    }
}
