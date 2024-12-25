package com.hasib.samsungalbumshowcase.domain.entities

import com.hasib.samsungalbumshowcase.domain.entities.Result.Success

sealed class Result<out T> {
    class Success<out T>(val data: T) : Result<T>()
    class Error(val code: Int, val message: String?) : Result<Nothing>()
    class Exception(val e: Throwable) : Result<Nothing>()
}

fun <T> Result<T>.doOnSuccess(block: (T) -> Unit) {
    if (this is Success) {
        block(data)
    }
}

fun <T> Result<T>.doOnError(block: (error: String) -> Unit) {
    if (this is Result.Error) {
        block(message ?: "Unknown error")
    }
}

fun <T> Result<T>.doOnException(block: (e: Throwable) -> Unit) {
    if (this is Result.Exception) {
        block(e)
    }
}