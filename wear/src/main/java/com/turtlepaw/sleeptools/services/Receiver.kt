package com.turtlepaw.sleeptools.services

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.annotation.Keep
import com.turtlepaw.sleeptools.common.BaseReceiver

@Keep
class Receiver: BaseReceiver() {
    override val tag = "Receiver"

    override fun onReceive(context: Context, intent: Intent) {
        if(intent.action == NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED){
            BedtimeModeListener().onReceive(context, intent)
        } else if(intent.action == NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED){
            ChargingReceiver().onReceive(context, intent)
        }
    }
}