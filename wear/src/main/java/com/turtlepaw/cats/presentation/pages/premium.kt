package com.turtlepaw.cats.presentation.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.scrollAway
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll
import com.turtlepaw.cats.R
import com.turtlepaw.cats.presentation.components.ItemsListWithModifier
import com.turtlepaw.cats.presentation.components.Page
import com.turtlepaw.cats.presentation.theme.SleepTheme

@OptIn(ExperimentalHorologistApi::class, ExperimentalWearFoundationApi::class)
@Composable
fun PremiumScreen() {
    Page {
        item {
            Icon(
                painter = painterResource(id = R.drawable.round_auto_awesome_24),
                contentDescription = "Auto Awesome",
                tint = MaterialTheme.colors.primary,
                //modifier = Modifier.shimmer()
            )
        }
        item {
            Text(text = "Do more with premium")
        }
        item {
            Row(
                modifier = Modifier.padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val style = MaterialTheme.typography.title3
                val fontSize: TextUnit = style.fontSize
                val lineHeightDp: Dp = with(LocalDensity.current) {
                    fontSize.toDp()
                }
                Icon(
                    painter = painterResource(id = R.drawable.round_cloud_off_24),
                    contentDescription = "Offline",
                    tint = MaterialTheme.colors.primary,
                    modifier = Modifier.size(lineHeightDp)
                )

                Spacer(modifier = Modifier.padding(3.dp))

                Text(
                    text = "Offline Access",
                    //fontSize = fontSize,
                    style = style,
                    fontWeight = FontWeight.Normal
                )
            }
        }
        item {
            Row(
                modifier = Modifier.padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val style = MaterialTheme.typography.title3
                val fontSize: TextUnit = style.fontSize
                val lineHeightDp: Dp = with(LocalDensity.current) {
                    fontSize.toDp()
                }
                Icon(
                    painter = painterResource(id = R.drawable.round_cloud_off_24),
                    contentDescription = "Offline",
                    tint = MaterialTheme.colors.primary,
                    modifier = Modifier.size(lineHeightDp)
                )

                Spacer(modifier = Modifier.padding(3.dp))

                Text(
                    text = "Select Image Source",
                    //fontSize = fontSize,
                    style = style,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
private fun PremiumPreview() {
    PremiumScreen()
}