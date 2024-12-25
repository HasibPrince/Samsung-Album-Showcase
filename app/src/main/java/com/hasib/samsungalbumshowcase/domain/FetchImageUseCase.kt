package com.hasib.samsungalbumshowcase.domain

import com.hasib.samsungalbumshowcase.data.api.handleApi
import com.hasib.samsungalbumshowcase.domain.entities.Album
import com.hasib.samsungalbumshowcase.domain.entities.PhotoDisplay
import com.hasib.samsungalbumshowcase.domain.entities.User
import com.hasib.samsungalbumshowcase.domain.entities.Result
import com.hasib.samsungalbumshowcase.domain.entities.doOnSuccess
import com.hasib.samsungalbumshowcase.domain.repositories.AlbumRepository
import com.hasib.samsungalbumshowcase.domain.repositories.PhotoRepository
import com.hasib.samsungalbumshowcase.domain.repositories.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FetchImageUseCase @Inject constructor(
    private val photoRepository: PhotoRepository,
    private val albumRepository: AlbumRepository,
    private val userRepository: UserRepository
) {

    private val albumMap = mutableMapOf<Int, Album>()
    private val userMap = mutableMapOf<Int, User>()

    operator fun invoke(page: Int, limit: Int): Flow<Result<List<PhotoDisplay>>> {
        return flow {
            emit(fetchPhotos(page, limit))
        }
    }

    suspend fun fetchPhotos(page: Int, limit: Int): Result<List<PhotoDisplay>> {
        val albums = albumRepository.fetchAlbums()
        val users = userRepository.fetchUsers()
        val photos = photoRepository.fetchPhotos(page, limit)

        Result.checkError(albums, users, photos)?.let {
            return it
        }

        albums.doOnSuccess {
            albumMap.putAll(it.associateBy { it.id })
        }

        users.doOnSuccess {
            userMap.putAll(it.associateBy { it.id })
        }

        val displayPhotos = mutableListOf<PhotoDisplay>()
        var errorResult: Result.BaseError<*>? = null

        photos.doOnSuccess {
            try {
                it.forEach { photo ->
                    val album = albumMap[photo.albumId]
                    val user = userMap[album?.userId ?: 0]
                    displayPhotos.add(PhotoDisplay(photo, album?.title ?: "", user?.username ?: ""))
                }
            } catch (e: NoSuchElementException) {
                errorResult = Result.BaseError.Exception(e)
            }
        }

        return Result.Success(displayPhotos)
    }
}