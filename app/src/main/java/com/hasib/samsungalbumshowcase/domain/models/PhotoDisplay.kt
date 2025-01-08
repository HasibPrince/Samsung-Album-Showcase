package com.hasib.samsungalbumshowcase.domain.models


data class PhotoDisplay(
    val photo: Photo,
    val albumName: String,
    val username: String,
    val thumbPhoto: Photo?,
    var thumbBytes: ByteArray?
)
