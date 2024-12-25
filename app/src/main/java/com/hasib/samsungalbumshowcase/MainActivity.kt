package com.hasib.samsungalbumshowcase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.hasib.samsungalbumshowcase.ui.AlbumScreen
import com.hasib.samsungalbumshowcase.ui.theme.SamsungAlbumShowcaseTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
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
}