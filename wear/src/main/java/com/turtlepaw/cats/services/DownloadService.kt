package com.turtlepaw.cats.services

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.provider.Settings
import androidx.annotation.Keep
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.edit
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.turtlepaw.cats.presentation.dataStore
import com.turtlepaw.cats.utils.ImageViewModel
import com.turtlepaw.cats.utils.SettingsBasics
import java.time.LocalDate


@Keep
class CatDownloadWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
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
        viewModel.downloadImages(applicationContext) { current, _ ->
            setProgress(Data.Builder().putInt("Progress", current).build())
        }

        sharedPreferences.edit {
            putString(
                com.turtlepaw.cats.utils.Settings.LAST_DOWNLOAD.getKey(),
                LocalDate.now().toString()
            )
        }

        connectivityManager.bindProcessToNetwork(null)
        connectivityManager.unregisterNetworkCallback(callback)
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "DownloadWorker"
    }
}
