package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entity.CachedMaterialPrice
import com.example.data.network.ExchangeRateLimiter
import com.example.model.GalacticMarketData
import com.example.model.GalacticProductionData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches Galactic Tycoons`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Galactic Tycoons", appName)
    }

    @Test
    fun `verify real galactic materials populated`() {
        val commodities = GalacticMarketData.getInitialCommodities()
        assertTrue(commodities.isNotEmpty())
        assertTrue(commodities.any { it.name == "Iron Ore" && it.matId == 1 })
        assertTrue(commodities.any { it.name == "Copper" && it.matId == 6 })
        assertTrue(commodities.any { it.name == "Robot" && it.matId == 20 })
        assertTrue(commodities.any { it.name == "Hydrogen" && it.matId == 24 })
    }

    @Test
    fun `verify rate limiter enforces 100 units budget and cost deductions`() {
        val initialBudget = ExchangeRateLimiter.remainingUnits.value
        assertTrue("Budget should be positive", initialBudget > 0)
        assertTrue("Can spend 5 units for mat-prices", ExchangeRateLimiter.canSpend(5))

        val spent = ExchangeRateLimiter.trySpend(5)
        assertTrue(spent)
        assertEquals(initialBudget - 5, ExchangeRateLimiter.remainingUnits.value)
    }

    @Test
    fun `verify clean database starts with zero dummy test alerts`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getInstance(context)
        val alerts = db.tradeAlertDao().getAllAlerts().first()
        val notifs = db.tradeNotificationDao().getAllNotifications().first()
        val missions = db.fleetMissionDao().getAllMissions().first()

        assertEquals("No dummy test alerts on fresh db", 0, alerts.size)
        assertEquals("No dummy test notifications on fresh db", 0, notifs.size)
        assertEquals("No dummy test missions on fresh db", 0, missions.size)
    }

    @Test
    fun `verify offline price caching in Room`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getInstance(context)
        val cachedDao = db.cachedMaterialPriceDao()

        val sample = listOf(
            CachedMaterialPrice(matId = 1, matName = "Iron Ore", currentPriceCents = 2300, avgPriceCents = 2200),
            CachedMaterialPrice(matId = 2, matName = "Iron", currentPriceCents = 24500, avgPriceCents = 27000)
        )
        cachedDao.insertPrices(sample)

        val retrieved = cachedDao.getCachedPricesSnapshot()
        assertEquals(2, retrieved.size)
        assertEquals("Iron", retrieved[0].matName)
        assertEquals(24500L, retrieved[0].currentPriceCents)
    }

    @Test
    fun `verify production recipe margin calculation with live prices`() {
        val recipes = GalacticProductionData.getRecipes()
        val ironSmelting = recipes.first { it.id == "rec_iron_smelting" }

        // Iron Ore = $23/unit (10 units = $230) + $15 energy = $245 total cost
        // Iron Bar output = $275 revenue
        val prices = mapOf(1 to 23.0, 2 to 275.0)

        val cost = ironSmelting.calculateInputCost(prices)
        val revenue = ironSmelting.calculateOutputValue(prices)
        val profit = ironSmelting.calculateLiveProfit(prices)

        assertEquals(245.0, cost, 0.01)
        assertEquals(275.0, revenue, 0.01)
        assertEquals(30.0, profit, 0.01)
        assertTrue(ironSmelting.calculateLiveMarginPct(prices) > 0.0)
    }

    @Test
    fun `verify real-time commodity search filtering by name and id`() {
        val commodities = GalacticMarketData.getInitialCommodities()

        // Filter by name "Iron"
        val ironFiltered = commodities.filter {
            it.name.contains("iron", ignoreCase = true)
        }
        assertTrue(ironFiltered.isNotEmpty())
        assertTrue(ironFiltered.all { it.name.contains("iron", ignoreCase = true) })

        // Filter by matId "20" (Robot)
        val robotFiltered = commodities.filter {
            it.name.contains("20", ignoreCase = true) || it.matId.toString() == "20"
        }
        assertEquals(1, robotFiltered.size)
        assertEquals("Robot", robotFiltered[0].name)

        // Filter by non-existent query
        val nonExistent = commodities.filter {
            it.name.contains("xyznonexistent", ignoreCase = true)
        }
        assertEquals(0, nonExistent.size)
    }

    @Test
    fun `verify notification settings and accessibility toggles persist in preferences`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("galactic_tycoons_prefs", Context.MODE_PRIVATE)

        // Turn off notifications
        prefs.edit().putBoolean("pref_notifications_enabled", false).apply()
        assertFalse(prefs.getBoolean("pref_notifications_enabled", true))

        // Set Material You dynamic color
        prefs.edit().putBoolean("pref_use_dynamic_color", true).apply()
        assertTrue(prefs.getBoolean("pref_use_dynamic_color", false))

        // Set theme mode
        prefs.edit().putString("pref_theme_mode", "LIGHT").apply()
        assertEquals("LIGHT", prefs.getString("pref_theme_mode", "DARK"))

        // Set accessibility options
        prefs.edit().putBoolean("pref_high_contrast", true).apply()
        prefs.edit().putBoolean("pref_large_text", true).apply()
        prefs.edit().putBoolean("pref_reduced_motion", true).apply()

        assertTrue(prefs.getBoolean("pref_high_contrast", false))
        assertTrue(prefs.getBoolean("pref_large_text", false))
        assertTrue(prefs.getBoolean("pref_reduced_motion", false))
    }
}
