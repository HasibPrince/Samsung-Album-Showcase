package com.hasib.samsungalbumshowcase.ui.features.albums

import android.graphics.drawable.ColorDrawable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.placeholder
import coil3.request.transformations
import coil3.transform.RoundedCornersTransformation
import com.hasib.samsungalbumshowcase.domain.entities.PhotoDisplay
import com.hasib.samsungalbumshowcase.ui.MainActivity
import com.hasib.samsungalbumshowcase.ui.theme.PurpleGrey80

@Composable
fun AlbumScreen(viewModel: ImageViewModel = viewModel()) {
    val photoListState = viewModel.photoItems

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(Modifier.fillMaxSize()) {
            AlbumList(modifier = Modifier, innerPadding)
            SimpleAlertDialog()
            if (photoListState.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun AlbumList(
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues,
    viewModel: ImageViewModel = viewModel()
) {
    val context = LocalContext.current
    val photoDisplays = viewModel.photoItems
    val lazyListState = rememberLazyListState()
    val shouldStartPaginate = remember {
        derivedStateOf {
            (lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                ?: -100) >= (lazyListState.layoutInfo.totalItemsCount - 2)
        }
    }

    LaunchedEffect(shouldStartPaginate.value) {
        if (photoDisplays.isEmpty()) {
            viewModel.startImageFetchingService(
                context,
                (context as MainActivity).serviceConnection
            )
            return@LaunchedEffect
        }

        if (shouldStartPaginate.value) {
            viewModel.startImageFetchingService(
                context,
                (context as MainActivity).serviceConnection
            )
        }
    }

    LazyColumn(modifier = Modifier.padding(innerPadding), state = lazyListState) {
        items(photoDisplays, key = { it.photo.thumbnailUrl }) { item ->
            ItemAlbum(modifier = modifier, item = item)
        }

        item {
            if (photoDisplays.isNotEmpty()) {
                LoadingIndicator(modifier)
            }
        }
    }
}

@Composable
private fun ItemAlbum(modifier: Modifier, item: PhotoDisplay) {
    ItemBox(modifier) {
        ItemContent(item)
    }
}

@Composable
private fun ItemBox(modifier: Modifier, content: @Composable BoxScope.() -> Unit) {
    Card(
        modifier = modifier.padding(16.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun ItemContent(item: PhotoDisplay) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LoadOptimizedImage(
            item.thumbPhoto?.thumbnailUrl ?: "",
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(2.dp, Color.Gray, RoundedCornerShape(8.dp))
        )

        LoadImageInfo(item)
    }
}

@Composable
private fun LoadImageInfo(item: PhotoDisplay) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(start = 8.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Album: ${item.albumName}",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = Bold,
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Photo title: ${item.photo.title}",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontStyle = FontStyle.Italic,
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Username: ${item.username}",
            style = MaterialTheme.typography.bodySmall.copy()
        )
    }
}

@Composable
fun LoadOptimizedImage(imageUrl: String, modifier: Modifier = Modifier) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .placeholder(ColorDrawable(PurpleGrey80.hashCode()))
            .crossfade(true)
            .size(100, 100)
            .transformations(RoundedCornersTransformation(16f))
            .build(),
        contentDescription = "Loaded Image",
        modifier = modifier.size(80.dp)
    )
}

@Composable
fun LoadingIndicator(modifier: Modifier) {
    ItemBox(modifier) {
        CircularProgressIndicator()
    }
}

@Composable
fun SimpleAlertDialog(viewModel: ImageViewModel = viewModel()) {
    var errorMessage = viewModel.errorMessage
    var showDialog = errorMessage.value.isNotEmpty()

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.errorMessage.value = "" },
            title = {
                Text(text = "Error!")
            },
            text = {
                Text(errorMessage.value)
            },
            confirmButton = {
                Button(onClick = { viewModel.errorMessage.value = "" }) {
                    Text("OK")
                }
            },
            dismissButton = {
                Button(onClick = { viewModel.errorMessage.value = "" }) {
                    Text("Cancel")
                }
            }
        )
    }
}
