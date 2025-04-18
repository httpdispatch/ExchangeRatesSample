package com.example.exchangerates

import android.app.Application
import com.example.exchangerates.di.AppComponent

class App :
    Application(),
    AppComponent.Holder {
    override val appComponent by lazy {
        AppComponent(
            AppComponent.Deps(
                context = this,
            ),
        )
    }
}
