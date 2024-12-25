package com.hasib.samsungalbumshowcase.data.repositories

import com.hasib.samsungalbumshowcase.data.api.ApiService
import com.hasib.samsungalbumshowcase.data.api.handleApi
import com.hasib.samsungalbumshowcase.domain.entities.Result
import com.hasib.samsungalbumshowcase.domain.entities.User
import com.hasib.samsungalbumshowcase.domain.entities.doOnSuccess
import com.hasib.samsungalbumshowcase.domain.repositories.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(private val apiService: ApiService) : UserRepository {
    private val users = mutableListOf<User>()
    override suspend fun fetchUsers(): Result<List<User>> {
        if (users.isEmpty()) {
            val userResult = handleApi{ apiService.getUsers() }
            userResult.doOnSuccess {
                users.addAll(it)
            }
            return userResult
        }
        return Result.Success(users)
    }
}