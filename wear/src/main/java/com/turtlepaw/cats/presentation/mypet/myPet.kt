package com.turtlepaw.cats.presentation.mypet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.material.CardDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TitleCard
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.turtlepaw.cats.R
import com.turtlepaw.cats.mypet.CatStatus
import com.turtlepaw.cats.presentation.MyPetRoutes
import com.turtlepaw.cats.presentation.components.Page
import com.turtlepaw.cats.presentation.navigate
import kotlinx.coroutines.delay
import java.util.Locale

private const val tag = "CatImageFetch"
val Hunger = listOf(
    "Very hungry",
    "Hungry",
    "Not hungry",
)

@OptIn(ExperimentalHorologistApi::class, ExperimentalWearFoundationApi::class)
@Composable
fun MyPetHome(
    data: CatStatus,
    navController: NavController
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        contentAlignment = Alignment.Center,
    ) {
        Page {
            item {}
            item {
//                    return when {
//        happiness < 20 -> "Very Unhappy"
//        happiness < 40 -> "Unhappy"
//        happiness < 60 -> "Neutral"
//        happiness < 80 -> "Happy"
//        else -> "Very Happy"
//    }
                var showImage by remember { mutableStateOf(true) }
                val composition by rememberLottieComposition(
                    LottieCompositionSpec.RawRes(
                        if (data.happiness == 100)
                            R.raw.cat_very_happy
                        else if (data.happiness < 20)
                            R.raw.cat_very_unhappy
                        else if (data.happiness < 40)
                            R.raw.cat_unhappy
                        else R.raw.cat_happy
                    )
                )
                val duration = (composition?.duration ?: 2000).toLong()
                // Control the animation
                val progress by animateLottieCompositionAsState(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    isPlaying = showImage
                )

                LaunchedEffect(Unit) {
                    delay(2000)
                    showImage = false
                }

                LaunchedEffect(key1 = showImage) {
                    if (showImage) {
                        delay(duration)
                        showImage = false
                    } else {
                        delay(4000)
                        showImage = true
                    }
                }

                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.size(70.dp),
                )
            }
            item {
                Text(
                    text = "Your cat is ${
                        happinessToReadableText(data.happiness).toLowerCase(
                            Locale.ROOT
                        )
                    }!", textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 5.dp)
                )
            }
            item {
                Spacer(modifier = Modifier.height(5.dp))
            }
            item {
                TitleCard(
                    onClick = {
                        navController.navigate(MyPetRoutes.HUNGER)
                    },
                    title = {
                        Text(text = "Hunger")
                    },
                    backgroundPainter = CardDefaults.surfaceBackground()
                ) {
                    Text(text = hungerToReadableText(data.hunger))
                }
            }
            item {
                TitleCard(
                    onClick = {
                        navController.navigate(MyPetRoutes.HAPPINESS)
                    },
                    title = {
                        Text(text = "Happiness")
                    },
                    backgroundPainter = CardDefaults.surfaceBackground()
                ) {
                    Text(text = happinessToReadableText(data.happiness))
                }
            }
        }
    }
}
//
//@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
//@Composable
//fun MyPetPreview() {
//    WearHome(
//        LocalContext.current, false, database = ImageViewModel(LocalContext.current.com.turtlepaw.cats.mypet.getDataStore)
//    ) {}
//}

@Composable
fun CardDefaults.surfaceBackground(): Painter {
    return CardDefaults.cardBackgroundPainter(
        startBackgroundColor = MaterialTheme.colors.onSurfaceVariant.copy(alpha = 0.20f)
            .compositeOver(MaterialTheme.colors.background)
    )
}

fun hungerToReadableText(hunger: Int): String {
    return when {
        hunger < 20 -> "Full"
        hunger < 40 -> "Satisfied"
        hunger < 60 -> "Peckish"
        hunger < 80 -> "Hungry"
        else -> "Very Hungry"
    }
}

fun happinessToReadableText(happiness: Int): String {
    return when {
        happiness < 20 -> "Very Unhappy"
        happiness < 40 -> "Unhappy"
        happiness < 60 -> "Neutral"
        happiness < 80 -> "Happy"
        else -> "Very Happy"
    }
}