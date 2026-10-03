package com.xplozder.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.xplozder.GalacticTycoonsApp
import com.xplozder.data.local.entity.CachedMaterialPrice
import com.xplozder.data.local.entity.FleetMission
import com.xplozder.data.local.entity.TradeAlert
import com.xplozder.data.local.entity.TradeNotification
import com.xplozder.data.local.entity.TycoonNote
import com.xplozder.data.network.ApiClient
import com.xplozder.data.network.ExchangeRateLimiter
import com.xplozder.data.network.PBaseDetailResponseModel
import com.xplozder.data.network.PMyCompanyModel
import com.xplozder.data.network.PWarehouseModel
import com.xplozder.data.repository.GalacticRepository
import com.xplozder.model.Commodity
import com.xplozder.model.CommodityCategory
import com.xplozder.model.GalacticMarketData
import com.xplozder.model.GalacticProductionData
import com.xplozder.model.OrderBookEntry
import com.xplozder.model.ProductionRecipe
import com.xplozder.service.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GalacticTycoonsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GalacticRepository =
        (application as GalacticTycoonsApp).repository

    private val sharedPrefs = application.getSharedPreferences("galactic_tycoons_prefs", Context.MODE_PRIVATE)

    // Rate Limiting state (Exposed to UI for transparency)
    val remainingRateUnits: StateFlow<Int> = ExchangeRateLimiter.remainingUnits
    val rateResetSeconds: StateFlow<Long> = ExchangeRateLimiter.secondsUntilReset

    private val _rateLimitWarning = MutableStateFlow<String?>(null)
    val rateLimitWarning: StateFlow<String?> = _rateLimitWarning.asStateFlow()

    // Player Company API Key state for Fleet Telemetry & Base Ops
    private val _apiKey = MutableStateFlow<String?>(sharedPrefs.getString("gt_company_api_key", null))
    val apiKey: StateFlow<String?> = _apiKey.asStateFlow()

    val isApiKeyConfigured: StateFlow<Boolean> = MutableStateFlow(!_apiKey.value.isNullOrBlank()).apply {
        viewModelScope.launch {
            _apiKey.collect { value = !it.isNullOrBlank() }
        }
    }

    private val _companyData = MutableStateFlow<PMyCompanyModel?>(null)
    val companyData: StateFlow<PMyCompanyModel?> = _companyData.asStateFlow()

    private val _companyBases = MutableStateFlow<List<PBaseDetailResponseModel>>(emptyList())
    val companyBases: StateFlow<List<PBaseDetailResponseModel>> = _companyBases.asStateFlow()

    private val _companyWarehouses = MutableStateFlow<List<PWarehouseModel>>(emptyList())
    val companyWarehouses: StateFlow<List<PWarehouseModel>> = _companyWarehouses.asStateFlow()

    private val _isLoadingCompany = MutableStateFlow(false)
    val isLoadingCompany: StateFlow<Boolean> = _isLoadingCompany.asStateFlow()

    private val _companyError = MutableStateFlow<String?>(null)
    val companyError: StateFlow<String?> = _companyError.asStateFlow()

    // Market state
    private val _commodities = MutableStateFlow<List<Commodity>>(GalacticMarketData.getInitialCommodities())
    val commodities: StateFlow<List<Commodity>> = _commodities.asStateFlow()

    private val _selectedCategory = MutableStateFlow(CommodityCategory.ALL)
    val selectedCategory: StateFlow<CommodityCategory> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCommodity = MutableStateFlow<Commodity?>(null)
    val selectedCommodity: StateFlow<Commodity?> = _selectedCommodity.asStateFlow()

    private val _isLoadingLivePrices = MutableStateFlow(false)
    val isLoadingLivePrices: StateFlow<Boolean> = _isLoadingLivePrices.asStateFlow()

    private val _apiError = MutableStateFlow<String?>(null)
    val apiError: StateFlow<String?> = _apiError.asStateFlow()

    // Production Recipe Catalog & Price Map
    val productionRecipes: List<ProductionRecipe> = GalacticProductionData.getRecipes()

    private val _priceMap = MutableStateFlow<Map<Int, Double>>(emptyMap())
    val priceMap: StateFlow<Map<Int, Double>> = _priceMap.asStateFlow()

    // Room reactive flows
    val tradeAlerts: StateFlow<List<TradeAlert>> = repository.allAlerts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val notifications: StateFlow<List<TradeNotification>> = repository.allNotifications.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val unreadNotifCount: StateFlow<Int> = repository.unreadCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val fleetMissions: StateFlow<List<FleetMission>> = repository.allMissions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val tycoonNotes: StateFlow<List<TycoonNote>> = repository.allNotes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Live market ticker polling
    private val _isLiveTickerActive = MutableStateFlow(true)
    val isLiveTickerActive: StateFlow<Boolean> = _isLiveTickerActive.asStateFlow()

    private var tickerJob: Job? = null

    // Web Client configuration
    private val _webGameUrl = MutableStateFlow("https://g2.galactictycoons.com/")
    val webGameUrl: StateFlow<String> = _webGameUrl.asStateFlow()

    private val _isDesktopMode = MutableStateFlow(false)
    val isDesktopMode: StateFlow<Boolean> = _isDesktopMode.asStateFlow()

    // Notification Settings
    private val _notificationsEnabled = MutableStateFlow(sharedPrefs.getBoolean("pref_notifications_enabled", true))
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _tradeNotificationsEnabled = MutableStateFlow(sharedPrefs.getBoolean("pref_trade_notifications", true))
    val tradeNotificationsEnabled: StateFlow<Boolean> = _tradeNotificationsEnabled.asStateFlow()

    private val _fleetNotificationsEnabled = MutableStateFlow(sharedPrefs.getBoolean("pref_fleet_notifications", true))
    val fleetNotificationsEnabled: StateFlow<Boolean> = _fleetNotificationsEnabled.asStateFlow()

    // Android 12+ Material You Dynamic Color & Theme Modes
    private val _useDynamicColor = MutableStateFlow(sharedPrefs.getBoolean("pref_use_dynamic_color", false))
    val useDynamicColor: StateFlow<Boolean> = _useDynamicColor.asStateFlow()

    private val _themeMode = MutableStateFlow(sharedPrefs.getString("pref_theme_mode", "DARK") ?: "DARK")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    // Accessibility Options
    private val _highContrastEnabled = MutableStateFlow(sharedPrefs.getBoolean("pref_high_contrast", false))
    val highContrastEnabled: StateFlow<Boolean> = _highContrastEnabled.asStateFlow()

    private val _largeTextEnabled = MutableStateFlow(sharedPrefs.getBoolean("pref_large_text", false))
    val largeTextEnabled: StateFlow<Boolean> = _largeTextEnabled.asStateFlow()

    private val _reducedMotionEnabled = MutableStateFlow(sharedPrefs.getBoolean("pref_reduced_motion", false))
    val reducedMotionEnabled: StateFlow<Boolean> = _reducedMotionEnabled.asStateFlow()

    fun setNotificationsEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("pref_notifications_enabled", enabled).apply()
        _notificationsEnabled.value = enabled
    }

    fun setTradeNotificationsEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("pref_trade_notifications", enabled).apply()
        _tradeNotificationsEnabled.value = enabled
    }

    fun setFleetNotificationsEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("pref_fleet_notifications", enabled).apply()
        _fleetNotificationsEnabled.value = enabled
    }

    fun setUseDynamicColor(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("pref_use_dynamic_color", enabled).apply()
        _useDynamicColor.value = enabled
    }

    fun setThemeMode(mode: String) {
        sharedPrefs.edit().putString("pref_theme_mode", mode).apply()
        _themeMode.value = mode
    }

    fun setHighContrastEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("pref_high_contrast", enabled).apply()
        _highContrastEnabled.value = enabled
    }

    fun setLargeTextEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("pref_large_text", enabled).apply()
        _largeTextEnabled.value = enabled
    }

    fun setReducedMotionEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("pref_reduced_motion", enabled).apply()
        _reducedMotionEnabled.value = enabled
    }

    init {
        loadOfflineCachedPrices()
        fetchLiveExchangePrices()
        startLiveMarketPolling()

        // If player has already saved their API key, load company operations automatically
        if (!_apiKey.value.isNullOrBlank()) {
            fetchCompanyFleet()
        }
    }

    private fun loadOfflineCachedPrices() {
        viewModelScope.launch {
            try {
                val cached = repository.getCachedPricesSnapshot()
                if (cached.isNotEmpty()) {
                    val mapped = cached.map { c ->
                        Commodity(
                            matId = c.matId,
                            name = c.matName,
                            category = GalacticMarketData.categorizeMaterial(c.matName),
                            currentPriceCents = c.currentPriceCents,
                            avgPriceCents = c.avgPriceCents,
                            totalQtyAvailable = 0,
                            description = "Cached exchange rate"
                        )
                    }
                    _commodities.value = mapped
                    _priceMap.value = mapped.associate { it.matId to (it.currentPriceCents / 100.0) }
                }
            } catch (e: Exception) {
                Log.e("GalacticTycoons", "Failed to load cached prices", e)
            }
        }
    }

    fun selectCategory(category: CommodityCategory) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCommodity(commodity: Commodity?) {
        _selectedCommodity.value = commodity
        if (commodity != null) {
            fetchCommodityDetails(commodity.matId)
        }
    }

    fun toggleLiveTicker() {
        _isLiveTickerActive.value = !_isLiveTickerActive.value
        if (_isLiveTickerActive.value) {
            startLiveMarketPolling()
        } else {
            tickerJob?.cancel()
        }
    }

    fun setDesktopMode(enabled: Boolean) {
        _isDesktopMode.value = enabled
    }

    fun navigateWebGameTo(url: String) {
        _webGameUrl.value = url
    }

    // ==========================================
    // Player Company API Key Management
    // ==========================================

    fun saveApiKey(rawKey: String) {
        val trimmed = rawKey.trim()
        if (trimmed.isNotBlank()) {
            sharedPrefs.edit().putString("gt_company_api_key", trimmed).apply()
            _apiKey.value = trimmed
            _companyError.value = null
            fetchCompanyFleet()
        }
    }

    fun clearApiKey() {
        sharedPrefs.edit().remove("gt_company_api_key").apply()
        _apiKey.value = null
        _companyData.value = null
        _companyBases.value = emptyList()
        _companyWarehouses.value = emptyList()
        _companyError.value = null
    }

    fun fetchCompanyFleet() {
        val currentKey = _apiKey.value
        if (currentKey.isNullOrBlank()) {
            _companyError.value = "Company API Key required to stream telemetry."
            return
        }

        viewModelScope.launch {
            _isLoadingCompany.value = true
            _companyError.value = null
            val header = if (currentKey.startsWith("Bearer ", ignoreCase = true)) currentKey else "Bearer $currentKey"

            try {
                val companyResult = ApiClient.exchangeApi.getMyCompany(header)
                _companyData.value = companyResult
            } catch (e: Exception) {
                Log.e("GalacticTycoons", "Failed to fetch company", e)
                _companyError.value = "Authentication failed: ${e.localizedMessage ?: "Invalid Key"}"
            }

            try {
                val basesResult = ApiClient.exchangeApi.getMyBases(header)
                _companyBases.value = basesResult
            } catch (e: Exception) {
                Log.e("GalacticTycoons", "Failed to fetch bases", e)
            }

            try {
                val warehousesResult = ApiClient.exchangeApi.getMyWarehouses(header)
                _companyWarehouses.value = warehousesResult
            } catch (e: Exception) {
                Log.e("GalacticTycoons", "Failed to fetch warehouses", e)
            }

            _isLoadingCompany.value = false
        }
    }

    // ==========================================
    // Public Exchange API with Rate Limiting
    // ==========================================

    /**
     * GET /public/exchange/mat-prices
     * Cost: 5 units per call (Limit: 100 units / 5 minutes)
     */
    fun fetchLiveExchangePrices() {
        if (!ExchangeRateLimiter.canSpend(ExchangeRateLimiter.COST_MAT_PRICES_ALL)) {
            val waitSec = ExchangeRateLimiter.getWaitTimeSeconds(ExchangeRateLimiter.COST_MAT_PRICES_ALL)
            _rateLimitWarning.value = "Rate limit reached. Available in ${waitSec}s to avoid 429."
            return
        }

        viewModelScope.launch {
            _isLoadingLivePrices.value = true
            _apiError.value = null
            _rateLimitWarning.value = null

            // Record expenditure: 5 units
            ExchangeRateLimiter.trySpend(ExchangeRateLimiter.COST_MAT_PRICES_ALL)

            try {
                val response = ApiClient.exchangeApi.getMaterialPrices()
                val existingMap = _commodities.value.associateBy { it.matId }

                val updatedList = response.prices.map { dto ->
                    val existing = existingMap[dto.matId]
                    val category = existing?.category ?: GalacticMarketData.categorizeMaterial(dto.matName)
                    val priceDollars = dto.currentPrice / 100.0

                    val newHistory = if (existing != null && existing.priceHistory.isNotEmpty()) {
                        (existing.priceHistory + priceDollars).takeLast(8)
                    } else {
                        val avgDollars = dto.avgPrice / 100.0
                        listOf(avgDollars * 0.98, avgDollars, priceDollars)
                    }

                    Commodity(
                        matId = dto.matId,
                        name = dto.matName,
                        category = category,
                        currentPriceCents = dto.currentPrice,
                        avgPriceCents = dto.avgPrice,
                        totalQtyAvailable = existing?.totalQtyAvailable ?: 0,
                        unit = "$",
                        description = existing?.description ?: "Official trade commodity on Galactic Tycoons Exchange.",
                        priceHistory = newHistory,
                        sellOrders = existing?.sellOrders ?: emptyList()
                    )
                }

                _commodities.value = updatedList
                _priceMap.value = updatedList.associate { it.matId to (it.currentPriceCents / 100.0) }

                // Cache in local Room DB for offline launch
                val cacheEntities = response.prices.map {
                    CachedMaterialPrice(
                        matId = it.matId,
                        matName = it.matName,
                        currentPriceCents = it.currentPrice,
                        avgPriceCents = it.avgPrice
                    )
                }
                repository.cachePrices(cacheEntities)

                checkAlertTriggers(updatedList)
            } catch (e: Exception) {
                Log.e("GalacticTycoons", "Error fetching live exchange data", e)
                _apiError.value = e.localizedMessage ?: "Failed to connect to Exchange API"
            } finally {
                _isLoadingLivePrices.value = false
            }
        }
    }

    /**
     * GET /public/exchange/mat-details/{materialId}
     * Cost: 5 units per call
     */
    fun fetchCommodityDetails(matId: Int) {
        if (!ExchangeRateLimiter.canSpend(ExchangeRateLimiter.COST_MAT_DETAILS_SINGLE)) {
            val waitSec = ExchangeRateLimiter.getWaitTimeSeconds(ExchangeRateLimiter.COST_MAT_DETAILS_SINGLE)
            _rateLimitWarning.value = "Rate limit budget low. Available in ${waitSec}s."
            return
        }

        viewModelScope.launch {
            ExchangeRateLimiter.trySpend(ExchangeRateLimiter.COST_MAT_DETAILS_SINGLE)
            try {
                val details = ApiClient.exchangeApi.getMaterialDetails(matId)
                val mappedOrders = details.orders.map { orderDto ->
                    OrderBookEntry(
                        id = orderDto.id,
                        playerTycoon = orderDto.cName,
                        quantity = orderDto.qty,
                        priceCents = orderDto.unitPrice
                    )
                }

                // Update current commodity with live orders & quantity
                _commodities.value = _commodities.value.map { comm ->
                    if (comm.matId == matId) {
                        comm.copy(
                            currentPriceCents = details.currentPrice,
                            avgPriceCents = details.avgPrice,
                            totalQtyAvailable = details.totalQtyAvailable,
                            sellOrders = mappedOrders
                        )
                    } else comm
                }

                // Also update selectedCommodity
                _selectedCommodity.value?.let { current ->
                    if (current.matId == matId) {
                        _selectedCommodity.value = current.copy(
                            currentPriceCents = details.currentPrice,
                            avgPriceCents = details.avgPrice,
                            totalQtyAvailable = details.totalQtyAvailable,
                            sellOrders = mappedOrders
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("GalacticTycoons", "Error fetching order book for $matId", e)
            }
        }
    }

    private fun startLiveMarketPolling() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (_isLiveTickerActive.value) {
                // Poll every 60 seconds (Cost = 5 units/min = 25 units/5min, safely within 100 units budget)
                delay(60000)
                fetchLiveExchangePrices()
            }
        }
    }

    private suspend fun checkAlertTriggers(marketCommodities: List<Commodity>) {
        val activeAlerts = repository.getActiveAlerts()
        val now = System.currentTimeMillis()

        for (alert in activeAlerts) {
            val commodity = marketCommodities.find {
                it.matId.toString() == alert.commodityId || it.name.equals(alert.commodityName, ignoreCase = true)
            } ?: continue

            val lastTriggered = alert.lastTriggeredAt ?: 0L
            if (now - lastTriggered < 1000 * 60 * 5) {
                continue
            }

            val isTriggered = when (alert.condition) {
                "BELOW" -> commodity.currentPrice <= alert.targetPrice
                "ABOVE" -> commodity.currentPrice >= alert.targetPrice
                else -> false
            }

            if (isTriggered) {
                val conditionStr = if (alert.condition == "BELOW") "dropped below" else "spiked above"
                val title = "Trade Alert: ${commodity.name}"
                val message = "${commodity.name} is now \$${String.format(java.util.Locale.US, "%.2f", commodity.currentPrice)} ($conditionStr target \$${String.format(java.util.Locale.US, "%.2f", alert.targetPrice)})!"

                repository.insertNotification(
                    TradeNotification(
                        title = title,
                        message = message,
                        timestamp = now,
                        alertType = if (alert.condition == "BELOW") "PRICE_DROP" else "PRICE_SPIKE",
                        relatedCommodityId = commodity.id,
                        currentPrice = commodity.currentPrice
                    )
                )

                repository.updateAlertTriggered(alert.id, now)

                NotificationHelper.sendTradeAlertNotification(
                    context = getApplication(),
                    notificationId = alert.id.toInt(),
                    title = title,
                    message = message
                )
            }
        }
    }

    // Trade Alert Actions
    fun createAlert(commodity: Commodity, condition: String, targetPrice: Double, notes: String) {
        viewModelScope.launch {
            repository.insertAlert(
                TradeAlert(
                    commodityId = commodity.id,
                    commodityName = commodity.name,
                    condition = condition,
                    targetPrice = targetPrice,
                    unit = "$",
                    notes = notes
                )
            )
        }
    }

    fun toggleAlert(id: Long, isActive: Boolean) {
        viewModelScope.launch {
            repository.toggleAlertStatus(id, isActive)
        }
    }

    fun deleteAlert(id: Long) {
        viewModelScope.launch {
            repository.deleteAlertById(id)
        }
    }

    // Notification Actions
    fun markNotificationRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications()
        }
    }

    fun deleteNotification(id: Long) {
        viewModelScope.launch {
            repository.deleteNotification(id)
        }
    }

    // Fleet Actions
    fun dispatchFleetMission(
        shipName: String,
        shipType: String,
        origin: String,
        destination: String,
        cargoManifest: String,
        tons: Int,
        valueDollars: Double,
        durationMinutes: Int
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val mission = FleetMission(
                shipName = shipName,
                shipType = shipType,
                originPlanet = origin,
                destinationExchange = destination,
                cargoManifest = cargoManifest,
                cargoTons = tons,
                estimatedValueCr = valueDollars,
                departureTime = now,
                travelDurationSec = durationMinutes * 60,
                status = "IN_TRANSIT"
            )
            repository.insertMission(mission)

            NotificationHelper.sendTradeAlertNotification(
                context = getApplication(),
                notificationId = (5000..8000).random(),
                title = "Fleet Dispatched: $shipName",
                message = "Departed $origin headed for $destination with $cargoManifest ($tons units). ETA: ${durationMinutes}m",
                channelId = NotificationHelper.CHANNEL_FLEET_UPDATES
            )
        }
    }

    fun deleteFleetMission(id: Long) {
        viewModelScope.launch {
            repository.deleteMission(id)
        }
    }

    // Tycoon Notes Actions
    fun addNote(title: String, content: String, category: String) {
        viewModelScope.launch {
            repository.insertNote(
                TycoonNote(
                    title = title,
                    content = content,
                    category = category
                )
            )
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            repository.deleteNoteById(id)
        }
    }
}
