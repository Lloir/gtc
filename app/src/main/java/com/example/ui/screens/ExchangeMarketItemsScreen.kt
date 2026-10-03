package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.network.ApiClient
import com.example.data.network.ExchangeRateLimiter
import com.example.data.network.GalacticExchangeApi
import com.example.data.network.MatDetailsResponse
import com.example.data.network.MatPriceDto
import com.example.model.Commodity
import com.example.model.CommodityCategory
import com.example.model.GalacticMarketData
import com.example.ui.components.LiveStatusPulse
import com.example.ui.components.PriceChangeBadge
import com.example.ui.components.SciFiCard
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.CyanElectric
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LossRed
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.SpaceCardBg
import com.example.ui.theme.SpaceCardBorder
import com.example.ui.theme.SpaceDark
import com.example.ui.theme.SpaceSurfaceLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.GalacticTycoonsViewModel
import kotlinx.coroutines.launch
import java.util.Locale

sealed interface ExchangeUiState {
    object Loading : ExchangeUiState
    data class Success(val items: List<MatPriceDto>) : ExchangeUiState
    data class Error(val message: String) : ExchangeUiState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExchangeMarketItemsScreen(
    viewModel: GalacticTycoonsViewModel,
    apiService: GalacticExchangeApi = ApiClient.exchangeApi,
    onNavigateToAlerts: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var uiState by remember { mutableStateOf<ExchangeUiState>(ExchangeUiState.Loading) }
    var isRefreshing by remember { mutableStateOf(false) }

    val vmSearchQuery by viewModel.searchQuery.collectAsState()
    var searchQuery by remember { mutableStateOf(vmSearchQuery) }
    var selectedCategory by remember { mutableStateOf(CommodityCategory.ALL) }

    LaunchedEffect(vmSearchQuery) {
        if (vmSearchQuery != searchQuery) {
            searchQuery = vmSearchQuery
        }
    }

    // Bottom sheet details for selected item
    var selectedMatId by remember { mutableStateOf<Int?>(null) }
    var matDetails by remember { mutableStateOf<MatDetailsResponse?>(null) }
    var isLoadingDetails by remember { mutableStateOf(false) }
    val detailsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val unreadNotifs by viewModel.unreadNotifCount.collectAsState()
    val remainingRateUnits by ExchangeRateLimiter.remainingUnits.collectAsState()
    val rateResetSeconds by ExchangeRateLimiter.secondsUntilReset.collectAsState()
    var rateLimitNotice by remember { mutableStateOf<String?>(null) }

    // Fetch exchange items function (Cost: 5 units)
    fun fetchItems() {
        if (!ExchangeRateLimiter.canSpend(ExchangeRateLimiter.COST_MAT_PRICES_ALL)) {
            val waitSec = ExchangeRateLimiter.getWaitTimeSeconds(ExchangeRateLimiter.COST_MAT_PRICES_ALL)
            rateLimitNotice = "Rate limit protection: Available in ${waitSec}s (100 units/5m budget)"
            return
        }

        coroutineScope.launch {
            isRefreshing = true
            rateLimitNotice = null
            ExchangeRateLimiter.trySpend(ExchangeRateLimiter.COST_MAT_PRICES_ALL)
            try {
                val response = apiService.getMaterialPrices()
                uiState = ExchangeUiState.Success(response.prices)
            } catch (e: Exception) {
                // If network fails, fallback to initial cached baseline
                val fallback = GalacticMarketData.getInitialCommodities().map {
                    MatPriceDto(
                        matId = it.matId,
                        matName = it.name,
                        currentPrice = it.currentPriceCents,
                        avgPrice = it.avgPriceCents
                    )
                }
                if (fallback.isNotEmpty()) {
                    uiState = ExchangeUiState.Success(fallback)
                } else {
                    uiState = ExchangeUiState.Error(e.localizedMessage ?: "Failed to connect to Galactic Exchange API")
                }
            } finally {
                isRefreshing = false
            }
        }
    }

    // Initial fetch on composition
    LaunchedEffect(Unit) {
        fetchItems()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Screen Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "EXCHANGE MARKET",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    letterSpacing = 1.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = CyanElectric,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "API Service: api.g2.galactictycoons.com",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyanGlow,
                        fontSize = 11.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                LiveStatusPulse(isActive = true, label = "EXCHANGE API")
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { fetchItems() },
                    modifier = Modifier
                        .size(36.dp)
                        .background(SpaceSurfaceLight, RoundedCornerShape(10.dp))
                        .testTag("refresh_exchange_api_button")
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            color = CyanElectric,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh API Data",
                            tint = CyanElectric,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Real-Time Search Bar at the top of Market screen
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                viewModel.setSearchQuery(it)
            },
            placeholder = { Text("Search materials by name or #ID (e.g. Iron, Copper)...", color = TextMuted, fontSize = 13.sp) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search Market", tint = CyanElectric)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            searchQuery = ""
                            viewModel.setSearchQuery("")
                        },
                        modifier = Modifier.testTag("clear_search_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Clear Search", tint = TextSecondary)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SpaceCardBg,
                unfocusedContainerColor = SpaceCardBg,
                focusedBorderColor = CyanElectric,
                unfocusedBorderColor = SpaceCardBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .testTag("market_search_bar")
        )

        // Rate Limit Budget Bar (100 units / 5 min per IP)
        Surface(
            color = if (remainingRateUnits < 15) Color(0x22FF5252) else Color(0x1200E5FF),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = if (remainingRateUnits < 15) AlertAmber else CyanElectric,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Rate Budget: $remainingRateUnits/100 units",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (remainingRateUnits < 15) AlertAmber else TextPrimary
                    )
                }
                val resetText = if (rateResetSeconds > 0) "Window reset in ${rateResetSeconds}s" else "Full budget available"
                Text(
                    text = resetText,
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }

        if (rateLimitNotice != null) {
            Surface(
                color = Color(0x33FF9100),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = AlertAmber, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(rateLimitNotice ?: "", fontSize = 11.sp, color = AlertAmber, modifier = Modifier.weight(1f))
                    IconButton(onClick = { rateLimitNotice = null }, modifier = Modifier.size(18.dp)) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = AlertAmber, modifier = Modifier.size(12.dp))
                    }
                }
            }
        }

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 6.dp)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CommodityCategory.values().forEach { category ->
                val isSelected = category == selectedCategory
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = category },
                    label = {
                        Text(
                            text = category.displayName,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = SpaceCardBg,
                        labelColor = TextSecondary,
                        selectedContainerColor = Color(0x3300E5FF),
                        selectedLabelColor = CyanGlow
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (isSelected) CyanElectric else SpaceCardBorder,
                        selectedBorderColor = CyanElectric,
                        enabled = true,
                        selected = isSelected
                    ),
                    modifier = Modifier.testTag("exchange_chip_${category.name.lowercase()}")
                )
            }
        }

        // Quick Alert Notice Strip
        Surface(
            color = GoldAccent.copy(alpha = 0.12f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable { onNavigateToAlerts() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Real-Time Trade Alerts & Orders",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = GoldAccent
                    )
                }
                if (unreadNotifs > 0) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = AlertAmber
                    ) {
                        Text(
                            text = "$unreadNotifs new",
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                } else {
                    Text(text = "View Alerts →", fontSize = 11.sp, color = GoldAccent)
                }
            }
        }

        // Main UI Body: Lazy List or State Handlers
        when (val state = uiState) {
            is ExchangeUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = CyanElectric)
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Connecting to Galactic Tycoons Exchange API...",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            is ExchangeUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = LossRed,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Exchange API Unreachable",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = state.message,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { fetchItems() },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanElectric)
                        ) {
                            Text("Retry Connection", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            is ExchangeUiState.Success -> {
                // Filter items based on category and search query
                val filteredItems = remember(state.items, selectedCategory, searchQuery) {
                    state.items.filter { item ->
                        val itemCategory = GalacticMarketData.categorizeMaterial(item.matName)
                        val matchesCategory = selectedCategory == CommodityCategory.ALL || itemCategory == selectedCategory
                        val matchesSearch = searchQuery.isBlank() ||
                                item.matName.contains(searchQuery, ignoreCase = true) ||
                                item.matId.toString() == searchQuery.trim() ||
                                itemCategory.displayName.contains(searchQuery, ignoreCase = true)
                        matchesCategory && matchesSearch
                    }
                }

                // Items Count Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val countText = if (searchQuery.isNotBlank()) {
                        "${filteredItems.size} FOUND FOR \"${searchQuery.uppercase()}\""
                    } else {
                        "${filteredItems.size} MATERIALS AVAILABLE"
                    }
                    Text(
                        text = countText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (searchQuery.isNotBlank()) CyanElectric else TextMuted,
                        letterSpacing = 0.5.sp
                    )

                    if (searchQuery.isNotBlank()) {
                        Text(
                            text = "Reset Search",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanGlow,
                            modifier = Modifier
                                .clickable {
                                    searchQuery = ""
                                    viewModel.setSearchQuery("")
                                }
                                .padding(4.dp)
                        )
                    } else {
                        Text(
                            text = "Prices in USD ($)",
                            fontSize = 10.sp,
                            color = CyanElectric
                        )
                    }
                }

                // Lazy Column of Exchange Items
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .testTag("exchange_items_lazy_list"),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (filteredItems.isEmpty()) {
                        item {
                            SciFiCard(
                                borderColor = SpaceCardBorder,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp)
                                    .testTag("empty_search_results_card")
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SearchOff,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(44.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = if (searchQuery.isNotBlank()) "No materials match \"$searchQuery\"" else "No materials in this category",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Try typing another material name, or reset your search filter.",
                                        fontSize = 12.sp,
                                        color = TextSecondary,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Button(
                                        onClick = {
                                            searchQuery = ""
                                            viewModel.setSearchQuery("")
                                            selectedCategory = CommodityCategory.ALL
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CyanElectric),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Clear Search & Filter", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    } else {
                        items(filteredItems, key = { it.matId }) { item ->
                        ExchangeItemCard(
                            item = item,
                            onClick = {
                                selectedMatId = item.matId
                                isLoadingDetails = true
                                matDetails = null
                                coroutineScope.launch {
                                    if (ExchangeRateLimiter.trySpend(ExchangeRateLimiter.COST_MAT_DETAILS_SINGLE)) {
                                        try {
                                            matDetails = apiService.getMaterialDetails(item.matId)
                                        } catch (e: Exception) {
                                            // Handle error cleanly
                                        } finally {
                                            isLoadingDetails = false
                                        }
                                    } else {
                                        isLoadingDetails = false
                                        val waitSec = ExchangeRateLimiter.getWaitTimeSeconds(ExchangeRateLimiter.COST_MAT_DETAILS_SINGLE)
                                        rateLimitNotice = "Rate limit reached (cost: 5 units). Available in ${waitSec}s."
                                    }
                                }
                            },
                            onSetAlert = {
                                // Add price alert for this item
                                val comm = Commodity(
                                    matId = item.matId,
                                    name = item.matName,
                                    category = GalacticMarketData.categorizeMaterial(item.matName),
                                    currentPriceCents = item.currentPrice,
                                    avgPriceCents = item.avgPrice
                                )
                                viewModel.createAlert(comm, "BELOW", (item.currentPrice / 100.0) * 0.95, "Exchange API threshold alert")
                            }
                        )
                    }
                }
            }
        }
    }
    }

    // Modal Bottom Sheet for Live Order Book & Item Details
    selectedMatId?.let { matId ->
        ModalBottomSheet(
            onDismissRequest = { selectedMatId = null },
            sheetState = detailsSheetState,
            containerColor = SpaceDark,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .size(36.dp, 4.dp)
                        .background(SpaceCardBorder, RoundedCornerShape(2.dp))
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .padding(bottom = 32.dp)
            ) {
                if (isLoadingDetails) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = CyanElectric)
                    }
                } else {
                    val details = matDetails
                    if (details != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = details.matName,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Material #${details.matId} • Central Exchange",
                                    fontSize = 12.sp,
                                    color = CyanElectric
                                )
                            }

                            val currentDollars = details.currentPrice / 100.0
                            val avgDollars = details.avgPrice / 100.0
                            val changePct = if (details.avgPrice > 0) {
                                ((details.currentPrice - details.avgPrice).toDouble() / details.avgPrice.toDouble()) * 100.0
                            } else 0.0

                            PriceChangeBadge(changePercent = changePct)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Stats Summary Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SpaceCardBg, RoundedCornerShape(12.dp))
                                .border(1.dp, SpaceCardBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("CURRENT", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                Text(String.format(Locale.US, "$%.2f", details.currentPrice / 100.0), fontSize = 15.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("24H AVERAGE", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                Text(String.format(Locale.US, "$%.2f", details.avgPrice / 100.0), fontSize = 15.sp, color = CyanElectric, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("SUPPLY", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                val supplyStr = if (details.totalQtyAvailable > 0) {
                                    String.format(Locale.US, "%,d", details.totalQtyAvailable)
                                } else "Active"
                                Text(supplyStr, fontSize = 14.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                            }
                        }

                        // D3 Historical Price Trend Component
                        val d3History = if (details.priceHistory.isNotEmpty()) {
                            details.priceHistory
                        } else {
                            listOf(
                                com.example.data.network.PExchangePriceHistoryModel(
                                    date = "24h Avg",
                                    avgPrice = if (details.avgPrice > 0) details.avgPrice else details.currentPrice,
                                    qtySold = 1000
                                ),
                                com.example.data.network.PExchangePriceHistoryModel(
                                    date = "Current",
                                    avgPrice = details.currentPrice,
                                    qtySold = details.totalQtyAvailable
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        com.example.ui.components.D3PriceTrendChart(
                            history = d3History,
                            matName = details.matName
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "LIVE PLAYER SELL ORDERS (${details.orders.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        if (details.orders.isEmpty()) {
                            Surface(
                                color = SpaceCardBg,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "No active public sell orders currently on the exchange for this material.",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0x1000E5FF), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                details.orders.take(8).forEach { order ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            order.cName,
                                            fontSize = 12.sp,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                String.format(Locale.US, "%,d units", order.qty),
                                                fontSize = 11.sp,
                                                color = TextSecondary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                String.format(Locale.US, "$%.2f", order.unitPrice / 100.0),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ProfitGreen
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExchangeItemCard(
    item: MatPriceDto,
    onClick: () -> Unit,
    onSetAlert: () -> Unit
) {
    val currentDollars = item.currentPrice / 100.0
    val avgDollars = item.avgPrice / 100.0
    val changePercent = if (item.avgPrice > 0) {
        ((item.currentPrice - item.avgPrice).toDouble() / item.avgPrice.toDouble()) * 100.0
    } else 0.0
    val isPositive = item.currentPrice >= item.avgPrice
    val category = GalacticMarketData.categorizeMaterial(item.matName)

    SciFiCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("exchange_item_card_${item.matId}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: ID, Name, Category, Change % Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = SpaceSurfaceLight,
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = "#${item.matId}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                        Text(
                            text = item.matName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = category.displayName,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                // % Price Difference Badge
                val badgeColor = if (isPositive) ProfitGreen else LossRed
                val badgeBg = if (isPositive) Color(0x2200E676) else Color(0x22FF5252)

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = String.format(Locale.US, "%+.1f%%", changePercent),
                            color = badgeColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Price Details and Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = String.format(Locale.US, "$%.2f", currentDollars),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = String.format(Locale.US, "Avg: $%.2f", avgDollars),
                            fontSize = 12.sp,
                            color = CyanElectric,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                    Text(
                        text = "Rate: ${item.currentPrice}¢ / unit",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = onSetAlert,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldAccent),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0x55FFD54F))
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAlert,
                            contentDescription = "Alert",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Alert", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
