package com.example.exchangerates.common.navigation

import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update

class SuspendingNavHostController {
    private val state = MutableStateFlow<NavHostController?>(null)
    fun attach(router: NavHostController) {
        state.update { router }
    }

    fun detach() {
        state.update { null }
    }

    suspend fun withAttached(block: suspend NavHostController.() -> Unit) {
        getRouter().block()
    }

    suspend fun getRouter(): NavHostController =
        state
            .filterNotNull()
            .first()
}
