package com.hasib.samsungalbumshowcase.ui.features.albums

import android.content.Context
import android.content.ServiceConnection
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasib.samsungalbumshowcase.domain.entities.PhotoDisplay
import com.hasib.samsungalbumshowcase.domain.entities.Result
import com.hasib.samsungalbumshowcase.domain.entities.doOnAnyTypeError
import com.hasib.samsungalbumshowcase.domain.entities.doOnSuccess
import com.hasib.samsungalbumshowcase.domain.usecase.FetchImageUseCase
import com.hasib.samsungalbumshowcase.service.ImageFetchingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val ITEMS_PER_PAGE = 5000
private const val TAG = "ImageViewModel"

@HiltViewModel
class ImageViewModel @Inject constructor(private val fetchImageUseCase: FetchImageUseCase) :
    ViewModel() {
    private val _photoItems = mutableStateListOf<PhotoDisplay>()
    val photoItems: SnapshotStateList<PhotoDisplay> get() = _photoItems

    val errorMessage: MutableState<String> = mutableStateOf("")

    private var page = 1
    private var isDataFetching = false

    fun startImageFetchingService(context: Context, serviceConnection: ServiceConnection) {
        Log.d(TAG, "Starting service for page: $page")
        if (isDataFetching) {
            return
        }

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

    suspend fun handlePhotoDisplayResult(result: Result<List<PhotoDisplay>>) {
        result.doOnSuccess {
            if (it.isEmpty()) {
                return@doOnSuccess
            }
            Log.d(TAG, "Received: ${it.map { photo -> photo.photo.id }}")
            _photoItems.addAll(it)
            page++
        }

        result.doOnAnyTypeError {
            errorMessage.value = it
        }

        isDataFetching = false
    }
}