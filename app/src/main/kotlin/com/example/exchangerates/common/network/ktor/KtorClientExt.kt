package com.example.exchangerates.common.network.ktor

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.catch
import arrow.core.right
import com.example.exchangerates.common.error.LoadError
import com.example.exchangerates.common.error.toLoadError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.util.reflect.TypeInfo
import io.ktor.util.reflect.typeInfo

private val HttpResponse.isSuccessful get() = status.value in 200..299

typealias LoadResult<T> = Either<LoadError, T>

suspend inline fun <reified T> HttpClient.eitherGet(
    urlString: String,
    noinline block: HttpRequestBuilder.() -> Unit = {},
): LoadResult<T> =
    eitherGet(
        info = typeInfo<T>(),
        urlString = urlString,
        block = block,
    )

suspend fun <T> HttpClient.eitherGet(
    info: TypeInfo,
    urlString: String,
    block: HttpRequestBuilder.() -> Unit = {},
): LoadResult<T> =
    catch({
        val response = get(urlString, block)
        if (response.isSuccessful.not()) {
            return LoadError
                .BackendError(
                    code = response.status.value,
                    body = response.bodyAsText(),
                ).left()
        }
        response.body<T>(info).right()
    }) { it.toLoadError().left() }
