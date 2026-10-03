package com.xplozder.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xplozder.model.Commodity
import com.xplozder.model.CommodityCategory
import com.xplozder.ui.components.LiveStatusPulse
import com.xplozder.ui.components.PriceChangeBadge
import com.xplozder.ui.components.SciFiCard
import com.xplozder.ui.components.SparklineMiniChart
import com.xplozder.ui.theme.AlertAmber
import com.xplozder.ui.theme.CyanElectric
import com.xplozder.ui.theme.CyanGlow
import com.xplozder.ui.theme.GoldAccent
import com.xplozder.ui.theme.LossRed
import com.xplozder.ui.theme.ProfitGreen
import com.xplozder.ui.theme.SpaceCardBg
import com.xplozder.ui.theme.SpaceCardBorder
import com.xplozder.ui.theme.SpaceDark
import com.xplozder.ui.theme.SpaceSurfaceLight
import com.xplozder.ui.theme.TextMuted
import com.xplozder.ui.theme.TextPrimary
import com.xplozder.ui.theme.TextSecondary
import com.xplozder.viewmodel.GalacticTycoonsViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExchangeMarketScreen(
    viewModel: GalacticTycoonsViewModel,
    onNavigateToAlerts: () -> Unit
) {
    val commodities by viewModel.commodities.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLiveActive by viewModel.isLiveTickerActive.collectAsState()
    val unreadNotifs by viewModel.unreadNotifCount.collectAsState()
    val isLoading by viewModel.isLoadingLivePrices.collectAsState()

    var sheetCommodity by remember { mutableStateOf<Commodity?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val filteredCommodities = remember(commodities, selectedCategory, searchQuery) {
        commodities.filter { item ->
            val matchesCategory = selectedCategory == CommodityCategory.ALL || item.category == selectedCategory
            val matchesSearch = searchQuery.isBlank() ||
                    item.name.contains(searchQuery, ignoreCase = true) ||
                    item.category.displayName.contains(searchQuery, ignoreCase = true) ||
                    item.matId.toString() == searchQuery.trim()
            matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "GALACTIC EXCHANGE",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = 1.sp
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = CyanElectric,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "api.g2.galactictycoons.com • ${commodities.size} Live Items",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyanGlow,
                        fontSize = 11.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                LiveStatusPulse(isActive = isLiveActive)
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { viewModel.fetchLiveExchangePrices() },
                    modifier = Modifier
                        .size(36.dp)
                        .background(SpaceSurfaceLight, RoundedCornerShape(10.dp))
                        .testTag("refresh_market_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = CyanElectric,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Market API",
                            tint = CyanElectric,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Search materials (Iron Ore, Copper, Robot, Concrete...)", color = TextMuted, fontSize = 13.sp) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = CyanElectric)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary)
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
                .padding(horizontal = 16.dp)
                .testTag("market_search_bar")
        )

        // Categories Scroll
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
                    onClick = { viewModel.selectCategory(category) },
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
                    modifier = Modifier.testTag("category_chip_${category.name.lowercase()}")
                )
            }
        }

        // Quick Alert Summary Bar
        Surface(
            color = Color(0x15FFD54F),
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
                        text = "Trade Alerts & Order Notifications",
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

        // Commodities List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredCommodities, key = { it.matId }) { commodity ->
                CommodityMarketCard(
                    commodity = commodity,
                    onClick = {
                        sheetCommodity = commodity
                        viewModel.fetchCommodityDetails(commodity.matId)
                    },
                    onSetAlert = {
                        sheetCommodity = commodity
                        viewModel.fetchCommodityDetails(commodity.matId)
                    }
                )
            }
        }
    }

    // Commodity Detail Bottom Sheet
    sheetCommodity?.let { commodity ->
        // Keep synced with state if details loaded
        val currentInState = commodities.find { it.matId == commodity.matId } ?: commodity

        ModalBottomSheet(
            onDismissRequest = { sheetCommodity = null },
            sheetState = sheetState,
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
            CommodityDetailSheetContent(
                commodity = currentInState,
                onDismiss = { sheetCommodity = null },
                onSaveAlert = { condition, price, notes ->
                    viewModel.createAlert(currentInState, condition, price, notes)
                    sheetCommodity = null
                }
            )
        }
    }
}

@Composable
fun CommodityMarketCard(
    commodity: Commodity,
    onClick: () -> Unit,
    onSetAlert: () -> Unit
) {
    SciFiCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("commodity_card_${commodity.matId}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                                text = "#${commodity.matId}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                        Text(
                            text = commodity.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = commodity.category.displayName,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                PriceChangeBadge(changePercent = commodity.changePercent)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Price Info
                Column {
                    val formattedPrice = String.format(Locale.US, "$%.2f", commodity.currentPrice)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = formattedPrice,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        val formattedAvg = String.format(Locale.US, "Avg $%.2f", commodity.avgPrice)
                        Text(
                            text = formattedAvg,
                            fontSize = 12.sp,
                            color = CyanElectric,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }

                    if (commodity.totalQtyAvailable > 0) {
                        val formattedSupply = String.format(Locale.US, "%,d", commodity.totalQtyAvailable)
                        Text(
                            text = "Market Supply: $formattedSupply",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    } else {
                        Text(
                            text = "Exchange Rate: ${commodity.currentPriceCents}¢",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                // Mini Graph
                SparklineMiniChart(
                    points = if (commodity.priceHistory.size >= 2) commodity.priceHistory else listOf(commodity.avgPrice, commodity.currentPrice),
                    isPositive = commodity.isTrendingUp,
                    modifier = Modifier
                        .size(width = 90.dp, height = 34.dp)
                        .padding(horizontal = 4.dp)
                )

                // Quick Alert Action
                OutlinedButton(
                    onClick = onSetAlert,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldAccent),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0x55FFD54F))),
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

@Composable
fun CommodityDetailSheetContent(
    commodity: Commodity,
    onDismiss: () -> Unit,
    onSaveAlert: (condition: String, targetPrice: Double, notes: String) -> Unit
) {
    var alertCondition by remember { mutableStateOf("BELOW") }
    var targetPriceInput by remember { mutableStateOf(String.format(Locale.US, "%.2f", commodity.currentPrice * 0.95)) }
    var notesInput by remember { mutableStateOf("") }
    var showSetAlertSection by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .padding(bottom = 32.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = commodity.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
                Text(
                    text = "ID: ${commodity.matId} • ${commodity.category.displayName} • Central Exchange",
                    fontSize = 12.sp,
                    color = CyanElectric
                )
            }
            PriceChangeBadge(changePercent = commodity.changePercent)
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = commodity.description.ifBlank { "Official trade commodity on the Galactic Tycoons Exchange." },
            fontSize = 12.sp,
            color = TextSecondary,
            lineHeight = 17.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Key stats 3-column box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SpaceCardBg, RoundedCornerShape(12.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("CURRENT", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                Text(String.format(Locale.US, "$%.2f", commodity.currentPrice), fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("DAY AVERAGE", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                Text(String.format(Locale.US, "$%.2f", commodity.avgPrice), fontSize = 14.sp, color = CyanElectric, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("TOTAL SUPPLY", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                val supplyStr = if (commodity.totalQtyAvailable > 0) {
                    String.format(Locale.US, "%,d", commodity.totalQtyAvailable)
                } else "Active"
                Text(supplyStr, fontSize = 13.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Real Player Orders Depth from API
        Text(
            text = "LIVE PLAYER ORDERS (CENTRAL EXCHANGE)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(6.dp))

        if (commodity.sellOrders.isEmpty()) {
            Surface(
                color = SpaceCardBg,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = "Loading real-time order depth from api.g2.galactictycoons.com...",
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
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                commodity.sellOrders.take(6).forEach { order ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(order.playerTycoon, fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(String.format(Locale.US, "%,d units", order.quantity), fontSize = 11.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                String.format(Locale.US, "$%.2f", order.priceDollars),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ProfitGreen
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Set Alert Form
        if (!showSetAlertSection) {
            Button(
                onClick = { showSetAlertSection = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("open_alert_form_button")
            ) {
                Icon(Icons.Default.AddAlert, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Set Real-Time Price Alert", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SpaceCardBg, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Text("CONFIGURE TRADE ALERT TRIGGER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { alertCondition = "BELOW" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (alertCondition == "BELOW") LossRed else SpaceSurfaceLight
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Price Drops Below ▼", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { alertCondition = "ABOVE" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (alertCondition == "ABOVE") ProfitGreen else SpaceSurfaceLight
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Price Spikes Above ▲", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = targetPriceInput,
                    onValueChange = { targetPriceInput = it },
                    label = { Text("Target Price ($)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = SpaceCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Strategy Note (e.g. Buy for smelter production)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanElectric,
                        unfocusedBorderColor = SpaceCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val parsed = targetPriceInput.toDoubleOrNull() ?: commodity.currentPrice
                        onSaveAlert(alertCondition, parsed, notesInput.ifBlank { "Price threshold alert" })
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanElectric),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("save_alert_confirm_button")
                ) {
                    Text("Arm Alert & Push Notifications", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
