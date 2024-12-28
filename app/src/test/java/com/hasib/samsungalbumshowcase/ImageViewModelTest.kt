package com.hasib.samsungalbumshowcase

import android.content.Context
import android.content.ServiceConnection
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.hasib.samsungalbumshowcase.domain.entities.Photo
import com.hasib.samsungalbumshowcase.domain.entities.PhotoDisplay
import com.hasib.samsungalbumshowcase.domain.entities.Result
import com.hasib.samsungalbumshowcase.domain.usecase.FetchImageUseCase
import com.hasib.samsungalbumshowcase.service.ImageFetchingService
import com.hasib.samsungalbumshowcase.ui.features.albums.ImageViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

@OptIn(ExperimentalCoroutinesApi::class)
class ImageViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var fetchImageUseCase: FetchImageUseCase

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var serviceConnection: ServiceConnection

    private lateinit var viewModel: ImageViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    private val mockPhotos = listOf(
        Photo(id = 1, albumId = 1, title = "Photo 1", url = "url1", thumbnailUrl = "thumb1"),
        Photo(id = 2, albumId = 2, title = "Photo 2", url = "url2", thumbnailUrl = "thumb2")
    )
    private val mockPhotoDisplays = listOf(
        PhotoDisplay(photo = mockPhotos[0], albumName = "Album 1", username = "User 1", thumbPhoto = mockPhotos[0]),
        PhotoDisplay(photo = mockPhotos[1], albumName = "Album 2", username = "User 2", thumbPhoto = mockPhotos[1])
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = ImageViewModel(fetchImageUseCase)
    }

    @Test
    fun `fetchImages should update photoItems and increment page`() = runTest {
        `when`(fetchImageUseCase(1, 5000)).thenReturn(
            flow {
                emit(Result.Success(mockPhotoDisplays))
            }
        )

        viewModel.fetchImages()

        advanceUntilIdle()
        assertEquals(2, viewModel.photoItems.size)
        assertEquals(mockPhotoDisplays, viewModel.photoItems)
        assertEquals(2, viewModel.photoItems.size)
    }

    @Test
    fun `fetchImages should handle errors and update errorMessage`() = runTest {
        val errorMessage = "Failed to fetch photos"
        `when`(fetchImageUseCase(1, 5000)).thenReturn(
            flow {
                emit(Result.BaseError.Error(404, errorMessage))
            }
        )

        viewModel.fetchImages()

        advanceUntilIdle()
        assertEquals(errorMessage, viewModel.errorMessage.value)
    }

    @Test
    fun `handlePhotoDisplayResult should add photos on Success`() = runTest {
        val successResult = Result.Success(mockPhotoDisplays)

        viewModel.handlePhotoDisplayResult(successResult)

        assertEquals(mockPhotoDisplays.size, viewModel.photoItems.size)
    }

    @Test
    fun `handlePhotoDisplayResult should not add photos on empty Success`() = runTest {
        val emptySuccessResult = Result.Success(emptyList<PhotoDisplay>())

        viewModel.handlePhotoDisplayResult(emptySuccessResult)

        assertTrue(viewModel.photoItems.isEmpty())
    }

    @Test
    fun `handlePhotoDisplayResult should update errorMessage on Error`() = runTest {
        val errorResult = Result.BaseError.Error(404, "Not Found")

        viewModel.handlePhotoDisplayResult(errorResult)

        assertEquals("Not Found", viewModel.errorMessage.value)
    }

    @Test
    fun `handlePhotoDisplayResult should update errorMessage on Exception`() = runTest {
        val exceptionResult = Result.BaseError.Exception(Throwable("An unexpected error occurred"))

        viewModel.handlePhotoDisplayResult(exceptionResult)

        assertEquals("An unexpected error occurred", viewModel.errorMessage.value)
    }
}
