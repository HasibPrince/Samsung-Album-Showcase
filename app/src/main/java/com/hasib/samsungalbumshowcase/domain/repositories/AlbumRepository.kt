package com.hasib.samsungalbumshowcase.domain.repositories

import com.hasib.samsungalbumshowcase.domain.models.Album
import com.hasib.samsungalbumshowcase.domain.models.Result

interface AlbumRepository {
    suspend fun fetchAlbums(): Result<List<Album>>
}