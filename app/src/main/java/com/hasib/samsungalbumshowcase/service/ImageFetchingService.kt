package com.hasib.samsungalbumshowcase.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import android.util.Log
import coil3.Bitmap
import coil3.ImageLoader
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import com.hasib.samsungalbumshowcase.domain.models.PhotoDisplay
import com.hasib.samsungalbumshowcase.domain.models.Result
import com.hasib.samsungalbumshowcase.domain.models.doOnSuccess
import com.hasib.samsungalbumshowcase.domain.usecase.FetchImageUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


private const val TAG = "ImageFetchingService"

@AndroidEntryPoint
class ImageFetchingService : Service() {

    private val coroutineScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    @Inject
    lateinit var fetchImageUseCase: FetchImageUseCase

    private val binder = LocalBinder()

    private val _imageList =
        MutableStateFlow<Result<List<PhotoDisplay>>>(Result.Success(emptyList<PhotoDisplay>()))
    val imageList: StateFlow<Result<List<PhotoDisplay>>> get() = _imageList

    private val thumbBitmapMap = mutableMapOf<String, Bitmap>()

    inner class LocalBinder : Binder() {
        fun getService(): ImageFetchingService = this@ImageFetchingService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(TAG, "Service started")

        val page = intent?.getIntExtra(KEY_PAGE, 1) ?: 1
        val itemsPerPage = intent?.getIntExtra(KEY_ITEMS_PER_PAGE, 20) ?: 20
        coroutineScope.launch {
            fetchImageUseCase(page, itemsPerPage).collect {
                Log.d(TAG, "Received result, starting thumbnail loading")
                withContext(Dispatchers.IO) {
                    it.doOnSuccess {
                        updateThumbBitmap(it)
                    }
                }
                Log.d(TAG, "Finished thumbnail loading")
                _imageList.value = it
            }
        }
        return START_STICKY
    }

    private suspend fun updateThumbBitmap(displays: List<PhotoDisplay>) {
        displays.forEach { photoDisplay ->
            if (photoDisplay.thumbPhoto == null) {
                return@forEach
            }

            if (thumbBitmapMap.containsKey(photoDisplay.thumbPhoto.thumbnailUrl)) {
                Log.d(TAG, "Found bitmap in map ${photoDisplay.photo.id}, album: ${photoDisplay.photo.albumId}")
                photoDisplay.thumbnail = thumbBitmapMap[photoDisplay.thumbPhoto.thumbnailUrl]
            } else {
                Log.d(TAG, "Loading bitmap for photo: ${photoDisplay.photo.id}, album: ${photoDisplay.photo.albumId}")
                val bitmap = loadImageAsBitmap(photoDisplay)
                if (bitmap != null) {
                    thumbBitmapMap[photoDisplay.thumbPhoto.thumbnailUrl] = bitmap
                    photoDisplay.thumbnail = bitmap
                }
            }
        }
    }

    private suspend fun loadImageAsBitmap(photoDisplay: PhotoDisplay): Bitmap? {
        val imageLoader = ImageLoader(applicationContext)
        val request = ImageRequest.Builder(applicationContext)
            .data(photoDisplay.thumbPhoto?.thumbnailUrl)
            .allowHardware(false)
            .build()

        // Send request and get the image result
        val imageResult = imageLoader.execute(request)
        if (imageResult !is SuccessResult) {
            Log.d(TAG, "Image request failed: ${photoDisplay.photo.id}, album: ${photoDisplay.photo.albumId}: ${(imageResult as ErrorResult).throwable.message}")
        }
        val result = imageResult as? SuccessResult
        val image = result?.image
        return image?.toBitmap(150, 150)
    }

    companion object {

        private const val KEY_PAGE = "page"
        private const val KEY_ITEMS_PER_PAGE = "items"

        fun startImageFetchingService(
            context: Context,
            serviceConnection: ServiceConnection,
            page: Int,
            itemsPerPage: Int,
            bindType: Int = BIND_AUTO_CREATE
        ) {
            val serviceIntent = Intent(context, ImageFetchingService::class.java)
            serviceIntent.putExtra(KEY_PAGE, page)
            serviceIntent.putExtra(KEY_ITEMS_PER_PAGE, itemsPerPage)
            context.startService(serviceIntent)
            context.bindService(serviceIntent, serviceConnection, bindType)
        }
    }
}