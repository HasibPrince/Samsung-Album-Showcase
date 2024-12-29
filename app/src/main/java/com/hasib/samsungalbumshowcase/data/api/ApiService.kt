package com.hasib.samsungalbumshowcase.data.api

import com.hasib.samsungalbumshowcase.domain.models.Album
import com.hasib.samsungalbumshowcase.domain.models.Photo
import com.hasib.samsungalbumshowcase.domain.models.User
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    companion object {
        const val BASE_URL = "https://jsonplaceholder.typicode.com/"
    }

    @GET("photos")
    suspend fun getPhotos(
        @Query("_page") page: Int,
        @Query("_limit") limit: Int
    ): Response<List<Photo>>

    @GET("albums")
    suspend fun getAlbums(): Response<List<Album>>

    @GET("users")
    suspend fun getUsers(): Response<List<User>>
}