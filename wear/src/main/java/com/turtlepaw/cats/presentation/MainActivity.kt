/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.turtlepaw.cats.presentation

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.turtlepaw.cats.presentation.pages.WearHome
import com.turtlepaw.cats.presentation.pages.settings.WearSettings
import com.turtlepaw.cats.presentation.theme.SleepTheme
import com.turtlepaw.cats.utils.ImageViewModel
import com.turtlepaw.cats.utils.ImageViewModelFactory
import com.turtlepaw.cats.utils.Settings
import com.turtlepaw.cats.utils.SettingsBasics
import java.time.LocalDate


enum class Routes(private val route: String) {
    HOME("/home"),
    SETTINGS("/settings"),
    GOAL_PICKER("/goal-picker"),
    SUN_PICKER("/sun-picker"),
    HISTORY("/history"),
    CLOCKWORK("/clockwork-toolkit");

    fun getRoute(query: String? = null): String {
        return if(query != null){
            "$route/$query"
        } else route
    }
}

// At the top level of your kotlin file:
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = SettingsBasics.HISTORY_STORAGE_BASE.getKey())

class MainActivity : ComponentActivity() {
    private lateinit var imageViewModelFactory: ImageViewModelFactory
    private lateinit var imageViewModel: ImageViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        // Initialize your BedtimeViewModelFactory here
        imageViewModelFactory = ImageViewModelFactory(dataStore)

        // Use the factory to create the BedtimeViewModel
        imageViewModel = ViewModelProvider(
            this,
            imageViewModelFactory
        )[ImageViewModel::class.java]

        setContent {
            WearPages(
                this,
                imageViewModel
            )
        }
    }
}

fun isNetworkConnected(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val network = connectivityManager.activeNetwork
    val capabilities = connectivityManager.getNetworkCapabilities(network)
    return capabilities != null &&
            (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
}

@Composable
fun WearPages(
    context: Context,
    viewModel: ImageViewModel
){
    SleepTheme {
        // Creates a navigation controller for our pages
        val navController = rememberSwipeDismissableNavController()
        var isConnected by remember { mutableStateOf<Boolean>(true) }
        val lifecycleOwner = LocalLifecycleOwner.current
        val state by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
        // Suspended functions
        LaunchedEffect(state) {
            isConnected = isNetworkConnected(context)
        }

        SwipeDismissableNavHost(
            navController = navController,
            startDestination = Routes.HOME.getRoute()
        ) {
            composable(Routes.HOME.getRoute()) {
                WearHome(
                    context,
                    isConnected,
                    viewModel
                ) {
                    navController.navigate(Routes.SETTINGS.getRoute())
                }
            }
            composable(Routes.SETTINGS.getRoute()){
                WearSettings(
                    context,
                    isConnected,
                    viewModel
                )
            }
        }
    }
}