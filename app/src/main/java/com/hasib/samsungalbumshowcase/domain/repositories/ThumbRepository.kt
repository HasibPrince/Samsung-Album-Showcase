package com.hasib.samsungalbumshowcase.domain.repositories

import com.hasib.samsungalbumshowcase.domain.models.Photo

interface ThumbRepository {
    suspend fun getThumb(photo: Photo): ByteArray?
}