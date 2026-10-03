package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.LiveStatusPulse
import com.example.ui.components.PriceChangeBadge
import com.example.ui.components.SciFiCard
import com.example.ui.components.SparklineMiniChart
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.CyanElectric
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.GoldAccent
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

@Composable
fun DashboardScreen(
    viewModel: GalacticTycoonsViewModel,
    onNavigateToMarket: () -> Unit,
    onNavigateToFleet: () -> Unit,
    onNavigateToAlerts: () -> Unit,
    onNavigateToGame: () -> Unit,
    onNavigateToCalculator: () -> Unit
) {
    val commodities by viewModel.commodities.collectAsState()
    val alerts by viewModel.tradeAlerts.collectAsState()
    val missions by viewModel.fleetMissions.collectAsState()
    val unreadNotifs by viewModel.unreadNotifCount.collectAsState()
    val isLiveActive by viewModel.isLiveTickerActive.collectAsState()

    val activeAlertsCount = alerts.count { it.isActive }
    val activeFleetCount = missions.count {
        val elapsedSec = (System.currentTimeMillis() - it.departureTime) / 1000
        elapsedSec < it.travelDurationSec
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Corporation Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "COMMAND DECK",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "Galactic Tycoon Corporation • Sector 01",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    LiveStatusPulse(isActive = isLiveActive)
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { viewModel.fetchLiveExchangePrices() },
                        modifier = Modifier
                            .size(36.dp)
                            .background(SpaceSurfaceLight, RoundedCornerShape(10.dp))
                            .testTag("dashboard_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = CyanElectric,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Hero Web Game Banner (Direct bridge to g2.galactictycoons.com)
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SpaceCardBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanElectric.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToGame() }
                    .testTag("dashboard_game_banner")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0x2200E5FF), Color(0x10131C31))
                            )
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0x3300E5FF),
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Public,
                                            contentDescription = null,
                                            tint = CyanElectric,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "GALACTIC TYCOONS WEB GAME",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanGlow
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Play live at g2.galactictycoons.com",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Text(
                                text = "Full mobile viewport with mining, orbital bases, and trade contracts.",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                maxLines = 1
                            )
                        }

                        Button(
                            onClick = onNavigateToGame,
                            colors = ButtonDefaults.buttonColors(containerColor = CyanElectric),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Launch", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Quick Stats Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DashboardMetricCard(
                    title = "MARKET RATES",
                    value = "${commodities.size}",
                    subtitle = "Tracked Commodities",
                    accentColor = CyanElectric,
                    icon = Icons.Default.CurrencyExchange,
                    onClick = onNavigateToMarket,
                    modifier = Modifier.weight(1f)
                )

                DashboardMetricCard(
                    title = "FLEETS IN FLIGHT",
                    value = "$activeFleetCount",
                    subtitle = "Active Haulers",
                    accentColor = GoldAccent,
                    icon = Icons.Default.LocalShipping,
                    onClick = onNavigateToFleet,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DashboardMetricCard(
                    title = "TRADE TRIGGERS",
                    value = "$activeAlertsCount",
                    subtitle = if (unreadNotifs > 0) "$unreadNotifs Unread Alerts" else "Armed Triggers",
                    accentColor = if (unreadNotifs > 0) AlertAmber else ProfitGreen,
                    icon = Icons.Default.NotificationsActive,
                    onClick = onNavigateToAlerts,
                    modifier = Modifier.weight(1f)
                )

                DashboardMetricCard(
                    title = "CALCULATOR",
                    value = "Tools",
                    subtitle = "Arbitrage & Chains",
                    accentColor = CyanGlow,
                    icon = Icons.Default.Calculate,
                    onClick = onNavigateToCalculator,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Market Highlights Section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MARKET TOP MOVERS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "View Market →",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanElectric,
                    modifier = Modifier.clickable { onNavigateToMarket() }
                )
            }
        }

        // Show top 3 movers
        item {
            val topMovers = commodities.sortedByDescending { kotlin.math.abs(it.changePercent) }.take(3)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                topMovers.forEach { commodity ->
                    SciFiCard(
                        onClick = onNavigateToMarket,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = commodity.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = commodity.category.displayName,
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }

                            SparklineMiniChart(
                                points = commodity.priceHistory,
                                isPositive = commodity.isTrendingUp,
                                modifier = Modifier.size(width = 80.dp, height = 28.dp)
                            )

                            Column(horizontalAlignment = Alignment.End) {
                                val formatted = String.format(Locale.US, "%,.1f %s", commodity.currentPrice, commodity.unit)
                                Text(
                                    text = formatted,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TextPrimary
                                )
                                PriceChangeBadge(changePercent = commodity.changePercent)
                            }
                        }
                    }
                }
            }
        }

        // Active Fleets Snapshot
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HAULERS IN TRANSIT",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "View All Fleets →",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent,
                    modifier = Modifier.clickable { onNavigateToFleet() }
                )
            }
        }

        item {
            val latestMissions = missions.take(2)
            if (latestMissions.isEmpty()) {
                SciFiCard(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("No active transport haulers. Dispatch from the Fleet tab.", color = TextMuted, fontSize = 12.sp)
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    latestMissions.forEach { mission ->
                        SciFiCard(
                            onClick = onNavigateToFleet,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.RocketLaunch,
                                        contentDescription = null,
                                        tint = CyanElectric,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(mission.shipName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                        Text("${mission.originPlanet} ➔ ${mission.destinationExchange}", fontSize = 10.sp, color = TextSecondary)
                                    }
                                }

                                Text(
                                    text = mission.cargoManifest,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GoldAccent
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardMetricCard(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SciFiCard(
        onClick = onClick,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                    }
                }

                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary
            )

            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                letterSpacing = 0.5.sp
            )

            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = TextSecondary,
                maxLines = 1
            )
        }
    }
}
