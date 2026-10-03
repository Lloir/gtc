package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.AppDatabase
import com.example.data.local.entity.TradeNotification
import com.example.data.network.ApiClient
import com.example.data.network.ExchangeRateLimiter
import com.example.data.repository.GalacticRepository
import com.example.service.NotificationHelper
import java.util.Locale

class MarketAlertWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("MarketAlertWorker", "Checking market alerts in background...")
        val prefs = applicationContext.getSharedPreferences("galactic_tycoons_prefs", android.content.Context.MODE_PRIVATE)
        if (!prefs.getBoolean("pref_notifications_enabled", true)) {
            Log.d("MarketAlertWorker", "Notifications disabled in settings. Skipping check.")
            return Result.success()
        }

        val db = AppDatabase.getInstance(applicationContext)
        val repository = GalacticRepository(db)

        val activeAlerts = repository.getActiveAlerts()
        if (activeAlerts.isEmpty()) {
            return Result.success()
        }

        // Rate limit check: cost 5 units for mat-prices
        if (!ExchangeRateLimiter.canSpend(ExchangeRateLimiter.COST_MAT_PRICES_ALL)) {
            Log.w("MarketAlertWorker", "Skipping background check: Rate limit budget exhausted.")
            return Result.retry()
        }

        ExchangeRateLimiter.trySpend(ExchangeRateLimiter.COST_MAT_PRICES_ALL)

        return try {
            val response = ApiClient.exchangeApi.getMaterialPrices()
            val priceMap = response.prices.associateBy { it.matId }
            val now = System.currentTimeMillis()

            for (alert in activeAlerts) {
                val matId = alert.commodityId.toIntOrNull() ?: continue
                val priceDto = priceMap[matId] ?: continue

                val currentPrice = priceDto.currentPrice / 100.0
                val lastTriggered = alert.lastTriggeredAt ?: 0L

                // Don't re-alert within 15 minutes of same alert
                if (now - lastTriggered < 15 * 60 * 1000L) continue

                val isTriggered = when (alert.condition) {
                    "BELOW" -> currentPrice <= alert.targetPrice
                    "ABOVE" -> currentPrice >= alert.targetPrice
                    else -> false
                }

                if (isTriggered) {
                    val conditionStr = if (alert.condition == "BELOW") "dropped below" else "spiked above"
                    val title = "Galactic Alert: ${alert.commodityName}"
                    val message = "${alert.commodityName} is \$${String.format(Locale.US, "%.2f", currentPrice)} ($conditionStr target \$${String.format(Locale.US, "%.2f", alert.targetPrice)})!"

                    repository.insertNotification(
                        TradeNotification(
                            title = title,
                            message = message,
                            timestamp = now,
                            alertType = if (alert.condition == "BELOW") "PRICE_DROP" else "PRICE_SPIKE",
                            relatedCommodityId = alert.commodityId,
                            currentPrice = currentPrice
                        )
                    )

                    repository.updateAlertTriggered(alert.id, now)

                    NotificationHelper.sendTradeAlertNotification(
                        context = applicationContext,
                        notificationId = alert.id.toInt(),
                        title = title,
                        message = message
                    )
                }
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("MarketAlertWorker", "Failed to check market prices in background", e)
            Result.retry()
        }
    }
}
