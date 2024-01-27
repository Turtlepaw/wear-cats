package com.turtlepaw.sleeptools.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.turtlepaw.sleeptools.utils.BedtimeModeManager
import com.turtlepaw.sleeptools.utils.BedtimeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BedtimeModeListener: BroadcastReceiver() {
    companion object {
        private const val TAG = "BEDTIME_MODE_LISTENER"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received bedtime mode change... ($intent)")
        CoroutineScope(Dispatchers.Default).launch {
            runReceiver(context)
        }
    }

    private suspend fun runReceiver(context: Context) {
        val bedtimeViewModel = BedtimeViewModel(context);
        val bedtimeManager = BedtimeModeManager()
        val bedtimeState = bedtimeManager.isBedtimeModeEnabled(context, bedtimeViewModel)
        Log.d(TAG, "Bedtime mode is currently $bedtimeState")
    }
}