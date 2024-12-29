package com.hasib.samsungalbumshowcase.data.repositories

import com.hasib.samsungalbumshowcase.data.api.ApiService
import com.hasib.samsungalbumshowcase.data.api.handleApi
import com.hasib.samsungalbumshowcase.domain.models.Album
import com.hasib.samsungalbumshowcase.domain.models.Result
import com.hasib.samsungalbumshowcase.domain.models.doOnSuccess
import com.hasib.samsungalbumshowcase.domain.repositories.AlbumRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlbumRepositoryImpl @Inject constructor(private val apiService: ApiService) :
    AlbumRepository {

    private val albums = mutableListOf<Album>()

    override suspend fun fetchAlbums(): Result<List<Album>> {
        if (albums.isNotEmpty()) {
            return Result.Success(albums)
        }

        val albumResult = handleApi { apiService.getAlbums() }
        albumResult.doOnSuccess {
            albums.addAll(it)
        }
        return albumResult
    }
}