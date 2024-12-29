package com.hasib.samsungalbumshowcase

import com.hasib.samsungalbumshowcase.data.api.ApiService
import com.hasib.samsungalbumshowcase.data.repositories.PhotoRepositoryImpl
import com.hasib.samsungalbumshowcase.domain.models.Photo
import com.hasib.samsungalbumshowcase.domain.models.Result
import com.hasib.samsungalbumshowcase.domain.repositories.PhotoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class PhotoRepositoryImplTest {

    @Mock
    private lateinit var apiService: ApiService
    private lateinit var photoRepository: PhotoRepository

    private val mockPhotos = listOf(
        Photo(id = 1, albumId = 1, title = "Photo 1", url = "url1", thumbnailUrl = "thumb1"),
        Photo(id = 2, albumId = 2, title = "Photo 2", url = "url2", thumbnailUrl = "thumb2")
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        photoRepository = PhotoRepositoryImpl(apiService)
    }

    @Test
    fun `fetchPhotos should return cached photos if available`() = runTest {
        `when`(apiService.getPhotos(any(), any())).thenReturn(Response.success(mockPhotos))

        var result = photoRepository.fetchPhotos(page = 1, limit = 2)
        result = photoRepository.fetchPhotos(page = 1, limit = 2)

        verify(apiService, times(1)).getPhotos(any(), any())
        assertTrue(result is Result.Success)
        assertEquals(mockPhotos, (result as Result.Success).data)
    }

    @Test
    fun `fetchPhotos should fetch photos from API when cache is not sufficient`() = runTest {
        `when`(apiService.getPhotos(page = 1, limit = 2)).thenReturn(Response.success(mockPhotos))

        val result = photoRepository.fetchPhotos(page = 1, limit = 2)

        verify(apiService).getPhotos(page = 1, limit = 2)
        assertTrue(result is Result.Success)
        assertEquals(mockPhotos, (result as Result.Success).data)
    }

    @Test
    fun `fetchPhotos should handle API failure`() = runTest {
        val exception = RuntimeException("API error")
        `when`(apiService.getPhotos(any(), any())).thenThrow(exception)

        val result = photoRepository.fetchPhotos(page = 1, limit = 2)

        verify(apiService).getPhotos(any(), any())
        assertTrue(result is Result.BaseError)
        assertEquals(exception, (result as Result.BaseError.Exception).e)
    }

    @Test
    fun `getPhotoByAlbumId should return the correct photo`() = runTest {
        val field = photoRepository.javaClass.getDeclaredField("albumMap")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        (field.get(photoRepository) as MutableMap<Int, Photo>)[1] = mockPhotos[0]

        val photo = photoRepository.getPhotoByAlbumId(1)

        assertNotNull(photo)
        assertEquals(mockPhotos[0], photo)
    }

    @Test
    fun `getPhotoByAlbumId should return null if album ID does not exist`() = runTest {
        val photo = photoRepository.getPhotoByAlbumId(99)

        assertNull(photo)
    }
}