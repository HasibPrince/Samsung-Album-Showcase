package com.hasib.samsungalbumshowcase.ui

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.hasib.samsungalbumshowcase.service.ImageFetchingService
import com.hasib.samsungalbumshowcase.ui.features.albums.AlbumScreen
import com.hasib.samsungalbumshowcase.ui.features.albums.ImageViewModel
import com.hasib.samsungalbumshowcase.ui.theme.SamsungAlbumShowcaseTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val imageViewModel: ImageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SamsungAlbumShowcaseTheme {
                AlbumScreen()
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        SamsungAlbumShowcaseTheme {
            AlbumScreen()
        }
    }

    val serviceConnection = object : ServiceConnection {

        private var imageFetchingService: ImageFetchingService? = null

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as ImageFetchingService.LocalBinder
            imageFetchingService = binder.getService()

            lifecycleScope.launch {
                imageFetchingService?.imageList?.flowWithLifecycle(
                    lifecycle,
                    Lifecycle.State.CREATED
                )?.collect {
                    imageViewModel.handlePhotoDisplayResult(it)
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            imageFetchingService = null
        }
    }

    override fun onDestroy() {
        unbindService(serviceConnection)
        super.onDestroy()
    }
}