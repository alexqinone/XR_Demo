package com.example.xr_demo.gallery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.xr_demo.ui.theme.XR_DemoTheme
import com.example.xr_demo.R
import coil.compose.AsyncImage

class GalleryActivity: ComponentActivity() {

    private val images = listOf(
        R.drawable.pexels_pixabay_01,
        R.drawable.pexels_pixabay_02,
        R.drawable.pexels_pixabay_03,
        R.drawable.pexels_pixabay_04,
        R.drawable.pexels_pixabay_05,
        R.drawable.pexels_pixabay_06,
        R.drawable.pexels_pixabay_07,
        R.drawable.pexels_apasaric_01,
        R.drawable.pexels_eberhardgross_01,
        R.drawable.pexels_eberhardgross_02,
        R.drawable.pexels_life_of_pix_01,
        R.drawable.pexels_markusspiske_01,
        R.drawable.pexels_nextvoyage_01,
        R.drawable.pexels_pixabay_11,
        R.drawable.pexels_pixabay_12
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            XR_DemoTheme {
                GalleryScreen(images)
            }
        }
    }

    @Composable
    fun GalleryScreen(imageUrls: List<Int>) {
        LazyRow (
            modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .widthIn(0.dp, 0.dp),
//            .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(imageUrls.size) {
                val imageUrl = imageUrls[it]
                ImageCard(imageUrl)
            }
        }
    }

    @Composable
    fun ImageCard(imageUrlId: Int) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            AsyncImage(
                model = imageUrlId,
                contentDescription = "Images",
//                contextScale = ContentScale.Crop,
//                modifier = Modifier.fillMaxSize()
            )
        }
    }
}