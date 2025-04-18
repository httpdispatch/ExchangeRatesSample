package com.example.exchangerates.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.exchangerates.common.navigation.NavGraphEntry
import com.example.exchangerates.common.navigation.SuspendingNavHostController
import com.example.exchangerates.di.AppComponent
import com.example.exchangerates.presentation.theme.ExchangeRatesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        with((applicationContext as AppComponent.Holder).appComponent) {
            setContent {
                MainScreen(navHostController, navGraphEntries)
            }
        }
    }
}

@Composable
fun MainScreen(
    navHostController: SuspendingNavHostController,
    navGraphEntries: List<NavGraphEntry>,
    modifier: Modifier = Modifier,
) {
    ExchangeRatesTheme {
        val navController by rememberUpdatedState(rememberNavController())
        LifecycleStartEffect(Unit) {
            navHostController.attach(navController)

            onStopOrDispose {
                navHostController.detach()
            }
        }

        Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
            AppNavHost(
                navGraphEntries = navGraphEntries,
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
fun AppNavHost(
    navGraphEntries: List<NavGraphEntry>,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = ExchangeRates,
        modifier = modifier,
    ) {
        navGraphEntries.forEach { it() }
    }
}
