package com.example.exchangerates.common.error

import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

sealed interface InfrastructureError

sealed interface LoadError : InfrastructureError {
    @JvmInline
    value class UnexpectedError(
        override val cause: Throwable,
    ) : LoadError,
        HasThrowableCause

    @JvmInline
    value class IoError(
        override val cause: Throwable,
    ) : LoadError,
        HasThrowableCause

    @JvmInline
    value class SerializationError(
        override val cause: Throwable,
    ) : LoadError,
        HasThrowableCause

    data class BackendError(
        val code: BackendErrorCode,
        val body: BackendBody,
    ) : LoadError
}

interface HasThrowableCause {
    val cause: Throwable
}

typealias BackendErrorCode = Int
typealias BackendBody = String

fun Throwable.toLoadError(): LoadError =
    when (this) {
        is IOException -> LoadError::IoError
        is SerializationException -> LoadError::SerializationError
        else -> LoadError::UnexpectedError
    }.invoke(this)
