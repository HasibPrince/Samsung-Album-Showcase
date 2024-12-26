package com.hasib.samsungalbumshowcase.domain.entities

import coil3.Bitmap

data class PhotoDisplay(
    val photo: Photo,
    val albumName: String,
    val username: String,
    val thumbPhoto: Photo?,
    val thumbnail: Bitmap? = null
)
