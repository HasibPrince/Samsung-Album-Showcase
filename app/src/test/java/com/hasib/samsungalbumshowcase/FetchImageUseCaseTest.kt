import com.hasib.samsungalbumshowcase.domain.entities.*
import com.hasib.samsungalbumshowcase.domain.repositories.AlbumRepository
import com.hasib.samsungalbumshowcase.domain.repositories.PhotoRepository
import com.hasib.samsungalbumshowcase.domain.repositories.UserRepository
import com.hasib.samsungalbumshowcase.domain.usecase.FetchImageUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

@OptIn(ExperimentalCoroutinesApi::class)
class FetchImageUseCaseTest {

    @Mock
    private lateinit var photoRepository: PhotoRepository

    @Mock
    private lateinit var albumRepository: AlbumRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @InjectMocks
    private lateinit var fetchImageUseCase: FetchImageUseCase

    private val mockPhotos = listOf(
        Photo(id = 1, albumId = 1, title = "Photo 1", url = "url1", thumbnailUrl = "thumb1"),
        Photo(id = 2, albumId = 2, title = "Photo 2", url = "url2", thumbnailUrl = "thumb2")
    )

    private val mockAlbums = listOf(
        Album(id = 1, title = "Album 1", userId = 1),
        Album(id = 2, title = "Album 2", userId = 2)
    )

    private val mockUsers = listOf(
        User(id = 1, name = "User 1", username = "user1"),
        User(id = 2, name = "User 2", username = "user2")
    )

    private val mockPhotoDisplays = listOf(
        PhotoDisplay(
            photo = mockPhotos[0],
            albumName = mockAlbums[0].title,
            username = mockUsers[0].username,
            thumbPhoto = mockPhotos[0]
        ),
        PhotoDisplay(
            photo = mockPhotos[1],
            albumName = mockAlbums[1].title,
            username = mockUsers[1].username,
            thumbPhoto = mockPhotos[1]
        )
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `invoke should fetch and map photos correctly`() = runTest {
        `when`(albumRepository.fetchAlbums()).thenReturn(Result.Success(mockAlbums))
        `when`(userRepository.fetchUsers()).thenReturn(Result.Success(mockUsers))
        `when`(photoRepository.fetchPhotos(eq(1), eq(10))).thenReturn(Result.Success(mockPhotos))
        `when`(photoRepository.getPhotoByAlbumId(anyInt())).thenAnswer { invocation ->
            val albumId = invocation.arguments[0] as Int
            mockPhotos.firstOrNull { it.albumId == albumId }
        }

        val result = fetchImageUseCase(page = 1, limit = 10).first()

        assertTrue(result is Result.Success)
        assertEquals(mockPhotoDisplays, (result as Result.Success).data)

        verify(albumRepository).fetchAlbums()
        verify(userRepository).fetchUsers()
        verify(photoRepository).fetchPhotos(1, 10)
        verify(photoRepository, times(mockPhotos.size)).getPhotoByAlbumId(anyInt())
    }

    @Test
    fun `invoke should handle repository errors gracefully`() = runTest {
        val errorResult = Result.BaseError.Exception(Exception("Repository Error"))
        `when`(albumRepository.fetchAlbums()).thenReturn(errorResult)
        `when`(userRepository.fetchUsers()).thenReturn(Result.Success(mockUsers))
        `when`(photoRepository.fetchPhotos(anyInt(), anyInt())).thenReturn(Result.Success(mockPhotos))

        val result = fetchImageUseCase(page = 1, limit = 10).first()

        assertTrue(result is Result.BaseError)
        assertEquals(errorResult, result)
    }

    @Test
    fun `invoke should return error if photo processing fails`() = runTest {
        `when`(albumRepository.fetchAlbums()).thenReturn(Result.Success(mockAlbums))
        `when`(userRepository.fetchUsers()).thenReturn(Result.Success(mockUsers))
        `when`(photoRepository.fetchPhotos(anyInt(), anyInt())).thenReturn(Result.Success(mockPhotos))

        `when`(photoRepository.getPhotoByAlbumId(anyInt())).thenThrow(NoSuchElementException("No thumbnail found"))

        val result = fetchImageUseCase(page = 1, limit = 10).first()

        assertTrue(result is Result.BaseError)
        assertTrue((result as Result.BaseError.Exception).e is NoSuchElementException)

        verify(albumRepository).fetchAlbums()
        verify(userRepository).fetchUsers()
        verify(photoRepository).fetchPhotos(1, 10)
    }
}