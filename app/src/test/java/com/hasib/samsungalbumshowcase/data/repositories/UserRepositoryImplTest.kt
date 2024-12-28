package com.hasib.samsungalbumshowcase.data.repositories

import com.hasib.samsungalbumshowcase.data.api.ApiService
import com.hasib.samsungalbumshowcase.domain.entities.Album
import com.hasib.samsungalbumshowcase.domain.entities.Photo
import com.hasib.samsungalbumshowcase.domain.entities.User
import com.hasib.samsungalbumshowcase.domain.entities.Result
import kotlinx.coroutines.test.runTest
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import retrofit2.Response

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class UserRepositoryImplTest {

    private lateinit var apiService: ApiService
    private lateinit var userRepo: UserRepositoryImpl

    @Before
    fun setUp() {
        apiService = DummyApiService()
        userRepo = UserRepositoryImpl(apiService)
    }

    @Test
    fun testFetchUsers() = runTest {
        val response = userRepo.fetchUsers()
        assertTrue(response is Result.Success)
        assertEquals(1, (response as Result.Success).data.size)
    }

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}

class DummyApiService : ApiService {
    override suspend fun getPhotos(
        page: Int,
        limit: Int
    ): Response<List<Photo>> {
        TODO("Not yet implemented")
    }

    override suspend fun getAlbums(): Response<List<Album>> {
        TODO("Not yet implemented")
    }

    override suspend fun getUsers(): Response<List<User>> {
        return Response.success(
            listOf(
                User(
                    id = 1,
                    name = "Hasib",
                    username = "hasib"
                )
            )
        )
    }

}