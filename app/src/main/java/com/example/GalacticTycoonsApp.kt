package com.example

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.data.local.AppDatabase
import com.example.data.repository.GalacticRepository
import com.example.service.NotificationHelper
import com.example.worker.MarketAlertWorker
import java.util.concurrent.TimeUnit

class GalacticTycoonsApp : Application(), Configuration.Provider {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: GalacticRepository
        private set

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        repository = GalacticRepository(database)
        NotificationHelper.createNotificationChannels(this)

        scheduleBackgroundAlertSync()
    }

    private fun scheduleBackgroundAlertSync() {
        try {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val periodicWorkRequest = PeriodicWorkRequestBuilder<MarketAlertWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "GalacticMarketAlertSync",
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWorkRequest
            )
        } catch (e: Exception) {
            Log.w("GalacticTycoonsApp", "WorkManager initialization skipped/deferred: ${e.message}")
        }
    }
}
