package com.hasib.samsungalbumshowcase

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.hasib.samsungalbumshowcase.domain.entities.PhotoDisplay
import com.hasib.samsungalbumshowcase.domain.entities.Result
import com.hasib.samsungalbumshowcase.domain.usecase.FetchImageUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


private const val TAG = "ImageFetchingService"

@AndroidEntryPoint
class ImageFetchingService : Service() {

    private val coroutineScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    @Inject
    lateinit var fetchImageUseCase: FetchImageUseCase

    private val binder = LocalBinder()

    private val _imageList =
        MutableStateFlow<Result<List<PhotoDisplay>>>(Result.Success(emptyList<PhotoDisplay>()))
    val imageList: StateFlow<Result<List<PhotoDisplay>>> get() = _imageList

    inner class LocalBinder : Binder() {
        fun getService(): ImageFetchingService = this@ImageFetchingService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(TAG, "Service started")

        val page = intent?.getIntExtra(KEY_PAGE, 1) ?: 1
        val itemsPerPage = intent?.getIntExtra(KEY_ITEMS_PER_PAGE, 20) ?: 20
        coroutineScope.launch {
            fetchImageUseCase(page, itemsPerPage).collect {
                _imageList.value = it
            }
        }
        return START_STICKY
    }

    companion object {

        private const val KEY_PAGE = "page"
        private const val KEY_ITEMS_PER_PAGE = "items"

        fun startImageFetchingService(
            context: Context,
            serviceConnection: ServiceConnection,
            page: Int,
            itemsPerPage: Int,
            bindType: Int = BIND_AUTO_CREATE
        ) {
            val serviceIntent = Intent(context, ImageFetchingService::class.java)
            serviceIntent.putExtra(KEY_PAGE, page)
            serviceIntent.putExtra(KEY_ITEMS_PER_PAGE, itemsPerPage)
            context.startService(serviceIntent)
            context.bindService(serviceIntent, serviceConnection, bindType)
        }
    }
}