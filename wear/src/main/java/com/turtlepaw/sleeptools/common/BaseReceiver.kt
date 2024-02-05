package com.turtlepaw.sleeptools.common

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.turtlepaw.sleeptools.services.Receiver

abstract class BaseReceiver: BroadcastReceiver() {
    protected abstract val tag: String
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(tag, "Received intent: ${intent.action}")
    }
}