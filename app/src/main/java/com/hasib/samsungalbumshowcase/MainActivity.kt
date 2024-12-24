package com.hasib.samsungalbumshowcase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hasib.samsungalbumshowcase.ui.theme.SamsungAlbumShowcaseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SamsungAlbumShowcaseTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AlbumList(modifier = Modifier, innerPadding)
                }
            }
        }
    }

    @Composable
    private fun AlbumList(modifier: Modifier = Modifier, innerPadding: PaddingValues) {
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            items(100) {
                ItemAlbum(modifier = modifier)
            }
        }
    }

    @Composable
    private fun ItemAlbum(modifier: Modifier) {
        Box(modifier = modifier.padding(16.dp)) {
            Row(modifier = modifier.padding(5.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_background),
                    contentDescription = null,
                    modifier = modifier.padding(5.dp).width(60.dp).height(60.dp)
                )
                Column (modifier = modifier.padding(horizontal = 5.dp)) {
                    Text("Image Title")
                    Text("Title of Album")
                    Text("User name")
                }
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        SamsungAlbumShowcaseTheme {
            AlbumList(Modifier, PaddingValues(5.dp))
        }
    }
}