package com.hasib.samsungalbumshowcase.data.repositories

import com.hasib.samsungalbumshowcase.data.api.ApiService
import com.hasib.samsungalbumshowcase.data.api.handleApi
import com.hasib.samsungalbumshowcase.domain.models.Result
import com.hasib.samsungalbumshowcase.domain.models.User
import com.hasib.samsungalbumshowcase.domain.models.doOnSuccess
import com.hasib.samsungalbumshowcase.domain.repositories.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(private val apiService: ApiService) : UserRepository {

    private val users = mutableListOf<User>()

    override suspend fun fetchUsers(): Result<List<User>> {
        if (users.isNotEmpty()) {
            return Result.Success(users)
        }

        val userResult = handleApi{ apiService.getUsers() }
        userResult.doOnSuccess {
            users.addAll(it)
        }
        return userResult
    }
}