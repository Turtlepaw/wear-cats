package com.turtlepaw.cats.presentation.pages

import AnimalPhoto
import AnimalPhotoSerializer
import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material.scrollAway
import androidx.wear.tooling.preview.devices.WearDevices
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll
import com.turtlepaw.cats.R
import com.turtlepaw.cats.presentation.components.ItemsListWithModifier
import com.turtlepaw.cats.presentation.dataStore
import com.turtlepaw.cats.presentation.theme.SleepTheme
import com.turtlepaw.cats.utils.Animals
import com.turtlepaw.cats.utils.ImageViewModel
import com.turtlepaw.cats.utils.Settings
import com.turtlepaw.cats.utils.SettingsBasics
import com.turtlepaw.cats.utils.decodeByteArray
import com.turtlepaw.cats.utils.enumFromJSON
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

private const val tag = "CatImageFetch"

suspend fun safelyFetch(types: List<Animals>, onSuccess: (data: List<AnimalPhoto>) -> Unit) {
    Log.d(tag, "Fetching images...")
    try {
        val photos = fetchPhotos(10, types)
        onSuccess(photos)
    } catch (e: Exception) {
        // Handle error
        Log.e(tag, "Failed to fetch photos: $e")
        delay(5000)
        // Auto Retry
        safelyFetch(types, onSuccess)
    }
}

suspend fun safelyFetchAsync(limit: Int = 1, types: List<Animals>): List<AnimalPhoto> {
    Log.d(tag, "Fetching images...")
    try {
        val photos = fetchPhotos(limit, types)
        return photos
    } catch (e: Exception) {
        // Handle error
        Log.e(tag, "Failed to fetch photos: $e")
        delay(5000)
        // Auto Retry
        return safelyFetchAsync(limit, types)
    }
}

@Composable
fun SettingsButton(openSettings: () -> Unit) {
    return Button(
        onClick = {
            openSettings()
        }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(
            backgroundColor = MaterialTheme.colors.primary
        )
    ) {
        Text(text = "Settings")
    }
}

@OptIn(ExperimentalHorologistApi::class, ExperimentalWearFoundationApi::class)
@Composable
fun WearHome(
    context: Context, isConnected: Boolean, viewModel: ImageViewModel, openSettings: () -> Unit
) {
    SleepTheme {
        val focusRequester = rememberActiveFocusRequester()
        val scalingLazyListState = rememberScalingLazyListState()
        // Use remember to store the result and trigger recomposition when it changes
        var animalPhotos by remember { mutableStateOf<List<Any>>(emptyList()) }
        var currentImageIndex by remember { mutableIntStateOf(0) }
        var isLoading by remember { mutableStateOf<Boolean>(true) }
        var lastConnectedState by remember { mutableStateOf<Boolean>(isConnected) }
        var error by remember { mutableStateOf<String?>(null) }
        val isOfflineAvailable = true
        val coroutineScope = rememberCoroutineScope()
        val lifecycleOwner = LocalLifecycleOwner.current
        val state by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
        val sharedPreferences = context.getSharedPreferences(
            SettingsBasics.SHARED_PREFERENCES.getKey(),
            SettingsBasics.SHARED_PREFERENCES.getMode()
        )
        var userWalkthroughComplete = sharedPreferences.getBoolean(
            Settings.USER_WALKTHROUGH_COMPLETE.getKey(),
            Settings.USER_WALKTHROUGH_COMPLETE.getDefaultAsBoolean()
        )
        // Suspended functions
        LaunchedEffect(state, isConnected) {
            Log.d("CatEffect", "IS connected: $isConnected ${animalPhotos.isEmpty()}")
            val animalTypes = sharedPreferences.getString(
                Settings.ANIMALS.getKey(),
                Settings.ANIMALS.getDefault()
            )
            val types = enumFromJSON(animalTypes)
            if (isConnected) error = null
            if (lastConnectedState != isConnected || animalPhotos.isEmpty()) {
                isLoading = true
                if (isConnected) {
                    safelyFetch(types) { data ->
                        animalPhotos = data.map {
                            it.url
                        }
                        isLoading = false
                    }
                } else {
                    val offlineImages = viewModel.getImages()
                    animalPhotos = offlineImages.map {
                        decodeByteArray(it)
                    }.shuffled()
                    if (offlineImages.isEmpty()) error =
                        if (isOfflineAvailable) "You haven't downloaded any offline images"
                        else "You're offline"
                    isLoading = false
                }
            }

            lastConnectedState = isConnected
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center,
        ) {
            ItemsListWithModifier(
                modifier = Modifier.rotaryWithScroll(
                    reverseDirection = false,
                    focusRequester = focusRequester,
                    scrollableState = scalingLazyListState,
                ),
                scrollableState = scalingLazyListState,
            ) {
                if (error != null) {
                    item {
                        Spacer(
                            modifier = Modifier.padding(
                                top = 42.dp, bottom = 5.dp
                            )
                        )
                    }
                    item {
                        Text(
                            text = error!!, textAlign = TextAlign.Center
                        )
                    }
                    item {
                        Spacer(
                            modifier = Modifier.padding(
                                5.dp
                            )
                        )
                    }
                    item {
                        SettingsButton(openSettings)
                    }
                } else if (animalPhotos.isNotEmpty()) {
                    item {
                        Spacer(
                            modifier = Modifier.padding(
                                top = 42.dp, bottom = 5.dp
                            )
                        )
                    }
//                    item {
//                        Image(bitmap = (decodeBase64(catPhotos[currentCatIndex] as ByteArray)).asImageBitmap(), contentDescription = "Cat")
//                    }
                    item {
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(128.dp)
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(14.dp))
//                                    .shimmer()
//                                    .background(MaterialTheme.colors.secondary)
                                    .background(MaterialTheme.colors.background)
                                    .shimmer()
                            )

                            SubcomposeAsyncImage(model = animalPhotos[currentImageIndex],
//                                loading = {
//                                    //CircularProgressIndicator()
//                                    Box(
//                                        modifier = Modifier
//                                            .size(128.dp)
//                                            .shimmer()
//                                            .background(MaterialTheme.colors.surface)
//                                            .shimmer()
//                                    )
//                                },
                                contentDescription = "Cat Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        coroutineScope.launch {
                                            if (!userWalkthroughComplete) {
                                                sharedPreferences.edit {
                                                    putBoolean(
                                                        Settings.USER_WALKTHROUGH_COMPLETE.getKey(),
                                                        true
                                                    )
                                                    apply()
                                                }

                                                userWalkthroughComplete = true
                                            }
                                            if (!isLoading) {
                                                isLoading = true
                                                val animalTypes = sharedPreferences.getString(
                                                    Settings.ANIMALS.getKey(),
                                                    Settings.ANIMALS.getDefault()
                                                )
                                                val types = enumFromJSON(animalTypes)
                                                if (currentImageIndex == animalPhotos.size.minus(1)) {
                                                    if (isConnected) {
                                                        safelyFetch(types) { data ->
                                                            currentImageIndex = 0
                                                            animalPhotos = data.map {
                                                                it.url
                                                            }
                                                            isLoading = false
                                                        }
                                                    } else {
                                                        animalPhotos = animalPhotos.shuffled()
                                                        currentImageIndex = 0
                                                    }
                                                } else {
                                                    currentImageIndex += 1
                                                }
                                                isLoading = false
                                            }
                                        }
                                    }
                            ) {
                                val paintState = painter.state
                                if (paintState is AsyncImagePainter.State.Loading || paintState is AsyncImagePainter.State.Error) {
                                    Box(
                                        modifier = Modifier
                                            .size(128.dp)
                                            .shimmer()
                                            .background(MaterialTheme.colors.secondary)
                                            .shimmer()
                                    )
                                } else {
                                    SubcomposeAsyncImageContent(
                                        modifier = if (!userWalkthroughComplete) Modifier.blur(
                                            20.dp
                                        ) else Modifier
                                    )
                                    if (!userWalkthroughComplete) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Transparent)
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Tap the image to refresh",
                                                color = Color.White,
                                                textAlign = TextAlign.Center,
                                                style = MaterialTheme.typography.body1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Spacer(
                            modifier = Modifier.padding(
                                5.dp
                            )
                        )
                    }
//                    item {
//                        Button(
//                            onClick = {
//                                coroutineScope.launch {
//                                    if (!isLoading) {
//                                        isLoading = true
//                                        val animalTypes = sharedPreferences.getString(
//                                            Settings.ANIMALS.getKey(),
//                                            Settings.ANIMALS.getDefault()
//                                        )
//                                        val types = enumFromJSON(animalTypes)
//                                        if (currentImageIndex == animalPhotos.size.minus(1)) {
//                                            if (isConnected) {
//                                                safelyFetch(types) { data ->
//                                                    currentImageIndex = 0
//                                                    animalPhotos = data.map {
//                                                        it.url
//                                                    }
//                                                    isLoading = false
//                                                }
//                                            } else {
//                                                animalPhotos = animalPhotos.shuffled()
//                                                currentImageIndex = 0
//                                            }
//                                        } else {
//                                            currentImageIndex += 1
//                                        }
//                                        isLoading = false
//                                    }
//                                }
//                            },
//                            modifier = Modifier.fillMaxWidth(),
//                            colors = ButtonDefaults.buttonColors(
//                                backgroundColor = MaterialTheme.colors.primary
//                            ),
//                            enabled = !isLoading
//                        ) {
//                            if (isLoading) {
//                                CircularProgressIndicator(indicatorColor = MaterialTheme.colors.primary)
//                            } else {
//                                Text(text = "Refresh")
//                            }
//                        }
//                    }

                    item {
                        SettingsButton(openSettings)
                    }

                    if (!isConnected) {
                        item {
                            Row(
                                modifier = Modifier.padding(top = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.round_cloud_off_24),
                                    contentDescription = "Offline",
                                    tint = MaterialTheme.colors.primary,
                                )

                                Spacer(modifier = Modifier.padding(3.dp))

                                Text(
                                    text = "Offline",
                                    fontSize = 18.sp
                                )
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
suspend fun fetchPhotos(limit: Int = 1, types: List<Animals>): List<AnimalPhoto> {
    return withContext(Dispatchers.IO) {
        val type = types.random()
        Log.d("Animals", "Current = ${type.name} | Available = ${types.map { it.name }}")
        // Perform network operations in the IO dispatcher
        val apiKey = "API_KEY"
        val apiUrl =
            when (type) {
                Animals.CATS -> "https://api.thecatapi.com/v1/images/search?limit=${limit}"
                Animals.BUNNIES -> "https://api.bunnies.io/v2/loop/random/?media=gif,png"
            }

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
        val animalPhotos = when (val jsonElement = Json.parseToJsonElement(response.toString())) {
            is JsonArray -> {
                json.decodeFromJsonElement(ListSerializer(AnimalPhotoSerializer), jsonElement)
            }

            is JsonObject -> {
                listOf(json.decodeFromJsonElement(AnimalPhotoSerializer, jsonElement))
            }

            else -> {
                emptyList()
            }
        }
        return@withContext animalPhotos
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearHome(
        LocalContext.current, false, viewModel = ImageViewModel(LocalContext.current.dataStore)
    ) {}
}