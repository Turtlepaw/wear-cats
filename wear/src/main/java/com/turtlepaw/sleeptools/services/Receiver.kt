package com.turtlepaw.sleeptools.services

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.Keep

@Keep
class Receiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if(intent.action == NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED){
            BedtimeModeReceiver().onReceive(context, intent)
        } else if(intent.action == Intent.ACTION_POWER_CONNECTED){
            ChargingReceiver().onReceive(context, intent)
        }
    }
}