package com.hasib.samsungalbumshowcase.domain.entities

data class Photo(
    val albumId: Int,
    val id: Int,
    val title: String,
    val url: String,
    val thumbnailUrl: String
)
