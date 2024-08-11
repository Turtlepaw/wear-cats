package com.turtlepaw.cats.presentation.pages

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.util.Base64
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.RevealValue
import androidx.wear.compose.foundation.rememberRevealState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.SwipeToRevealCard
import androidx.wear.compose.material.SwipeToRevealDefaults
import androidx.wear.compose.material.SwipeToRevealPrimaryAction
import androidx.wear.compose.material.Text
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.imageLoader
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.turtlepaw.cats.R
import com.turtlepaw.cats.complication.getCroppedSquareBitmap
import com.turtlepaw.cats.database.AppDatabase
import com.turtlepaw.cats.presentation.components.Page
import com.turtlepaw.cats.tile.loadImage
import com.turtlepaw.cats.utils.decodeBase64
import com.turtlepaw.cats.utils.decodeByteArray
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.padding(end = 10.dp)) {
                Icon(
                    painter = painterResource(id = R.drawable.favorite),
                    contentDescription = "Favorite",
                    tint = MaterialTheme.colors.onPrimary,
                )
            }
            Text(
                text = "Favorites",
                color = MaterialTheme.colors.onPrimary
            )
        }
    }
}

fun saveBase64StringToInternalStorage(context: Context, base64String: String, fileName: String) {
    try {
        // Save the file to the internal storage directory
        val file = File(context.filesDir, "$fileName.txt")

        val fos = FileOutputStream(file)
        val writer = OutputStreamWriter(fos)
        writer.write(base64String)
        writer.close()

        Log.d("Base64Save", "Base64 string saved to: ${file.absolutePath}")
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun saveBase64StringToExternalStorage(context: Context, base64String: String, fileName: String) {
    try {
        // Get the external storage directory (Downloads folder)
        val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, "$fileName.txt")

        // Write the Base64 string to the file
        val fos = FileOutputStream(file)
        val writer = OutputStreamWriter(fos)
        writer.write(base64String)
        writer.close()

        // Log the file path
        Log.d("Base64Save", "Base64 string saved to: ${file.absolutePath}")
    } catch (e: Exception) {
        e.printStackTrace()
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
    val coroutineScope = rememberCoroutineScope()
    var animalPhotos by remember { mutableStateOf<List<Pair<Int, ByteArray>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        animalPhotos = database.favoritesDao().getFavorites().map {
            it.id to decodeByteArray(it.value)
        }
        Log.d("Favorites", animalPhotos.size.toString())
        isLoading = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        contentAlignment = Alignment.Center,
    ) {
        Page {
            if (animalPhotos.isNotEmpty()) {
                item {
                    Text(
                        text = "${animalPhotos.size} favorite${if (animalPhotos.size > 1) "s" else ""}",
                        modifier = Modifier.padding(
                            bottom = 5.dp
                        )
                    )
                }
                items(animalPhotos.size) {
                    val revealState = rememberRevealState()
                    SwipeToRevealCard(
                        revealState = revealState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 1.dp),
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
                                        animalPhotos =
                                            database.favoritesDao().getFavorites().map {
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
                }
                item {
                    Text(
                        text = "Delete items by fully swiping them to the left",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(
                            top = 5.dp, bottom = 20.dp
                        )
                    )
                }
            } else if (isLoading) {
                item {
                    Spacer(modifier = Modifier.padding(top = 45.dp))
                }
                items(3) {
                    Box(
                        modifier = Modifier.shimmer()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .size(128.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colors.surface)
                        )
                    }
                }
            } else {
                item {
                    Text(text = "No favorites")
                }
                item {
                    Text(
                        text = "Add favorite by long pressing images",
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}