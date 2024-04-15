package com.turtlepaw.cats.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.Keep
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.edit
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.turtlepaw.cats.R
import com.turtlepaw.cats.presentation.dataStore
import com.turtlepaw.cats.utils.ImageViewModel
import com.turtlepaw.cats.utils.SettingsBasics
import java.time.LocalDate


@Keep
class CatDownloadWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    private val notificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    override suspend fun doWork(): Result {
        showNotification(0, 100)
        val sharedPreferences = applicationContext.getSharedPreferences(
            SettingsBasics.SHARED_PREFERENCES.getKey(),
            SettingsBasics.SHARED_PREFERENCES.getMode()
        )
        // Request Wifi Connectivity
        val connectivityManager =
            applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                // The Wi-Fi network has been acquired. Bind it to use this network by default.
                connectivityManager.bindProcessToNetwork(network)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                // Called when a network disconnects or otherwise no longer satisfies this request or callback.
            }
        }
        connectivityManager.requestNetwork(
            NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build(),
            callback
        )

        val viewModel = ImageViewModel.getInstance(applicationContext.dataStore)
        viewModel.downloadImages(applicationContext) { current, max ->
//            setProgress(Data.Builder().putInt("Progress", current).build())
//            Log.d("DownloadProgress", "Downloading $current out of $max")
            setProgress(workDataOf("Progress" to current))
            showNotification(current, max)
            Log.d("DownloadProgress", "Setting progress to: $current of $max")
        }

        sharedPreferences.edit {
            putString(
                com.turtlepaw.cats.utils.Settings.LAST_DOWNLOAD.getKey(),
                LocalDate.now().toString()
            )
        }

        notificationManager.cancel(NOTIFICATION_ID)
        connectivityManager.bindProcessToNetwork(null)
        connectivityManager.unregisterNetworkCallback(callback)
        return Result.success()
    }

    private fun showNotification(currentProgress: Int, maxProgress: Int) {
        val progressNotification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("Downloading")
            .setContentText("$currentProgress of $maxProgress images")
            .setSmallIcon(R.drawable.offline_download)
            .setProgress(maxProgress, currentProgress, false)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .build()
        notificationManager.notify(NOTIFICATION_ID, progressNotification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Download Notification",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Offline download progress"
        }
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val WORK_NAME = "DownloadWorker"
        const val PERIODIC_WORK_NAME = "PeriodicDownloadWorker"
        private const val CHANNEL_ID = "DownloadChannel"
        private const val NOTIFICATION_ID = 1
    }
}
