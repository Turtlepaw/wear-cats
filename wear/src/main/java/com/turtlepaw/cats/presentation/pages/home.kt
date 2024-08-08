package com.turtlepaw.cats.presentation.pages

import AnimalPhoto
import AnimalPhotoSerializer
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
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
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll
import com.turtlepaw.cats.R
import com.turtlepaw.cats.database.AppDatabase
import com.turtlepaw.cats.database.Favorite
import com.turtlepaw.cats.database.Image
import com.turtlepaw.cats.database.downloadImage
import com.turtlepaw.cats.presentation.Routes
import com.turtlepaw.cats.presentation.components.ItemsListWithModifier
import com.turtlepaw.cats.utils.Animals
import com.turtlepaw.cats.utils.DOWNLOAD_LIMIT
import com.turtlepaw.cats.utils.ImageControls
import com.turtlepaw.cats.utils.Settings
import com.turtlepaw.cats.utils.decodeByteArray
import com.turtlepaw.cats.utils.encodeToBase64
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import android.util.Base64
import com.turtlepaw.cats.presentation.MyPetButton
import com.turtlepaw.cats.presentation.isMyPetAvailable

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
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.padding(end = 10.dp)) {
                Icon(
                    painter = painterResource(id = R.drawable.settings),
                    contentDescription = "Settings",
                    tint = MaterialTheme.colors.onPrimary,
                )
            }
            Text(
                text = "Settings",
                color = MaterialTheme.colors.onPrimary
            )
        }
    }
}

enum class IntroductionProgress {
    TapToRefresh,
    LongPressToFavorite,
    None
}

enum class LoadingType {
    Shimmering,
    Rotating,
    None
}

val animationSpec = TweenSpec<Float>(durationMillis = 300)

fun loadOfflineImages(
    offlineImages: List<Image>,
    onProgressUpdate: (progress: Float) -> Unit
): List<ByteArray> {
    var completedImages = 0
    val updatedPhotos = mutableListOf<ByteArray>()

    for (image in offlineImages) {
        completedImages += 1
        val offlineLoadProgress = completedImages.toFloat() / DOWNLOAD_LIMIT.toFloat()
        onProgressUpdate(offlineLoadProgress)
        Log.d("OfflineLoad", "$completedImages complete, $offlineLoadProgress prg")
        updatedPhotos.add(decodeByteArray(image.value))
    }

    return updatedPhotos.shuffled()
}

@OptIn(
    ExperimentalHorologistApi::class, ExperimentalWearFoundationApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun WearHome(
    context: Context,
    isConnected: Boolean,
    database: AppDatabase,
    sharedPreferences: SharedPreferences,
    isLoading: LoadingType,
    error: String?,
    progress: Float,
    animalPhotos: List<Any>,
    controls: ImageControls,
    open: (route: Routes) -> Unit,
    refresh: () -> Unit
) {
    val focusRequester = rememberActiveFocusRequester()
    val scalingLazyListState = rememberScalingLazyListState()
    // Use remember to store the result and trigger recomposition when it changes
    var isFavoriting by remember { mutableStateOf<Boolean>(false) }
    val offlineLoadProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 600)
    )
    val animatedProgress = remember { Animatable(initialValue = 0f) }
    var introductionProgress by remember { mutableStateOf(IntroductionProgress.TapToRefresh) }
    val coroutineScope = rememberCoroutineScope()

    var userWalkthroughComplete = sharedPreferences.getBoolean(
        Settings.USER_WALKTHROUGH_COMPLETE.getKey(),
        Settings.USER_WALKTHROUGH_COMPLETE.getDefaultAsBoolean()
    )

    LaunchedEffect(userWalkthroughComplete) {
        animatedProgress.animateTo(
            if (userWalkthroughComplete) 0f else 1f,
            animationSpec
        )
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
                    Icon(
                        painter = painterResource(id = R.drawable.error),
                        contentDescription = "Error",
                        tint = MaterialTheme.colors.primary,
                        modifier = Modifier.padding(bottom = 2.dp)
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
                    SettingsButton {
                        open(Routes.SETTINGS)
                    }
                }
                item {
                    FavoritesButton {
                        open(Routes.FAVORITES)
                    }
                }
            } else if (animalPhotos.isNotEmpty() || isLoading != LoadingType.None) {
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
                        val _isLoading = isLoading != LoadingType.None
                        if (_isLoading) {
                            val rotation = remember { Animatable(0f) }

                            LaunchedEffect(isLoading) {
                                if (isLoading == LoadingType.Rotating) {
                                    while (isLoading == LoadingType.Rotating) {
                                        rotation.animateTo(
                                            targetValue = 360f,
                                            animationSpec = tween(
                                                durationMillis = 10000,
                                                easing = LinearEasing
                                            )
                                        )
                                        rotation.snapTo(0f)
                                    }
                                } else {
                                    rotation.stop()
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(14.dp))
                            ) {
//                                    Image(
//                                        painter = painterResource(id = R.drawable.loading_bg),
//                                        contentDescription = "Loading Background",
//                                        modifier = Modifier
//                                            .rotate(rotation.value)
//                                            .scale(1.7f)  // Adjust the scale factor to make the image larger
//                                            .fillMaxSize(),
//                                        contentScale = ContentScale.Crop
//                                    )

                                if (isLoading == LoadingType.Rotating) {
                                    Box(
                                        modifier = Modifier
                                            .rotate(rotation.value)
                                            .scale(1.57f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    brush = Brush.linearGradient(
                                                        listOf(
                                                            MaterialTheme.colors.primary,
                                                            MaterialTheme.colors.secondary
                                                        )
                                                    )
                                                )
                                            //.rotate(rotation.value)
                                            //.scale(1.7f)  // Adjust the scale factor to make the image larger
                                        )
                                    }

                                    Column(
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Box(modifier = Modifier.size(30.dp)) {
                                            if (isConnected) CircularProgressIndicator(
                                                indicatorColor = MaterialTheme.colors.onPrimary,
                                                trackColor = MaterialTheme.colors.onPrimary.copy(
                                                    alpha = 0.3f
                                                )
                                            )
                                            else CircularProgressIndicator(
                                                progress = offlineLoadProgress,
                                                indicatorColor = MaterialTheme.colors.onPrimary
                                            )
                                        }

                                        Text(
                                            text = if (isConnected) "Preparing"
                                            else "Loading Offline\nImages",
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colors.onPrimary,
                                            modifier = Modifier.padding(top = 8.dp)
                                        )
                                    }
                                } else {
                                    Box(modifier = Modifier.shimmer())
                                }
                            }
                        } else {
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

                            SubcomposeAsyncImage(
                                model = if (isLoading == LoadingType.None) animalPhotos[controls.current]
                                else R.drawable.loading_bg,
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
                                    .then(
                                        if (isLoading != LoadingType.None) {
                                            Modifier.height(130.dp)
                                        } else
                                            Modifier
                                    )
                                    .combinedClickable(
                                        onClick = {
                                            coroutineScope.launch {
                                                if (!userWalkthroughComplete) {
                                                    animatedProgress.animateTo(
                                                        0f,
                                                        animationSpec
                                                    )
                                                    introductionProgress =
                                                        IntroductionProgress.LongPressToFavorite
                                                    delay(1500)
                                                    animatedProgress.animateTo(
                                                        1f,
                                                        animationSpec
                                                    )
                                                }
                                            }

                                            refresh()
                                        },
                                        onLongClick = {
                                            coroutineScope.launch {
                                                if (!userWalkthroughComplete) {
                                                    animatedProgress.animateTo(
                                                        0f,
                                                        animationSpec
                                                    )

                                                    sharedPreferences.edit {
                                                        putBoolean(
                                                            Settings.USER_WALKTHROUGH_COMPLETE.getKey(),
                                                            true
                                                        )
                                                        apply()
                                                    }

                                                    userWalkthroughComplete = true
                                                }

                                                if (isLoading == LoadingType.None) {
                                                    val current =
                                                        animalPhotos[controls.current]
                                                    val data = when (current) {
                                                        is ByteArray -> encodeToBase64(
                                                            animalPhotos[controls.current] as ByteArray
                                                        )

                                                        is String -> {
                                                            downloadImage(current, context)
                                                        }

                                                        else -> null
                                                    }

                                                    if (data != null) {
                                                        database
                                                            .favoritesDao()
                                                            .insertFavorite(
                                                                Favorite(
                                                                    timestamp = LocalDateTime.now(),
                                                                    value = data
                                                                )
                                                            )

                                                        isFavoriting = true
                                                        animatedProgress.animateTo(
                                                            1f,
                                                            animationSpec
                                                        )

                                                        val vibrator = context.getSystemService(
                                                            Vibrator::class.java
                                                        )
                                                        if (vibrator != null && vibrator.hasVibrator()) {
                                                            vibrator.vibrate(
                                                                VibrationEffect
                                                                    .startComposition()
                                                                    .addPrimitive(
                                                                        VibrationEffect.Composition.PRIMITIVE_CLICK,
                                                                        1f
                                                                    )
                                                                    .compose()
                                                            )
                                                        }

                                                        delay(1200)

                                                        animatedProgress.animateTo(
                                                            0f,
                                                            animationSpec
                                                        )
                                                        isFavoriting = false
                                                    }
                                                }
                                            }
                                        }
                                    )
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
                                    val maxBlur = 20f
                                    val progress = animatedProgress.value
                                    val blurValue = lerp(0f, maxBlur, progress).dp

                                    SubcomposeAsyncImageContent(
                                        modifier =
                                        if (!userWalkthroughComplete || isFavoriting) Modifier
                                            .blur(
                                                blurValue
                                            )
                                        else Modifier
                                    )
                                }

                                Box(modifier = Modifier.alpha(animatedProgress.value)) {
                                    if (!userWalkthroughComplete && introductionProgress != IntroductionProgress.None) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Transparent)
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (introductionProgress == IntroductionProgress.TapToRefresh)
                                                    "Tap the image to refresh"
                                                else "Long press to favorite",
                                                color = Color.White,
                                                textAlign = TextAlign.Center,
                                                style = MaterialTheme.typography.body1
                                            )
                                        }
                                    } else if (isFavoriting) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Transparent)
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.favorite),
                                                contentDescription = "Favorite",
                                                modifier = Modifier.size(50.dp)
                                            )
                                        }
                                    } else if (isLoading != LoadingType.None) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Transparent)
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column {
                                                CircularProgressIndicator()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    Spacer(
                        modifier = Modifier.padding(
                            2.dp
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
                    SettingsButton {
                        open(Routes.SETTINGS)
                    }
                }

                item {
                    FavoritesButton {
                        open(Routes.FAVORITES)
                    }
                }

                if(isMyPetAvailable){
                    item {
                        MyPetButton(context)
                    }
                }

                if (!isConnected) {
                    item {
                        Row(
                            modifier = Modifier.padding(top = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val style = MaterialTheme.typography.title3
                            val fontSize: TextUnit = style.fontSize
                            val lineHeightDp: Dp = with(LocalDensity.current) {
                                fontSize.toDp()
                            }
                            Icon(
                                painter = painterResource(id = R.drawable.round_cloud_off_24),
                                contentDescription = "Offline",
                                tint = MaterialTheme.colors.primary,
                                modifier = Modifier.size(lineHeightDp)
                            )

                            Spacer(modifier = Modifier.padding(3.dp))

                            Text(
                                text = "Offline",
                                //fontSize = fontSize,
                                style = style,
                                fontWeight = FontWeight.Normal
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

//@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
//@Composable
//fun DefaultPreview() {
//    WearHome(
//        LocalContext.current,
//        false,
//        database = AppDatabase.getDatabase(LocalContext.current),
//        LocalContext.current.getSharedPreferences(SettingsBasics.SHARED_PREFERENCES.getKey(), SettingsBasics.SHARED_PREFERENCES.getMode()),
//        false,
//    ) {}
//}