package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.model.GalacticProductionData
import com.example.model.ProductionRecipe
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
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndustryCalculatorScreen(
    viewModel: GalacticTycoonsViewModel
) {
    val commodities by viewModel.commodities.collectAsState()
    val priceMap by viewModel.priceMap.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    // Trade Margin Calculator Inputs
    var selectedCommodity by remember { mutableStateOf(commodities.firstOrNull()) }
    var cargoTonsInput by remember { mutableStateOf("1000") }
    var buyPriceInput by remember { mutableStateOf(selectedCommodity?.currentPrice?.let { String.format(Locale.US, "%.2f", it * 0.90) } ?: "20.00") }
    var sellPriceInput by remember { mutableStateOf(selectedCommodity?.currentPrice?.let { String.format(Locale.US, "%.2f", it) } ?: "23.00") }
    var fuelCostInput by remember { mutableStateOf("150.0") }
    var brokerFeePctInput by remember { mutableStateOf("3.5") }
    var isCommodityDropdownExpanded by remember { mutableStateOf(false) }

    // Calculation results
    val tons = cargoTonsInput.toDoubleOrNull() ?: 0.0
    val buyPrice = buyPriceInput.toDoubleOrNull() ?: 0.0
    val sellPrice = sellPriceInput.toDoubleOrNull() ?: 0.0
    val fuelCost = fuelCostInput.toDoubleOrNull() ?: 0.0
    val brokerFeePct = brokerFeePctInput.toDoubleOrNull() ?: 0.0

    val grossRevenue = tons * sellPrice
    val totalPurchaseCost = tons * buyPrice
    val brokerFee = grossRevenue * (brokerFeePct / 100.0)
    val totalExpense = totalPurchaseCost + fuelCost + brokerFee
    val netProfit = grossRevenue - totalExpense
    val roiPercent = if (totalExpense > 0) (netProfit / totalExpense) * 100 else 0.0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "INDUSTRY CALCULATOR",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Arbitrage Margins & Production Chain Economics",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = SpaceSurfaceLight,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Calculate, contentDescription = null, tint = CyanElectric, modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SpaceCardBg,
                contentColor = CyanElectric,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = CyanElectric
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Route Arbitrage", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Production Recipes", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTab) {
                0 -> {
                    // Route Arbitrage Calculator Form
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 90.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Profit Projection Card
                        item {
                            SciFiCard(
                                borderColor = if (netProfit >= 0) ProfitGreen else LossRed,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "ESTIMATED NET MARGIN",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextMuted,
                                        letterSpacing = 0.5.sp
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = String.format(Locale.US, "$%,.2f", netProfit),
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (netProfit >= 0) ProfitGreen else LossRed
                                        )

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (roiPercent >= 0) Color(0x2200E676) else Color(0x22FF5252)
                                        ) {
                                            Text(
                                                text = String.format(Locale.US, "%s%.1f%% ROI", if (roiPercent >= 0) "+" else "", roiPercent),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (roiPercent >= 0) ProfitGreen else LossRed,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Breakdown row
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(SpaceSurfaceLight, RoundedCornerShape(8.dp))
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("GROSS SALES", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                            Text(String.format(Locale.US, "$%,.0f", grossRevenue), fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                                        }
                                        Column {
                                            Text("PURCHASE COST", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                            Text(String.format(Locale.US, "$%,.0f", totalPurchaseCost), fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                                        }
                                        Column {
                                            Text("FEES & FUEL", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                            Text(String.format(Locale.US, "$%,.0f", brokerFee + fuelCost), fontSize = 12.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // Input Parameters Card
                        item {
                            SciFiCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "TRADE PARAMETERS",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyanElectric,
                                        letterSpacing = 0.5.sp
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Commodity Selector
                                    ExposedDropdownMenuBox(
                                        expanded = isCommodityDropdownExpanded,
                                        onExpandedChange = { isCommodityDropdownExpanded = !isCommodityDropdownExpanded },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedTextField(
                                            value = selectedCommodity?.name ?: "Select Commodity",
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("Commodity") },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCommodityDropdownExpanded) },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = CyanElectric,
                                                unfocusedBorderColor = SpaceCardBorder,
                                                focusedTextColor = TextPrimary,
                                                unfocusedTextColor = TextPrimary
                                            ),
                                            modifier = Modifier
                                                .menuAnchor()
                                                .fillMaxWidth()
                                        )

                                        ExposedDropdownMenu(
                                            expanded = isCommodityDropdownExpanded,
                                            onDismissRequest = { isCommodityDropdownExpanded = false },
                                            modifier = Modifier.background(SpaceCardBg)
                                        ) {
                                            commodities.forEach { comm ->
                                                DropdownMenuItem(
                                                    text = { Text("${comm.name} (\$${String.format(Locale.US, "%.2f", comm.currentPrice)})", color = TextPrimary) },
                                                    onClick = {
                                                        selectedCommodity = comm
                                                        sellPriceInput = String.format(Locale.US, "%.2f", comm.currentPrice)
                                                        buyPriceInput = String.format(Locale.US, "%.2f", comm.currentPrice * 0.90)
                                                        isCommodityDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = cargoTonsInput,
                                        onValueChange = { cargoTonsInput = it },
                                        label = { Text("Hauler Cargo Units") },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CyanElectric,
                                            unfocusedBorderColor = SpaceCardBorder,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = buyPriceInput,
                                            onValueChange = { buyPriceInput = it },
                                            label = { Text("Buy Price ($/unit)") },
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = CyanElectric,
                                                unfocusedBorderColor = SpaceCardBorder,
                                                focusedTextColor = TextPrimary,
                                                unfocusedTextColor = TextPrimary
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )

                                        OutlinedTextField(
                                            value = sellPriceInput,
                                            onValueChange = { sellPriceInput = it },
                                            label = { Text("Sell Price ($/unit)") },
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = CyanElectric,
                                                unfocusedBorderColor = SpaceCardBorder,
                                                focusedTextColor = TextPrimary,
                                                unfocusedTextColor = TextPrimary
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = fuelCostInput,
                                            onValueChange = { fuelCostInput = it },
                                            label = { Text("Fuel Cost ($)") },
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = CyanElectric,
                                                unfocusedBorderColor = SpaceCardBorder,
                                                focusedTextColor = TextPrimary,
                                                unfocusedTextColor = TextPrimary
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )

                                        OutlinedTextField(
                                            value = brokerFeePctInput,
                                            onValueChange = { brokerFeePctInput = it },
                                            label = { Text("Exchange Fee %") },
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = CyanElectric,
                                                unfocusedBorderColor = SpaceCardBorder,
                                                focusedTextColor = TextPrimary,
                                                unfocusedTextColor = TextPrimary
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Production Recipes with Live Exchange Margins
                    val recipes = remember { GalacticProductionData.getRecipes() }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 90.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Surface(
                                color = Color(0x1200E5FF),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = CyanElectric, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Margins calculated live against Galactic Exchange market prices.",
                                        fontSize = 11.sp,
                                        color = CyanGlow
                                    )
                                }
                            }
                        }

                        items(recipes, key = { it.id }) { recipe ->
                            LiveProductionRecipeCard(recipe = recipe, priceMap = priceMap)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveProductionRecipeCard(
    recipe: ProductionRecipe,
    priceMap: Map<Int, Double>
) {
    val inputCost = recipe.calculateInputCost(priceMap)
    val outputValue = recipe.calculateOutputValue(priceMap)
    val profit = recipe.calculateLiveProfit(priceMap)
    val marginPct = recipe.calculateLiveMarginPct(priceMap)
    val isProfit = profit >= 0.0

    SciFiCard(
        borderColor = if (isProfit) ProfitGreen.copy(alpha = 0.5f) else LossRed.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = recipe.outputName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Yield: ${recipe.outputQuantity} ${recipe.outputUnit} • ${recipe.facilityRequired}",
                        fontSize = 11.sp,
                        color = CyanElectric
                    )
                }

                Surface(
                    color = if (isProfit) Color(0x2200E676) else Color(0x22FF5252),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    val formatted = String.format(Locale.US, "%s$%,.2f (%+.1f%%)", if (isProfit) "+" else "", profit, marginPct)
                    Text(
                        text = formatted,
                        color = if (isProfit) ProfitGreen else LossRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Inputs Required
            Text("INPUTS REQUIRED:", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                recipe.inputs.forEach { inp ->
                    val unitPrice = priceMap[inp.matId] ?: 0.0
                    Surface(
                        color = SpaceSurfaceLight,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${inp.name} x${inp.quantity} (${String.format(Locale.US, "$%.2f", unitPrice * inp.quantity)})",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = String.format(Locale.US, "Cost: $%.2f • Output: $%.2f", inputCost, outputValue),
                    fontSize = 10.sp,
                    color = TextMuted
                )
                Text(
                    text = "Cycle: ${recipe.cycleTimeMinutes}m",
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }
    }
}
