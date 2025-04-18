package com.example.exchangerates.presentation

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.composable
import com.example.exchangerates.common.navigation.NavGraphEntry
import com.example.exchangerates.domain.entities.CurrencyInfo
import com.example.exchangerates.domain.entities.RatesInfo
import com.example.exchangerates.domain.tea.rates.ExchangeRatesState
import com.example.exchangerates.domain.tea.rates.LocalState
import com.example.exchangerates.domain.tea.rates.Msg
import com.example.exchangerates.domain.tea.rates.OnAddCurrencies
import com.example.exchangerates.domain.tea.rates.OnCurrencyRemoved
import com.example.exchangerates.domain.tea.rates.RatesCmdCtx
import kotlinx.serialization.Serializable

@Composable
fun ExchangeRatesScreen(
    cmdContext: () -> RatesCmdCtx,
    modifier: Modifier = Modifier,
) {
    ExchangeRatesScreen(
        viewModel =
            viewModel<ExchangeRatesViewModel>(
                factory = remember { ExchangeRatesViewModel.factory(cmdContext = cmdContext()) },
            ),
        modifier = modifier,
    )
}

@Composable
fun ExchangeRatesScreen(
    viewModel: ExchangeRatesViewModel,
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
    state: ExchangeRatesState,
    accept: (Msg) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (val localState = state.localState) {
        is LocalState.Data -> Rates(
            state = localState.value,
            accept = accept,
            modifier = modifier,
        )

        LocalState.Initial -> Unit
    }
}

@Composable
fun Rates(
    state: RatesInfo,
    accept: (Msg) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Text(
                text = "Last updated: ${state.lastUpdated}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        item {
            Text(
                text = "Last attempted: ${state.lastAttempted}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        items(items = state.currencies, key = { it.currency }) { currencyInfo ->
            SwipeBox(
                onDelete = { accept(OnCurrencyRemoved(currencyInfo.currency)) },
                modifier = Modifier.animateItem()
            ) {
                CurrencyItem(
                    currencyInfo = currencyInfo,
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                )
            }
        }

        item {
            Button(
                onClick = { accept(OnAddCurrencies) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text("Add currencies")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeBox(
    modifier: Modifier = Modifier,
    onDelete: () -> Unit,
    content: @Composable () -> Unit,
) {
    val swipeState = rememberSwipeToDismissBoxState()

    val icon: ImageVector
    val alignment: Alignment
    val color: Color

    when (swipeState.dismissDirection) {
        SwipeToDismissBoxValue.EndToStart, SwipeToDismissBoxValue.Settled -> {
            icon = Icons.Outlined.Delete
            alignment = Alignment.CenterEnd
            color = Color.Transparent
        }

        SwipeToDismissBoxValue.StartToEnd -> {
            icon = Icons.Outlined.Edit
            color = Color.Transparent
            alignment = Alignment.CenterStart
        }
    }

    SwipeToDismissBox(
        enableDismissFromStartToEnd = false,
        modifier = modifier.animateContentSize(),
        state = swipeState,
        backgroundContent = {
            Box(
                contentAlignment = alignment,
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
            ) {
                Icon(
                    modifier = Modifier.minimumInteractiveComponentSize(),
                    imageVector = icon,
                    contentDescription = null
                )
            }
        }
    ) {
        content()
    }

    when (swipeState.currentValue) {
        SwipeToDismissBoxValue.EndToStart -> onDelete()

        SwipeToDismissBoxValue.StartToEnd, SwipeToDismissBoxValue.Settled -> Unit
    }
}

@Composable
fun CurrencyItem(
    currencyInfo: CurrencyInfo,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = currencyInfo.currency.uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = when (currencyInfo) {
                    is CurrencyInfo.Added -> "Added"
                    is CurrencyInfo.NotFound -> "Not Found"
                    is CurrencyInfo.Rate -> currencyInfo.value.toString()
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Serializable
data object ExchangeRates

fun createExchangeRatesNavGraphEntry(cmdContext: () -> RatesCmdCtx): NavGraphEntry =
    {
        composable<ExchangeRates> {
            ExchangeRatesScreen(
                cmdContext = cmdContext,
            )
        }
    }
