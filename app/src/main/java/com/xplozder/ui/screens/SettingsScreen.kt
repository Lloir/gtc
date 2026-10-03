package com.xplozder.ui.screens

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MotionPhotosOff
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xplozder.ui.components.SciFiCard
import com.xplozder.ui.theme.AlertAmber
import com.xplozder.ui.theme.CyanElectric
import com.xplozder.ui.theme.CyanGlow
import com.xplozder.ui.theme.GoldAccent
import com.xplozder.ui.theme.ProfitGreen
import com.xplozder.ui.theme.SpaceCardBg
import com.xplozder.ui.theme.SpaceCardBorder
import com.xplozder.ui.theme.SpaceSurfaceLight
import com.xplozder.ui.theme.TextMuted
import com.xplozder.ui.theme.TextPrimary
import com.xplozder.ui.theme.TextSecondary
import com.xplozder.viewmodel.GalacticTycoonsViewModel

@Composable
fun SettingsScreen(
    viewModel: GalacticTycoonsViewModel,
    onNavigateBack: () -> Unit
) {
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val tradeNotificationsEnabled by viewModel.tradeNotificationsEnabled.collectAsState()
    val fleetNotificationsEnabled by viewModel.fleetNotificationsEnabled.collectAsState()

    val useDynamicColor by viewModel.useDynamicColor.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    val highContrastEnabled by viewModel.highContrastEnabled.collectAsState()
    val largeTextEnabled by viewModel.largeTextEnabled.collectAsState()
    val reducedMotionEnabled by viewModel.reducedMotionEnabled.collectAsState()

    val isDynamicColorSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

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
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(36.dp)
                        .background(SpaceSurfaceLight, RoundedCornerShape(10.dp))
                        .testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = CyanElectric,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "SETTINGS & PREFERENCES",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Theme, Notifications & Accessibility",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ==========================================
                // THEME & ANDROID "YOU" (DYNAMIC COLOR)
                // ==========================================
                item {
                    Text(
                        text = "THEME & MATERIAL YOU",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanElectric,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    SciFiCard(
                        borderColor = if (useDynamicColor) CyanElectric else SpaceCardBorder,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Material You Dynamic Color Section
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0x3300E5FF),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = CyanElectric,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Material You (Dynamic Color)",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = TextPrimary
                                            )
                                        }
                                        Text(
                                            text = if (isDynamicColorSupported) {
                                                "Extract colors from device wallpaper (Android 12+)"
                                            } else {
                                                "Requires Android 12 (API 31+) or newer"
                                            },
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }

                                Switch(
                                    checked = useDynamicColor,
                                    onCheckedChange = { viewModel.setUseDynamicColor(it) },
                                    enabled = isDynamicColorSupported,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.Black,
                                        checkedTrackColor = CyanElectric,
                                        uncheckedThumbColor = TextMuted,
                                        uncheckedTrackColor = SpaceSurfaceLight
                                    ),
                                    modifier = Modifier.testTag("settings_dynamic_color_switch")
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Appearance Modes
                            Text(
                                text = "COLOR SCHEME MODE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = themeMode == "DARK",
                                    onClick = { viewModel.setThemeMode("DARK") },
                                    label = { Text("Deep Space Dark", fontSize = 11.sp) },
                                    leadingIcon = { Icon(Icons.Default.DarkMode, contentDescription = null, modifier = Modifier.size(14.dp)) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = SpaceCardBg,
                                        selectedContainerColor = Color(0x3300E5FF),
                                        selectedLabelColor = CyanGlow
                                    ),
                                    modifier = Modifier.weight(1f).testTag("theme_dark_chip")
                                )
                                FilterChip(
                                    selected = themeMode == "LIGHT",
                                    onClick = { viewModel.setThemeMode("LIGHT") },
                                    label = { Text("Light Mode", fontSize = 11.sp) },
                                    leadingIcon = { Icon(Icons.Default.LightMode, contentDescription = null, modifier = Modifier.size(14.dp)) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = SpaceCardBg,
                                        selectedContainerColor = Color(0x3300E5FF),
                                        selectedLabelColor = CyanGlow
                                    ),
                                    modifier = Modifier.weight(1f).testTag("theme_light_chip")
                                )
                                FilterChip(
                                    selected = themeMode == "SYSTEM",
                                    onClick = { viewModel.setThemeMode("SYSTEM") },
                                    label = { Text("System", fontSize = 11.sp) },
                                    leadingIcon = { Icon(Icons.Default.SettingsBrightness, contentDescription = null, modifier = Modifier.size(14.dp)) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = SpaceCardBg,
                                        selectedContainerColor = Color(0x3300E5FF),
                                        selectedLabelColor = CyanGlow
                                    ),
                                    modifier = Modifier.weight(0.9f).testTag("theme_system_chip")
                                )
                            }
                        }
                    }
                }

                // ==========================================
                // NOTIFICATIONS SETTINGS
                // ==========================================
                item {
                    Text(
                        text = "NOTIFICATIONS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    SciFiCard(
                        borderColor = if (notificationsEnabled) GoldAccent.copy(alpha = 0.5f) else SpaceCardBorder,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Master Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (notificationsEnabled) Color(0x33FFD54F) else SpaceSurfaceLight,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = if (notificationsEnabled) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                                contentDescription = null,
                                                tint = if (notificationsEnabled) GoldAccent else TextMuted,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "App Notifications",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = if (notificationsEnabled) "Receiving alerts & status" else "Turned off completely",
                                            fontSize = 11.sp,
                                            color = if (notificationsEnabled) ProfitGreen else TextMuted
                                        )
                                    }
                                }

                                Switch(
                                    checked = notificationsEnabled,
                                    onCheckedChange = { viewModel.setNotificationsEnabled(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.Black,
                                        checkedTrackColor = GoldAccent,
                                        uncheckedThumbColor = TextMuted,
                                        uncheckedTrackColor = SpaceSurfaceLight
                                    ),
                                    modifier = Modifier.testTag("settings_notifications_master_switch")
                                )
                            }

                            if (notificationsEnabled) {
                                Spacer(modifier = Modifier.height(14.dp))

                                // Trade Price Alerts Sub-Toggle
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Market Price Trigger Alerts", fontSize = 13.sp, color = TextPrimary)
                                        Text("Notify when market prices drop below or spike above alert thresholds", fontSize = 11.sp, color = TextSecondary)
                                    }
                                    Switch(
                                        checked = tradeNotificationsEnabled,
                                        onCheckedChange = { viewModel.setTradeNotificationsEnabled(it) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.Black,
                                            checkedTrackColor = GoldAccent
                                        ),
                                        modifier = Modifier.testTag("settings_trade_notifications_switch")
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Fleet Logistics Sub-Toggle
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Fleet Logistics & Cargo Alerts", fontSize = 13.sp, color = TextPrimary)
                                        Text("Notify when transport starships reach planetary destinations", fontSize = 11.sp, color = TextSecondary)
                                    }
                                    Switch(
                                        checked = fleetNotificationsEnabled,
                                        onCheckedChange = { viewModel.setFleetNotificationsEnabled(it) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.Black,
                                            checkedTrackColor = GoldAccent
                                        ),
                                        modifier = Modifier.testTag("settings_fleet_notifications_switch")
                                    )
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // ACCESSIBILITY OPTIONS
                // ==========================================
                item {
                    Text(
                        text = "ACCESSIBILITY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ProfitGreen,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    SciFiCard(
                        borderColor = if (highContrastEnabled) ProfitGreen.copy(alpha = 0.5f) else SpaceCardBorder,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // High Contrast Mode
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0x3300E676),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Visibility,
                                                contentDescription = null,
                                                tint = ProfitGreen,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("High Contrast Outlines", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                        Text("Strengthen borders, divider lines, and text contrast", fontSize = 11.sp, color = TextSecondary)
                                    }
                                }

                                Switch(
                                    checked = highContrastEnabled,
                                    onCheckedChange = { viewModel.setHighContrastEnabled(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.Black,
                                        checkedTrackColor = ProfitGreen
                                    ),
                                    modifier = Modifier.testTag("settings_high_contrast_switch")
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Large Text Scaler
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0x3300E676),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.TextFields,
                                                contentDescription = null,
                                                tint = ProfitGreen,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Enhanced Text Readability", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                        Text("Scale text elements for easier viewing on handheld screens", fontSize = 11.sp, color = TextSecondary)
                                    }
                                }

                                Switch(
                                    checked = largeTextEnabled,
                                    onCheckedChange = { viewModel.setLargeTextEnabled(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.Black,
                                        checkedTrackColor = ProfitGreen
                                    ),
                                    modifier = Modifier.testTag("settings_large_text_switch")
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Reduced Motion
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0x3300E676),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.MotionPhotosOff,
                                                contentDescription = null,
                                                tint = ProfitGreen,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Reduced Motion", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                        Text("Suppress pulsing animations and glowing transitions", fontSize = 11.sp, color = TextSecondary)
                                    }
                                }

                                Switch(
                                    checked = reducedMotionEnabled,
                                    onCheckedChange = { viewModel.setReducedMotionEnabled(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.Black,
                                        checkedTrackColor = ProfitGreen
                                    ),
                                    modifier = Modifier.testTag("settings_reduced_motion_switch")
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
