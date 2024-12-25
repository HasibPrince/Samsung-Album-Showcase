package com.hasib.samsungalbumshowcase.domain.repositories

import com.hasib.samsungalbumshowcase.domain.entities.Result
import com.hasib.samsungalbumshowcase.domain.entities.User

interface UserRepository {
    suspend fun fetchUsers(): Result<List<User>>
}