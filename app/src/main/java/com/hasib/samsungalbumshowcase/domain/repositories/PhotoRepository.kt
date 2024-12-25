package com.hasib.samsungalbumshowcase.domain.repositories

import com.hasib.samsungalbumshowcase.domain.entities.Photo
import com.hasib.samsungalbumshowcase.domain.entities.Result

interface PhotoRepository {
    suspend fun fetchPhotos(page: Int, limit: Int): Result<List<Photo>>
}