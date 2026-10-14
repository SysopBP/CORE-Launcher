package app.lawnchair.ui.preferences

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import app.lawnchair.preferences.observeAsState
import app.lawnchair.preferences.preferenceManager
import app.lawnchair.ui.theme.isSelectedThemeDark

val LocalCoreSettingsGlass = staticCompositionLocalOf { false }
val LocalCoreSettingsOpacity = staticCompositionLocalOf { 1f }

@Composable
fun CoreSettingsTheme(content: @Composable () -> Unit) {
    val prefs = preferenceManager()
    val miuix by prefs.coreSettingsMiuix.observeAsState()
    val glass by prefs.coreSettingsGlass.observeAsState()
    val opacity by prefs.coreSettingsOpacity.observeAsState()
    val dark = isSelectedThemeDark
    val base = MaterialTheme.colorScheme
    val scheme = if (!miuix) base else base.copy(
        surface = if (dark) Color(0xFF08090B) else Color(0xFFF3F4F6),
        surfaceContainer = if (dark) Color(0xFF202126) else Color.White,
        onSurface = if (dark) Color(0xFFF5F5F7) else Color(0xFF17181B),
        onSurfaceVariant = if (dark) Color(0xFFB9BCC5) else Color(0xFF60636C),
        primary = if (dark) Color(0xFFA6C8FF) else Color(0xFF2865CF),
    )
    val typography = MaterialTheme.typography
    val cleanTypography = if (!miuix) typography else typography.copy(
        headlineLarge = typography.headlineLarge.copy(fontFamily = FontFamily.SansSerif),
        headlineMedium = typography.headlineMedium.copy(fontFamily = FontFamily.SansSerif),
        headlineSmall = typography.headlineSmall.copy(fontFamily = FontFamily.SansSerif),
        titleLarge = typography.titleLarge.copy(fontFamily = FontFamily.SansSerif),
        titleMedium = typography.titleMedium.copy(fontFamily = FontFamily.SansSerif),
        bodyLarge = typography.bodyLarge.copy(fontFamily = FontFamily.SansSerif),
        bodyMedium = typography.bodyMedium.copy(fontFamily = FontFamily.SansSerif),
    )
    val shapes = if (miuix) MaterialTheme.shapes.copy(
        large = RoundedCornerShape(28.dp), extraLarge = RoundedCornerShape(32.dp),
    ) else MaterialTheme.shapes
    MaterialTheme(colorScheme = scheme, typography = cleanTypography, shapes = shapes) {
        CompositionLocalProvider(
            LocalCoreSettingsGlass provides glass,
            LocalCoreSettingsOpacity provides opacity.coerceIn(0.4f, 1f),
        ) {
            val background = if (glass) Modifier.background(Brush.verticalGradient(
                if (dark) listOf(Color(0xFF172333), scheme.surface, Color(0xFF241A2B))
                else listOf(Color(0xFFE5EDFA), scheme.surface, Color(0xFFF0E6F4)),
            )) else Modifier.background(scheme.surface)
            Box(background) { content() }
        }
    }
}
