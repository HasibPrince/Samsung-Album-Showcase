package com.hasib.samsungalbumshowcase.domain.repositories

import com.hasib.samsungalbumshowcase.domain.models.Result
import com.hasib.samsungalbumshowcase.domain.models.User

interface UserRepository {
    suspend fun fetchUsers(): Result<List<User>>
}