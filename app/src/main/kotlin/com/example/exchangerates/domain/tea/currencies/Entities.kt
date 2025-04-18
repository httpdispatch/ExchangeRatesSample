package com.example.exchangerates.domain.tea.currencies

import arrow.core.raise.Raise
import com.example.exchangerates.domain.entities.CurrencyCode
import com.example.exchangerates.domain.entities.CurrencyError
import kotlinx.coroutines.flow.Flow

sealed class AddCurrenciesState {
    abstract val addedCurrencies: List<CurrencyCode>

    data class Initial(override val addedCurrencies: List<CurrencyCode> = emptyList()) :
        AddCurrenciesState()

    data class Loading(override val addedCurrencies: List<CurrencyCode>) : AddCurrenciesState()

    data class Error(override val addedCurrencies: List<CurrencyCode>) : AddCurrenciesState()

    data class Data(
        val availableCurrencies: List<CurrencyCode>,
        override val addedCurrencies: List<CurrencyCode>,
    ) : AddCurrenciesState()
}

class AddCurrenciesCmdCtx(
    val loadCurrencies: suspend Raise<CurrencyError>.() -> List<CurrencyCode>,
    val addCurrency: suspend (CurrencyCode) -> Unit,
    val addedCurrencies: Flow<List<CurrencyCode>>,
    val log: (() -> String) -> Unit,
)

data class Update(
    val state: AddCurrenciesState,
    val cmds: List<Cmd> = emptyList(),
)

fun interface Msg {
    operator fun invoke(state: AddCurrenciesState): Update
}

fun interface Cmd {
    operator fun AddCurrenciesCmdCtx.invoke(): Flow<Msg>
}
