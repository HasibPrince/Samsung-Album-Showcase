package com.hasib.samsungalbumshowcase.domain.repositories

import com.hasib.samsungalbumshowcase.domain.models.Photo
import com.hasib.samsungalbumshowcase.domain.models.Result

interface PhotoRepository {
    suspend fun fetchPhotos(page: Int, limit: Int): Result<List<Photo>>
    fun getPhotoByAlbumId(id: Int): Photo?
}