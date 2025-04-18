# 💱 Exchange Rates – Demo Project

A demo Android app showcasing a currency exchange rates screen, built with modern Android development tools and architecture.

---

## 🚀 Setup Instructions

1. Clone the repo
2. Open in Android Studio
3. Sync Gradle and run on an emulator or device

---

## 📡 API

This project does **not** use any third-party API. Instead, it uses a **mocked backend** built with Ktor.  
The mock API:
- Returns **randomly generated currency rates** on each request
- Simulates **server errors** with a small probability to test error handling

---

## 🧱 Tech Stack

- **Jetpack Compose** – Declarative UI
- **Jetpack Navigation** – In-app navigation
- **AndroidX DataStore** – Persistent local storage
- **Ktor** – For network requests and the mock server
- **Kotlinx Serialization** – JSON parsing
- **Kotlinx Datetime** – Date and time utilities
- **Arrow** – Functional programming and error handling

---

## 🗂 Code Structure

```
common/ # Shared utilities and helpers
data/ # Local/remote data sources, DTOs, entities, DataStore
di/ # Manual dependency injection setup
domain/ # Domain models and TEA (The Elm Architecture) workflows
presentation/ # UI screens and ViewModel logic
```

---

## 🚧 Known Limitations

- ❌ No unit test coverage
- 🔄 Exchange rates continue to update in background (should pause with lifecycle)
- 🧱 UI is functional but minimally styled

---

## 📸 Screenshots / Video

!(./ExchangeRatesScreenCast.gif)
