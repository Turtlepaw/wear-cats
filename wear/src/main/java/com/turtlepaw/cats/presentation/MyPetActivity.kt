package com.turtlepaw.cats.presentation

import android.Manifest
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.turtlepaw.cats.presentation.pages.MyPetHome
import com.turtlepaw.cats.presentation.pages.WearHome
import com.turtlepaw.cats.presentation.pages.settings.WearSettings
import com.turtlepaw.cats.presentation.theme.SleepTheme
import com.turtlepaw.cats.services.scheduleMyPetWorker
import com.turtlepaw.cats.utils.ImageViewModel
import com.turtlepaw.cats.utils.ImageViewModelFactory
import com.turtlepaw.cats.utils.SettingsBasics


enum class MyPetRoutes(private val route: String) {
    HOME("/home"),
    SETTINGS("/settings");

    fun getRoute(query: String? = null): String {
        return if (query != null) {
            "$route/$query"
        } else route
    }
}

class MyPetActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            MyPetNavGraph(
                this
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MyPetNavGraph(
    context: Context
) {
    SleepTheme {
        // Creates a navigation controller for our pages
        val navController = rememberSwipeDismissableNavController()
        var isConnected by remember { mutableStateOf(true) }
        val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
        val state by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
        // Suspended functions
        LaunchedEffect(state) {
            isConnected = isNetworkConnected(context)
        }

        val permissions = rememberPermissionState(Manifest.permission.ACTIVITY_RECOGNITION){
            context.scheduleMyPetWorker()
        }

        LaunchedEffect(Unit) {
            if(permissions.status.isGranted){
                context.scheduleMyPetWorker()
            } else {
                permissions.launchPermissionRequest()
            }
        }

        SwipeDismissableNavHost(
            navController = navController,
            startDestination = Routes.HOME.getRoute()
        ) {
            composable(Routes.HOME.getRoute()) {
                MyPetHome(
                    context,
                    isConnected
                ) {
                    navController.navigate(MyPetRoutes.SETTINGS.getRoute())
                }
            }
        }
    }
}