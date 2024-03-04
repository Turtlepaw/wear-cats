package com.turtlepaw.cats.presentation.pages

import android.graphics.drawable.shapes.Shape
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
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
import androidx.wear.compose.material.Shapes
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
import kotlinx.coroutines.delay
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
import java.time.LocalDate

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

private const val tag = "CatImageFetch"

suspend fun safelyFetch(onSuccess: (data: List<CatPhoto>) -> Unit){
    Log.d(tag, "Fetching images...")
    try {
        val photos = fetchPhotos()
        onSuccess(photos)
    } catch (e: Exception) {
        // Handle error
        Log.e(tag, "Failed to fetch photos: $e")
        delay(5000)
        // Auto Retry
        safelyFetch(onSuccess)
    }
}

suspend fun safelyFetchAsync(limit: Int = 1): List<CatPhoto> {
    Log.d(tag, "Fetching images...")
    try {
        val photos = fetchPhotos(limit)
        return photos
    } catch (e: Exception) {
        // Handle error
        Log.e(tag, "Failed to fetch photos: $e")
        delay(5000)
        // Auto Retry
        return safelyFetchAsync(limit)
    }
}

@OptIn(ExperimentalHorologistApi::class, ExperimentalWearFoundationApi::class)
@Composable
fun WearHome() {
    SleepTheme {
        val focusRequester = rememberActiveFocusRequester()
        val scalingLazyListState = rememberScalingLazyListState()
        // Use remember to store the result and trigger recomposition when it changes
        var catPhotos by remember { mutableStateOf<List<CatPhoto>>(emptyList()) }
        var currentCatPhoto by remember { mutableStateOf<CatPhoto?>(null) }
        var isLoading by remember { mutableStateOf<Boolean>(true) }
        val coroutineScope = rememberCoroutineScope()
        val lifecycleOwner = LocalLifecycleOwner.current
        val state by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
        // Suspended functions
        LaunchedEffect(state) {
            if(currentCatPhoto == null){
                isLoading = true
                safelyFetch { data ->
                    catPhotos = data
                    currentCatPhoto = data.first()
                    isLoading = false
                }
            }
        }

        // Fetch data on first composition using LaunchedEffect
        LaunchedEffect(true) {
            isLoading = true
            safelyFetch { data ->
                catPhotos = data
                currentCatPhoto = data.first()
                isLoading = false
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
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        coroutineScope.launch {
                                            if(!isLoading){
                                                isLoading = true
                                                safelyFetch { data ->
                                                    catPhotos = data
                                                    currentCatPhoto = data.first()
                                                    isLoading = false
                                                }
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
                                    isLoading = true
                                    safelyFetch { data ->
                                        catPhotos = data
                                        currentCatPhoto = data.first()
                                        isLoading = false
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = MaterialTheme.colors.primary
                            ),
                            enabled = !isLoading
                        ) {
                            if(isLoading){
                                CircularProgressIndicator(indicatorColor = MaterialTheme.colors.primary)
                            } else {
                                Text(text = "Refresh")
                            }
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
suspend fun fetchPhotos(limit: Int = 1): List<CatPhoto> {
    return withContext(Dispatchers.IO) {
        // Perform network operations in the IO dispatcher
        val apiKey = "API_KEY"
        val apiUrl = "https://api.thecatapi.com/v1/images/search?limit=${limit}"

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