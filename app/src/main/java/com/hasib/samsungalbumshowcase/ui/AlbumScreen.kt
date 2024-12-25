package com.hasib.samsungalbumshowcase.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hasib.samsungalbumshowcase.R
import com.hasib.samsungalbumshowcase.domain.entities.PhotoDisplay
import com.hasib.samsungalbumshowcase.domain.entities.Result

@Composable
fun AlbumScreen() {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        AlbumList(modifier = Modifier, innerPadding)
    }
}

@Composable
private fun AlbumList(modifier: Modifier = Modifier, innerPadding: PaddingValues, viewModel: ImageViewModel = viewModel()) {
    val items = viewModel.fetchImages().collectAsState(initial = Result.Success(emptyList()))

    LazyColumn(modifier = Modifier.padding(innerPadding)) {
        items((items.value as Result.Success<List<PhotoDisplay>>).data) { item ->
            ItemAlbum(modifier = modifier, item = item)
        }
    }
}

@Composable
private fun ItemAlbum(modifier: Modifier, item: PhotoDisplay) {
    Box(modifier = modifier.padding(16.dp)) {
        Row(modifier = modifier.padding(5.dp)) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_background),
                contentDescription = null,
                modifier = modifier.padding(5.dp).width(60.dp).height(60.dp)
            )
            Column (modifier = modifier.padding(horizontal = 5.dp)) {
                Text(item.albumName)
                Text(item.photo.title)
                Text(item.username)
            }
        }
    }
}