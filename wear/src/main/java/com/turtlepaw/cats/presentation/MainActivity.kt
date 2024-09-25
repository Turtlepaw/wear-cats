/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.turtlepaw.cats.presentation

import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import coil.imageLoader
import com.turtlepaw.cats.complication.getCroppedSquareBitmap
import com.turtlepaw.cats.database.AppDatabase
import com.turtlepaw.cats.database.ThemeViewModel
import com.turtlepaw.cats.database.ThemeViewModelFactory
import com.turtlepaw.cats.presentation.pages.Favorites
import com.turtlepaw.cats.presentation.pages.LoadingType
import com.turtlepaw.cats.presentation.pages.WearHome
import com.turtlepaw.cats.presentation.pages.loadOfflineImages
import com.turtlepaw.cats.presentation.pages.safelyFetch
import com.turtlepaw.cats.presentation.pages.settings.ThemePicker
import com.turtlepaw.cats.presentation.pages.settings.WearSettings
import com.turtlepaw.cats.presentation.theme.SleepTheme
import com.turtlepaw.cats.tile.loadImage
import com.turtlepaw.cats.utils.ImageControls
import com.turtlepaw.cats.utils.Settings
import com.turtlepaw.cats.utils.SettingsBasics
import com.turtlepaw.cats.utils.enumFromJSON
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream


enum class Routes(private val route: String) {
    HOME("/home"),
    SETTINGS("/settings"),
    FAVORITES("/favorites"),
    THEME_PICKER("/theme-picker");

    fun getRoute(query: String? = null): String {
        return if (query != null) {
            "$route/$query"
        } else route
    }
}

class MainActivity : ComponentActivity() {
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        database = AppDatabase.getDatabase(this)

        setContent {
            val themeViewModel = ViewModelProvider(
                this,
                ThemeViewModelFactory(
                    getSharedPreferences(
                        SettingsBasics.SHARED_PREFERENCES.getKey(),
                        SettingsBasics.SHARED_PREFERENCES.getMode()
                    )
                )
            )[ThemeViewModel::class.java]

            SleepTheme(
                themeViewModel = themeViewModel
            ) {
                WearPages(
                    this,
                    database,
                    themeViewModel
                )
            }
        }
    }
}

fun isNetworkConnected(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}


@Composable
fun WearPages(
    context: Context,
    database: AppDatabase,
    themeViewModel: ThemeViewModel
) {
    SleepTheme {
        // Creates a navigation controller for our pages
        val navController = rememberSwipeDismissableNavController()
        var isConnected by remember { mutableStateOf<Boolean>(true) }
        val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
        val state by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
        var isLoading by remember { mutableStateOf<LoadingType>(LoadingType.Rotating) }
        var lastConnectedState by remember { mutableStateOf<Boolean>(isConnected) }
        var error by remember { mutableStateOf<String?>(null) }
        var progress by remember { mutableStateOf(0f) }
        var animalPhotos by remember { mutableStateOf<List<Any>>(emptyList()) }
        val coroutineScope = rememberCoroutineScope()
        val sharedPreferences = context.getSharedPreferences(
            SettingsBasics.SHARED_PREFERENCES.getKey(),
            SettingsBasics.SHARED_PREFERENCES.getMode()
        )
        val controls = ImageControls()
        // Suspended functions
        LaunchedEffect(state) {
            isConnected = isNetworkConnected(context)
        }

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
                isLoading = LoadingType.Rotating
                if (isConnected) {
                    safelyFetch(types) { data ->
                        animalPhotos = data.map {
                            it.url
                        }
                        isLoading = LoadingType.None
                    }
                } else {
                    coroutineScope.launch {
                        delay(300)
                        val totalDurationMillis = 7000
                        val snapPoints = listOf(0.2f, 0.4f, 0.8f)
                        val stepTime = totalDurationMillis / snapPoints.size

                        for (point in snapPoints) {
                            progress = point
                            delay(stepTime.toLong())
                        }
                    }

                    val offlineImages = database.imageDao().getImages()
                    if (offlineImages.isEmpty() || !isOfflineAvailable) error =
                        if (isOfflineAvailable) "You haven't downloaded any offline images"
                        else "You're offline"
                    animalPhotos = loadOfflineImages(offlineImages) {
                        progress = it
                    }
                    progress = 1f
                    delay(300)
                    isLoading = LoadingType.None
                }
            }

            lastConnectedState = isConnected
        }

        SwipeDismissableNavHost(
            navController = navController,
            startDestination = Routes.HOME.getRoute()
        ) {
            composable(Routes.HOME.getRoute()) {
                WearHome(
                    context,
                    isConnected,
                    database,
                    sharedPreferences,
                    isLoading,
                    error,
                    progress,
                    animalPhotos,
                    controls, {
                        navController.navigate(it.getRoute())
                    }
                ) {
                    coroutineScope.launch {
                        if (isLoading == LoadingType.None) {
                            isLoading = LoadingType.Shimmering
                            val animalTypes =
                                sharedPreferences.getString(
                                    Settings.ANIMALS.getKey(),
                                    Settings.ANIMALS.getDefault()
                                )
                            val types = enumFromJSON(animalTypes)
                            if (controls.current == animalPhotos.size.minus(
                                    1
                                )
                            ) {
                                if (isConnected) {
                                    safelyFetch(types) { data ->
                                        controls.setValue(0)
                                        animalPhotos = data.map {
                                            it.url
                                        }
                                        isLoading = LoadingType.None
                                    }
                                } else {
                                    animalPhotos =
                                        animalPhotos.shuffled()
                                    controls.setValue(0)
                                }
                            } else {
                                controls.increase()
                            }
                            isLoading = LoadingType.None
                        }
                    }
                }
            }
            composable(Routes.SETTINGS.getRoute()) {
                WearSettings(
                    context,
                    isConnected,
                    themeViewModel,
                    navController
                )
            }
            composable(Routes.FAVORITES.getRoute()) {
                Favorites(context, database)
            }
            composable(Routes.THEME_PICKER.getRoute()) {
                ThemePicker(themeViewModel)
            }
        }
    }
}