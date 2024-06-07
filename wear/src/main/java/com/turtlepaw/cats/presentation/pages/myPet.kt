package com.turtlepaw.cats.presentation.pages

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rememberActiveFocusRequester
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
import com.turtlepaw.cats.utils.ImageViewModel
import com.turtlepaw.cats.utils.Settings
import com.turtlepaw.cats.utils.SettingsBasics
import com.turtlepaw.cats.utils.decodeByteArray
import com.turtlepaw.cats.utils.enumFromJSON
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.launch

private const val tag = "CatImageFetch"

@OptIn(ExperimentalHorologistApi::class, ExperimentalWearFoundationApi::class)
@Composable
fun MyPetHome(
    context: Context,
    isConnected: Boolean,
    viewModel: ImageViewModel,
    openSettings: () -> Unit
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
                                2.dp
                            )
                        )
                    }

                    item {
                        SettingsButton(openSettings)
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
}
//
//@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
//@Composable
//fun MyPetPreview() {
//    WearHome(
//        LocalContext.current, false, database = ImageViewModel(LocalContext.current.dataStore)
//    ) {}
//}