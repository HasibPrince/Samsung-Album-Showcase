package com.hasib.samsungalbumshowcase

import com.hasib.samsungalbumshowcase.data.api.ApiService
import com.hasib.samsungalbumshowcase.data.repositories.AlbumRepositoryImpl
import com.hasib.samsungalbumshowcase.domain.models.Album
import com.hasib.samsungalbumshowcase.domain.models.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.MockitoAnnotations
import org.mockito.Mockito.`when`
import org.mockito.kotlin.verify
import retrofit2.Response


@OptIn(ExperimentalCoroutinesApi::class)
class AlbumRepositoryImplTest {

    @Mock
    private lateinit var apiService: ApiService
    private lateinit var albumRepository: AlbumRepositoryImpl

    private val mockAlbums = listOf(
        Album(id = 1, title = "Album 1", userId = 1),
        Album(id = 2, title = "Album 2", userId = 2)
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        albumRepository = AlbumRepositoryImpl(apiService)
    }

    @Test
    fun `fetchAlbums should return cached albums if already loaded`() = runTest {

        `when`(apiService.getAlbums()).thenReturn(Response.success(mockAlbums))

        var result = albumRepository.fetchAlbums()
        result = albumRepository.fetchAlbums()

        verify(apiService, times(1)).getAlbums()
        assertTrue(result is Result.Success)
        assertEquals(mockAlbums, (result as Result.Success).data)
    }

    @Test
    fun `fetchAlbums should fetch albums from API when cache is empty`() = runTest {
        `when`(apiService.getAlbums()).thenReturn(Response.success(mockAlbums))

        val result = albumRepository.fetchAlbums()

        verify(apiService).getAlbums()
        assertTrue(result is Result.Success)
        assertEquals(mockAlbums, (result as Result.Success).data)
    }

    @Test
    fun `fetchAlbums should handle API failure`() = runTest {
        val exception = RuntimeException("API error")
        `when`(apiService.getAlbums()).thenThrow(exception)

        val result = albumRepository.fetchAlbums()

        verify(apiService).getAlbums()
        assertTrue(result is Result.BaseError)
        assertEquals(exception, (result as Result.BaseError.Exception).e)
    }
}