package com.hasib.samsungalbumshowcase.ui

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasib.samsungalbumshowcase.domain.FetchImageUseCase
import com.hasib.samsungalbumshowcase.domain.entities.PhotoDisplay
import com.hasib.samsungalbumshowcase.domain.entities.Result
import com.hasib.samsungalbumshowcase.domain.entities.doOnAnyTypeError
import com.hasib.samsungalbumshowcase.domain.entities.doOnSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val ITEMS_PER_PAGE = 20

@HiltViewModel
class ImageViewModel @Inject constructor(private val fetchImageUseCase: FetchImageUseCase) :
    ViewModel() {
    private val _photoItems = mutableStateListOf<PhotoDisplay>()
    val photoItems: SnapshotStateList<PhotoDisplay> get() = _photoItems

    private val _errorMessage = mutableStateOf("")
    val errorMessage: MutableState<String> get() = _errorMessage

    private var page = 1

    fun fetchImages() {
        viewModelScope.launch {
            fetchImageUseCase(page, ITEMS_PER_PAGE).collect {
                handlePhotoDisplayResult(it)
            }
        }
    }

    private suspend fun handlePhotoDisplayResult(result: Result<List<PhotoDisplay>>) {
        result.doOnSuccess {
            Log.d("ImageViewModel", "Received: ${it.map { photo -> photo.photo.id }}")
            _photoItems.addAll(it)
            page++
        }

        result.doOnAnyTypeError {
            _errorMessage.value = it
        }
    }
}