package com.hasib.samsungalbumshowcase

import com.hasib.samsungalbumshowcase.data.api.ApiService
import com.hasib.samsungalbumshowcase.data.repositories.UserRepositoryImpl
import com.hasib.samsungalbumshowcase.domain.models.Result
import com.hasib.samsungalbumshowcase.domain.models.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import retrofit2.HttpException
import retrofit2.Response

@ExperimentalCoroutinesApi
class UserRepositoryImplTest {

    @Mock
    private lateinit var apiService: ApiService
    private lateinit var userRepository: UserRepositoryImpl

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        userRepository = UserRepositoryImpl(apiService)
    }

    @Test
    fun `fetchUsers should return cached users if already fetched`() = runTest {
        val mockUsers = listOf(
            User(id = 1, name = "Fahim Salam", "fahimsalam"),
            User(id = 2, name = "Minhazur Rahman", "minhazur")
        )

        `when`(apiService.getUsers()).thenReturn(Response.success(mockUsers))

        var result = userRepository.fetchUsers()
        result = userRepository.fetchUsers()

        assertTrue(result is Result.Success)
        assertEquals(mockUsers, (userRepository.fetchUsers() as Result.Success).data)
        verify(apiService, times(1)).getUsers()
    }

    @Test
    fun `fetchUsers should fetch from API when cache is empty`() = runTest {
        val mockUsers = listOf(
            User(id = 1, name = "Fahim Salam", "fahimsalam"),
            User(id = 2, name = "Minhazur Rahman", "minhazur")
        )
        val response = Response.success(mockUsers)
        `when`(apiService.getUsers()).thenReturn(response)

        val result = userRepository.fetchUsers()

        assertTrue(result is Result.Success)
        assertEquals(mockUsers, (result as Result.Success).data)
        verify(apiService, times(1)).getUsers()
    }

    @Test
    fun `fetchUsers should return API error if call fails`() = runTest {
        val mockError = HttpException(Response.error<List<User>>(404, ResponseBody.create(null, "")))
        `when`(apiService.getUsers()).thenThrow(mockError)

        val result = userRepository.fetchUsers()

        assertTrue(result is Result.BaseError)
        assertEquals(404, (result as Result.BaseError.Error).code)
    }
}
