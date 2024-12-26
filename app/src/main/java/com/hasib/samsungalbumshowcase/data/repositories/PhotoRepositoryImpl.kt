package com.hasib.samsungalbumshowcase.data.repositories

import com.hasib.samsungalbumshowcase.data.api.ApiService
import com.hasib.samsungalbumshowcase.data.api.handleApi
import com.hasib.samsungalbumshowcase.domain.entities.Photo
import com.hasib.samsungalbumshowcase.domain.entities.Result
import com.hasib.samsungalbumshowcase.domain.entities.doOnSuccess
import com.hasib.samsungalbumshowcase.domain.repositories.PhotoRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoRepositoryImpl @Inject constructor(private val apiService: ApiService) : PhotoRepository {
    private val photos = mutableListOf<Photo>()
    private val albumMap = mutableMapOf<Int, Photo>()

    override suspend fun fetchPhotos(page: Int, limit: Int): Result<List<Photo>> {
        val refinedLimit = if (photos.size % limit == 0) limit else photos.size % limit
        val startIndex = (page - 1) * limit
        val endIndex = startIndex + refinedLimit

        if (photos.size >= endIndex) {
            return Result.Success(photos.subList(startIndex, endIndex))
        }

        val photoResult = handleApi { apiService.getPhotos(page, limit) }
        photoResult.doOnSuccess {
            it.forEach {
                photos.add(it)
                albumMap.putIfAbsent(it.albumId, it)
            }
        }

        return photoResult
    }

    override fun getPhotoByAlbumId(id: Int): Photo? {
        return albumMap[id]
    }
}