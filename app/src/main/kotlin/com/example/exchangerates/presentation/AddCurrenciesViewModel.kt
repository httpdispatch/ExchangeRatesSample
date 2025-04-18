package com.example.exchangerates.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.exchangerates.domain.tea.currencies.AddCurrenciesCmdCtx
import com.example.exchangerates.domain.tea.currencies.AddCurrenciesState
import com.example.exchangerates.domain.tea.currencies.Msg
import com.example.exchangerates.domain.tea.currencies.OnInit
import com.example.exchangerates.domain.tea.currencies.Update
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class AddCurrenciesViewModel(
    val cmdContext: AddCurrenciesCmdCtx,
) : ViewModel() {

    val state = MutableStateFlow<AddCurrenciesState>(AddCurrenciesState.Initial())

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
        fun factory(cmdContext: AddCurrenciesCmdCtx) = viewModelFactory {
            initializer<AddCurrenciesViewModel> {
                AddCurrenciesViewModel(
                    cmdContext = cmdContext,
                ).apply { accept(OnInit) }
            }
        }
    }
}
