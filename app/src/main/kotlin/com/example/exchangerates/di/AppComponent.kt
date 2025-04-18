package com.example.exchangerates.di

import android.content.Context
import com.example.exchangerates.common.navigation.NavGraphEntry
import com.example.exchangerates.common.navigation.SuspendingNavHostController

class AppComponent(private val deps: Deps) {

    private val _navGraphEntries = mutableListOf<NavGraphEntry>()
    val navGraphEntries: List<NavGraphEntry> = _navGraphEntries
    val navHostController by lazy { SuspendingNavHostController() }

    class Deps(
        val context: Context,
    )

    private val currenciesModule = CurrenciesModule(
        deps = CurrenciesModule.Deps(
            context = deps.context,
            navHostController = navHostController,
            registerNavGraphEntry = _navGraphEntries::add
        )
    )

    interface Holder {
        val appComponent: AppComponent
    }
}
