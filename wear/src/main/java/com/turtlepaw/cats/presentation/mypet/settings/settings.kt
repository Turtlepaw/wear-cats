package com.turtlepaw.cats.presentation.mypet.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.turtlepaw.cats.R
import com.turtlepaw.cats.presentation.MyPetRoutes
import com.turtlepaw.cats.presentation.components.Page
import com.turtlepaw.cats.presentation.navigate
import java.util.Locale

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun Settings(
    navController: NavHostController,
    stepGoal: Int
) {
    Page {
        item {
            Text(text = "My Pet Settings")
        }
        item {
            Spacer(modifier = Modifier.height(5.dp))
        }
        item {
            Chip(
                onClick = {
                    navController.navigate(MyPetRoutes.StepGoal.getRoute())
                },
                label = { Text(text = "Step Goal") },
                icon = {
                    Icon(painter = painterResource(id = R.drawable.steps), contentDescription = "Steps")
                },
                modifier = Modifier.fillMaxWidth(),
                secondaryLabel = {
                    Text(
                        String.format(Locale.getDefault(), "%,d", stepGoal)
                    )
                }
            )
        }
    }
}