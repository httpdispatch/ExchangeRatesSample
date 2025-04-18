package com.example.exchangerates.domain.tea.rates

import arrow.core.raise.Raise
import com.example.exchangerates.domain.entities.RatesInfo
import com.example.exchangerates.domain.entities.CurrencyCode
import com.example.exchangerates.domain.entities.CurrencyError
import kotlinx.coroutines.flow.Flow

data class ExchangeRatesState(
    val localState: LocalState = LocalState.Initial,
    val loadingState: LoadingState = LoadingState.Initial,
)

sealed class LocalState {
    data object Initial : LocalState()

    data class Data(
        val value: RatesInfo,
    ) : LocalState()
}

sealed class LoadingState {
    data object Initial : LoadingState()

    data object Loading : LoadingState()

    data object Success : LoadingState()

    data object Error : LoadingState()
}

class RatesCmdCtx(
    val ratesInfo: Flow<RatesInfo>,
    val refreshRates: suspend Raise<CurrencyError>.() -> Unit,
    val removeCurrency: suspend (CurrencyCode) -> Unit,
    val navigateToAddCurrencies: suspend () -> Unit,
    val log: (() -> String) -> Unit
)

data class Update(
    val state: ExchangeRatesState,
    val cmds: List<Cmd> = emptyList()
)

fun interface Msg {
    operator fun invoke(state: ExchangeRatesState): Update
}

fun interface Cmd {
    operator fun RatesCmdCtx.invoke(): Flow<Msg>
}
