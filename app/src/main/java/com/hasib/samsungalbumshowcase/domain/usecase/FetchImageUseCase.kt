package com.hasib.samsungalbumshowcase.domain.usecase

import androidx.annotation.OpenForTesting
import com.hasib.samsungalbumshowcase.domain.entities.Album
import com.hasib.samsungalbumshowcase.domain.entities.Photo
import com.hasib.samsungalbumshowcase.domain.entities.PhotoDisplay
import com.hasib.samsungalbumshowcase.domain.entities.Result
import com.hasib.samsungalbumshowcase.domain.entities.User
import com.hasib.samsungalbumshowcase.domain.entities.doOnSuccess
import com.hasib.samsungalbumshowcase.domain.repositories.AlbumRepository
import com.hasib.samsungalbumshowcase.domain.repositories.PhotoRepository
import com.hasib.samsungalbumshowcase.domain.repositories.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@OpenForTesting
open class FetchImageUseCase @Inject constructor(
    private val photoRepository: PhotoRepository,
    private val albumRepository: AlbumRepository,
    private val userRepository: UserRepository
) {

    private val albumMap = mutableMapOf<Int, Album>()
    private val userMap = mutableMapOf<Int, User>()

    @OpenForTesting
    open operator fun invoke(page: Int, limit: Int): Flow<Result<List<PhotoDisplay>>> {
        return flow {
            emit(fetchPhotos(page, limit))
        }
    }

    private suspend fun fetchPhotos(page: Int, limit: Int): Result<List<PhotoDisplay>> =
        coroutineScope {
            val albumsResult = async { albumRepository.fetchAlbums() }
            val usersResult = async { userRepository.fetchUsers() }
            val photosResult = async { photoRepository.fetchPhotos(page, limit) }

            val albums = albumsResult.await()
            val users = usersResult.await()
            val photos = photosResult.await()

            Result.checkError(albums, users, photos)?.let {
                return@coroutineScope it
            }

            albums.doOnSuccess {
                albumMap.putAll(it.associateBy { it.id })
            }

            users.doOnSuccess {
                userMap.putAll(it.associateBy { it.id })
            }

            val displayPhotos = mutableListOf<PhotoDisplay>()
            var errorResult: Result.BaseError<Nothing>? = null

            photos.doOnSuccess {
                try {
                    processPhotoList(it, displayPhotos)
                } catch (e: NoSuchElementException) {
                    errorResult = Result.BaseError.Exception(e)
                }
            }

            errorResult?.let {
                return@coroutineScope it
            }

            Result.Success(displayPhotos)
        }

    private fun processPhotoList(
        photoList: List<Photo>,
        displayPhotos: MutableList<PhotoDisplay>
    ) {
        photoList.forEach { photo ->
            val album = albumMap[photo.albumId]
            val user = userMap[album?.userId ?: 0]
            val photoForThumb = photoRepository.getPhotoByAlbumId(photo.albumId)
            displayPhotos.add(
                PhotoDisplay(
                    photo,
                    album?.title ?: "",
                    user?.username ?: "",
                    photoForThumb
                )
            )
        }
    }
}