package com.example.exchangerates.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.exchangerates.domain.tea.rates.RatesCmdCtx
import com.example.exchangerates.domain.tea.rates.ExchangeRatesState
import com.example.exchangerates.domain.tea.rates.Msg
import com.example.exchangerates.domain.tea.rates.OnInit
import com.example.exchangerates.domain.tea.rates.Update
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class ExchangeRatesViewModel(
    val cmdContext: RatesCmdCtx,
) : ViewModel() {

    val state = MutableStateFlow(ExchangeRatesState())

    fun accept(msg: Msg) {
        cmdContext.log { "Accepting: $msg" }
        lateinit var update: Update
        state.update { previous ->
            msg(previous)
                .also { update = it }
                .state
        }
        cmdContext.log { "New state: ${update.state}" }
        update.cmds.forEach { cmd ->
            cmdContext.log { "Launching: $cmd" }
            with(cmdContext) {
                cmd.run { invoke() }
                    .onEach(::accept)
                    .launchIn(viewModelScope)
            }
        }
    }

    companion object {
        fun factory(cmdContext: RatesCmdCtx) = viewModelFactory {
            initializer<ExchangeRatesViewModel> {
                ExchangeRatesViewModel(
                    cmdContext = cmdContext,
                ).apply { accept(OnInit) }
            }
        }
    }
}
