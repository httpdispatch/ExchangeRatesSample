package com.example.exchangerates.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.exchangerates.common.navigation.NavGraphEntry
import com.example.exchangerates.domain.entities.CurrencyCode
import com.example.exchangerates.domain.tea.currencies.AddCurrenciesState
import com.example.exchangerates.domain.tea.currencies.AddCurrenciesCmdCtx
import com.example.exchangerates.domain.tea.currencies.Msg
import com.example.exchangerates.domain.tea.currencies.OnCurrencyAdded
import com.example.exchangerates.domain.tea.currencies.OnRetry
import kotlinx.serialization.Serializable

@Composable
fun AddCurrenciesScreen(
    cmdContext: () -> AddCurrenciesCmdCtx,
    modifier: Modifier = Modifier,
) {
    AddCurrenciesScreen(
        viewModel =
            viewModel<AddCurrenciesViewModel>(
                factory = remember { AddCurrenciesViewModel.factory(cmdContext = cmdContext()) },
            ),
        modifier = modifier,
    )
}

@Composable
fun AddCurrenciesScreen(
    viewModel: AddCurrenciesViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Content(
        state = state,
        accept = viewModel::accept,
        modifier = modifier,
    )
}

@Composable
private fun Content(
    state: AddCurrenciesState,
    accept: (Msg) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        is AddCurrenciesState.Data -> CurrenciesList(
            currencies = state.availableCurrencies,
            addedCurrencies = state.addedCurrencies,
            accept = accept,
            modifier = modifier,
        )

        is AddCurrenciesState.Error -> Error(accept = accept, modifier = modifier)
        is AddCurrenciesState.Initial -> Unit
        is AddCurrenciesState.Loading -> Loading(modifier = modifier)
    }
}

@Composable
fun CurrenciesList(
    currencies: List<CurrencyCode>,
    addedCurrencies: List<CurrencyCode>,
    accept: (Msg) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(items = currencies, key = { it }) { currency ->
            CurrencyItem(
                currency = currency,
                accept = accept,
                isAdded = addedCurrencies.contains(currency),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
fun CurrencyItem(
    currency: CurrencyCode,
    isAdded: Boolean,
    accept: (Msg) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = currency.uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
            if(isAdded.not()) {
                Button(onClick = {
                    accept(OnCurrencyAdded(currency))
                }) {
                    Text("Add")
                }
            }
        }
    }
}

@Composable
private fun Loading(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun Error(
    accept: (Msg) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(24.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Unable to load currencies",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { accept(OnRetry) }) {
                Text("Retry")
            }
        }
    }
}

@Serializable
data object AddCurrency

fun createAddCurrenciesNavGraphEntry(cmdContext: () -> AddCurrenciesCmdCtx): NavGraphEntry =
    {
        composable<AddCurrency> {
            AddCurrenciesScreen(
                cmdContext = cmdContext,
            )
        }
    }

fun NavHostController.navigateToAddCurrencies() {
    navigate(AddCurrency)
}
