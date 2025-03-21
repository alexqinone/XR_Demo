package com.example.xr_demo.rendering

import android.os.Bundle
import android.print.PrintAttributes
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.xr_demo.R
import com.example.xr_demo.ui.theme.XR_DemoTheme

class RenderingActivity : ComponentActivity(){

    companion object {
        const val GLB_FILE = "shiba_inu_texture_updated.glb"
    }

    private val thumbnails = listOf(
        R.drawable.emoji_01,
        R.drawable.emoji_02,
        R.drawable.emoji_03,
        R.drawable.emoji_04,
        R.drawable.emoji_05,
        R.drawable.emoji_06
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XR_DemoTheme {
                renderingScreen(thumbnails)
            }
        }
    }

    @Composable
    fun renderingScreen(thumbnailUrls: List<Int>) {
        LazyRow (
            modifier = Modifier
                .fillMaxSize()
                .padding(100.dp)
                .widthIn(0.dp, 0.dp),
//                .horizontalScroll(rememberScrollState()),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(30.dp)
        ) {
            items(thumbnailUrls.size) {
                val imageUrl = thumbnailUrls[it]
                thumbnailsCard(imageUrl)
            }
        }
    }

    @Composable
    fun thumbnailsCard(thumbUrls: Int) {
        var isHovered by remember { mutableStateOf(false) }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            when (event.type) {
                                PointerEventType.Enter -> isHovered = true
                                PointerEventType.Exit -> isHovered = false
                                else -> {}
                            }
                        }
                    }
                },
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            AsyncImage(
                model = thumbUrls,
                contentDescription = "Images",
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        Toast.makeText(baseContext, "Image Clicked: $thumbUrls", Toast.LENGTH_SHORT).show()
                    }
            )

            if (isHovered) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                )
            }
        }
    }




}