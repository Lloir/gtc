package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Deep Space Sci-Fi Modern Material 3 Color Scheme
val GalacticDarkColorScheme = darkColorScheme(
    primary = CyanElectric,
    onPrimary = Color(0xFF00363D),
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    inversePrimary = CyanDark,

    secondary = GoldAccent,
    onSecondary = Color(0xFF3F2E00),
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,

    tertiary = PurpleNebula,
    onTertiary = Color(0xFF381E72),
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,

    background = SpaceBlack,
    onBackground = TextPrimary,

    surface = SpaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SpaceCardBg,
    onSurfaceVariant = TextSecondary,

    surfaceDim = DarkSurfaceDim,
    surfaceBright = DarkSurfaceBright,
    surfaceContainerLowest = DarkSurfaceContainerLowest,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest,

    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    scrim = DarkScrim,

    inverseSurface = TextPrimary,
    inverseOnSurface = SpaceDark,

    error = LossRed,
    onError = Color.White,
    errorContainer = LossRedDim,
    onErrorContainer = Color(0xFFFFDAD6)
)

val GalacticLightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    error = LossRed,
    onError = Color.White
)

@Composable
fun GalacticTycoonsTheme(
    darkTheme: Boolean = true, // Space Tycoon universe defaults to deep dark theme
    dynamicColor: Boolean = false, // Set to true to adapt to system wallpaper when on Android 12+
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme: ColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> GalacticDarkColorScheme
        else -> GalacticLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    GalacticTycoonsTheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
        content = content
    )
}
