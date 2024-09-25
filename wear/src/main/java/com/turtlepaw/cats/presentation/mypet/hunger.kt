package com.turtlepaw.cats.presentation.mypet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
fun MyPetsHunger(
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
                    "\uD83D\uDC1F",
                    fontSize = 70.sp
                )
            }
            item {
                Text(text = "Hunger", style = MaterialTheme.typography.title3)
            }
            item {
                Text(text = hungerToReadableText(data.hunger))
            }
            item {
                Spacer(modifier = Modifier.height(5.dp))
            }
            item {
                val coroutineScope = rememberCoroutineScope()
                val context = LocalContext.current
                Button(onClick = {
                        coroutineScope.launch {
                            feedCatTreat(context, data.treats + data.dailyTreatsUsed)
                        }
                }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.primaryButtonColors(backgroundColor = Color(0xFF2986AE)), enabled = data.treats > 0) {
                    Text("${if(data.treats > 0) "Feed" else "No"} ${if(data.treats == 1) "treat" else ("treats")} (${data.treats.coerceAtLeast(0)})")
                }
            }
        }
    }
}