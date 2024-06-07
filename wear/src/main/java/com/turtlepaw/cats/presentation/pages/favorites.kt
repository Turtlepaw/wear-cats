package com.turtlepaw.cats.presentation.pages

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.RevealValue
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.wear.compose.foundation.rememberRevealState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.SwipeToRevealCard
import androidx.wear.compose.material.SwipeToRevealDefaults
import androidx.wear.compose.material.SwipeToRevealPrimaryAction
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
import com.turtlepaw.cats.presentation.components.ItemsListWithModifier
import com.turtlepaw.cats.presentation.theme.SleepTheme
import com.turtlepaw.cats.utils.decodeByteArray
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.launch

@Composable
fun FavoritesButton(openSettings: () -> Unit) {
    return Button(
        onClick = {
            openSettings()
        }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(
            backgroundColor = MaterialTheme.colors.primary
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                painter = painterResource(id = R.drawable.favorite),
                contentDescription = "Favorite",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.padding(3.dp))
            Text(text = "Favorites")
        }
    }
}

@OptIn(
    ExperimentalHorologistApi::class, ExperimentalWearFoundationApi::class,
    ExperimentalFoundationApi::class, ExperimentalWearMaterialApi::class
)
@Composable
fun Favorites(
    context: Context,
    database: AppDatabase,
) {
    SleepTheme {
        val focusRequester = rememberActiveFocusRequester()
        val scalingLazyListState = rememberScalingLazyListState()
        val coroutineScope = rememberCoroutineScope()
        var animalPhotos by remember { mutableStateOf<List<Pair<Int, ByteArray>>>(emptyList()) }
        LaunchedEffect(Unit) {
            animalPhotos = database.favoritesDao().getFavorites().map {
                it.id to decodeByteArray(it.value)
            }
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
                if (animalPhotos.isNotEmpty()) {
                    item {
                        Spacer(
                            modifier = Modifier.padding(
                                top = 42.dp, bottom = 5.dp
                            )
                        )
                    }
                    items(animalPhotos.size) {
                        val revealState = rememberRevealState()
                        SwipeToRevealCard(
                            revealState = revealState,
                            modifier = Modifier.fillMaxSize(),
                            primaryAction = {
                                SwipeToRevealPrimaryAction(
                                    revealState = revealState,
                                    icon = { Icon(SwipeToRevealDefaults.Delete, "Delete") },
                                    label = { Text("Delete") },
                                    modifier = Modifier.fillParentMaxSize(),
                                    onClick = {
                                        coroutineScope.launch {
                                            database.favoritesDao()
                                                .deleteFavoriteById(animalPhotos[it].first)
                                            animalPhotos = database.favoritesDao().getFavorites().map {
                                                it.id to decodeByteArray(it.value)
                                            }
                                            revealState.snapTo(RevealValue.Covered)
                                        }
                                    }
                                )
                            },
                            onFullSwipe = {
                                coroutineScope.launch {
                                    database.favoritesDao()
                                        .deleteFavoriteById(animalPhotos[it].first)
                                    animalPhotos = database.favoritesDao().getFavorites().map {
                                        it.id to decodeByteArray(it.value)
                                    }
                                    revealState.snapTo(RevealValue.Covered)
                                }
                            }
                        ) {
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

                            SubcomposeAsyncImage(
                                model = animalPhotos[it].second,
                                contentDescription = "Favorite ${it + 1}",
                                contentScale = ContentScale.FillBounds,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(14.dp))
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
                                } else
                                    SubcomposeAsyncImageContent()
                            }
                        }
                    }
                    Spacer(
                        modifier = Modifier.padding(
                            2.dp
                        )
                    )
                }
                item {
                    Text(
                        text = "${animalPhotos.size} item${if (animalPhotos.size > 1) "s" else ""}",
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
            } else {
            items(5) {
                Box(
                    modifier = Modifier
                        .size(128.dp)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colors.background)
                        .shimmer()
                )
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