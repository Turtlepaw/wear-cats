package com.turtlepaw.cats.presentation.pages

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material.scrollAway
import androidx.wear.tooling.preview.devices.WearDevices
import coil.compose.SubcomposeAsyncImage
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll
import com.turtlepaw.cats.presentation.components.ItemsListWithModifier
import com.turtlepaw.cats.presentation.theme.SleepTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

@Serializable
data class CatPhoto(
    @SerialName("id")
    val id: String,
    @SerialName("url")
    val url: String,
    @SerialName("width")
    val width: Int,
    @SerialName("height")
    val height: Int
)

@OptIn(ExperimentalHorologistApi::class, ExperimentalWearFoundationApi::class)
@Composable
fun WearHome() {
    SleepTheme {
        val focusRequester = rememberActiveFocusRequester()
        val scalingLazyListState = rememberScalingLazyListState()
        // Use remember to store the result and trigger recomposition when it changes
        var catPhotos by remember { mutableStateOf<List<CatPhoto>>(emptyList()) }
        var currentCatPhoto by remember { mutableStateOf<CatPhoto?>(null) }
        val coroutineScope = rememberCoroutineScope()

        // Fetch data on first composition using LaunchedEffect
        LaunchedEffect(true) {
            Log.d("Cat", "Fetching images...")
            try {
                catPhotos = fetchPhotos()
                currentCatPhoto = catPhotos.first()
            } catch (e: Exception) {
                // Handle error
                Log.e("Cat", "Failed to fetch photos: $e")
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center,
        ) {
            ItemsListWithModifier(
                modifier = Modifier
                    .rotaryWithScroll(
                        reverseDirection = false,
                        focusRequester = focusRequester,
                        scrollableState = scalingLazyListState,
                    ),
                scrollableState = scalingLazyListState,
            ) {
//                item {
//                    // Display the cat photos in your UI
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(8.dp),
//                        verticalArrangement = Arrangement.Top,
//                        horizontalAlignment = Alignment.CenterHorizontally
//                    ) {
//                        // Inside the for loop where you display cat photos
//                        for (catPhoto in catPhotos) {
//                            Text(text = catPhoto.url)
//                            SubcomposeAsyncImage(
//                                model = catPhoto.url,
//                                loading = {
//                                    CircularProgressIndicator()
//                                },
//                                contentDescription = "Cat Photo",
//                                modifier = Modifier.size(100.dp) // Adjust the size as needed
//                            )
//                        }
//                    }
//                }
                if(currentCatPhoto != null){
                    item {
                        Spacer(
                            modifier = Modifier.padding(
                                top = 42.dp,
                                bottom = 5.dp
                            )
                        )
                    }
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                        ){
                            SubcomposeAsyncImage(
                                model = currentCatPhoto!!.url,
                                loading = {
                                    CircularProgressIndicator()
                                },
                                contentDescription = "Cat Photo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable {
                                        coroutineScope.launch {
                                            Log.d("Cat", "Fetching images...")
                                            try {
                                                catPhotos = fetchPhotos()
                                                currentCatPhoto = catPhotos.first()
                                            } catch (e: Exception) {
                                                // Handle error
                                                Log.e("Cat", "Failed to fetch photos: $e")
                                            }
                                        }
                                    }
                            )
                        }
                    }
                    item {
                        Spacer(
                            modifier = Modifier.padding(
                                5.dp
                            )
                        )
                    }
                    item {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    Log.d("Cat", "Fetching images...")
                                    try {
                                        catPhotos = fetchPhotos()
                                        currentCatPhoto = catPhotos.first()
                                    } catch (e: Exception) {
                                        // Handle error
                                        Log.e("Cat", "Failed to fetch photos: $e")
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = MaterialTheme.colors.primary
                            )
                        ) {
                            Text(text = "Refresh")
                        }
                    }
                } else {
                    item {
                        CircularProgressIndicator()
                    }
                }
            }
            TimeText(
                modifier = Modifier.scrollAway(scalingLazyListState)
            )
            PositionIndicator(
                scalingLazyListState = scalingLazyListState
            )
            Vignette(vignettePosition = VignettePosition.TopAndBottom)
        }
    }
}

// Function to fetch data from network
private suspend fun fetchPhotos(): List<CatPhoto> {
    return withContext(Dispatchers.IO) {
        // Perform network operations in the IO dispatcher
        val apiKey = "live_fovq4asUWISV2ny8WGmJNlTXTyzAaqD1KhuDZ6b5FS7GC8OLVbz4NwEk8Wa6rkVm"
        val apiUrl = "https://api.thecatapi.com/v1/images/search?limit=1&api_key=$apiKey"

        val url = URL(apiUrl)
        val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        // Read the response
        val reader = BufferedReader(InputStreamReader(connection.inputStream))
        val response = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            response.append(line)
        }
        reader.close()

        // Parse the JSON response using kotlinx.serialization
        val json = Json { ignoreUnknownKeys = true }
        val catPhotos = json.decodeFromString<List<CatPhoto>>(response.toString())
        Log.d("Cat", catPhotos.toString())
        catPhotos
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearHome()
}