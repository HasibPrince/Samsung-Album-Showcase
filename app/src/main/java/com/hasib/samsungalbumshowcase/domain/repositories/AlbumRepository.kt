package com.hasib.samsungalbumshowcase.domain.repositories

import com.hasib.samsungalbumshowcase.domain.entities.Album
import com.hasib.samsungalbumshowcase.domain.entities.Result

interface AlbumRepository {
    suspend fun fetchAlbums(): Result<List<Album>>
}