package com.hasib.samsungalbumshowcase.data.api

import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import retrofit2.Response
import com.hasib.samsungalbumshowcase.domain.entities.Result

suspend fun <T> handleApi(apiCall: suspend () -> Response<T>): Result<T> {
    return try {
        val response = apiCall()
        val body = response.body()
        if (response.isSuccessful && body != null) {
            Result.Success(body)
        } else {
            Result.Error(code = response.code(), message = response.message())
        }
    } catch (e: HttpException) {
        Result.Error(code = e.code(), message = e.message())
    } catch (e: Throwable) {
        Result.Exception(e)
    }
}