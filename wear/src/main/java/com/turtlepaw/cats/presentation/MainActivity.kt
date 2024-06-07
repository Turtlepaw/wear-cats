/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.turtlepaw.cats.presentation

import android.content.Context
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
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.turtlepaw.cats.database.AppDatabase
import com.turtlepaw.cats.presentation.pages.Favorites
import com.turtlepaw.cats.presentation.pages.WearHome
import com.turtlepaw.cats.presentation.pages.settings.WearSettings
import com.turtlepaw.cats.presentation.theme.SleepTheme
import com.turtlepaw.cats.utils.SettingsBasics


enum class Routes(private val route: String) {
    HOME("/home"),
    SETTINGS("/settings"),
    FAVORITES("/favorites");

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
            WearPages(
                this,
                database
            )
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
    database: AppDatabase
) {
    SleepTheme {
        // Creates a navigation controller for our pages
        val navController = rememberSwipeDismissableNavController()
        var isConnected by remember { mutableStateOf<Boolean>(true) }
        val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
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
                    database
                ) {
                    navController.navigate(it.getRoute())
                }
            }
            composable(Routes.SETTINGS.getRoute()) {
                WearSettings(
                    context,
                    isConnected
                )
            }
            composable(Routes.FAVORITES.getRoute()) {
                Favorites(context, database)
            }
        }
    }
}