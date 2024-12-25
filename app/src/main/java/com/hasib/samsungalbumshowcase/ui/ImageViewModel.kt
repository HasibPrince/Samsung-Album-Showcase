package com.hasib.samsungalbumshowcase.ui

import androidx.lifecycle.ViewModel
import com.hasib.samsungalbumshowcase.domain.FetchImageUseCase
import com.hasib.samsungalbumshowcase.domain.entities.PhotoDisplay
import com.hasib.samsungalbumshowcase.domain.entities.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class ImageViewModel @Inject constructor(private val fetchImageUseCase: FetchImageUseCase) : ViewModel() {
    fun fetchImages(): Flow<Result<List<PhotoDisplay>>> {
        return fetchImageUseCase.invoke(1, 20)
    }
}