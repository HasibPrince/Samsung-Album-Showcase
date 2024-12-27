package com.hasib.samsungalbumshowcase.ui.features.albums

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasib.samsungalbumshowcase.ImageFetchingService
import com.hasib.samsungalbumshowcase.domain.entities.PhotoDisplay
import com.hasib.samsungalbumshowcase.domain.entities.Result
import com.hasib.samsungalbumshowcase.domain.entities.doOnAnyTypeError
import com.hasib.samsungalbumshowcase.domain.entities.doOnSuccess
import com.hasib.samsungalbumshowcase.domain.usecase.FetchImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val ITEMS_PER_PAGE = 5000

private const val TAG = "ImageViewModel"

@HiltViewModel
class ImageViewModel @Inject constructor(private val fetchImageUseCase: FetchImageUseCase) :
    ViewModel(), ImageFetchingService.OnImageFetchedListener {
    private val _photoItems = mutableStateListOf<PhotoDisplay>()
    val photoItems: SnapshotStateList<PhotoDisplay> get() = _photoItems

    private val _errorMessage = mutableStateOf("")
    val errorMessage: MutableState<String> get() = _errorMessage

    private var page = 1
    private var isDataFetching = false

    fun startImageFetchingService(context: Context) {
        Log.d(TAG, "Starting service")
        if (isDataFetching) return
        isDataFetching = true
        ImageFetchingService.startImageFetchingService(
            context,
            serviceConnection,
            page,
            ITEMS_PER_PAGE
        )
    }

    fun fetchImages() {
        viewModelScope.launch {
            fetchImageUseCase(page, ITEMS_PER_PAGE).collect {
                handlePhotoDisplayResult(it)
            }
        }
    }

    private suspend fun handlePhotoDisplayResult(result: Result<List<PhotoDisplay>>) {
        result.doOnSuccess {
            Log.d(TAG, "Received: ${it.map { photo -> photo.photo.id }}")
            _photoItems.addAll(it)
            page++
        }

        result.doOnAnyTypeError {
            _errorMessage.value = it
        }
    }

    private val serviceConnection = object : ServiceConnection {

        private var imageFetchingService: ImageFetchingService? = null

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.d(TAG, "Service connected")
            val binder = service as ImageFetchingService.LocalBinder
            imageFetchingService = binder.getService()
            imageFetchingService?.listener = this@ImageViewModel
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.d(TAG, "Service disconnected")
            imageFetchingService?.listener = null
            imageFetchingService = null
            isDataFetching = false
        }
    }

    override suspend fun onImageFetched(photoList: Result<List<PhotoDisplay>>) {
        handlePhotoDisplayResult(photoList)
        isDataFetching = false
    }

}