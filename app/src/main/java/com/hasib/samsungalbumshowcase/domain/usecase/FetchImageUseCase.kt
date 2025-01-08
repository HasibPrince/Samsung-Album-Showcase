package com.hasib.samsungalbumshowcase.domain.usecase

import androidx.annotation.OpenForTesting
import com.hasib.samsungalbumshowcase.domain.models.Album
import com.hasib.samsungalbumshowcase.domain.models.Photo
import com.hasib.samsungalbumshowcase.domain.models.PhotoDisplay
import com.hasib.samsungalbumshowcase.domain.models.Result
import com.hasib.samsungalbumshowcase.domain.models.User
import com.hasib.samsungalbumshowcase.domain.models.doOnSuccess
import com.hasib.samsungalbumshowcase.domain.repositories.AlbumRepository
import com.hasib.samsungalbumshowcase.domain.repositories.PhotoRepository
import com.hasib.samsungalbumshowcase.domain.repositories.ThumbRepository
import com.hasib.samsungalbumshowcase.domain.repositories.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import kotlin.collections.chunked

@OpenForTesting
open class FetchImageUseCase @Inject constructor(
    private val photoRepository: PhotoRepository,
    private val albumRepository: AlbumRepository,
    private val userRepository: UserRepository,
    private val thumbRepository: ThumbRepository
) {

    private val albumMap = mutableMapOf<Int, Album>()
    private val userMap = mutableMapOf<Int, User>()

    @OpenForTesting
    open operator fun invoke(page: Int, limit: Int): Flow<Result<List<PhotoDisplay>>> {
        return flow {
            fetchPhotos(this@flow, page, limit)
        }
    }

    private suspend fun fetchPhotos(
        flowScope: FlowCollector<Result<List<PhotoDisplay>>>,
        page: Int,
        limit: Int
    ) =
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

            var errorResult: Result.BaseError<Nothing>? = null

            photos.doOnSuccess {
                try {
                    processPhotoList(flowScope, it)
                } catch (e: NoSuchElementException) {
                    errorResult = Result.BaseError.Exception(e)
                }
            }

            errorResult?.let {
                flowScope.emit(it)
            }
        }

    private suspend fun processPhotoList(
        flowScope: FlowCollector<Result<List<PhotoDisplay>>>,
        photoList: List<Photo>,
    ) {
        val photoBatches = photoList.chunked(50)
        photoBatches.forEach {
            val photoDisplayBatch = mutableListOf<PhotoDisplay>()
            val failedPhotoDisplay = mutableListOf<PhotoDisplay>()
            it.forEach { photo ->
                generatePhotoDisplays(photo, failedPhotoDisplay, photoDisplayBatch)
            }
            flowScope.emit(Result.Success(photoDisplayBatch))
        }
    }

    private suspend fun generatePhotoDisplays(
        photo: Photo,
        failedPhotoDisplay: MutableList<PhotoDisplay>,
        photoDisplayBatch: MutableList<PhotoDisplay>
    ) {
        val album = albumMap[photo.albumId]
        val user = userMap[album?.userId ?: 0]
        val photoForThumb = photoRepository.getPhotoByAlbumId(photo.albumId)
        var thumbBytes: ByteArray? = null
        if (photoForThumb != null) {
            thumbBytes = thumbRepository.getThumb(photoForThumb)
        }

        var photoDisplay = PhotoDisplay(
            photo,
            album?.title ?: "",
            user?.username ?: "",
            photoForThumb,
            thumbBytes
        )

        if (thumbBytes != null) {
            failedPhotoDisplay.forEach {
                it.thumbBytes = thumbBytes
            }
            failedPhotoDisplay.clear()
        } else {
            failedPhotoDisplay.add(photoDisplay)
        }

        photoDisplayBatch.add(photoDisplay)
    }
}