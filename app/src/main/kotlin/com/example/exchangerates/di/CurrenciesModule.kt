package com.example.exchangerates.di

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioStorage
import com.example.exchangerates.common.navigation.NavGraphEntry
import com.example.exchangerates.common.navigation.SuspendingNavHostController
import com.example.exchangerates.data.CurrenciesRepository
import com.example.exchangerates.data.local.CurrenciesInfoDb
import com.example.exchangerates.data.local.CurrenciesInfoDbSerializer
import com.example.exchangerates.data.local.CurrenciesInfoLocalSource
import com.example.exchangerates.data.local.DATA_STORE_FILE_NAME
import com.example.exchangerates.data.remote.CurrenciesInfoRemoteSource
import com.example.exchangerates.data.remote.createClient
import com.example.exchangerates.data.remote.createMockEngine
import com.example.exchangerates.domain.entities.CurrencyInfo
import com.example.exchangerates.domain.tea.currencies.AddCurrenciesCmdCtx
import com.example.exchangerates.domain.tea.rates.RatesCmdCtx
import com.example.exchangerates.presentation.createAddCurrenciesNavGraphEntry
import com.example.exchangerates.presentation.createExchangeRatesNavGraphEntry
import com.example.exchangerates.presentation.navigateToAddCurrencies
import kotlinx.coroutines.flow.map
import okio.FileSystem
import okio.Path.Companion.toPath

class CurrenciesModule(
    private val deps: Deps,
) {
    init {
        deps.registerNavGraphEntry(createExchangeRatesNavGraphEntry { ratesCmdContext })
        deps.registerNavGraphEntry(createAddCurrenciesNavGraphEntry { addCurrenciesCmdContext })
    }

    val dataStore: DataStore<CurrenciesInfoDb> by lazy {
        DataStoreFactory.create(
            storage = OkioStorage(
                fileSystem = FileSystem.SYSTEM,
                producePath = {
                    deps.context.filesDir
                        .resolve(DATA_STORE_FILE_NAME)
                        .absolutePath
                        .toPath()
                },
                serializer = CurrenciesInfoDbSerializer,
            )
        )
    }

    val repository
        get() =
            CurrenciesRepository(
                localSource = CurrenciesInfoLocalSource(dataStore),
                remoteSource =
                    CurrenciesInfoRemoteSource(
                        client = createClient(engine = createMockEngine()),
                    ),
            )

    val ratesCmdContext: RatesCmdCtx
        get() {
            val repository = repository
            return RatesCmdCtx(
                ratesInfo = repository.state,
                refreshRates = { repository.run { loadRates() } },
                removeCurrency = repository::removeCurrency,
                navigateToAddCurrencies = {
                    deps.navHostController.withAttached { navigateToAddCurrencies() }
                },
                log = { Log.d("ExchangeRates", it()) }
            )
        }

    val addCurrenciesCmdContext: AddCurrenciesCmdCtx
        get() {
            val repository = repository
            return AddCurrenciesCmdCtx(
                loadCurrencies = { repository.run { loadCurrencies() } },
                addCurrency = repository::addCurrency,
                addedCurrencies = repository.state.map { state ->
                    state.currencies.map(CurrencyInfo::currency)
                },
                log = { Log.d("AddCurrencies", it()) }
            )
        }

    class Deps(
        val context: Context,
        val navHostController: SuspendingNavHostController,
        val registerNavGraphEntry: (NavGraphEntry) -> Unit,
    )
}
