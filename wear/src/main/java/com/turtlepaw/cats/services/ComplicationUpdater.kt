package com.turtlepaw.cats.services

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import com.turtlepaw.cats.complication.MainComplicationService
import com.turtlepaw.cats.tile.hapticClick

class ComplicationUpdater : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("ComplicationUpdater", "Received broadcast")
        if (context != null) {
            hapticClick(context)
        }

        // Perform the action here
        ComplicationDataSourceUpdateRequester
            .create(
                context = context!!,
                complicationDataSourceComponent = ComponentName(context, MainComplicationService::class.java)
            )
            .requestUpdateAll()
        // Launch the app
//        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
//        launchIntent?.let {
//            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK // Ensure the activity starts in a new task
//            context.startActivity(it)
//        }
    }
}
