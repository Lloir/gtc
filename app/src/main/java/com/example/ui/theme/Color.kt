package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ==========================================
// Base Color Primitives (Dark & Light Schemes)
// ==========================================

// Core Brand Primary
val DarkCyanElectric = Color(0xFF00E5FF)
val DarkCyanGlow = Color(0xFF33EBFF)
val CyanDark = Color(0xFF00838F)
val PrimaryContainerDark = Color(0xFF004D56)
val OnPrimaryContainerDark = Color(0xFF70F5FF)

val LightCyanElectric = Color(0xFF0284C7) // Rich ocean cyan - high contrast on light backgrounds
val LightCyanGlow = Color(0xFF0369A1)
val LightPrimary = Color(0xFF0284C7)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFE0F2FE)
val LightOnPrimaryContainer = Color(0xFF0369A1)

// Secondary (Gold / Amber)
val DarkGoldAccent = Color(0xFFFFD54F) // Celestial yellow gold for dark backgrounds
val GoldWarm = Color(0xFFFFB300)
val SecondaryContainerDark = Color(0xFF533F00)
val OnSecondaryContainerDark = Color(0xFFFFDF9E)

val LightGoldAccent = Color(0xFFB45309) // Deep warm amber - high legibility (NO bright yellow on white!)
val LightGoldWarm = Color(0xFF92400E)

// Tertiary (Nebula Purple)
val PurpleNebula = Color(0xFFB388FF)
val TertiaryContainerDark = Color(0xFF3C2371)
val OnTertiaryContainerDark = Color(0xFFEADBFF)

// Alerts / Profits
val ProfitGreen = Color(0xFF00E676)
val ProfitGreenDim = Color(0xFF1B5E20)
val LossRed = Color(0xFFFF5252)
val LossRedDim = Color(0xFFB71C1C)
val AlertAmber = Color(0xFFFF9100)

// Surface Roles - Dark Scheme (Deep Space Sci-Fi)
val DarkSpaceBlack = Color(0xFF060910)
val DarkSpaceDark = Color(0xFF0A0F1D)
val DarkSpaceCardBg = Color(0xFF11182B)
val DarkSpaceCardBorder = Color(0xFF1E2B4A)
val DarkSpaceSurfaceLight = Color(0xFF17223D)

val DarkSurfaceDim = Color(0xFF090D18)
val DarkSurfaceBright = Color(0xFF223052)
val DarkSurfaceContainerLowest = Color(0xFF04060C)
val DarkSurfaceContainerLow = Color(0xFF0D1322)
val DarkSurfaceContainer = Color(0xFF121A2E)
val DarkSurfaceContainerHigh = Color(0xFF18233C)
val DarkSurfaceContainerHighest = Color(0xFF1F2C4A)

val DarkOutline = Color(0xFF38496B)
val DarkOutlineVariant = Color(0xFF223050)
val DarkScrim = Color(0xCC000000)

val DarkTextPrimary = Color(0xFFF1F5FD)
val DarkTextSecondary = Color(0xFF9FB0D0)
val DarkTextMuted = Color(0xFF627393)

// Surface Roles - Light Scheme (Crisp Clean Futuristic White)
val LightSpaceBlack = Color(0xFFF6F8FC)
val LightSpaceDark = Color(0xFFFFFFFF)
val LightSpaceCardBg = Color(0xFFFFFFFF) // Crisp white card background (NO dark boxes in light mode!)
val LightSpaceCardBorder = Color(0xFFE2E8F0) // Subtle outline
val LightSpaceSurfaceLight = Color(0xFFEDF2F7) // Soft slate surface for chips and buttons

val LightBackground = Color(0xFFF6F8FC)
val LightOnBackground = Color(0xFF0F172A)
val LightSurface = Color(0xFFFFFFFF)
val LightOnSurface = Color(0xFF0F172A)
val LightSurfaceVariant = Color(0xFFFFFFFF)
val LightOnSurfaceVariant = Color(0xFF334155)
val LightOutline = Color(0xFFE2E8F0)

val LightTextPrimary = Color(0xFF0F172A) // Rich slate black - clear readability
val LightTextSecondary = Color(0xFF334155) // Medium slate
val LightTextMuted = Color(0xFF64748B) // Subtle muted slate

// ==========================================
// Dynamic Composable Theme-Aware Accessors
// Automatically switch between Dark & Light
// ==========================================

val CyanElectric: Color
    @Composable get() = if (LocalIsDarkTheme.current) DarkCyanElectric else LightCyanElectric

val CyanGlow: Color
    @Composable get() = if (LocalIsDarkTheme.current) DarkCyanGlow else LightCyanGlow

val GoldAccent: Color
    @Composable get() = if (LocalIsDarkTheme.current) DarkGoldAccent else LightGoldAccent

val SpaceBlack: Color
    @Composable get() = if (LocalIsDarkTheme.current) DarkSpaceBlack else LightSpaceBlack

val SpaceDark: Color
    @Composable get() = if (LocalIsDarkTheme.current) DarkSpaceDark else LightSpaceDark

val SpaceCardBg: Color
    @Composable get() = if (LocalIsDarkTheme.current) DarkSpaceCardBg else LightSpaceCardBg

val SpaceCardBorder: Color
    @Composable get() = if (LocalIsDarkTheme.current) DarkSpaceCardBorder else LightSpaceCardBorder

val SpaceSurfaceLight: Color
    @Composable get() = if (LocalIsDarkTheme.current) DarkSpaceSurfaceLight else LightSpaceSurfaceLight

val TextPrimary: Color
    @Composable get() = if (LocalIsDarkTheme.current) DarkTextPrimary else LightTextPrimary

val TextSecondary: Color
    @Composable get() = if (LocalIsDarkTheme.current) DarkTextSecondary else LightTextSecondary

val TextMuted: Color
    @Composable get() = if (LocalIsDarkTheme.current) DarkTextMuted else LightTextMuted
