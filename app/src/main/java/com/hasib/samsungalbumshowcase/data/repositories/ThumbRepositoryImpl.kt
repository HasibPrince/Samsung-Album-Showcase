package com.hasib.samsungalbumshowcase.data.repositories

import android.content.Context
import android.util.Log
import coil3.Bitmap
import coil3.ImageLoader
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import com.hasib.samsungalbumshowcase.domain.models.Photo
import com.hasib.samsungalbumshowcase.domain.repositories.ThumbRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ThumbRepositoryImpl"

@Singleton
class ThumbRepositoryImpl @Inject constructor(@ApplicationContext private val applicationContext: Context) :
    ThumbRepository {

    private val thumbBitmapMap = mutableMapOf<String, ByteArray>()

    override suspend fun getThumb(photo: Photo): ByteArray? {

        if (thumbBitmapMap.containsKey(photo.thumbnailUrl)) {
            Log.d(TAG, "Found bitmap in map ${photo.id}, album: ${photo.albumId}")
            return thumbBitmapMap[photo.thumbnailUrl]
        }

        Log.d(TAG, "Loading bitmap for photo: ${photo.id}, album: ${photo.albumId}")
        val imageLoader = ImageLoader(applicationContext)
        val request = ImageRequest.Builder(applicationContext)
            .data(photo.thumbnailUrl)
            .allowHardware(false)
            .build()

        // Send request and get the image result
        val imageResult = imageLoader.execute(request)
        if (imageResult !is SuccessResult) {
            Log.d(
                TAG,
                "Image request failed: ${photo.id}, album: ${photo.albumId}: ${(imageResult as ErrorResult).throwable.message}"
            )
            return null
        }

        val result = imageResult
        val image = result.image

        val ninePatchChunk = bitmapToByteArray(image.toBitmap(150, 150))
        Log.d(TAG, "Adding bitmap to map ${photo.id}, album: ${photo.albumId}")
        thumbBitmapMap[photo.thumbnailUrl] = ninePatchChunk
        return ninePatchChunk
    }

    private fun bitmapToByteArray(
        bitmap: Bitmap,
        format: android.graphics.Bitmap.CompressFormat = android.graphics.Bitmap.CompressFormat.PNG,
        quality: Int = 100
    ): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(format, quality, outputStream) // Compress the Bitmap
        return outputStream.toByteArray() // Convert to ByteArray
    }
}