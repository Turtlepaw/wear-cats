package com.turtlepaw.cats.services

import android.content.Context
import androidx.annotation.Keep
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.turtlepaw.cats.presentation.dataStore
import com.turtlepaw.cats.utils.ImageViewModel
@Keep
class CatDownloadWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val viewModel = ImageViewModel.getInstance(applicationContext.dataStore)
        viewModel.downloadImages(applicationContext)
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "DownloadWorker"
    }
}
