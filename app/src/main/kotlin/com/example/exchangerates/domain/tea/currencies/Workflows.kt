package com.example.exchangerates.domain.tea.currencies

import arrow.core.Either
import arrow.core.raise.either
import com.example.exchangerates.domain.entities.CurrencyCode
import com.example.exchangerates.domain.entities.CurrencyError
import com.example.exchangerates.domain.tea.currencies.AddCurrenciesState.Data
import com.example.exchangerates.domain.tea.currencies.AddCurrenciesState.Error
import com.example.exchangerates.domain.tea.currencies.AddCurrenciesState.Initial
import com.example.exchangerates.domain.tea.currencies.AddCurrenciesState.Loading
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

data object OnInit : Msg {
    override fun invoke(state: AddCurrenciesState) = when (state) {
        is Initial -> Update(
            state = Loading(addedCurrencies = state.addedCurrencies),
            cmds = listOf(ObserveAddedCurrencies, LoadCurrencies)
        )

        is Data, is Error, is Loading -> Update(state = state)
    }

    private data object ObserveAddedCurrencies : Cmd {
        override fun AddCurrenciesCmdCtx.invoke() =
            addedCurrencies.map(::OnAddedCurrenciesUpdated)

    }

    private data class OnAddedCurrenciesUpdated(
        val currencies: List<CurrencyCode>,
    ) : Msg {
        override fun invoke(state: AddCurrenciesState) = Update(
            state = when(state) {
                is Data -> state.copy(addedCurrencies = currencies)
                is Error -> state.copy(addedCurrencies = currencies)
                is Initial -> state.copy(addedCurrencies = currencies)
                is Loading -> state.copy(addedCurrencies = currencies)
            }
        )
    }
}

data object OnRetry : Msg {
    override fun invoke(state: AddCurrenciesState): Update = Update(
        state = Loading(addedCurrencies = state.addedCurrencies),
        cmds = listOf(LoadCurrencies)
    )
}

private data object LoadCurrencies : Cmd {
    override fun AddCurrenciesCmdCtx.invoke() = flow {
        emit(either {
            loadCurrencies()
        }.let(::OnLoadCurrenciesResult))
    }

    data class OnLoadCurrenciesResult(
        val result: Either<CurrencyError, List<CurrencyCode>>,
    ) : Msg {
        override fun invoke(state: AddCurrenciesState): Update = result.fold(
            ifLeft = { error -> state.onLoadError(error) },
            ifRight = { currencies -> state.onCurrenciesLoaded(currencies) }
        )

        private fun AddCurrenciesState.onLoadError(error: CurrencyError) = Update(
            state = when (this) {
                is Data, is Error, is Initial -> this
                is Loading -> Error(addedCurrencies = addedCurrencies)
            },
            cmds = listOf(LogError(error = error))
        )

        private fun AddCurrenciesState.onCurrenciesLoaded(currencies: List<CurrencyCode>) = Update(
            state = Data(availableCurrencies = currencies, addedCurrencies = addedCurrencies),
        )
    }

    private data class LogError(val error: CurrencyError) : Cmd {
        override fun AddCurrenciesCmdCtx.invoke() = flow<Nothing> {
            log { "Load currencies failed with error: $error" }
        }

    }
}

data class OnCurrencyAdded(val currency: CurrencyCode) : Msg {
    override fun invoke(state: AddCurrenciesState): Update = Update(
        state = state,
        cmds = listOf(AddCurrency(currency))
    )

    private data class AddCurrency(val currency: CurrencyCode) : Cmd {
        override fun AddCurrenciesCmdCtx.invoke() = flow<Nothing> {
            addCurrency(currency)
        }
    }
}
