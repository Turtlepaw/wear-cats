package com.turtlepaw.cats.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.turtlepaw.cats.database.ThemeViewModel
import com.turtlepaw.cats.database.ThemeViewModelFactory
import com.turtlepaw.cats.mypet.getCatStatusFlow
import com.turtlepaw.cats.presentation.mypet.MyPetHome
import com.turtlepaw.cats.presentation.mypet.MyPetsHunger
import com.turtlepaw.cats.presentation.theme.SleepTheme
import com.turtlepaw.cats.services.scheduleMyPetWorker
import com.turtlepaw.cats.utils.SettingsBasics
import kotlinx.coroutines.flow.first
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.turtlepaw.cats.R
import com.turtlepaw.cats.mypet.CatStatus
import com.turtlepaw.cats.presentation.mypet.Happiness

const val isMyPetAvailable = false;
@Composable
fun MyPetButton(context: Context) {
    return Button(
        onClick = {
            Intent(context, MyPetActivity::class.java).also {
                context.startActivity(it)
            }
        }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(
            backgroundColor = MaterialTheme.colors.primary
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.padding(end = 10.dp)) {
                Icon(
                    painter = painterResource(id = R.drawable.round_auto_awesome_24),
                    contentDescription = "Auto Awesome",
                    tint = MaterialTheme.colors.onPrimary,
                )
            }
            Text(
                text = "My Pet",
                color = MaterialTheme.colors.onPrimary
            )
        }
    }
}

enum class MyPetRoutes(private val route: String) {
    HOME("/home"),
    HUNGER("/hunger"),
    HAPPINESS("/happiness");

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
        scheduleMyPetWorker()

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

            SleepTheme(themeViewModel) {
                MyPetNavGraph(
                    this
                )
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MyPetNavGraph(
    context: Context
) {
    // Creates a navigation controller for our pages
    val navController = rememberSwipeDismissableNavController()
    var data by remember { mutableStateOf<CatStatus?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Observe CatStatus
    LaunchedEffect(Unit) {
        getCatStatusFlow(context).collect { status ->
            data = status
        }
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
        startDestination = MyPetRoutes.HOME.getRoute()
    ) {
        composable(MyPetRoutes.HOME) {
            data?.let { MyPetHome(it, navController) }
        }
        composable(MyPetRoutes.HUNGER) {
            data?.let { MyPetsHunger(it) }
        }
        composable(MyPetRoutes.HAPPINESS) {
            data?.let { Happiness(it) }
        }
    }
}

fun NavGraphBuilder.composable(
    route: MyPetRoutes,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable (NavBackStackEntry) -> Unit
){
    return composable(route.getRoute(), arguments, deepLinks, content)
}

fun NavController.navigate(route: MyPetRoutes){
    navigate(route.getRoute())
}