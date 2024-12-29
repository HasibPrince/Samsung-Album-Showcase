package com.hasib.samsungalbumshowcase.domain.models

import com.hasib.samsungalbumshowcase.domain.models.Result.Success

sealed class Result<out T> {
    class Success<out T>(val data: T) : Result<T>()
    sealed class BaseError<out T>(val errorMessage: String?) : Result<T>() {
        class Error(val code: Int, errorMessage: String?) : BaseError<Nothing>(errorMessage)
        class Exception(val e: Throwable) : BaseError<Nothing>(e.message)
    }

    companion object {
        fun <T> checkError(vararg results: Result<out T>): BaseError<Nothing>? {
            results.forEach {
                if (it is BaseError.Error || it is BaseError.Exception) {
                    return it
                }
            }
            return null
        }
    }
}

suspend fun <T> Result<T>.doOnSuccess(block: suspend (T) -> Unit) {
    if (this is Success) {
        block(data)
    }
}

fun <T> Result<T>.doOnError(block: (error: String) -> Unit) {
    if (this is Result.BaseError.Error) {
        block(errorMessage ?: "Unknown error")
    }
}

fun <T> Result<T>.doOnException(block: (e: Throwable) -> Unit) {
    if (this is Result.BaseError.Exception) {
        block(e)
    }
}

fun <T> Result<T>.doOnAnyTypeError(block: (errorMessage: String) -> Unit) {
    if (this is Result.BaseError<*>) {
        block(errorMessage ?: "Unknown error")
    }
}