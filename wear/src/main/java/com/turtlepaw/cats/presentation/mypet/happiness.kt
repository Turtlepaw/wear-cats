package com.turtlepaw.cats.presentation.mypet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.turtlepaw.cats.mypet.CatStatus
import com.turtlepaw.cats.mypet.saveCatStatus
import com.turtlepaw.cats.presentation.components.Page
import feedCatTreat
import kotlinx.coroutines.launch

@OptIn(ExperimentalHorologistApi::class, ExperimentalWearFoundationApi::class)
@Composable
fun Happiness(
    data: CatStatus
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
                Text(
                    "🌟",
                    fontSize = 50.sp
                )
            }
            item {
                Spacer(modifier = Modifier.height(5.dp))
            }
            item {
                Text(text = "Happiness", style = MaterialTheme.typography.title3)
            }
            item {
                Text(text = happinessToReadableText(data.happiness))
            }
            item {
                Spacer(modifier = Modifier.height(5.dp))
            }
            items(data.happinessReasons.size){
                val reason = data.happinessReasons.toList().elementAt(it)
                Row {
                    Text(reason.second.toString(), color = if(reason.second > 0) MaterialTheme.colors.primary else MaterialTheme.colors.error, style = MaterialTheme.typography.title2)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(reason.first, style = MaterialTheme.typography.title2)
                }
            }
        }
    }
}