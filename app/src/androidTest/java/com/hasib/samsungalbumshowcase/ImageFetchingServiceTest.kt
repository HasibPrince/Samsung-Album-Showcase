package com.hasib.samsungalbumshowcase

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hasib.samsungalbumshowcase.domain.entities.Photo
import com.hasib.samsungalbumshowcase.domain.entities.PhotoDisplay
import com.hasib.samsungalbumshowcase.domain.entities.Result
import com.hasib.samsungalbumshowcase.service.ImageFetchingService
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations

@RunWith(AndroidJUnit4::class)
class ImageFetchingServiceTest {

    private lateinit var context: Context
    private var serviceBound = false
    private lateinit var service: ImageFetchingService
    private var binder: ImageFetchingService.LocalBinder? = null

    private val mockPhotos = listOf(
        Photo(id = 1, albumId = 1, title = "Photo 1", url = "url1", thumbnailUrl = "thumb1"),
        Photo(id = 2, albumId = 2, title = "Photo 2", url = "url2", thumbnailUrl = "thumb2")
    )
    private val mockPhotoDisplays = listOf(
        PhotoDisplay(photo = mockPhotos[0], albumName = "Album 1", username = "User 1", thumbPhoto = mockPhotos[0]),
        PhotoDisplay(photo = mockPhotos[1], albumName = "Album 2", username = "User 2", thumbPhoto = mockPhotos[1])
    )

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        context = ApplicationProvider.getApplicationContext()

        val serviceIntent = Intent(context, ImageFetchingService::class.java)
        context.startService(serviceIntent)
        context.bindService(serviceIntent, object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                binder = service as ImageFetchingService.LocalBinder
                this@ImageFetchingServiceTest.service = binder!!.getService()
                serviceBound = true
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                serviceBound = false
            }
        }, Context.BIND_AUTO_CREATE)
    }

    @Test
    fun testFetchImages_onStartCommand_ShouldCallFetchImageUseCase() = runTest {
        val page = 1
        val itemsPerPage = 20

        Thread.sleep(3000)

        val serviceIntent = Intent(context, ImageFetchingService::class.java)
        serviceIntent.putExtra("page", page)
        serviceIntent.putExtra("items", itemsPerPage)
        service.onStartCommand(serviceIntent, 0, 1)
        Thread.sleep(2000)

        assert(service.imageList.value is Result.Success)
        assert((service.imageList.value as Result.Success).data.size == 20)
    }

    @Test
    fun testServiceBinding() {
        val serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                binder = service as ImageFetchingService.LocalBinder
                this@ImageFetchingServiceTest.service = binder!!.getService()
                serviceBound = true
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                serviceBound = false
            }
        }

        context.bindService(Intent(context, ImageFetchingService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
        Thread.sleep(2000)

        assert(serviceBound)
    }
}