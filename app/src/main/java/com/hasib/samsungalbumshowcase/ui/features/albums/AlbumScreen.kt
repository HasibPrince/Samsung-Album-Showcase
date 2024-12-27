package com.hasib.samsungalbumshowcase.ui.features.albums

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.Bitmap
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.request.crossfade
import coil3.request.transformations
import coil3.toBitmap
import coil3.transform.RoundedCornersTransformation
import com.hasib.samsungalbumshowcase.R
import com.hasib.samsungalbumshowcase.domain.entities.PhotoDisplay

@Composable
fun AlbumScreen() {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        AlbumList(modifier = Modifier, innerPadding)
        SimpleAlertDialog()
    }
}

@Composable
private fun AlbumList(
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues,
    viewModel: ImageViewModel = viewModel()
) {
    val context = LocalContext.current
    val photoListState = viewModel.photoItems
    val photoDisplays = photoListState
    val lazyListState = rememberLazyListState()
    val shouldStartPaginate = remember {
        derivedStateOf {
            (lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                ?: -100) >= (lazyListState.layoutInfo.totalItemsCount - 2)

        }
    }

    LaunchedEffect(shouldStartPaginate.value) {
        if (photoDisplays.isEmpty()) {
            viewModel.startImageFetchingService(context)
            return@LaunchedEffect
        }

        if (shouldStartPaginate.value) {
            viewModel.startImageFetchingService(context)
        }
    }

    LazyColumn(modifier = Modifier.padding(innerPadding), state = lazyListState) {
        items(photoDisplays, key = { it.photo.thumbnailUrl }) { item ->
            ItemAlbum(modifier = modifier, item = item)
        }

        item {
            if (photoDisplays.isNotEmpty()) {
                LoadingIndicator()
            }
        }
    }
}

@Composable
private fun ItemAlbum(modifier: Modifier, item: PhotoDisplay) {
    Box(modifier = modifier.padding(16.dp)) {
        Row(modifier = modifier.padding(5.dp)) {
            LoadImageManually(item.thumbPhoto?.thumbnailUrl ?: "", modifier = Modifier.size(80.dp))
            Column(modifier = modifier.padding(horizontal = 5.dp)) {
                Text(item.albumName)
                Text(item.photo.title)
                Text(item.username)
            }
        }
    }
}

@Composable
fun LoadOptimizedImage(imageUrl: String) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .crossfade(true)
            .size( 100, 100)
            .transformations(RoundedCornersTransformation(16f))
            .build(),
        contentDescription = "Loaded Image",
        modifier = Modifier.size(80.dp)
    )
}

@Composable
fun LoadImageManually(imageUrl: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(imageUrl) {
        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .allowHardware(false)
            .build()

        val result = loader.execute(request)
        if (result is SuccessResult) {
            bitmap = (result.image).toBitmap()
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = "Loaded Image",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        Image(
            painter = painterResource(R.drawable.ic_launcher_background),
            contentDescription = "Loading Image",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun SimpleAlertDialog(viewModel: ImageViewModel = viewModel()) {
    var errorMessage = viewModel.errorMessage
    var showDialog = errorMessage.value.isNotEmpty()

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(text = "Error!")
            },
            text = {
                Text(errorMessage.value)
            },
            confirmButton = {
                Button(onClick = { showDialog = false }) {
                    Text("OK")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
